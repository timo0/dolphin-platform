/*
 * Copyright 2015-2017 Canoo Engineering AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.canoo.platform.remoting.client;

import com.canoo.dp.impl.client.ClientContextImpl;
import com.canoo.dp.impl.client.DolphinPlatformHttpClientConnector;
import com.canoo.dp.impl.client.legacy.ClientModelStore;
import com.canoo.dp.impl.client.legacy.communication.AbstractClientConnector;
import com.canoo.dp.impl.platform.client.http.CookieRequestHandler;
import com.canoo.dp.impl.platform.client.http.CookieResponseHandler;
import com.canoo.dp.impl.platform.client.http.HttpClientCookieHandler;
import com.canoo.dp.impl.platform.client.http.HttpClientImpl;
import com.canoo.dp.impl.platform.client.session.ClientSessionStore;
import com.canoo.dp.impl.platform.client.session.ClientSessionSupportingURLConnectionRequestHandler;
import com.canoo.dp.impl.platform.client.session.ClientSessionSupportingURLConnectionResponseHandler;
import com.canoo.dp.impl.platform.client.session.StrictClientSessionSupportingURLConnectionResponseHandler;
import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.dp.impl.remoting.codec.OptimizedJsonCodec;
import com.canoo.dp.impl.remoting.legacy.util.Function;
import com.canoo.platform.client.http.HttpURLConnectionFactory;
import com.canoo.platform.client.http.HttpURLConnectionHandler;
import com.canoo.platform.remoting.DolphinRemotingException;
import com.google.gson.Gson;

import java.net.CookieStore;
import java.util.concurrent.CompletableFuture;

/**
 * Factory to create a {@link ClientContext}. Normally you will create a {@link ClientContext} at the bootstrap of your
 * client by using the {@link #create(ClientConfiguration)} method and use this context as a singleton in your client.
 * The {@link ClientContext} defines the connection between the client and the Dolphin Platform server endpoint.
 */
public class ClientContextFactory {

    private ClientContextFactory() {
    }

    /**
     * Create a {@link ClientContext} based on the given configuration. This method doesn't block and returns a
     * {@link CompletableFuture} to receive its result. If the {@link ClientContext} can't be created the
     * {@link CompletableFuture#get()} will throw a {@link ClientInitializationException}.
     *
     * @param clientConfiguration the configuration
     * @return the future
     */
    public static ClientContext create(final ClientConfiguration clientConfiguration) {
        Assert.requireNonNull(clientConfiguration, "clientConfiguration");

        final HttpURLConnectionFactory connectionFactory = clientConfiguration.getConnectionFactory();
        final ClientSessionStore clientSessionStore = new ClientSessionStore();
        final HttpClientImpl httpClient = new HttpClientImpl(new Gson(), connectionFactory);
        final HttpURLConnectionHandler clientSessionRequestHandler = new ClientSessionSupportingURLConnectionRequestHandler(clientSessionStore);
        final HttpURLConnectionHandler clientSessionResponseHandler = new ClientSessionSupportingURLConnectionResponseHandler(clientSessionStore);
        final HttpURLConnectionHandler clientSessionCheckResponseHandler = new StrictClientSessionSupportingURLConnectionResponseHandler(clientConfiguration.getServerEndpoint());

        final CookieStore cookieStore = clientConfiguration.getCookieStore();
        final HttpClientCookieHandler clientCookieHandler = new HttpClientCookieHandler(cookieStore);
        final CookieRequestHandler cookieRequestHandler = new CookieRequestHandler(clientCookieHandler);
        final CookieResponseHandler cookieResponseHandler = new CookieResponseHandler(clientCookieHandler);

        httpClient.addRequestHandler(cookieRequestHandler);
        httpClient.addRequestHandler(clientSessionRequestHandler);
        httpClient.addResponseHandler(cookieResponseHandler);
        httpClient.addResponseHandler(clientSessionResponseHandler);
        httpClient.addResponseHandler(clientSessionCheckResponseHandler);

        return new ClientContextImpl(clientConfiguration, new Function<ClientModelStore, AbstractClientConnector>() {
            @Override
            public AbstractClientConnector call(final ClientModelStore clientModelStore) {
                return new DolphinPlatformHttpClientConnector(clientConfiguration, clientModelStore, OptimizedJsonCodec.getInstance(), new RemotingExceptionHandler() {
                    @Override
                    public void handle(DolphinRemotingException e) {
                        for(RemotingExceptionHandler handler : clientConfiguration.getRemotingExceptionHandlers()) {
                            handler.handle(e);
                        }
                    }
                }, httpClient);
            }
        }, httpClient, clientSessionStore);
    }

}