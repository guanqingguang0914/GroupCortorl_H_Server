package com.partnerx.roboth_server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import android.util.Log;
import android.util.LogPrinter;

import java.util.logging.Logger;

public class ServerHandler extends ChannelHandlerAdapter {
    // private static ByteBuf data;
    // private static byte[] receive;
    private int loss_connect_time = 0;
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            if(event.state() == IdleState.READER_IDLE){
                loss_connect_time ++;
                if(loss_connect_time > 2){
                    ctx.channel().close();
                }
            }

        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {// 架包里应该有个循环在重复调用这个方法
        LogMgr.i("channelRead + DataProcess.clientCTXList = " + DataProcess.clientCTXList.size());
        DataProcess.lockCTX.lock();
        if (!DataProcess.clientCTXList.contains(ctx)){
            DataProcess.clientCTXList.add(ctx);
        }
        DataProcess.lockCTX.unlock();
        ByteBuf data = (ByteBuf) msg;
        byte[] receive = new byte[data.readableBytes()];
        data.readBytes(receive);
        LogMgr.i("receive = " + FileUtils.bytesToString(receive,receive.length));
        // getHexstr(receive);//将接收的数据转换为16进制String
        DataProcess.GetManger().DataType(receive, ctx);// 处理数据，并将同类数据分到各自缓冲区
        data.clear();// 清空缓冲区
    }

    private String getHexstr(byte[] data) {// 将接收的数据转换为16进制String
        int v;
        String hv = "";
        for (int i = 0; i < data.length; i++) {
            v = data[i] & 0xFF;
            if (v <= 0x0f)
                hv = hv + " 0" + Integer.toHexString(v);
            else
                hv = hv + " " + Integer.toHexString(v);
        }
        return hv;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        DataProcess.lockCTX.lock();
        if (DataProcess.clientCTXList.contains(ctx))
            DataProcess.clientCTXList.remove(ctx);
        DataProcess.lockCTX.unlock();
        LogMgr.i("exceptionCaught", "ServerHandler 连接异常");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LogMgr.i("channelActive  ");
        DataProcess.lockCTX.lock();
        if (!DataProcess.clientCTXList.contains(ctx)){
            LogMgr.i("!DataProcess.clientCTXList.contains(ctx)  ");
            DataProcess.clientCTXList.add(ctx);
        }
        DataProcess.lockCTX.unlock();
        LogMgr.i("channelActive", "ServerHandler 连接上了");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        DataProcess.lockCTX.lock();
        if (DataProcess.clientCTXList.contains(ctx))
            DataProcess.clientCTXList.remove(ctx);
        DataProcess.lockCTX.unlock();
        LogMgr.i("channelInactive", "ServerHandler 中断连接了");
    }
}
