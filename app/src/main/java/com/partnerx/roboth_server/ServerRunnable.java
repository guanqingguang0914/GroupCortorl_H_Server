package com.partnerx.roboth_server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import android.util.Log;

public class ServerRunnable implements Runnable {
    private String IP = null;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private ServerBootstrap serverBootstrap;
    public ChannelFuture channelFuture01;// 开端口号
    public static boolean isRunning = false;
    // public ChannelFuture channelFuture02;

    public ServerRunnable(String ip) {
        IP = ip;
    }

    @Override
    public void run() {
//        while (true) {// 不允许线程结束，用于断线重连
//            try {
//                if(isRunning == false){
//                    synchronized (ServerRunnable.class){
//                        if(isRunning == true){
//                            LogMgr.e("文件传输tcp服务端已建立，不重复建立");
//                            break;
//                        }
//                        runServer();
//                    }
//                }else {
//                    LogMgr.e("文件传输tcp服务端已建立，不重复建立");
//                    break;
//                }
//            } catch (Exception e) {
//                Log.i("ServerRunnable  ", "  Server建立服务器 出错了  " + e);
//            } finally {
//                try {
//                    Thread.sleep(2000);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
        while (true) {// 不允许线程结束，用于断线重连
            try {
                runServer();
            } catch (Exception e) {
                Log.i("ServerRunnable  ", "  Server建立服务器 出错了  " + e);
            } finally {
                try {
                    Thread.sleep(300);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void runServer() {
        LogMgr.i("runServer");
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            serverBootstrap = new ServerBootstrap();// 引导辅助程序
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1000)// 标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50。
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            LogMgr.i("initChannel   p.addLast(new ServerHandler());");
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new ServerHandler());
                        }
                    });
            LogMgr.i("group");
            channelFuture01 = serverBootstrap.bind(IP, 50001).sync();// 开端口号,可以指定服务器地址也可以默认不指定
            // channelFuture02 = serverBootstrap.bind(40002).sync();
//            channelFuture01 = serverBootstrap.bind(50001).sync();
            if(channelFuture01.isSuccess()){
                isRunning = true;
            }
            channelFuture01.channel().closeFuture().sync();
            // channelFuture02.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            // e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
