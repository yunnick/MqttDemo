package com.demo.mqtt.server;

import io.netty.handler.ssl.SslHandler;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.springframework.util.Base64Utils;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class MqttSslHandlerProvider {

    private String sslProtocol;
    private static final String KEY_STORE_FILE = "mqttserver.jks";
    private static final String KEY_STORE_PASSWORD = "@lmggTy6XNZmJwu7";
    private static final String KEY_PASSWORD = "@lmggTy6XNZmJwu7";

//    private static final String KEY_STORE_PASSWORD = "WETuK4*z%zRB!Ew0";
//    private static final String KEY_PASSWORD = "WETuK4*z%zRB!Ew0";
    private static final String KEY_STORE_TYPE = "JKS";

    private TransportService transportService = new TransportService();

    public SslHandler getSslHandler() {
        try {
            TrustManagerFactory tmFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            KeyStore trustStore = KeyStore.getInstance(KEY_STORE_TYPE);
            try (InputStream tsFileInputStream =
                         this.getClass().getClassLoader().getResourceAsStream(KEY_STORE_FILE)) {
                trustStore.load(tsFileInputStream, KEY_STORE_PASSWORD.toCharArray());
            }
            tmFactory.init(trustStore);

            KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE);
            try (InputStream ksFileInputStream =
                         this.getClass().getClassLoader().getResourceAsStream(KEY_STORE_FILE)) {
                ks.load(ksFileInputStream, KEY_STORE_PASSWORD.toCharArray());
            }
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, KEY_PASSWORD.toCharArray());

            KeyManager[] km = kmf.getKeyManagers();
            TrustManager x509wrapped = getX509TrustManager(tmFactory);
            TrustManager[] tm = {x509wrapped};
            if (StringUtils.isEmpty(sslProtocol)) {
                sslProtocol = "TLSV1.2";
            }
            SSLContext sslContext = SSLContext.getInstance(sslProtocol);
            sslContext.init(km, tm, null);
            SSLEngine sslEngine = sslContext.createSSLEngine();
            sslEngine.setUseClientMode(false);
            sslEngine.setNeedClientAuth(false);
            sslEngine.setWantClientAuth(true);
            sslEngine.setEnabledProtocols(sslEngine.getSupportedProtocols());
            sslEngine.setEnabledCipherSuites(sslEngine.getSupportedCipherSuites());
            sslEngine.setEnableSessionCreation(true);
            return new SslHandler(sslEngine);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get SSL handler", e);
        }
    }

    private TrustManager getX509TrustManager(TrustManagerFactory tmf) {
        X509TrustManager x509Tm = null;
        for (TrustManager tm : tmf.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                x509Tm = (X509TrustManager) tm;
                break;
            }
        }
        return new ThingsboardMqttX509TrustManager(x509Tm, transportService);
    }
    static class TransportService{
        void process(Object msg){
            System.out.println("msg:" + msg);
        }
    }

    static class ThingsboardMqttX509TrustManager implements X509TrustManager {

        private final X509TrustManager trustManager;
        private TransportService transportService;

        ThingsboardMqttX509TrustManager(X509TrustManager trustManager, TransportService transportService) {
            this.trustManager = trustManager;
            this.transportService = transportService;
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return trustManager.getAcceptedIssuers();
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
            trustManager.checkServerTrusted(chain, authType);
        }

        /**
         * 自定义客户端认证方式
         */
        @Override
        public void checkClientTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
            /**
             * 下面这行代码标识使用默认证书验证
             */
            //            trustManager.checkClientTrusted(chain, authType);
            /**
             * 如下通过获取唯一标识，自定义验证。也是ThingsBoard的验证方式
             */
            String credentialsBody = null;
            for (X509Certificate cert : chain) {
                try {
                    System.out.println("cert:" + cert);
                    String strCert = getX509CertificateString(cert);
                    String sha3Hash = getSha3Hash(strCert);
                    final String[] credentialsBodyHolder = new String[1];
                    System.out.println("=========================");
                    System.out.println("sha3Hash:" + sha3Hash);
                    transportService.process(sha3Hash);
                    if (strCert.equals(credentialsBodyHolder[0])) {
                        credentialsBody = credentialsBodyHolder[0];
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("credentialsBody:" + credentialsBody);
        }

        static String getX509CertificateString(X509Certificate cert)
                throws CertificateEncodingException, IOException {
            Base64Utils.encodeToString(cert.getEncoded());
            return trimNewLines(Base64Utils.encodeToString(cert.getEncoded()));
        }
        static String getSha3Hash(String data) {
            String trimmedData = trimNewLines(data);
            byte[] dataBytes = trimmedData.getBytes();
            SHA3Digest md = new SHA3Digest(256);
            md.reset();
            md.update(dataBytes, 0, dataBytes.length);
            byte[] hashedBytes = new byte[256 / 8];
            md.doFinal(hashedBytes, 0);
            return ByteUtils.toHexString(hashedBytes);
        }
        static String trimNewLines(String input) {
            return input.replaceAll("-----BEGIN CERTIFICATE-----", "")
                    .replaceAll("-----END CERTIFICATE-----", "")
                    .replaceAll("\n","")
                    .replaceAll("\r","");
        }

        public static void main(String[] args) {
            //from  mqttclient.pub.pem
            String cert = "-----BEGIN CERTIFICATE-----\n" +
                    "MIIDbTCCAlWgAwIBAgIEGHY6zTANBgkqhkiG9w0BAQsFADBnMQswCQYDVQQGEwJV\n" +
                    "UzELMAkGA1UECBMCQ0ExCzAJBgNVBAcTAlNGMRQwEgYDVQQKEwtUaGluZ3Nib2Fy\n" +
                    "ZDEUMBIGA1UECxMLVGhpbmdzYm9hcmQxEjAQBgNVBAMTCWxvY2FsaG9zdDAeFw0x\n" +
                    "OTExMDUwOTM3MjlaFw00NzAzMjIwOTM3MjlaMGcxCzAJBgNVBAYTAlVTMQswCQYD\n" +
                    "VQQIEwJDQTELMAkGA1UEBxMCU0YxFDASBgNVBAoTC1RoaW5nc2JvYXJkMRQwEgYD\n" +
                    "VQQLEwtUaGluZ3Nib2FyZDESMBAGA1UEAxMJbG9jYWxob3N0MIIBIjANBgkqhkiG\n" +
                    "9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0vj7qohRadR9TLYB1FNKlxDGuwPenI9yhDrX\n" +
                    "caE6heVcadjcvqR06lcOsKgZ/FQysfu2lIX09R1JWI/tnq8sIBKOlDFGxk0DQrt7\n" +
                    "IEj6T8IMhhcVfZT44OElKe/GPY24PPPCqbVcIUVOgYdJcT4OZLXMCbeUsB9zc+c7\n" +
                    "9wMzHnXZql4Q1Ye5pzqmfOM+HibVDpyWPWYq8BGOy3tJygS/y03VAHCrI0RPho5w\n" +
                    "DFcrG7Iz45QiKVw2fdTcvY6W/jIawSYiUxCnoTtO6d+m+l0BfUjH41eIzaPxXa6E\n" +
                    "mNzvSGwhdCgdj2pQsW1s/MlNmDz0i9H1RyFd3H1LGGO2S9nKWwIDAQABoyEwHzAd\n" +
                    "BgNVHQ4EFgQUIiiggybh25rLUwnwn4atUFEOdgIwDQYJKoZIhvcNAQELBQADggEB\n" +
                    "AMQuH2+vyStRTJDuIPRoV4MjbaRgUqYHnT1Jb+pNVxw8FVgb5ES7NciF9oU1k+4A\n" +
                    "EQAI1cCpQJFhO14u0DrEqyMRN8tY8hfzVvFeEckN9K6rIKwnmnQNlPuNpki73GJe\n" +
                    "Ji1yThaqJgylpwsjJaY9cTJ0KRIoPyZWs5N/xvM5DW4YIGnuHx9UaYihiR3T5maH\n" +
                    "w/WhJo8rNu2+fdcx07Nn9SqGY0HnxabLhAPRF/t+fBymXhMb3YydgkRKIAOq8Fwl\n" +
                    "5CjhyNEUNrTI0nzhI6e0OXA8aeq/zYKqfbmZ84hPv/8T1XZC2iyWekAdGDAumI5r\n" +
                    "NCGYa7yXfItpKTRsdpCnhNo=\n" +
                    "-----END CERTIFICATE-----\n";
            String strCert = trimNewLines(cert);
            String sha3Hash = getSha3Hash(strCert);
            System.out.println("sha3Hash:" + sha3Hash);

        }
    }


}
