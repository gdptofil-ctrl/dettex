/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.annotations.SerializedName
 */
package ru.recoin.implement.features.modules.misc.anydesk;

import com.google.gson.annotations.SerializedName;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Consumer;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509TrustManager;
import ru.recoin.implement.features.modules.misc.anydesk.AnyDeskError;
import ru.recoin.implement.features.modules.misc.anydesk.AnyDeskPublicAddressDecoder;
import ru.recoin.implement.features.modules.misc.anydesk.AnyDeskRelayProtocol;
import ru.recoin.implement.features.modules.misc.anydesk.AnyDeskTlsIdentity;

public final class AnyDeskRelayClient
implements AutoCloseable {
    private static final int CONNECT_TIMEOUT_MILLIS = 8000;
    private static final String DEFAULT_BOOT_HOST = "boot-01.net.anydesk.com";
    private final Consumer<Status> statusConsumer;
    @SerializedName(value="\u0412")
    private volatile AnyDeskError sessionError = AnyDeskError.NONE;
    @SerializedName(value="\u0412")
    private ConnectionWorker connectionWorker;
    private static final String WORKER_THREAD_NAME = "Recoin-AnyDesk";
    private static final String FRESH_IDENTITY_DETAIL = "Creating a fresh identity";
    private static final long MILLIS_PER_SECOND = 1000L;
    private static final String NORMAL_STOP_DETAIL = "Stopped";
    private static final String INTERRUPTED_STOP_DETAIL = "Stopped";
    private static final String ERROR_STOP_DETAIL = "Stopped";
    private static final String FINALLY_STOP_DETAIL = "Stopped";
    private static final String BOOT_HOST = "boot-01.net.anydesk.com";
    private static final String REDIRECT_LOOP_ERROR = "Relay redirect loop";
    private static final String TLS_PROTOCOL = "TLSv1.2";
    private static final String EXPECTED_VERSION_OFFER = "Expected relay version offer";
    private static final String INVALID_REGISTRATION_CHANNEL = "Invalid registration channel";
    private static final String EXPECTED_ACKNOWLEDGEMENT = "Expected relay acknowledgement";
    private static final String REGISTERED_DETAIL = "Registered; automatic acceptance enabled";
    private static final String REDIRECT_LIMIT_ERROR = "Relay redirect limit";
    private static final int CHANNEL_SEARCH_LIMIT = 65534;
    private static final int CHANNEL_ALLOCATION_LIMIT = 65534;
    private static final String UNSUPPORTED_CHANNEL_CLOSURE = "Unsupported channel closure revision";
    private static final String TRANSPORT_CLOSED = "Relay closed the transport; reconnecting";
    private static final String INVALID_SESSION_TERMINATION = "Invalid session termination";
    private static final String FREEZE_DETAIL = "Session negotiation held open";
    private static final String APPROVAL_DETAIL = "Approval sent; screen and input are not implemented";
    private static final long CAPABILITY_MESSAGE_TYPE = 6L;
    private static final String FRAME_PEER_CLOSED = "Peer closed connection";
    private static final long PACKET_TIMEOUT_NANOS = 8000000000L;
    private static final int FRAME_LENGTH_LIMIT = 65535;
    private static final String INVALID_FRAME_LENGTH = "Invalid frame length";
    private static final String PACKET_DEADLINE_EXCEEDED = "Packet deadline exceeded";
    private static final long NANOS_PER_MILLISECOND = 1000000L;
    private static final String READ_PEER_CLOSED = "Peer closed connection";
    private static final String ROOT_CERTIFICATE_RESOURCE = "/assets/recoin/anydesk/anynet-root-ca-2.pem";
    private static final String MISSING_ROOT_CERTIFICATE = "Missing AnyNet root certificate resource";
    private static final String CERTIFICATE_TYPE = "X.509";
    private static final String ROOT_CERTIFICATE_ALIAS = "anynet-root-ca-2";
    private static final String SSL_CONTEXT_PROTOCOL = "TLS";

    private static byte[] readExact(Socket socket, InputStream input, int length, long deadlineNanos) throws IOException {
        int readCount;
        byte[] bytes = new byte[length];
        for (int bytesRead = 0; bytesRead < length; bytesRead += readCount) {
            long remainingNanos = deadlineNanos - System.nanoTime();
            if (remainingNanos <= 0L) {
                throw new SocketTimeoutException(PACKET_DEADLINE_EXCEEDED);
            }
            socket.setSoTimeout((int)Math.max(1L, remainingNanos / 1000000L));
            readCount = input.read(bytes, bytesRead, length - bytesRead);
            if (readCount >= 0) continue;
            throw new EOFException("Peer closed connection");
        }
        return bytes;
    }

    static AnyDeskRelayProtocol.RelayFrame readFrame(Socket socket, boolean idlePolling) throws IOException {
        int firstLengthByte;
        InputStream input = socket.getInputStream();
        socket.setSoTimeout(idlePolling ? 500 : 8000);
        try {
            firstLengthByte = input.read();
        }
        catch (SocketTimeoutException timeout) {
            if (idlePolling) {
                return null;
            }
            throw timeout;
        }
        if (firstLengthByte < 0) {
            throw new EOFException("Peer closed connection");
        }
        long deadlineNanos = System.nanoTime() + 8000000000L;
        int frameLength = firstLengthByte << 8 | AnyDeskRelayClient.readExact(socket, input, 1, deadlineNanos)[0] & 0xFF;
        if (frameLength >= 3 && frameLength < 65535) {
            byte[] frameBytes = AnyDeskRelayClient.readExact(socket, input, frameLength, deadlineNanos);
            return new AnyDeskRelayProtocol.RelayFrame((frameBytes[0] & 0xFF) << 8 | frameBytes[1] & 0xFF, frameBytes[2] & 0xFF, Arrays.copyOfRange(frameBytes, 3, frameBytes.length));
        }
        throw new IOException(INVALID_FRAME_LENGTH);
    }

    private static void sendStreamMessage(Socket socket, int channel, int lane, byte[] messageBody) throws IOException {
        AnyDeskRelayClient.sendFrame(socket, channel, 0, AnyDeskRelayProtocol.encode(lane, messageBody));
    }

    public AnyDeskRelayClient(Consumer<Status> statusConsumer) {
        this.statusConsumer = statusConsumer;
    }

    private void handleSessions(ConnectionWorker worker, SSLSocket tlsSocket, long clientId) throws IOException {
        HashMap<Integer, AnyDeskRelayProtocol.IncomingRequest> requestsByChannel = new HashMap<Integer, AnyDeskRelayProtocol.IncomingRequest>();
        HashMap<Integer, String> peerIpsByChannel = new HashMap<Integer, String>();
        HashMap<Integer, AnyDeskRelayProtocol.StreamDecoder> decodersByChannel = new HashMap<Integer, AnyDeskRelayProtocol.StreamDecoder>();
        HashSet<Integer> negotiatedChannels = new HashSet<Integer>();
        int nextChannel = 1;
        block0: while (!worker.stopped) {
            AnyDeskRelayProtocol.RelayFrame frame = AnyDeskRelayClient.readFrame(tlsSocket, true);
            if (frame == null) continue;
            int channel = frame.channel();
            if (channel == 0 && frame.type() == 10) {
                if (requestsByChannel.size() >= 4) continue;
                AnyDeskRelayProtocol.IncomingRequest request = AnyDeskRelayProtocol.readIncomingRequest(frame.body());
                while (requestsByChannel.containsKey(nextChannel)) {
                    nextChannel = nextChannel == 65534 ? 1 : nextChannel + 1;
                }
                requestsByChannel.put(nextChannel, request);
                peerIpsByChannel.put(nextChannel, AnyDeskPublicAddressDecoder.decodePublicAddresses(frame.body()));
                AnyDeskRelayClient.sendFrame(tlsSocket, 0, 11, AnyDeskRelayProtocol.encode(request.token(), nextChannel));
                nextChannel = nextChannel == 65534 ? 1 : nextChannel + 1;
                continue;
            }
            if (channel == 0 && frame.type() == 14) {
                AnyDeskRelayProtocol.PacketReader closureReader = new AnyDeskRelayProtocol.PacketReader(frame.body());
                if (closureReader.readUnsignedByte() != 0) {
                    throw new IOException(UNSUPPORTED_CHANNEL_CLOSURE);
                }
                int closedChannel = closureReader.readUnsignedShort();
                int closureReason = closureReader.readUnsignedByte();
                closureReader.requireEnd();
                AnyDeskRelayProtocol.IncomingRequest closedRequest = (AnyDeskRelayProtocol.IncomingRequest)requestsByChannel.remove(closedChannel);
                peerIpsByChannel.remove(closedChannel);
                decodersByChannel.remove(closedChannel);
                negotiatedChannels.remove(closedChannel);
                if (closedRequest == null || !requestsByChannel.isEmpty()) continue;
                this.publishStatus(worker, State.ONLINE, clientId, 0L, "Waiting; peer channel closed, reason " + closureReason);
                continue;
            }
            if (channel == 0 && frame.type() == 3) {
                throw new IOException(TRANSPORT_CLOSED);
            }
            if (!requestsByChannel.containsKey(channel) || frame.type() != 0) continue;
            for (AnyDeskRelayProtocol.StreamMessage message : decodersByChannel.computeIfAbsent(channel, channelKey -> new AnyDeskRelayProtocol.StreamDecoder()).decode(frame.body())) {
                int messageType = message.body()[0] & 0xFF;
                if (messageType == 2) {
                    if (message.body().length != 2) {
                        throw new IOException(INVALID_SESSION_TERMINATION);
                    }
                    requestsByChannel.remove(channel);
                    peerIpsByChannel.remove(channel);
                    decodersByChannel.remove(channel);
                    negotiatedChannels.remove(channel);
                    if (!requestsByChannel.isEmpty()) continue block0;
                    this.publishStatus(worker, State.ONLINE, clientId, 0L, "Waiting; peer ended session, reason " + (message.body()[1] & 0xFF));
                    continue block0;
                }
                if (messageType != 0 || negotiatedChannels.contains(channel)) continue;
                int sessionVersion = AnyDeskRelayProtocol.selectSessionVersion(message.body());
                AnyDeskError sessionError = this.sessionError;
                AnyDeskRelayClient.negotiateSession(tlsSocket, channel, message.lane(), sessionVersion, clientId, sessionError);
                negotiatedChannels.add(channel);
                String peerIp = (String)peerIpsByChannel.get(channel);
                if (sessionError == AnyDeskError.FREEZE) {
                    this.publishStatus(worker, State.CONNECTING, clientId, ((AnyDeskRelayProtocol.IncomingRequest)requestsByChannel.get(channel)).peerId(), peerIp, FREEZE_DETAIL);
                    continue;
                }
                this.publishStatus(worker, sessionError == AnyDeskError.NONE ? State.APPROVED : State.SESSION_ERROR, clientId, ((AnyDeskRelayProtocol.IncomingRequest)requestsByChannel.get(channel)).peerId(), peerIp, sessionError == AnyDeskError.NONE ? APPROVAL_DETAIL : sessionError.getProtocolKey());
            }
        }
    }

    private synchronized void publishStatus(ConnectionWorker worker, State state, long clientId, long peerId, String peerIp, String detail) {
        if (this.connectionWorker == worker && !worker.stopped) {
            this.statusConsumer.accept(new Status(state, clientId, peerId, peerIp, detail));
        }
    }

    private synchronized void publishStatus(ConnectionWorker worker, State state, long clientId, long peerId, String detail) {
        this.publishStatus(worker, state, clientId, peerId, null, detail);
    }

    static SSLContext createSslContext() throws Exception {
        X509Certificate rootCertificate;
        try (InputStream certificateStream = AnyDeskRelayClient.class.getResourceAsStream(ROOT_CERTIFICATE_RESOURCE);){
            if (certificateStream == null) {
                throw new IOException(MISSING_ROOT_CERTIFICATE);
            }
            rootCertificate = (X509Certificate)CertificateFactory.getInstance(CERTIFICATE_TYPE).generateCertificate(certificateStream);
        }
        rootCertificate.checkValidity();
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        trustStore.setCertificateEntry(ROOT_CERTIFICATE_ALIAS, rootCertificate);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        final X509TrustManager delegateTrustManager = (X509TrustManager)trustManagerFactory.getTrustManagers()[0];
        X509TrustManager serviceTrustManager = new X509TrustManager(){
            private static final String UNEXPECTED_SERVICE_CERTIFICATE = "Unexpected AnyNet service certificate";
            private static final String INVALID_SERVICE_NAME = "Invalid AnyNet service name";
            private static final String COMMON_NAME_ATTRIBUTE = "CN";
            private static final String RELAY_COMMON_NAME = "AnyNet Relay";

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                delegateTrustManager.checkClientTrusted(chain, authType);
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return delegateTrustManager.getAcceptedIssuers();
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                delegateTrustManager.checkServerTrusted(chain, authType);
                try {
                    boolean serviceNameMatches = new LdapName(chain[0].getSubjectX500Principal().getName()).getRdns().stream().anyMatch(rdn -> rdn.getType().equalsIgnoreCase(COMMON_NAME_ATTRIBUTE) && rdn.getValue().equals(RELAY_COMMON_NAME));
                    if (!serviceNameMatches) {
                        throw new CertificateException(UNEXPECTED_SERVICE_CERTIFICATE);
                    }
                }
                catch (InvalidNameException invalidName) {
                    throw new CertificateException(INVALID_SERVICE_NAME, invalidName);
                }
            }
        };
        final X509ExtendedKeyManager delegateKeyManager = (X509ExtendedKeyManager)AnyDeskTlsIdentity.createKeyManager().getKeyManagers()[0];
        X509ExtendedKeyManager clientKeyManager = new X509ExtendedKeyManager(){
            private static final String RSA_KEY_TYPE = "RSA";
            private static final String CLIENT_KEY_ALIAS = "ephemeral";

            @Override
            public String chooseEngineClientAlias(String[] keyTypes, Principal[] issuers, SSLEngine engine) {
                return this.chooseRsaAlias(keyTypes);
            }

            @Override
            public String chooseClientAlias(String[] keyTypes, Principal[] issuers, Socket socket) {
                return this.chooseRsaAlias(keyTypes);
            }

            private String chooseRsaAlias(String[] keyTypes) {
                return Arrays.asList(keyTypes).contains(RSA_KEY_TYPE) ? CLIENT_KEY_ALIAS : null;
            }

            @Override
            public PrivateKey getPrivateKey(String alias) {
                return delegateKeyManager.getPrivateKey(alias);
            }

            @Override
            public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
                return null;
            }

            @Override
            public String[] getClientAliases(String keyType, Principal[] issuers) {
                return delegateKeyManager.getClientAliases(keyType, issuers);
            }

            @Override
            public String[] getServerAliases(String keyType, Principal[] issuers) {
                return delegateKeyManager.getServerAliases(keyType, issuers);
            }

            @Override
            public X509Certificate[] getCertificateChain(String alias) {
                return delegateKeyManager.getCertificateChain(alias);
            }
        };
        SSLContext sslContext = SSLContext.getInstance(SSL_CONTEXT_PROTOCOL);
        sslContext.init(new KeyManager[]{clientKeyManager}, new TrustManager[]{serviceTrustManager}, null);
        return sslContext;
    }

    public void setSessionError(AnyDeskError error) {
        this.sessionError = Objects.requireNonNull(error);
    }

    public synchronized void start() {
        if (this.connectionWorker == null) {
            ConnectionWorker worker;
            this.connectionWorker = worker = new ConnectionWorker();
            worker.thread = Thread.ofPlatform().daemon().name(WORKER_THREAD_NAME).unstarted(() -> this.runWorker(worker));
            worker.thread.start();
        }
    }

    private static void sendFrame(Socket socket, int channel, int frameType, byte[] body) throws IOException {
        socket.getOutputStream().write(AnyDeskRelayProtocol.encode(channel, frameType, body));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void runWorker(ConnectionWorker worker) {
        block50: {
            block52: {
                boolean cleanupRequired = false;
                try {
                    cleanupRequired = true;
                    this.publishStatus(worker, State.STARTING, 0L, 0L, "", FRESH_IDENTITY_DETAIL);
                    SSLContext sslContext = AnyDeskRelayClient.createSslContext();
                    int retryDelaySeconds = 2;
                    while (!worker.stopped) {
                        block48: {
                            block49: {
                                try {
                                    this.connectRelay(worker, sslContext);
                                    retryDelaySeconds = 2;
                                    break block48;
                                }
                                catch (Exception connectionFailure) {
                                    if (worker.stopped) break block49;
                                    this.publishStatus(worker, State.DISCONNECTED, 0L, 0L, "", connectionFailure.getMessage() == null ? connectionFailure.getClass().getSimpleName() : connectionFailure.getMessage());
                                    break block48;
                                }
                                finally {
                                    worker.closeSocket();
                                }
                            }
                            cleanupRequired = false;
                            break block50;
                        }
                        if (worker.stopped) {
                            cleanupRequired = false;
                            break block50;
                        }
                        Thread.sleep((long)retryDelaySeconds * 1000L);
                        retryDelaySeconds = Math.min(30, retryDelaySeconds * 2);
                    }
                    cleanupRequired = false;
                    break block50;
                }
                catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    cleanupRequired = false;
                }
                catch (Exception startupFailure) {
                    this.publishStatus(worker, State.ERROR, 0L, 0L, "", startupFailure.toString());
                    cleanupRequired = false;
                    break block52;
                }
                finally {
                    if (cleanupRequired) {
                        worker.closeSocket();
                        this.publishStatus(worker, State.STOPPED, 0L, 0L, "", "Stopped");
                        AnyDeskRelayClient interrupted = this;
                        synchronized (interrupted) {
                            if (this.connectionWorker == worker) {
                                this.connectionWorker = null;
                            }
                        }
                    }
                }
                worker.closeSocket();
                this.publishStatus(worker, State.STOPPED, 0L, 0L, "", "Stopped");
                AnyDeskRelayClient anyDeskRelayClient = this;
                synchronized (anyDeskRelayClient) {
                    if (this.connectionWorker == worker) {
                        this.connectionWorker = null;
                    }
                    return;
                }
            }
            worker.closeSocket();
            this.publishStatus(worker, State.STOPPED, 0L, 0L, "", "Stopped");
            AnyDeskRelayClient anyDeskRelayClient = this;
            synchronized (anyDeskRelayClient) {
                if (this.connectionWorker == worker) {
                    this.connectionWorker = null;
                }
                return;
            }
        }
        worker.closeSocket();
        this.publishStatus(worker, State.STOPPED, 0L, 0L, "", "Stopped");
        AnyDeskRelayClient anyDeskRelayClient = this;
        synchronized (anyDeskRelayClient) {
            if (this.connectionWorker == worker) {
                this.connectionWorker = null;
            }
        }
    }

    static void negotiateSession(Socket socket, int channel, int lane, int sessionVersion, long clientId, AnyDeskError error) throws IOException {
        Objects.requireNonNull(error);
        if (error != AnyDeskError.FREEZE) {
            AnyDeskRelayClient.sendStreamMessage(socket, channel, lane, AnyDeskRelayProtocol.encode(sessionVersion, clientId));
            if (error != AnyDeskError.NONE) {
                AnyDeskRelayClient.sendStreamMessage(socket, channel, 3, new byte[]{2, (byte)error.getErrorCode()});
            } else {
                AnyDeskRelayClient.sendStreamMessage(socket, channel, 3, new byte[]{24, 0, 0, 0, 0, 0});
                AnyDeskRelayClient.sendStreamMessage(socket, channel, 3, new byte[]{5});
                for (int capability : AnyDeskRelayProtocol.CAPABILITIES) {
                    AnyDeskRelayClient.sendStreamMessage(socket, channel, 0, new AnyDeskRelayProtocol.PacketWriter().write(6L, 1).write(capability, 4).write(0L, 4).toBytes());
                }
                AnyDeskRelayClient.sendStreamMessage(socket, channel, 3, new byte[]{8, 0, 0});
            }
        }
    }

    @Override
    public synchronized void close() {
        ConnectionWorker worker = this.connectionWorker;
        this.connectionWorker = null;
        if (worker != null) {
            worker.stop();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void connectRelay(ConnectionWorker worker, SSLContext sslContext) throws Exception {
        String relayHost = "boot-01.net.anydesk.com";
        HashSet<String> visitedHosts = new HashSet<String>();
        for (int redirectCount = 0; redirectCount < 3 && !worker.stopped; ++redirectCount) {
            if (!visitedHosts.add(relayHost)) {
                throw new IOException(REDIRECT_LOOP_ERROR);
            }
            try (Socket transportSocket = new Socket();){
                worker.attachSocket(transportSocket);
                transportSocket.connect(new InetSocketAddress(relayHost, 443), 8000);
                try (SSLSocket tlsSocket = (SSLSocket)sslContext.getSocketFactory().createSocket(transportSocket, relayHost, 443, true);){
                    worker.attachSocket(tlsSocket);
                    tlsSocket.setEnabledProtocols(new String[]{TLS_PROTOCOL});
                    tlsSocket.setSoTimeout(8000);
                    tlsSocket.startHandshake();
                    AnyDeskRelayProtocol.RelayFrame versionOffer = AnyDeskRelayClient.readFrame(tlsSocket, false);
                    if (versionOffer.channel() != 0 || versionOffer.type() != 0) {
                        throw new IOException(EXPECTED_VERSION_OFFER);
                    }
                    AnyDeskRelayClient.sendFrame(tlsSocket, 0, 1, AnyDeskRelayProtocol.encode(AnyDeskRelayProtocol.selectRelayVersion(versionOffer.body())));
                    AnyDeskRelayProtocol.RelayFrame registrationReply = AnyDeskRelayClient.readFrame(tlsSocket, false);
                    if (registrationReply.channel() != 0) {
                        throw new IOException(INVALID_REGISTRATION_CHANNEL);
                    }
                    if (registrationReply.type() != 3) {
                        if (registrationReply.type() != 2) {
                            throw new IOException(EXPECTED_ACKNOWLEDGEMENT);
                        }
                        long clientId = AnyDeskRelayProtocol.readClientId(registrationReply.body());
                        this.publishStatus(worker, State.ONLINE, clientId, 0L, REGISTERED_DETAIL);
                        this.handleSessions(worker, tlsSocket, clientId);
                        return;
                    }
                    relayHost = AnyDeskRelayProtocol.readRedirectHost(registrationReply.body());
                    continue;
                }
            }
            finally {
                worker.closeSocket();
            }
        }
        if (!worker.stopped) {
            throw new IOException(REDIRECT_LIMIT_ERROR);
        }
    }

    private static final class ConnectionWorker {
        volatile boolean stopped;
        @SerializedName(value="\u0412")
        Thread thread;
        @SerializedName(value="\u0412")
        private Socket socket;
        private static final String STOPPED_ERROR = "Stopped";

        private ConnectionWorker() {
        }

        synchronized void closeSocket() {
            if (this.socket != null) {
                try {
                    this.socket.close();
                }
                catch (IOException iOException) {
                    // empty catch block
                }
                this.socket = null;
            }
        }

        synchronized void attachSocket(Socket socket) throws IOException {
            if (this.stopped) {
                socket.close();
                throw new IOException("Stopped");
            }
            this.socket = socket;
        }

        synchronized void stop() {
            this.stopped = true;
            this.closeSocket();
            if (this.thread != null) {
                this.thread.interrupt();
            }
        }
    }

    public static enum State {
        STARTING,
        ONLINE,
        APPROVED,
        SESSION_ERROR,
        DISCONNECTED,
        ERROR,
        STOPPED,
        CONNECTING;

    }

    public record Status(State state, long clientId, long peerId, String peerIp, String detail) {
    }
}

