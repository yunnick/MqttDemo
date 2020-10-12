package com.demo.http2;

import com.demo.mqtt.client.MqttTwoWaySslClient;
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

public class Http2ClientDemoProductTwoWay {

    private static OkHttpClient client;
//    static String hostName = "https://test-iot-as-http2.stc-seedland.com.cn:8443";
//    static String hostName = "https://iot-as-http2.stc-seedland.com.cn:8443";
    static String hostName = "https://testhttp2.stc-seedland.com.cn:8443";
    public static void main(String[] args) {

        try {
            initClient();
            doGetDemo();
//            doPostHeartbeat();
//            doPostTelemetry();
//            doPostAttributes();
//            doGetAttributes();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static void initClient() throws Exception {
        // 初始化sslContext
        SSLContext sslContext = MqttTwoWaySslClient.getSllContext();

        // 获得sslSocketFactory
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        // 初始化client builder
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(sslSocketFactory)
                .hostnameVerifier(new HostnameVerifier() { // 放过host验证
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });

        // build client
        client = builder.build();
    }

    static void doGetDemo() throws Exception {

//        String url = "https://test-iot-as-http2.stc-seedland.com.cn/api/v1/product/heartbeat";
        String url = hostName+"/api/v1/product/demo";
//        String url = "https://10.22.30.36:8443/api/v1/product/heartbeat";
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


    static void doPostHeartbeat() throws Exception {

        String url = hostName + "/api/v1/product/heartbeat";
//        String url = "https://test-iot-as-http2.stc-seedland.com.cn:8443/api/v1/product/demo";
//        String url = "https://10.22.30.36:8443/api/v1/product/heartbeat";
        String algorithm = "hmacsha1";
        String id ="1eb02d1e7c54e208ac2dda33080d9f3";//prod
        String secret = "hCDtmNAtBucnSq8Ahkmj";
//        String id = "1eb0186d8d671b0ac55b387b07b3667";//test
//        String secret = "bfWTd8cpqhKPiiiEv1sS";
//        String id = "1ea73f0bb40e8c09ca89798392184f7";//local
//        String secret = "dmVonNMXf1f44XbRpaor";
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
                        "{\"device_name\":\"httpDevice221\",\"tm_event_id\":\"httpuptel\",\"ts\":1601018100344,\"state\":500,\"error_code\":true,\"msg\":1231.3,\"msg2\":1233}"))
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

        String url = "https://10.22.30.36:8443/api/v1/product/attributes";
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



}
