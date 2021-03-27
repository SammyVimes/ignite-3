/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.network.scalecube;

import io.scalecube.cluster.transport.api.Message;
import io.scalecube.cluster.transport.api.Transport;
import io.scalecube.net.Address;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.ignite.network.internal.SerializerProvider;
import org.apache.ignite.network.internal.netty.NettyClient;
import org.apache.ignite.network.internal.netty.NettyServer;
import org.apache.ignite.network.message.NetworkMessage;
import org.apache.ignite.network.scalecube.message.ScaleCubeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.netty.resources.LoopResources;

public class ScaleCubeDirectMarshallerTransport implements Transport {

    private static final Logger LOGGER = LoggerFactory.getLogger(Transport.class);

    private final SerializerProvider provider;

    // Subject
    private final DirectProcessor<Message> subject = DirectProcessor.create();
    private final FluxSink<Message> sink = subject.sink();
    // Close handler
    private final MonoProcessor<Void> stop = MonoProcessor.create();
    private final MonoProcessor<Void> onStop = MonoProcessor.create();
    private final LoopResources loopResources = LoopResources.create("sc-cluster-io", 1, true);
    private NettyServer server;
    private Address address;

    private final Map<Address, Mono<? extends NettyClient>> connections = new ConcurrentHashMap<>();

    public ScaleCubeDirectMarshallerTransport(NettyServer server, SerializerProvider provider) {
        this.server = server;
        this.provider = provider;
        this.server.addListener(this::onMessage);
        this.address = prepareAddress(server);
        // Setup cleanup
        stop.then(doStop())
            .doFinally(s -> onStop.onComplete())
            .subscribe(
                null, ex -> LOGGER.warn("[{}][doStop] Exception occurred: {}", address, ex.toString()));
    }

    private static Address prepareAddress(NettyServer server) {
        InetAddress address = server.address().getAddress();
        int port = server.address().getPort();
        if (address.isAnyLocalAddress()) {
            return Address.create(Address.getLocalIpAddress().getHostAddress(), port);
        } else {
            return Address.create(address.getHostAddress(), port);
        }
    }

    private Mono<Void> doStop() {
        return Mono.defer(
            () -> {
                LOGGER.info("[{}][doStop] Stopping", address);
                // Complete incoming messages observable
                sink.complete();
                return Flux.concatDelayError(shutdownLoopResources())
                    .then()
                    .doFinally(s -> connections.clear())
                    .doOnSuccess(avoid -> LOGGER.info("[{}][doStop] Stopped", address));
            });
    }


    private Mono<Void> shutdownLoopResources() {
        return Mono.fromRunnable(loopResources::dispose).then(loopResources.disposeLater());
    }

    @Override public Address address() {
        return address;
    }

    @Override public Mono<Transport> start() {
        return Mono.just(this);
    }

    @Override public Mono<Void> stop() {
        return Mono.defer(
            () -> {
                stop.onComplete();
                return onStop;
            });
    }

    @Override public boolean isStopped() {
        return onStop.isDisposed();
    }

    @Override public Mono<Void> send(Address address, Message message) {
        return Mono.defer(() -> {
            return connections.computeIfAbsent(address, this::connect0);
        }).flatMap(client -> {
            client.send(fromMessage(message));
            return Mono.empty().then();
        });
    }

    private void onMessage(NetworkMessage msg) {
        Message t = fromNetworkMessage(msg);
        if (t != null) {
            sink.next(t);
        }
    }

    NetworkMessage fromMessage(Message message) {
        Object dataObj = message.data();
        String className = dataObj.getClass().getCanonicalName();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream o;
        try {
            o = new ObjectOutputStream(stream);
            o.writeObject(dataObj);
        }
        catch (IOException ignored) {
            ignored.printStackTrace();
        }
        return new ScaleCubeMessage(stream.toByteArray(), className, message.headers());
    }

    @Override public Mono<Message> requestResponse(Address address, final Message request) {
        return Mono.create(
            sink -> {
                Objects.requireNonNull(request, "request must be not null");
                Objects.requireNonNull(request.correlationId(), "correlationId must be not null");

                Disposable receive =
                    listen()
                        .filter(resp -> resp.correlationId() != null)
                        .filter(resp -> resp.correlationId().equals(request.correlationId()))
                        .take(1)
                        .subscribe(sink::success, sink::error, sink::success);

                Disposable send =
                    send(address, request)
                        .subscribe(
                            null,
                            ex -> {
                                receive.dispose();
                                sink.error(ex);
                            });

                sink.onDispose(Disposables.composite(send, receive));
            });
    }

    Message fromNetworkMessage(NetworkMessage networkMessage) {
        if (networkMessage instanceof ScaleCubeMessage) {
            ScaleCubeMessage msg = (ScaleCubeMessage) networkMessage;

            Map<String, String> headers = msg.getHeaders();

            Object obj = null;
            try {
                obj = new ObjectInputStream(new ByteArrayInputStream(msg.getArray())).readObject();
            }
            catch (Exception ignored) {
            }

            return Message.withHeaders(headers).data(obj).build();
        }
        return null;
    }


    private Mono<? extends NettyClient> connect0(Address address1) {
        final CompletableFuture<NettyClient> start = NettyClient.start(address1.port(), provider, this::onMessage);

        return Mono.fromFuture(start)
            .doOnSuccess(
                connection -> {
//                    connection.onDispose().doOnTerminate(() -> connections.remove(address1)).subscribe();
//                    LOGGER.debug(
//                        "[{}][connected][{}] Channel: {}", address, address1, connection.channel());
                })
            .doOnError(
                th -> {
                    LOGGER.warn(
                        "[{}][connect0][{}] Exception occurred: {}", address, address1, th.toString());
                    connections.remove(address1);
                })
            .cache();
    }

    @Override public final Flux<Message> listen() {
        return subject.onBackpressureBuffer();
    }
}
