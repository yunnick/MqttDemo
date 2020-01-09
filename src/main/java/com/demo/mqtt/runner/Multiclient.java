package com.demo.mqtt.runner;

import com.google.common.io.Resources;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Multiclient {

    private static final String MQTT_URL = "ssl://test-iot-as-mqtt.stc-seedland.com.cn:8883";

    private static final String CLIENT_ID = "MQTT_SSL_JAVA_CLIENT";
    private static final String KEY_STORE_FILE = "mqttserver.pub.pem";

    private static final String JKS = "JKS";
    private static final String TLS = "TLSV1.2";

    static MqttClient initClient() throws Exception {
        MqttClient mqttClient = new MqttClient(MQTT_URL, CLIENT_ID, new MemoryPersistence());
        return mqttClient;
    }

    static MqttConnectOptions initConnOpiton() throws Exception {
        CertificateFactory cAf = CertificateFactory.getInstance("X.509");
        FileInputStream caIn = new FileInputStream(new File(Resources.getResource(KEY_STORE_FILE).toURI()));
        X509Certificate ca = (X509Certificate) cAf.generateCertificate(caIn);
        KeyStore caKs = KeyStore.getInstance(JKS);
        caKs.load(null, null);
        caKs.setCertificateEntry("ca-certificate", ca);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(caKs);

        // finally, create SSL socket factory
        SSLContext sslContext = SSLContext.getInstance(TLS);
        sslContext.init(null, tmf.getTrustManagers(), null);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setSocketFactory(sslContext.getSocketFactory());
        options.setUserName("aAbJ3Ey0gpglBng3Fb0O");

        return options;
    }

    static ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("mqtt-cliet-pool-%d").build();
    static ExecutorService es = new ThreadPoolExecutor(9, 9,
            10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1000), threadFactory);

    static AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {

        int clientNum = 150;
        int cycleNum = 30;
        for (int i = 0; i < cycleNum; i++) {
            connect(clientNum);
        }
        System.out.println("all done");
        System.exit(0);
    }

    static void connect(int clientNum) throws Exception {
        CompletableFuture<Boolean>[] futures = new CompletableFuture[clientNum];
        for (int i = 0; i < clientNum; i++) {

            futures[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    MqttClient client = initClient();
                    client.connect(initConnOpiton());
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }, es);
            int idx = i;
            futures[i].thenAccept(s -> {
                if (s){
                    counter.getAndIncrement();
                    System.out.println("client " + idx + " connected, total " + counter.get());
                }else{
                    System.out.println("client " + idx + " connected failed, total " + counter.get());
                }
            });
        }

        CompletableFuture.allOf(futures).get();
    }
}
