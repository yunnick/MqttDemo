package com.demo.mqtt.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ResourceLeakDetector;

/**
 */
public  class IOTMqttServer {

    private static final int PORT = 8883;
    private static final String LEAK_DETECTOR_LEVEL = "DISABLED";
    private static final Integer BOSS_GROUP_THREAD_COUNT = 1;
    private static final Integer WORKER_GROUP_THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 2 + 1;
    private static final Integer MAX_PAYLOAD_SIZE = 65536;

    public static void main(String[] args) throws Exception {
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.valueOf(LEAK_DETECTOR_LEVEL.toUpperCase()));

        EventLoopGroup bossGroup = new NioEventLoopGroup(BOSS_GROUP_THREAD_COUNT);
        EventLoopGroup workerGroup = new NioEventLoopGroup(WORKER_GROUP_THREAD_COUNT);

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new MqttTransportServerInitializer(MAX_PAYLOAD_SIZE));
            ChannelFuture f = b.bind("0.0.0.0", PORT);
            System.out.println("bind success!!");
            f.channel().closeFuture().sync();
        } catch (Throwable e){
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
