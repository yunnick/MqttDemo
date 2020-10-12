package com.demo.http2;

import com.google.common.io.Resources;
import okhttp3.*;
import okhttp3.internal.http2.Http2Connection;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class Http2ClientDemoDevice {

    private static OkHttpClient client;
    public static void main(String[] args) {
        try {
            initClient();
            doSimpleGet();
//            doGetRpc();
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

        String url = "https://test-iot-as-http2.stc-seedland.com.cn:8443/api/v1/9bbsYlUKF4SUnqlzVovU/rpc";
        // build request
        Request request = new Request.Builder()
                .addHeader("Connection", "Upgrade, HTTP2-Settings")
                .addHeader("Upgrade", "h2c")
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
