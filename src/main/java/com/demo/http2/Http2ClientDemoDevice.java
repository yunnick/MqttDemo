package com.demo.http2;

import com.google.common.io.Resources;
import okhttp3.*;
import okhttp3.internal.http2.Http2Connection;
import org.apache.commons.codec.digest.HmacUtils;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class Http2ClientDemoDevice {

    private static OkHttpClient client  = new OkHttpClient.Builder().build();
        static String hostName = "https://ppe-iot-as-http2.stc-seedland.com.cn:8843";
//    static String hostName = "https://test-iot-as-http2.stc-seedland.com.cn:8443";
//    static String hostName = "https://iot-as-http2.stc-seedland.com.cn:8443";
//    static String hostName = "https://testhttp2.stc-seedland.com.cn:8843";
//    static String hostName = "http://testhttp2.stc-seedland.com.cn:8080";
    public static void main(String[] args) {
        try {
//            initClient();
//            doSimpleGet();
            doGetRpc();
//            doGetAttributeUpdate();
//            doPostAttribute();
//            doGetAttribute();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static void doSimpleGet() throws Exception {

        String url = "https://test-iot-as-http2.stc-seedland.com.cn:8443";
        // build request
        Request request = new Request.Builder()
                .addHeader("Connection", "Upgrade, HTTP2-Settings")
                .addHeader("Upgrade", "h2c")
                .url(url)
                .build();
        execute(request);
    }

    static void doPostAttribute() throws Exception {

        String url = "https://test-iot-as-http2.stc-seedland.com.cn:8443/api/v1/9bbsYlUKF4SUnqlzVovU/attributes";
        // build request
        Request request = new Request.Builder()
                .addHeader("Connection", "Upgrade, HTTP2-Settings")
                .addHeader("Upgrade", "h2c")
                .method("POST", RequestBody.create(MediaType.parse("application/json"), "{\"client_attr\":\"av1\"}"))
                .url(url)
                .build();
        execute(request);
    }
    static void doGetRpc() throws Exception {

        String url = hostName + "/api/v1/device/rpc?timeout=100000";
        String algorithm = "hmacsha1";
        String id = "1eb295d3e7f9ca08e49cb0ad6dcf41a";
        String secret = "q3JT98ibplqYfbjpK7sW";
        String ts = System.currentTimeMillis() + "";
        String sign = Base64.getEncoder().encodeToString(HmacUtils.getInitializedMac(algorithm, secret.getBytes()).doFinal((ts).getBytes()));

        // build request
        Request request = new Request.Builder()
                .addHeader("device-version", "1.0")
                .addHeader("request-ts", ts)
                .addHeader("authorization", "hmac id="+id+",algorithm="+algorithm+",headers=request-ts,signature="+sign)
                .addHeader("Connection", "Upgrade, HTTP2-Settings")
                .addHeader("Upgrade", "h2c")
                .method("GET", null)
                .url(url)
                .build();
        execute(request);
    }

    static void doGetAttributeUpdate() throws Exception {

        String url = "https://test-iot-as-http2.stc-seedland.com.cn:8443/api/v1/9bbsYlUKF4SUnqlzVovU/attributes/updates";
        // build request
        Request request = new Request.Builder()
                .addHeader("Connection", "Upgrade, HTTP2-Settings")
                .addHeader("Upgrade", "h2c")
                .url(url)
                .build();
        execute(request);
    }

    static void doGetAttribute() throws Exception {

        String url = "https://test-iot-as-http2.stc-seedland.com.cn:8443/api/v1/9bbsYlUKF4SUnqlzVovU/attributes?clientKeys=client_attr,groupId,confs";
        // build request
        Request request = new Request.Builder()
                .addHeader("Connection", "Upgrade, HTTP2-Settings")
                .addHeader("Upgrade", "h2c")
                .url(url)
                .build();
        execute(request);
    }

    static void execute(Request request)throws Exception{
        // send request
        Response response = client.newCall(request).execute();
        // print message
        String rslt = response.body().string();
        String protocol = response.protocol().name();
        System.out.println(rslt); // 打印响应：ECHO
        System.out.println(protocol); // 打印http protocol
    }

    static void initClient() throws Exception {
        CertificateFactory cAf = CertificateFactory.getInstance("X.509");
        FileInputStream caIn = new FileInputStream(new File(Resources.getResource("htt2server-local.pub.pem").toURI()));
//        FileInputStream caIn = new FileInputStream(new File(Resources.getResource("stc-seedland.com.cn.pub.pem").toURI()));
        X509Certificate ca = (X509Certificate) cAf.generateCertificate(caIn);
        KeyStore caKs = KeyStore.getInstance("JKS");
        caKs.load(null, null);
        caKs.setCertificateEntry("ca-certificate", ca);

        // 初始化证书管理factory
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(caKs);

        // 获得X509TrustManager
        TrustManager[] trustManagers = factory.getTrustManagers();
        X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

        // 初始化sslContext
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2"); // 这里选择的是tls1.2版本
        sslContext.init(null, new TrustManager[] { trustManager }, null);

        // 获得sslSocketFactory
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        // 初始化client builder
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(sslSocketFactory, trustManager)
                .hostnameVerifier(new HostnameVerifier() { // 放过host验证
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });

        // build client
        client = builder.build();
    }


}
