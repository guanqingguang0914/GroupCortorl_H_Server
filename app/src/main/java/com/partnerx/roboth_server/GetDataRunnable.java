package com.partnerx.roboth_server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.util.Log;

class GetDataRunnable implements Runnable {
    private static final int serverPort = 7777;
    private static final int serverPort2 = 51000;
    public static DatagramSocket socketOne = null;
    private static DatagramPacket getpacket3;
    private static DatagramPacket sendpacketOne3;
    private byte[] getdata3 = new byte[1024]; // 接收反馈数据
    private int comd = 101;

    @Override
    public void run() {
        int datalen0 = 0;
        int packetlen0 = 0;
        byte[] data32 = getDateByte(32); // 发送数据32
        try {
            while (true) {
                Log.i("", "00000getDataRunnable00 AddressList.size() = " + DataBuffer.AddressList.size());
                if (socketOne == null)
                    socketOne = new DatagramSocket(serverPort2);
                getpacket3 = new DatagramPacket(getdata3, getdata3.length);//
                socketOne.receive(getpacket3);
                LogMgr.i("getDataRunnable", "00000getDataRunnable22 AddressList.size() = " + DataBuffer.AddressList.size());
                datalen0 = bytesToInt2(getdata3, 1) + 3;
                packetlen0 = getpacket3.getLength();
                if (packetlen0 < 12 || (packetlen0 != datalen0))
                    continue;
                if ((getdata3[0] & 0xff) == 0xff && (getdata3[datalen0 - 1] & 0xff) == 0xAA
                        && (getdata3[5] & 0xff) == comd && XORcheckSend(getdata3, datalen0) == getdata3[datalen0 - 2] && (getdata3[8] & 0xff) == 3) {// 校验头、尾
                    LogMgr.i("数据位：" + bytesToInt2(getdata3, 10));
                    InetAddress Address = getpacket3.getAddress();
                    sendpacketOne3 = new DatagramPacket(data32, data32.length, Address, serverPort);
                    socketOne.send(sendpacketOne3); // 调用socket对象的send方法，发送数据
                    DataBuffer.lockAddress.lock();
                    if (!DataBuffer.AddressList.contains(Address))
                        DataBuffer.AddressList.add(Address);
                    DataBuffer.lockAddress.unlock();

                    LogMgr.i("getDataRunnable",
                            "00000getDataRunnable22 AddressList.size() = " + DataBuffer.AddressList.size());
                }
            }
        } catch (Exception e) {
            Log.i("getDataRunnable", "00000getDataRunnable 出错了 " + e);
        }
    }

    public byte[] getDateByte(int cmd) {// 指令打包
        byte[] data0 = new byte[12]; // 发送数据
        data0[0] = (byte) 0xff;// 报头
        data0[1] = (byte) (0xff & ((12 - 3) >> 8));// 长度高位
        data0[2] = (byte) (0xff & (12 - 3));// 长度低位
        data0[3] = (byte) (DataBuffer.sendIndex++);//
        data0[4] = (byte) (0);//
        data0[5] = (byte) cmd;// 指令种类
        data0[8] = (byte) 3;// 机器人类型
        data0[11] = (byte) 0xAA;// 报尾
        data0[10] = XORcheckSend(data0, 12);
        return data0;
    }

    public static byte XORcheckSend(byte[] buf, int len) {// 传参是完整报文,生成CRC
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
