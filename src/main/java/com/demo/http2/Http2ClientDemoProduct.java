package com.demo.http2;

import com.google.common.io.Resources;
import okhttp3.*;
import org.apache.commons.codec.digest.HmacUtils;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class Http2ClientDemoProduct {

    private static OkHttpClient client = new OkHttpClient.Builder().build();
    static String hostName = "https://ppe-iot-as-http2.stc-seedland.com.cn:8843";
//    static String hostName = "https://test-iot-as-http2.stc-seedland.com.cn:8443";
//    static String hostName = "https://iot-as-http2.stc-seedland.com.cn:8443";
//static String hostName = "https://testhttp2.stc-seedland.com.cn:8843";
//    static String hostName = "http://testhttp2.stc-seedland.com.cn:8080";
    public static void main(String[] args) {

        try {
            String ca = "stc-seedland.com.cn.pub.pem";
//            String ca = "htt2server-local.pub.pem";
//            initClient(ca);
//            doGetDemo();
            doPostHeartbeat();
//            doPostTelemetry();
//            doPostAttributes();
//            doGetAttributes();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static void doGetDemo() throws Exception {

//        String url = "https://test-iot-as-http2.stc-seedland.com.cn/api/v1/product/heartbeat";
        String url = hostName+"/api/v1/product/demo";
//        String url = "https://10.22.30.36:8443/api/v1/product/heartbeat";
        String algorithm = "hmacsha1";
        String id = "1ea005c51873c70a91be533014be8ca";
        String secret = "AsYVQPSb6iHQzkPi1mJC";
        String ts = System.currentTimeMillis() + "";
        String sign = Base64.getEncoder().encodeToString(HmacUtils.getInitializedMac(algorithm, secret.getBytes()).doFinal((ts).getBytes()));
        // build request
        Request request = new Request.Builder()
                .addHeader("Device-version", "1.0")
                .addHeader("request-ts", ts)
                .addHeader("authorization", "hmac id="+id+",algorithm="+algorithm+",headers=request-ts,signature="+sign)
                .addHeader("Connection", "Upgrade, HTTP2-Settings")
                .addHeader("Upgrade", "h2c")
                .method("GET", null)
                .url(url)
                .build();
        execute(request);
    }


    static void doPostHeartbeat() throws Exception {

        String url = hostName + "/api/v1/product/heartbeat";
//        String url = "https://test-iot-as-http2.stc-seedland.com.cn:8443/api/v1/product/demo";
//        String url = "https://10.22.30.36:8443/api/v1/product/heartbeat";
        String algorithm = "hmacsha1";
//        String id ="1eb0d328ff77fe0a9b365991606617d";//prod
//        String secret = "SKR2477UA1pwAy5OCSiN";
//        String id = "1eb2966b1fd69b0ad6e15eba815a469";//test
//        String secret = "aCyg7RfV2hFSnJ5k1LKe";
        String id = "1eab549288ab3409514b74da6ec48fc";//local
        String secret = "fkm8EoISNYcXGvuUVOe8";
        String ts = System.currentTimeMillis() + "";
        String sign = Base64.getEncoder().encodeToString(HmacUtils.getInitializedMac(algorithm, secret.getBytes()).doFinal((ts).getBytes()));
        // build request
//        String body = "{\"device_name\":\"httpDevice221\",\"tm_event_id\":\"httpuptel\",\"ts\":1601018100344,\"state\":500,\"error_code\":true,\"msg\":1231.3,\"msg2\":1233}";
        String body = "{\"device_name\":\"http11Device2221\",\"tm_event_id\":\"httpuptel2\",\"ts\":"+System.currentTimeMillis()+",\"state\":200,\"error_code\":true,\"msg\":1221.1,\"msg2\":12133}";
        Request request = new Request.Builder()
                .addHeader("device-version", "1.0")
                .addHeader("request-ts", ts)
                .addHeader("authorization", "hmac id="+id+",algorithm="+algorithm+",headers=request-ts,signature="+sign)
                .addHeader("Connection", "Upgrade, HTTP2-Settings")
                .addHeader("Upgrade", "h2c")
                .method("POST", RequestBody.create(MediaType.parse("application/json"), body))
                .url(url)
                .build();
        execute(request);
    }

    static void doPostTelemetry() throws Exception {

        String url = "https://10.22.30.36:8443/api/v1/product/telemetry";
        String algorithm = "hmacsha1";
        String id = "1ea73f0bb40e8c09ca89798392184f7";
        String secret = "dmVonNMXf1f44XbRpaor";
        String ts = System.currentTimeMillis() + "";
        String sign = Base64.getEncoder().encodeToString(HmacUtils.getInitializedMac(algorithm, secret.getBytes()).doFinal((ts).getBytes()));        // build request
        Request request = new Request.Builder()
                .addHeader("device-version", "1.0")
                .addHeader("request-ts", ts)
                .addHeader("authorization", "hmac id="+id+",algorithm="+algorithm+",headers=request-ts,signature="+sign)
                .addHeader("Connection", "Upgrade, HTTP2-Settings")
                .addHeader("Upgrade", "h2c")
                .method("POST", RequestBody.create(MediaType.parse("application/json"),
                        "{\"device_name\":\"httpDevice2\",\"tm_event_id\":\"httpuptel2\",\"ts\":1601018100344,\"error_code\":true,\"msg\":1231.3,\"msg2\":1233}"))
                .url(url)
                .build();
        execute(request);
    }

    static void doPostAttributes() throws Exception {

        String url = hostName + "/api/v1/product/attributes";
        String algorithm = "hmacsha1";
        String id = "1ea73f0bb40e8c09ca89798392184f7";
        String secret = "dmVonNMXf1f44XbRpaor";
        String ts = System.currentTimeMillis() + "";
        String sign = Base64.getEncoder().encodeToString(HmacUtils.getInitializedMac(algorithm, secret.getBytes()).doFinal((ts).getBytes()));
        // build request
        Request request = new Request.Builder()
                .addHeader("device-version", "1.0")
                .addHeader("request-ts", ts)
                .addHeader("authorization", "hmac id="+id+",algorithm="+algorithm+",headers=request-ts,signature="+sign)
                .addHeader("Connection", "Upgrade, HTTP2-Settings")
                .addHeader("Upgrade", "h2c")
                .method("POST", RequestBody.create(MediaType.parse("application/json"),
                        "{\"device_name\":\"httpDevice2\",\"att1\":\"httpupte22l\",\"ts\":1601018100344,\"att2\":true,\"att3\":1231.3,\"att4\":1233}"))
                .url(url)
                .build();
        execute(request);
    }

    static void doGetAttributes() throws Exception {

        String url = "https://10.22.30.36:8443/api/v1/product/attributes?deviceName=httpDevice2";
        String algorithm = "hmacsha1";
        String id = "1ea73f0bb40e8c09ca89798392184f7";
        String secret = "dmVonNMXf1f44XbRpaor";
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

    static void execute(Request request)throws Exception{
        // send request
        Response response = client.newCall(request).execute();
        // print message
        String rslt = response.body().string();
        String protocol = response.protocol().name();
        System.out.println(rslt); // 打印响应：ECHO
        System.out.println(protocol); // 打印http protocol
    }

    static void initClient(String caPath) throws Exception {
        CertificateFactory cAf = CertificateFactory.getInstance("X.509");
        FileInputStream caIn = new FileInputStream(new File(Resources.getResource(caPath).toURI()));
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
