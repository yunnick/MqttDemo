package com.demo.mqtt.client;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ScheduleExecutorServiceTest {
    public static void main(String[] args) {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(100);

        for (int i = 0; i < 200; i++){
            int count = i;
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    System.out.println(count);
                }
            });
        }
    }


}
