/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.annotations.SerializedName
 */
package ru.recoin.implement.features.modules.misc.anydesk;

import com.google.gson.annotations.SerializedName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class AnyDeskRelayProtocol {
    static final int STREAM_BUFFER_LIMIT = 0x100000;
    public static final int[] CAPABILITIES = new int[]{1, 2, 3, 4, 7, 9, 10, 11, 12, 13, 14, 15};
    private static final int CHANNEL_LIMIT = 65535;
    private static final int FRAME_LENGTH_LIMIT = 65535;
    private static final String INVALID_RELAY_FRAME = "Invalid relay frame";
    private static final String NO_SUPPORTED_RELAY_VERSION = "No supported relay version";
    private static final long REGISTRATION_MAJOR_VERSION = 6L;
    private static final long REGISTRATION_MINOR_VERSION = 5L;
    private static final String UNSUPPORTED_ACK_REVISION = "Unsupported ACK revision";
    private static final String UNKNOWN_ACK_FLAGS = "Unknown ACK flags";
    private static final long ACK_ADDRESS_BYTES = 6L;
    private static final long ACK_TOKEN_BYTES = 16L;
    private static final long ACK_ALTERNATE_ADDRESS_BYTES = 6L;
    private static final long ACK_ENTRY_BYTES = 16L;
    private static final long ACK_ENDPOINT_BYTES = 8L;
    private static final String MISSING_CLIENT_ID = "Relay did not assign an ID";
    private static final String UNSUPPORTED_TERMINAL_REVISION = "Unsupported relay terminal revision";
    private static final String RELAY_HOST_SUFFIX = ".net.anydesk.com";
    private static final String UNSUPPORTED_REQUEST_REVISION = "Unsupported incoming request revision";
    private static final long REQUEST_ADDRESS_BYTES = 7L;
    private static final long REQUEST_OPTION_BYTES = 8L;
    private static final long REQUEST_EXTRA_OPTION_BYTES = 8L;
    private static final String EXPECTED_SESSION_REQUEST = "Expected session request";
    private static final String INVALID_EXCLUDED_VERSION_COUNT = "Invalid excluded version count";
    private static final long EXTENSION_MINIMUM_VERSION = 2L;
    private static final long EXTENSION_MAXIMUM_VERSION = 2L;
    private static final long EXTENSION_VERSION = 2L;
    private static final long SESSION_EXTENSION_BYTES = 10L;
    private static final long SESSION_EXTRA_BYTES = 8L;
    private static final long SESSION_FLAG_BYTES = 6L;
    private static final String NO_SUPPORTED_SESSION_VERSION = "No supported session version";
    private static final String CLIENT_DESCRIPTION = "Recoin-AnyDesk";
    private static final String ACCESS_DESCRIPTION = "No screen or input access";
    private static final long SESSION_PLATFORM_CODE = 2L;
    private static final long SESSION_MAJOR_VERSION = 6L;
    private static final long SESSION_MINOR_VERSION = 5L;
    private static final int STREAM_MESSAGE_LIMIT = 60000;
    private static final String INVALID_STREAM_MESSAGE = "Invalid outgoing stream message";

    public static byte[] encode(int lane, byte[] messageBody) {
        if (lane >= 0 && lane <= 10 && messageBody.length != 0 && messageBody.length <= 60000) {
            byte[] messageRecord = new PacketWriter().write(messageBody.length).write(messageBody).toBytes();
            byte[] laneRecord = new PacketWriter().write(lane, 1).write(messageRecord.length).write(messageRecord).toBytes();
            return new PacketWriter().write(laneRecord.length).write(laneRecord).toBytes();
        }
        throw new IllegalArgumentException(INVALID_STREAM_MESSAGE);
    }

    public static int selectRelayVersion(byte[] offerBody) throws IOException {
        PacketReader reader = new PacketReader(offerBody);
        int minimumVersion = reader.readUnsignedShort();
        int maximumVersion = reader.readUnsignedShort();
        int excludedCount = reader.readUnsignedShort();
        HashSet<Integer> excludedVersions = new HashSet<Integer>();
        for (int excludedIndex = 0; excludedIndex < excludedCount; ++excludedIndex) {
            excludedVersions.add(reader.readUnsignedShort());
        }
        reader.requireEnd();
        for (int candidateVersion : new int[]{3, 2}) {
            if (minimumVersion > candidateVersion || candidateVersion > maximumVersion || excludedVersions.contains(candidateVersion)) continue;
            return candidateVersion;
        }
        throw new IOException(NO_SUPPORTED_RELAY_VERSION);
    }

    public static String readRedirectHost(byte[] terminalBody) throws IOException {
        PacketReader reader = new PacketReader(terminalBody);
        if (reader.readUnsignedByte() != 1) {
            throw new IOException(UNSUPPORTED_TERMINAL_REVISION);
        }
        int closureReason = reader.readUnsignedByte();
        reader.readUnsignedInt();
        String redirectHost = null;
        int groupCount = reader.readUnsignedByte();
        for (int groupIndex = 0; groupIndex < groupCount; ++groupIndex) {
            reader.readBytes(reader.readUnsignedByte());
            int endpointCount = reader.readUnsignedByte();
            for (int endpointIndex = 0; endpointIndex < endpointCount; ++endpointIndex) {
                reader.readUnsignedInt();
                String hostname = new String(reader.readBytes(reader.readUnsignedByte()), StandardCharsets.US_ASCII);
                int portCount = reader.readUnsignedByte();
                for (int portIndex = 0; portIndex < portCount; ++portIndex) {
                    int port = reader.readUnsignedShort();
                    if (redirectHost != null || port != 443 || !hostname.endsWith(RELAY_HOST_SUFFIX)) continue;
                    redirectHost = hostname;
                }
            }
        }
        reader.requireEnd();
        if (redirectHost == null) {
            throw new IOException("Relay closed registration, reason " + closureReason);
        }
        return redirectHost;
    }

    public static long readClientId(byte[] acknowledgementBody) throws IOException {
        PacketReader reader = new PacketReader(acknowledgementBody);
        if (reader.readUnsignedByte() != 0) {
            throw new IOException(UNSUPPORTED_ACK_REVISION);
        }
        int flags = reader.readUnsignedShort();
        if ((flags & 0xFFFFFF00) != 0) {
            throw new IOException(UNKNOWN_ACK_FLAGS);
        }
        long clientId = 0L;
        if ((flags & 1) != 0) {
            clientId = reader.readUnsignedInt();
            reader.skipLengthPrefixed();
        }
        if ((flags & 2) != 0) {
            reader.readBytes(6L);
        }
        if ((flags & 4) != 0) {
            reader.readUnsignedByte();
            reader.skipLengthPrefixed();
            reader.readBytes(16L);
        }
        if ((flags & 8) != 0) {
            reader.readBytes(6L);
        }
        if ((flags & 0x10) != 0) {
            reader.readBytes(reader.readUnsignedByte());
        }
        if ((flags & 0x20) != 0) {
            reader.readBytes(reader.readUnsignedInt() * 16L);
        }
        if ((flags & 0x40) != 0) {
            reader.readBytes((long)reader.readUnsignedByte() * 8L);
        }
        if ((flags & 0x80) != 0) {
            reader.skipLengthPrefixed();
        }
        reader.requireEnd();
        if (clientId == 0L) {
            throw new IOException(MISSING_CLIENT_ID);
        }
        return clientId;
    }

    public static byte[] encode(int sessionVersion, long clientId) {
        return new PacketWriter().write(1L, 1).write(sessionVersion, 4).write(clientId, 4).writeLengthPrefixed(CLIENT_DESCRIPTION.getBytes(StandardCharsets.UTF_8)).writeLengthPrefixed(ACCESS_DESCRIPTION.getBytes(StandardCharsets.UTF_8)).write(0L, 1).write(0L, 8).write(0L, 2).write(2L, 4).write(6L, 2).write(0L, 2).write(5L, 2).toBytes();
    }

    public static byte[] encode(int relayVersion) {
        return new PacketWriter().write(0L, 1).write(relayVersion, 2).write(6L, 2).write(0L, 2).write(5L, 2).write(0L, 2).write(1L, 2).toBytes();
    }

    public static int selectSessionVersion(byte[] requestBody) throws IOException {
        PacketReader reader = new PacketReader(requestBody);
        if (reader.readUnsignedByte() != 0) {
            throw new IOException(EXPECTED_SESSION_REQUEST);
        }
        long minimumVersion = reader.readUnsignedInt();
        reader.readUnsignedInt();
        reader.skipLengthPrefixed();
        reader.skipLengthPrefixed();
        if (reader.readUnsignedByte() != 0) {
            reader.skipLengthPrefixed();
        }
        long maximumVersion = 1L;
        HashSet<Long> excludedVersions = new HashSet<Long>();
        if (reader.remaining() > 0) {
            long flags;
            maximumVersion = reader.readUnsignedInt();
            long excludedCount = reader.readUnsignedInt();
            if (excludedCount > (long)(reader.remaining() / 4)) {
                throw new IOException(INVALID_EXCLUDED_VERSION_COUNT);
            }
            for (long excludedIndex = 0L; excludedIndex < excludedCount; ++excludedIndex) {
                excludedVersions.add(reader.readUnsignedInt());
            }
            if (minimumVersion <= 2L && maximumVersion >= 2L && !excludedVersions.contains(2L)) {
                reader.readUnsignedByte();
            }
            if (reader.remaining() > 0) {
                reader.readBytes(10L);
            }
            if (reader.remaining() > 0) {
                reader.skipLengthPrefixed();
                reader.readBytes(8L);
            }
            long l = flags = reader.remaining() > 0 ? reader.readUnsignedInt() : 0L;
            if ((flags & 1L) != 0L) {
                reader.readBytes(6L);
            }
        }
        for (int candidateVersion = 4; candidateVersion >= 1; --candidateVersion) {
            if (minimumVersion > (long)candidateVersion || (long)candidateVersion > maximumVersion || excludedVersions.contains(candidateVersion)) continue;
            return candidateVersion;
        }
        throw new IOException(NO_SUPPORTED_SESSION_VERSION);
    }

    AnyDeskRelayProtocol() {
    }

    public static byte[] encode(int channel, int frameType, byte[] body) {
        if (channel >= 0 && channel < 65535 && body.length + 3 < 65535) {
            return new PacketWriter().write(body.length + 3, 2).write(channel, 2).write(frameType, 1).write(body).toBytes();
        }
        throw new IllegalArgumentException(INVALID_RELAY_FRAME);
    }

    public static byte[] encode(long requestToken, int channel) {
        return new PacketWriter().write(0L, 1).write(requestToken, 4).write(1L, 1).write(channel, 2).write(0L, 4).toBytes();
    }

    public static IncomingRequest readIncomingRequest(byte[] requestBody) throws IOException {
        PacketReader reader = new PacketReader(requestBody);
        if (reader.readUnsignedByte() != 0) {
            throw new IOException(UNSUPPORTED_REQUEST_REVISION);
        }
        long requestToken = reader.readUnsignedInt();
        int flags = reader.readUnsignedByte();
        long peerId = reader.readUnsignedInt();
        reader.readUnsignedShort();
        reader.readBytes(reader.readUnsignedInt() * 7L);
        if ((flags & 8) != 0) {
            reader.readBytes(8L);
        }
        if ((flags & 0x10) != 0) {
            reader.readBytes(8L);
        }
        reader.requireEnd();
        return new IncomingRequest(requestToken, peerId);
    }

    public static final class PacketWriter {
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();

        PacketWriter write(byte[] bytes) {
            this.output.writeBytes(bytes);
            return this;
        }

        PacketWriter writeLengthPrefixed(byte[] bytes) {
            return this.write(bytes.length, 4).write(bytes);
        }

        public byte[] toBytes() {
            return this.output.toByteArray();
        }

        public PacketWriter write(long value, int width) {
            for (int byteIndex = width - 1; byteIndex >= 0; --byteIndex) {
                this.output.write((int)(value >>> 8 * byteIndex));
            }
            return this;
        }

        PacketWriter write(int value) {
            while (value >= 128) {
                this.output.write(value & 0x7F | 0x80);
                value >>>= 7;
            }
            this.output.write(value);
            return this;
        }
    }

    public static final class PacketReader {
        private final byte[] data;
        @SerializedName(value="4\u0413")
        private int offset;
        private static final String TRUNCATED_BYTES = "Truncated packet";
        private static final String TRUNCATED_BYTE = "Truncated packet";
        private static final String VARINT_OVERFLOW = "Varint32 overflow";
        private static final String UNTERMINATED_VARINT = "Unterminated varint";
        private static final String UNEXPECTED_TRAILING_DATA = "Unexpected trailing data";

        public byte[] readBytes(long length) throws IOException {
            if (length >= 0L && length <= (long)this.remaining()) {
                byte[] bytes = Arrays.copyOfRange(this.data, this.offset, this.offset + (int)length);
                this.offset += (int)length;
                return bytes;
            }
            throw new IOException("Truncated packet");
        }

        public PacketReader(byte[] data) {
            this.data = data;
        }

        public long readUnsignedInt() throws IOException {
            return (long)this.readUnsignedShort() << 16 | (long)this.readUnsignedShort();
        }

        public int readUnsignedByte() throws IOException {
            if (this.remaining() == 0) {
                throw new IOException("Truncated packet");
            }
            return this.data[this.offset++] & 0xFF;
        }

        public int readUnsignedShort() throws IOException {
            return this.readUnsignedByte() << 8 | this.readUnsignedByte();
        }

        public void requireEnd() throws IOException {
            if (this.remaining() != 0) {
                throw new IOException(UNEXPECTED_TRAILING_DATA);
            }
        }

        int remaining() {
            return this.data.length - this.offset;
        }

        long readVarint() throws IOException {
            long value = 0L;
            for (int byteIndex = 0; byteIndex < 5; ++byteIndex) {
                int nextByte = this.readUnsignedByte();
                if (byteIndex == 4 && nextByte > 15) {
                    throw new IOException(VARINT_OVERFLOW);
                }
                value |= (long)(nextByte & 0x7F) << 7 * byteIndex;
                if (nextByte >= 128) continue;
                return value;
            }
            throw new IOException(UNTERMINATED_VARINT);
        }

        void skipLengthPrefixed() throws IOException {
            this.readBytes(this.readUnsignedInt());
        }
    }

    public record IncomingRequest(long token, long peerId) {
    }

    public record StreamMessage(int lane, byte[] body) {
    }

    public record RelayFrame(int channel, int type, byte[] body) {
    }

    static final class RecordReassembler {
        private byte[] pendingBytes = new byte[0];
        private static final long BUFFER_LIMIT = 0x100000L;
        private static final String STREAM_BUFFER_OVERFLOW = "Stream buffer limit";
        private static final String RECORD_VARINT_OVERFLOW = "Varint32 overflow";
        private static final long RECORD_LENGTH_LIMIT = 0x100000L;
        private static final String INVALID_RECORD_LENGTH = "Invalid stream record length";

        RecordReassembler() {
        }

        List<byte[]> append(byte[] incomingBytes) throws IOException {
            if ((long)this.pendingBytes.length + (long)incomingBytes.length > 0x100000L) {
                throw new IOException(STREAM_BUFFER_OVERFLOW);
            }
            byte[] combinedBytes = Arrays.copyOf(this.pendingBytes, this.pendingBytes.length + incomingBytes.length);
            System.arraycopy(incomingBytes, 0, combinedBytes, this.pendingBytes.length, incomingBytes.length);
            ArrayList<byte[]> records = new ArrayList<byte[]>();
            int consumedOffset = 0;
            while (consumedOffset < combinedBytes.length) {
                int readOffset = consumedOffset;
                long recordLength = 0L;
                boolean lengthComplete = false;
                for (int varintByteIndex = 0; varintByteIndex < 5 && readOffset < combinedBytes.length; ++varintByteIndex) {
                    int nextByte = combinedBytes[readOffset++] & 0xFF;
                    if (varintByteIndex == 4 && nextByte > 15) {
                        throw new IOException(RECORD_VARINT_OVERFLOW);
                    }
                    recordLength |= (long)(nextByte & 0x7F) << varintByteIndex * 7;
                    if (nextByte >= 128) {
                        continue;
                    }
                    lengthComplete = true;
                    break;
                }
                if (!lengthComplete) break;
                if (recordLength < 1L || recordLength > 0x100000L) {
                    throw new IOException(INVALID_RECORD_LENGTH);
                }
                if (recordLength > (long)(combinedBytes.length - readOffset)) break;
                records.add(Arrays.copyOfRange(combinedBytes, readOffset, readOffset + (int)recordLength));
                consumedOffset = readOffset + (int)recordLength;
            }
            this.pendingBytes = Arrays.copyOfRange(combinedBytes, consumedOffset, combinedBytes.length);
            return records;
        }
    }

    public static final class StreamDecoder {
        private final RecordReassembler outerRecords = new RecordReassembler();
        private final Map<Integer, RecordReassembler> laneRecords = new HashMap<Integer, RecordReassembler>();
        private static final String UNSUPPORTED_STREAM_LANE = "Unsupported stream lane";

        public List<StreamMessage> decode(byte[] streamBytes) throws IOException {
            ArrayList<StreamMessage> messages = new ArrayList<StreamMessage>();
            for (byte[] record : this.outerRecords.append(streamBytes)) {
                PacketReader reader = new PacketReader(record);
                while (reader.remaining() > 0) {
                    int lane = reader.readUnsignedByte();
                    if (lane > 10) {
                        throw new IOException(UNSUPPORTED_STREAM_LANE);
                    }
                    byte[] laneBytes = reader.readBytes(reader.readVarint());
                    for (byte[] messageBody : this.laneRecords.computeIfAbsent(lane, laneKey -> new RecordReassembler()).append(laneBytes)) {
                        messages.add(new StreamMessage(lane, messageBody));
                    }
                }
            }
            return messages;
        }
    }
}

