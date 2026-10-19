/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.misc.anydesk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import javax.net.ssl.KeyManagerFactory;

final class AnyDeskTlsIdentity {
    private static final byte[] SIGNATURE_ALGORITHM_DER = HexFormat.of().parseHex("300d06092a864886f70d01010b0500");
    private static final String KEY_ALGORITHM = "RSA";
    private static final String COMMON_NAME_OID_HEX = "0603550403";
    private static final String CLIENT_COMMON_NAME = "AnyDesk Client";
    private static final long VALIDITY_BACKDATE_SECONDS = 60L;
    private static final long VALIDITY_DURATION_SECONDS = 31536000L;
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String CERTIFICATE_TYPE = "X.509";
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String KEY_ALIAS = "ephemeral";
    private static final String GENERALIZED_TIME_PATTERN = "yyyyMMddHHmmss'Z'";
    private static final String SIGNATURE_ALGORITHM_HEX = "300d06092a864886f70d01010b0500";

    private AnyDeskTlsIdentity() {
    }

    private static byte[] encodeDer(Instant timestamp) {
        return AnyDeskTlsIdentity.encodeDer(24, new byte[][]{DateTimeFormatter.ofPattern(GENERALIZED_TIME_PATTERN).withZone(ZoneOffset.UTC).format(timestamp).getBytes(StandardCharsets.US_ASCII)});
    }

    private static byte[] encodeDer(int tag, byte[] ... components) {
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        for (byte[] component : components) {
            content.writeBytes(component);
        }
        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        encoded.write(tag);
        int contentLength = content.size();
        if (contentLength < 128) {
            encoded.write(contentLength);
        } else {
            int lengthOctets = (32 - Integer.numberOfLeadingZeros(contentLength) + 7) / 8;
            encoded.write(0x80 | lengthOctets);
            for (int octetIndex = lengthOctets - 1; octetIndex >= 0; --octetIndex) {
                encoded.write(contentLength >>> 8 * octetIndex);
            }
        }
        encoded.writeBytes(content.toByteArray());
        return encoded.toByteArray();
    }

    static KeyManagerFactory createKeyManager() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        byte[] subjectName = AnyDeskTlsIdentity.encodeDer(48, new byte[][]{AnyDeskTlsIdentity.encodeDer(49, new byte[][]{AnyDeskTlsIdentity.encodeDer(48, HexFormat.of().parseHex(COMMON_NAME_OID_HEX), AnyDeskTlsIdentity.encodeDer(12, new byte[][]{CLIENT_COMMON_NAME.getBytes(StandardCharsets.UTF_8)}))})});
        Instant now = Instant.now();
        byte[] certificateBody = AnyDeskTlsIdentity.encodeDer(48, AnyDeskTlsIdentity.encodeDer(160, new byte[][]{AnyDeskTlsIdentity.encodeDer(2, new byte[][]{{2}})}), AnyDeskTlsIdentity.encodeDer(2, new byte[][]{new BigInteger(159, new SecureRandom()).add(BigInteger.ONE).toByteArray()}), SIGNATURE_ALGORITHM_DER, subjectName, AnyDeskTlsIdentity.encodeDer(48, AnyDeskTlsIdentity.encodeDer(now.minusSeconds(60L)), AnyDeskTlsIdentity.encodeDer(now.plusSeconds(31536000L))), subjectName, keyPair.getPublic().getEncoded());
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(keyPair.getPrivate());
        signature.update(certificateBody);
        byte[] certificateDer = AnyDeskTlsIdentity.encodeDer(48, certificateBody, SIGNATURE_ALGORITHM_DER, AnyDeskTlsIdentity.encodeDer(3, new byte[]{0}, signature.sign()));
        X509Certificate certificate = (X509Certificate)CertificateFactory.getInstance(CERTIFICATE_TYPE).generateCertificate(new ByteArrayInputStream(certificateDer));
        certificate.checkValidity();
        certificate.verify(keyPair.getPublic());
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        keyStore.load(null, null);
        char[] password = new char[]{};
        keyStore.setKeyEntry(KEY_ALIAS, keyPair.getPrivate(), password, new X509Certificate[]{certificate});
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);
        return keyManagerFactory;
    }
}

