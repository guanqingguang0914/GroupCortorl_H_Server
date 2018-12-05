package com.partnerx.roboth_server;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by hel on 2017/8/18.
 */

public class UDPsend {

    private static UDPsend instance;
    private  DatagramSocket socket = null;
    private  InetAddress  sendAddress = null;
    private static int serverPort = 7777;
    private static DatagramPacket sendpacket;

    public UDPsend(){
        try {
            socket = new DatagramSocket(); // //首先创建一个DatagramSocket对象
            socket.setBroadcast(true);
            sendAddress = InetAddress.getByName("255.255.255.255");
        } catch (Exception e) {
            Log.i("udpBroadcast", "udpBroadcast 开设端口号出错0" + e);
        }
    }

    public static UDPsend getInstance() {
        if (instance == null) {
            synchronized (UDPsend.class) {
                if (instance == null) {
                    instance = new UDPsend();
                }
            }
        }
        return instance;
    }
    public void send(int num){
        switch(DataBuffer.type){
            case DataBuffer.H0:
            case DataBuffer.H1:
                Log.e("helei","11111111111111");
                sendtoH0(num);
                break;
            case DataBuffer.H3:
                sendtoH3(num);
                break;
        }
    }
    private int sendIndex = 1;
    private void sendtoH0(int num){
        sendIndex++;
        serverPort = 7777;
        byte cmd1 = (byte)0xC0;
        byte cmd2 = (byte)0x03;//都是执行。
        byte[] senddata =  null;//文件名是固定的。
        String name = "";
        switch(num){
            case 80:
                name = "forward(H1)";
                senddata = shujuwei(name);
                break;
            case 81:
                name = "LEFT_H1";
                senddata = shujuwei(name);
                break;
            case 82:
                name = "backward(H1)";
                senddata = shujuwei(name);
                break;
            case 83:
                name = "RIGHT_H1";
                senddata = shujuwei(name);
                break;
            case 84://特殊处理。
                //0x06;
                cmd2 = (byte)0x06;
                senddata = shujuwei(null);
                break;

        }
       final  byte[]data =  sendProtocol((byte)0x00,cmd1,cmd2,senddata);
        Log.e("helei","发送： "+bytesToString(data,data.length)+"  sendAddress: "+sendAddress+ " serverPort"+serverPort);
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendpacket = new DatagramPacket(data, data.length, sendAddress, serverPort);
                try {
                    socket.send(sendpacket); // 调用socket对象的send方法，发送数据

                } catch (Exception e) {
                    Log.i("udpBroadcast", "udpBroadcast 发送广播出错000" + e);
                }
            }
        });


    }
    private void sendtoH3(int num){
        serverPort = 7777;
        byte [] buffsendfor =  new byte[20];
        buffsendfor[0] = (byte)0xAA;
        buffsendfor[1] = (byte)0x55;
        buffsendfor[2] = (byte)num;
        buffsendfor[19] = (byte)0xff;
        sendpacket = new DatagramPacket(buffsendfor, buffsendfor.length, sendAddress, serverPort);
        try {
            socket.send(sendpacket); // 调用socket对象的send方法，发送数据
        } catch (Exception e) {
            Log.i("udpBroadcast", "udpBroadcast 发送广播出错000" + e);
        }
    }
    private byte[] shujuwei(String name){

        byte[] senddata =  null;
        if(name != null){
            byte[] namebuff = name.getBytes();
            int len = namebuff.length;
            senddata = new byte[6+1+len];
            senddata[0] = (byte)sendIndex;
            senddata[1] = 5;
            senddata[2] = 5;//1,2 相等立即执行。
            senddata[6] = (byte)len;
            System.arraycopy(namebuff,0,senddata,7,namebuff.length);
        }else{
            senddata = new byte[6];
            senddata[0] = (byte)sendIndex;
            senddata[1] = 5;
            senddata[2] = 5;//1,2 相等立即执行。
        }
        return senddata;
    }
    public byte[] sendProtocol(byte type, byte cmd1, byte cmd2, byte[] data) {
        byte[] sendbuff;
        if (data == null) {
            byte[] buf = new byte[8];
            buf[0] = type;
            buf[1] = cmd1;
            buf[2] = cmd2;
            sendbuff = addProtocol(buf);
        } else {
            byte[] buf = new byte[8 + data.length];
            buf[0] = type;
            buf[1] = cmd1;
            buf[2] = cmd2;
            System.arraycopy(data, 0, buf, 7, data.length);
            sendbuff = addProtocol(buf);
        }
        return sendbuff;
    }

    // 协议封装： AA 55 len1 len2 type cmd1 cmd2 00 00 00 00 (data) check
    public byte[] addProtocol(byte[] buff) {
        short len = (short) (buff.length);
        byte[] sendbuff = new byte[len + 4];
        sendbuff[0] = (byte) 0xAA; // 头
        sendbuff[1] = (byte) 0x55;
        sendbuff[3] = (byte) (len & 0x00FF); // 长度: 从type到check
        sendbuff[2] = (byte) ((len >> 8) & 0x00FF);
        System.arraycopy(buff, 0, sendbuff, 4, buff.length); // type - data

        byte check = 0x00; // 校验位
        for (int n = 0; n <= len + 2; n++) {
            check += sendbuff[n];
        }
        sendbuff[len + 3] = (byte) (check & 0x00FF);
        return sendbuff;
    }
    public String bytesToString(byte[] buf, int len) {
        String str = null, str1;
        for (int n = 0; n < len; n++) {
            str1 = String.format("%02x ", buf[n]);
            if (n == 0)
                str = str1;
            else
                str += str1;
        }
        return str;
    }



}
