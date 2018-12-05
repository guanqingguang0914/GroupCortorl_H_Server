package com.partnerx.roboth_server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Handler;
import android.util.Log;

public class DataProcess {
    private static DataProcess dataProcess = null;

    private Handler handler;
    // public static ChannelHandlerContext ctx01;
    // public static Channel currentChannel;
    // public static boolean isW;
    // public static boolean isC;
    // public static ByteBuf encoded;
    public static Lock lockCTX = new ReentrantLock(); // 锁对象
    public static List<ChannelHandlerContext> clientCTXList = new ArrayList<ChannelHandlerContext>();//
    public static int packetLen = 1024;
    public boolean isSuccess = false;

    public static DataProcess GetManger() {
        // 单例
        if (dataProcess == null) {
            dataProcess = new DataProcess();
        }
        return dataProcess;
    }

    public void getHandler(Handler handler) {
        this.handler = handler;
    }

    public void DataType(byte[] recv, final ChannelHandlerContext ctx) {
        LogMgr.i("DataType");
        int len = recv.length;
        if (len >= 12 && (recv[0] & 0xff) == 0xff && (recv[len - 1] & 0xff) == 0xAA && (recv[8] & 0xff) == 3) {// 校验头、尾
            if (len == (bytesToInt2(recv, 1) + 3) && recv[10] == XORcheckSend(recv) && (recv[5] & 0xff) == 102) {
                new Thread(new Runnable() {// 发送文件
                    @Override
                    public void run() {
                        try {
                            sendFile(DataBuffer.filePath, ctx);
                        } catch (Exception ex) {
                            Log.i("DataProcess", "sendMsg 50001出错了" + ex + "");
                        }
                    }
                }).start();
            }
        }
    }

	/*
     * arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
	 * 实现数组之间的复制, src:源数组； srcPos:源数组要复制的起始位置； dest:目的数组； destPos:目的数组放置的起始位置；
	 * length:复制的长度。 注意：src and dest都必须是同类型或者可以进行转换类型的数组． 有趣的是这个函数可以实现自己到自己复制
	 */

    public synchronized void sendMsgCtx(byte[] send, ChannelHandlerContext ctx) {// 发送给mobile
        LogMgr.i("sendMsgCtx");
        Channel currentChannel = ctx.channel();
        boolean isW = currentChannel.isWritable();
        boolean isC = currentChannel.isActive();
        if ((isW && isC)) {// 通过Netty传递，都需要基于流，以ChannelBuffer的形式传递。所以，Object
            // ->ChannelBuffer
            // Netty框架中，所有消息的传输都依赖于ByteBuf接口，ByteBuf是Netty NIO框架中的缓冲区
            ByteBuf encoded = currentChannel.alloc().buffer(send.length);// 生成send.length个字节的buf对象
            encoded.writeBytes(send);// 将数据写入向缓冲区
            currentChannel.write(encoded);// 消息传递都是基于流，通过ChannelBuffer传递的
            currentChannel.flush();// 发送数据
            currentChannel.writeAndFlush(encoded).addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if (future.isSuccess()) {// 发送成功
                        isSuccess = true;
                    } else {// //发送失败
                        isSuccess = false;
                    }
                }
            });
            long timepp = System.currentTimeMillis();
            while (!isSuccess && (System.currentTimeMillis() - timepp < 50)) {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public synchronized void sendMsg(byte[] send, ChannelHandlerContext ctx) { // I2C的数据不解析直接转发给Mobile
        if (ctx != null && null != ctx.channel()) {
            try {
                sendMsgCtx(send, ctx);
            } catch (Exception ex) {
                Log.i("DataProcess", "sendMsg 50001出错了" + ex + "");
            }
        }
    }

    public void sendFile(String filePath, ChannelHandlerContext ctx) {
        LogMgr.i("sendFile");
        InputStream fileOutStream = null;
        long fileLen = 0;
        File file = new File(filePath);
        byte packetByte[] = new byte[packetLen];
        int packetCount = 0;
        int lastPacketLen = 0;
        try {
            fileOutStream = new FileInputStream(file);
            fileLen = DataBuffer.sendbinLen;
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("SetActivity download", "SetActivity download bin文件没找到");
        }
        packetCount = (int) (fileLen / (long) packetLen);// 分帧满帧数
        lastPacketLen = (int) (fileLen % (long) packetLen);// 最后一帧长度
        if (packetCount > 0) {
            for (int i = 0; i < packetCount; i++) {
                try {
                    fileOutStream.read(packetByte, 0, packetLen);
                    Thread.sleep(10);
                    sendMsg(packetByte, ctx);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        if (lastPacketLen > 0) {
            try {
                byte lastpacketByte[] = new byte[lastPacketLen];
                fileOutStream.read(lastpacketByte, 0, lastPacketLen);
                sendMsg(lastpacketByte, ctx);
                LogMgr.i("sendMsg  lastpacketByte  result");
                // 发送出去
                fileOutStream.close();// 发送完后关掉
            } catch (Exception e) {
                Log.i("SetActivity download", "SetActivity download bin文件没找到");
            }
        }
    }

    public static byte XORcheckSend(byte[] buf) {// 传参是完整报文,生成CRC
        int len = buf.length;
        if (len < 12)
            return -1;

        byte crc = buf[0];
        for (int i = 1; i <= len - 3; i++) {
            crc = (byte) (crc ^ (buf[i]));
        }
        return crc;
    }

    public static int bytesToInt2(byte[] bytes, int begin) {// 两个字节转化为int
        // 高位在前低位在后
        return (int) (0x00ff & bytes[begin + 1]) | ((0x00ff & bytes[begin]) << 8);
    }
}
