package com.github.czyzby.websocket.impl;

import java.security.KeyStore;
import java.security.Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.badlogic.gdx.Files;
import com.github.czyzby.websocket.data.WebSocketCloseCode;
import com.github.czyzby.websocket.data.WebSocketException;
import com.github.czyzby.websocket.data.WebSocketState;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;

/** Default web socket implementation for desktop and mobile platforms.
 *
 * @author MJ */
public class NvWebSocket extends com.github.czyzby.websocket.impl.AbstractWebSocket {
    private final WebSocketFactory webSocketFactory = new WebSocketFactory();
    private WebSocket webSocket;
    private boolean isSecure;
    private SSLContext sslContext;

    public NvWebSocket(final String url) {
        super(url);
        this.isSecure = false;
        Security.addProvider(new BouncyCastleProvider());
    }
    
    public NvWebSocket(final String url, final Files gdxFiles, final String keystorePath, final String storeType, final String keyPass, final String storePass) {
        super(url);
        this.isSecure = true;
        
        try {
	        //Create SSL context
	        KeyStore ks = KeyStore.getInstance(storeType);
			//File kf = gdxFiles.internal(keystorePath).file();
			ks.load(gdxFiles.internal(keystorePath).read(), storePass.toCharArray());
	
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, keyPass.toCharArray());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ks);
	
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		} catch (final Throwable exception) {
			throw new WebSocketException("Unable to create SSL Context. Please verify you passwords, path and store type.", exception);
		}
    }

    @Override
    public void connect() throws WebSocketException {
        try {
            dispose();
            if (isSecure)
            	webSocketFactory.setSSLContext(sslContext);
            final WebSocket currentWebSocket = webSocket = webSocketFactory.createSocket(getUrl());
            currentWebSocket.addListener(new NvWebSocketListener(this));
            currentWebSocket.connectAsynchronously();
        } catch (final Throwable exception) {
            throw new WebSocketException("Unable to connect.", exception);
        }
    }

    /** Removes current web socket instance. */
    protected void dispose() {
        final WebSocket currentWebSocket = webSocket;
        if (currentWebSocket != null && currentWebSocket.isOpen()) {
            try {
                currentWebSocket.disconnect(WebSocketCloseCode.AWAY.getCode());
            } catch (final Exception exception) {
                postErrorEvent(exception);
            }
        }
    }

    @Override
    public WebSocketState getState() {
        final WebSocket currentWebSocket = webSocket;
        return currentWebSocket == null ? WebSocketState.CLOSED : convertState(currentWebSocket.getState());
    }

    private static WebSocketState convertState(final com.neovisionaries.ws.client.WebSocketState state) {
        switch (state) {
            case CLOSED:
            case CREATED:
                return WebSocketState.CLOSED;
            case CLOSING:
                return WebSocketState.CLOSING;
            case CONNECTING:
                return WebSocketState.CONNECTING;
            case OPEN:
                return WebSocketState.OPEN;
        }
        return WebSocketState.CLOSED;
    }

    @Override
    public boolean isSecure() {
        final WebSocket currentWebSocket = webSocket;
        return currentWebSocket != null && "wss".equalsIgnoreCase(currentWebSocket.getURI().getScheme());
    }

    @Override
    public boolean isOpen() {
        final WebSocket currentWebSocket = webSocket;
        return currentWebSocket != null && currentWebSocket.isOpen();
    }

    @Override
    public void close(final WebSocketCloseCode code, final String reason) throws WebSocketException {
        final WebSocket currentWebSocket = webSocket;
        if (currentWebSocket != null) {
            try {
                currentWebSocket.disconnect(code.getCode(), reason);
            } catch (final Throwable exception) {
                throw new WebSocketException("Unable to close the web socket.", exception);
            }
        }
    }

    @Override
    protected void sendBinary(final byte[] packet) throws Exception {
        webSocket.sendBinary(packet);
    }

    @Override
    protected void sendString(final String packet) throws Exception {
        webSocket.sendText(packet);
    }
}
