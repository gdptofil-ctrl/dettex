/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.misc.anydesk;

import java.io.IOException;
import java.util.LinkedHashSet;
import ru.recoin.implement.features.modules.misc.anydesk.AnyDeskRelayProtocol;

public final class AnyDeskPublicAddressDecoder {
    private static final long ADDRESS_LIST_OFFSET = 12L;
    private static final String ADDRESS_SEPARATOR = ", ";

    private static boolean isPublicIpv4(int firstOctet, int secondOctet, int thirdOctet) {
        return !(firstOctet == 0 || firstOctet == 10 || firstOctet == 127 || firstOctet >= 224 || firstOctet == 100 && secondOctet >= 64 && secondOctet <= 127 || firstOctet == 169 && secondOctet == 254 || firstOctet == 172 && secondOctet >= 16 && secondOctet <= 31 || firstOctet == 192 && (secondOctet == 168 || secondOctet == 0 && (thirdOctet == 0 || thirdOctet == 2) || secondOctet == 88 && thirdOctet == 99) || firstOctet == 198 && (secondOctet == 18 || secondOctet == 19 || secondOctet == 51 && thirdOctet == 100) || firstOctet == 203 && secondOctet == 0 && thirdOctet == 113);
    }

    private AnyDeskPublicAddressDecoder() {
    }

    public static String decodePublicAddresses(byte[] requestBody) throws IOException {
        AnyDeskRelayProtocol.readIncomingRequest(requestBody);
        AnyDeskRelayProtocol.PacketReader reader = new AnyDeskRelayProtocol.PacketReader(requestBody);
        reader.readBytes(12L);
        long addressCount = reader.readUnsignedInt();
        LinkedHashSet<String> publicAddresses = new LinkedHashSet<String>();
        for (long addressIndex = 0L; addressIndex < addressCount; ++addressIndex) {
            int addressType = reader.readUnsignedByte();
            int firstOctet = reader.readUnsignedByte();
            int secondOctet = reader.readUnsignedByte();
            int thirdOctet = reader.readUnsignedByte();
            int fourthOctet = reader.readUnsignedByte();
            int port = reader.readUnsignedShort();
            if (addressType != 1 && addressType != 2 || port == 0 || !AnyDeskPublicAddressDecoder.isPublicIpv4(firstOctet, secondOctet, thirdOctet)) continue;
            publicAddresses.add(firstOctet + "." + secondOctet + "." + thirdOctet + "." + fourthOctet);
        }
        return publicAddresses.isEmpty() ? null : String.join(ADDRESS_SEPARATOR, publicAddresses);
    }
}

