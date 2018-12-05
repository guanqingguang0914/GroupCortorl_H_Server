package com.partnerx.roboth_server;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.partnerx.sqlite.SQLBeanRobot;

public class UdpBroadcast implements Runnable {
    private byte[] data = new byte[12]; // 发送数据
    private byte[] getdata = new byte[16]; // 接收反馈数据
    private static DatagramSocket socket = null;
    public static DatagramSocket socketOne = null;
    private static DatagramPacket sendpacket;
    private static DatagramPacket getpacket;
    private static DatagramPacket sendpacketOne;
    public static InetAddress sendAddress;
    private int AddressListSize = 0;
    private static int serverPort = 7777;
    private static final int serverPort2 = 51000;
    private static Handler handler;
    private static byte[] binName = null;
    private boolean ismusic = false;
    public static List<InetAddress> sendAddressList = new ArrayList<InetAddress>();//

    public UdpBroadcast(Handler mhandler) {
        if (mhandler != null)
            handler = mhandler;
    }

    @Override
    public void run() {
        int len = 12;
        int binLen = 0;
        switch (DataBuffer.comID) {
            case 3:// 动作执行
                if (DataBuffer.binName != null) {
                    binName = (DataBuffer.binName).getBytes();
                    binLen = binName.length;
                }
                len = 12 + binLen;
                data = new byte[len];
                System.arraycopy(binName, 0, data, 10, binLen);
                // Log.e("helei","changdu: "+len);
                break;
            case 20:// 发送bin文件信息
                if (DataBuffer.binName != null) {
                    binName = (DataBuffer.binName).getBytes();
                    binLen = binName.length;
                }
                len = 12 + 16 + binLen;
                data = new byte[len];
                System.arraycopy(binName, 0, data, 26, binLen);

                // data[10]=
                break;
            case 40:// 分组动作执行
                // String messageData = getData();
                byte[] bs = getMessageData();
//                LogMgr.e("bs = " + FileUtils.bytesToString(bs,bs.length));
                if (bs != null && bs.length > 0) {
                    binName = bs;
                    binLen = bs.length;
                }else{
                    return;
                }
                len = 12 + binLen;
                data = new byte[len];
                System.arraycopy(binName, 0, data, 10, binLen);
                break;
            default:// 其他
                len = 12;
                data = new byte[len];
                break;
        }
        ismusic = false;
        try {
            socket = new DatagramSocket(); // //首先创建一个DatagramSocket对象
            socket.setBroadcast(true);
            sendAddress = InetAddress.getByName("255.255.255.255");
        } catch (Exception e) {
            Log.i("udpBroadcast", "udpBroadcast 开设端口号出错0" + e);
        }
        data[0] = (byte) 0xff;// 报头
        data[1] = (byte) (0xff & ((len - 3) >> 8));// 长度高位
        data[2] = (byte) (0xff & (len - 3));// 长度低位
        data[3] = (byte) (DataBuffer.sendIndex++);// 发送广播次数
        data[5] = (byte) DataBuffer.comID;// 指令种类
        data[6] = (byte) DataBuffer.countDownTime;// 指令执行延时时间
        data[8] = (byte) 3;// 机器人类型
        data[len - 1] = (byte) 0xAA;// 报尾

        switch (DataBuffer.comID) {
            //15关闭了40，因为小品编辑，16打开
            case 40:
                for (int i = 0; i < DataBuffer.countDownTime; i++) {
                    data[4] = (byte) (i);// 一次按钮发送广播次数
                    data[len - 2] = XORcheckSend(data);//我只需要在这里做一个协议转换即可。
                    LogMgr.e("helei: ", "data: " + bytesToString(data, data.length) + "  serverPort: " + serverPort + sendAddress.toString());
                    sendpacket = new DatagramPacket(data, data.length, sendAddress, serverPort); // 创建一个DatagramPacket对象，并指定要将这个数据包发送到网络当中的哪个、地址，以及端口号
                    try {
                        socket.send(sendpacket); // 调用socket对象的send方法，发送数据
                        // Log.i("udpBroadcast", "udpBroadcast 广播间隔时间");
                        Thread.sleep(50);
                    } catch (Exception e) {
                        Log.i("udpBroadcast", "udpBroadcast 发送广播出错555" + e);
                    }
                    if (i % 20 == 0) {
                        Message msg2 = new Message();
                        msg2.what = 20001;
                        msg2.obj = ((DataBuffer.countDownTime - i) * 50) / 1000;
                        handler.sendMessage(msg2);
                    }
                }
                break;
            case 31:// 发送bin文件
                DataBuffer.ifdownload = true;
                DataBuffer.isBreak = true;
                byte[] databinfile = getDateByte(DataBuffer.sendbinName);
                if (databinfile == null) {// 文件为空不发送
                    Message msg5 = new Message();
                    msg5.what = 20002;
                    msg5.obj = 0;
                    handler.sendMessage(msg5);
                    DataBuffer.ifdownload = false;
                    return;
                }
                data[4] = (byte) 0;// 一次按钮发送广播次数
                data[6] = (byte) DataBuffer.countDownTime;// 指令执行延时时间
                data[len - 2] = XORcheckSend(data);
                if (!DataBuffer.isstart101) {
                    DataBuffer.isstart101 = true;
                    new Thread(new GetDataRunnable()).start();// 监听回复101
                }
                for (int i = 0; i < DataBuffer.countDownTime; i++) { // 发送（指令31
                    sendpacket = new DatagramPacket(data, data.length, sendAddress, serverPort); // 创建一个DatagramPacket对象，并指定要将这个数据包发送到网络当中的哪个、地址，以及端口号
                    try {
                        socket.send(sendpacket); // 调用socket对象的send方法，发送数据
                        Thread.sleep(50);
                    } catch (Exception e) {
                        Log.i("udpBroadcast", "udpBroadcast 发送广播出错000" + e);
                    }
                }
                if (socket != null)
                    socket.close();
                try {
//                    Thread.sleep(1000);
                    Thread.sleep(2000);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                // 搜集发送文件信息，
                InetAddress sendOneAddress = null;
                LogMgr.i("udpBroadcast", "000000 udpBroadcast AddressList.size() = " + DataBuffer.AddressList.size());
                long sleeptime65 = System.currentTimeMillis();
                sendAddressList = new ArrayList<InetAddress>();// 初始化
                while (true) {// 分批发送文件信息及服务器地址
                    try {
                        if ((System.currentTimeMillis() - sleeptime65 <= 2000) && !(DataBuffer.AddressList.size() > 0)) {
                            Thread.sleep(100);
                            continue;
                        } else if (!(DataBuffer.AddressList.size() > 0)) {
                            break;
                        }
                        DataBuffer.lockAddress.lock();
                        sendOneAddress = DataBuffer.AddressList.get(0);
                        LogMgr.i("sendOneAddress = " + sendOneAddress.toString());
                        DataBuffer.lockAddress.unlock();
                        if (!sendAddressList.contains(sendOneAddress)) {
                            LogMgr.i(" 1 ");
                            for (int i = 0; i < 20; i++) { // 发送文件帧信息（指令33)
                                if (socketOne == null)
                                    socketOne = new DatagramSocket();
                                sendpacketOne = new DatagramPacket(databinfile, databinfile.length, sendOneAddress,
                                        serverPort);
                                socketOne.send(sendpacketOne); // 调用socket对象的send方法，发送数据
                                Thread.sleep(5);
                            }
                            sendAddressList.add(sendOneAddress);
                        }
                        LogMgr.i("sendAddressList.size = " + sendAddressList.size());
//                        if(sendAddressList.size() >= DataBuffer.AddressList.size()){
//                            DataBuffer.isBreak = false;
//                        }
                        DataBuffer.lockAddress.lock();
                        DataBuffer.AddressList.remove(0);
                        DataBuffer.lockAddress.unlock();
                        LogMgr.i("DataProcess.clientCTXList" + DataProcess.clientCTXList.size());
                        while (DataProcess.clientCTXList.size() > 3){
                            Thread.sleep(10);// 控制tcp client最多连接3个
                            LogMgr.i("DataProcess.clientCTXList.size()3 = " + DataProcess.clientCTXList.size());
                        }
                        if ((DataProcess.clientCTXList.size() > 2)){
                            Thread.sleep(100);// 每个tcp连接间隔
                            LogMgr.i("DataProcess.clientCTXList.size()2 = " + DataProcess.clientCTXList.size());
                        }
                    } catch (Exception e) {
                        Log.i("udpBroadcast", "udpBroadcast 发送定点udp出错111" + e);
                    }
                }
                sendAddressList.clear();
                Message msg3 = new Message();
                msg3.what = 20002;
                msg3.obj = 0;
                handler.sendMessage(msg3);
                DataBuffer.ifdownload = false;
                break;
            case 20:// 发送bin文件
                new Thread() {// 接收没收到的机器人的反馈信息
                    @Override
                    public void run() {
                        int datalen0 = 0;
                        int packetlen0 = 0;
                        try {
                            getpacket = new DatagramPacket(getdata, getdata.length, serverPort); // 创建一个DatagramSocket对象，并指定监听的端口号
                            while (true) {
                                socket.receive(getpacket);
                                datalen0 = bytesToInt2(data, 1) + 3;
                                packetlen0 = getpacket.getLength();
                                if (packetlen0 < 16 || (packetlen0 != datalen0))
                                    continue;
                                if ((getdata[0] & 0xff) == 0xff && (getdata[datalen0 - 1] & 0xff) == 0xAA
                                        && (data[8] & 0xff) == 3 && XORcheckSend(getdata) == getdata[datalen0 - 2]
                                        && (getdata[5] & 0xff) == 101) {// 校验头、尾
                                    InetAddress Address = getpacket.getAddress();
                                    DataBuffer.lockAddress.lock();
                                    if (!DataBuffer.AddressList.contains(Address))
                                        DataBuffer.AddressList.add(Address);
                                    DataBuffer.lockAddress.unlock();
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                }.start();

                data[4] = (byte) 0;// 一次按钮发送广播次数
                data[len - 2] = XORcheckSend(data);
                for (int i = 0; i < 20; i++) { // 发送文件帧信息（指令20
                    sendpacket = new DatagramPacket(data, data.length, sendAddress, serverPort); // 创建一个DatagramPacket对象，并指定要将这个数据包发送到网络当中的哪个、地址，以及端口号
                    try {
                        socket.send(sendpacket); // 调用socket对象的send方法，发送数据
                        Thread.sleep(50);
                    } catch (Exception e) {
                        Log.i("udpBroadcast", "udpBroadcast 发送广播出错222" + e);
                    }
                }

                int filelen = 1040;
                byte[] datafile = new byte[filelen];
                datafile[1] = (byte) (0xff & ((filelen - 3) >> 8));// 长度高位
                datafile[2] = (byte) (0xff & (filelen - 3));// 长度低位
                datafile[3] = (byte) (DataBuffer.sendIndex++);// 发送广播次数
                datafile[4] = (byte) 0;//
                datafile[5] = (byte) 21;// 指令种类
                datafile[8] = (byte) 3;// 机器人类型
                datafile[filelen - 1] = (byte) 0xAA;// 报尾
                datafile[filelen - 2] = XORcheckSend(datafile);
                for (int i = 0; i < 20; i++) { // 发送文件帧（指令21
                    sendpacket = new DatagramPacket(datafile, datafile.length, sendAddress, serverPort); // 创建一个DatagramPacket对象，并指定要将这个数据包发送到网络当中的哪个、地址，以及端口号
                    try {
                        socket.send(sendpacket); // 调用socket对象的send方法，发送数据
                        Thread.sleep(50);
                    } catch (Exception e) {
                        Log.i("udpBroadcast", "udpBroadcast 发送广播出错333" + e);
                    }
                }
                while (DataBuffer.AddressList != null && AddressListSize > 0) {
                    try {
                        socketOne = new DatagramSocket(serverPort, DataBuffer.AddressList.get(0));
                        sendpacket = new DatagramPacket(datafile, datafile.length, serverPort);
                        socketOne.send(sendpacket); // 调用socket对象的send方法，发送数据
                        Thread.sleep(2);
                        DataBuffer.lockAddress.lock();
                        try {
                            DataBuffer.AddressList.remove(0);
                            AddressListSize = DataBuffer.AddressList.size();
                        } catch (Exception e) {

                        }
                        DataBuffer.lockAddress.unlock();
                    } catch (Exception e) {
                        Log.i("udpBroadcast", "udpBroadcast 发送广播出错4444" + e);
                    }
                }
                break;
            case 80://前进
            case 81://左转
            case 82://后退。
            case 83://右转。
            case 84://停止。
            case 76:
            case 77:
                byte[] buffsendfor = new byte[20];
                buffsendfor[0] = (byte) 0xAA;
                buffsendfor[1] = (byte) 0x55;
                buffsendfor[2] = DataBuffer.comID;
                buffsendfor[19] = (byte) 0xff;

                byte[] newsitch = getsendbuff(buffsendfor);
                Log.e("helei", "发送： " + bytesToString(newsitch, newsitch.length) + "  sendAddress: " + sendAddress + " serverPort" + serverPort);
                sendpacket = new DatagramPacket(newsitch, newsitch.length, sendAddress, serverPort);
                try {
                    socket.send(sendpacket); // 调用socket对象的send方法，发送数据
                    Thread.sleep(50);
                } catch (Exception e) {
                    Log.i("udpBroadcast", "udpBroadcast 发送广播出错000" + e);
                }
                break;

            default:// 其他
                for (int i = 0; i < DataBuffer.countDownTime; i++) {
                    data[4] = (byte) (i);// 一次按钮发送广播次数
                    data[len - 2] = XORcheckSend(data);//我只需要在这里做一个协议转换即可。
                    byte[] senddata = switchProtocol(data);
//                    Log.e("helei: ", "data: " + bytesToString(senddata, senddata.length) + "  serverPort: " + serverPort + sendAddress.toString());
                    sendpacket = new DatagramPacket(senddata, senddata.length, sendAddress, serverPort); // 创建一个DatagramPacket对象，并指定要将这个数据包发送到网络当中的哪个、地址，以及端口号
                    try {
                        socket.send(sendpacket); // 调用socket对象的send方法，发送数据
                        // Log.i("udpBroadcast", "udpBroadcast 广播间隔时间");
                        Thread.sleep(50);
                    } catch (Exception e) {
//                        Log.i("udpBroadcast", "udpBroadcast 发送广播出错555" + e);
                    }
                    if (i % 20 == 0) {
                        Message msg2 = new Message();
                        msg2.what = 20001;
                        msg2.obj = ((DataBuffer.countDownTime - i) * 50) / 1000;
                        handler.sendMessage(msg2);
                    }
                    if (DataBuffer.ifOpen && DataBuffer.sleepTime < 0 && !ismusic
                            && -DataBuffer.sleepTime > ((DataBuffer.countDownTime - (i + 1)) * 50) && DataBuffer.comID == 3) {
                        try {
//                            Log.e("helei", "------------music_______________");
                            DataBuffer.mp.start();
                            ismusic = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                Message msg1 = new Message();
                msg1.what = 20002;
                msg1.obj = 0;
                handler.sendMessage(msg1);
                if (socket != null)
                    socket.close();
                if (DataBuffer.ifOpen && DataBuffer.sleepTime >= 0 && DataBuffer.comID == 3) {
                    try {
                        if (DataBuffer.sleepTime > 0)
                            Thread.sleep(DataBuffer.sleepTime);
                        DataBuffer.mp.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (DataBuffer.bStartMedia && DataBuffer.sleepTime >= 0 && DataBuffer.comID == 3) {
                    DataBuffer.mediaPlayer.start();
                }
                break;
        }
    }

    /**
     * 把数据转化成Json数据
     *
     * @return
     */
    private String getData() {
        if (DataBuffer.managerList.size() <= 0) {
            return "";
        }
        try {
            JSONArray array = new JSONArray();
            for (SQLBeanRobot beanRobot : DataBuffer.managerList) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("group", beanRobot.getGid());
                jsonObject.put("file_name", beanRobot.getBinfile());
                jsonObject.put("delay_time", beanRobot.getTime());
                array.put(jsonObject);
            }
            LogMgr.d("json:" + array.toString());
            return array.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    private byte[] switchProtocol(byte[] data) {
//        if (DataBuffer.type != DataBuffer.H0) {
//            Log.e("helei:", "come ");
//            serverPort = 7777;
//            return data;
//        } else {
//            serverPort = 7777;
            byte cmd1 = (byte) 0xC0;
            byte cmd2 = (byte) data[5];//命令种类。
            //还有数据位。
            byte[] senddata = null;
            int len = bytesToInt2(data, 1);
//            Log.e("helei: ", "changdu: " + len + "原始数据： " + bytesToString(data, data.length));
            if (len > 10) {
                senddata = new byte[len - 9 + 6 + 1];
                senddata[0] = data[3];
                senddata[1] = data[4];
                senddata[2] = data[6];
                senddata[3] = data[7];
                senddata[4] = data[8];
                senddata[5] = data[9];
                //文件名的长度N。
                senddata[6] = (byte) (len - 9);
                System.arraycopy(data, 10, senddata, 7, len - 9);
            } else {
                senddata = new byte[6];
                senddata[0] = data[3];
                senddata[1] = data[4];
                senddata[2] = data[6];
                senddata[3] = data[7];
                senddata[4] = data[8];
                senddata[5] = data[9];
            }
            data = sendProtocol((byte) 0x00, cmd1, cmd2, senddata);
//        }
        return data;
    }

    private int sendIndex = 1;

    private byte[] getsendbuff(byte[] data) {

        if (DataBuffer.type != DataBuffer.H0) {
            Log.e("helei:", "come ");
            serverPort = 7777;
            return data;
        } else {
            sendIndex++;
            serverPort = 7777;
            byte cmd1 = (byte) 0xC0;
            byte cmd2 = (byte) 0x03;//都是执行。
            byte[] senddata = null;//文件名是固定的。
            String name = "";
            switch (data[2]) {
                case 80:
                    name = "forward(H1)";
                    senddata = shujuwei(name);
                    break;
                case 81:
                    name = "RIGHT_H";
                    senddata = shujuwei(name);
                    break;
                case 82:
                    name = "backward(H1)";
                    senddata = shujuwei(name);
                    break;
                case 83:
                    name = "LEFT_H";
                    senddata = shujuwei(name);
                    break;
                case 84://特殊处理。
                    //0x06;
                    cmd2 = (byte) 0x06;
                    senddata = shujuwei(null);
                    break;
                case 78://打开平衡
                    senddata = new byte[7];
                    senddata[0] = (byte) sendIndex;
                    senddata[1] = 5;
                    senddata[2] = 5;
                    break;
                case 79://关闭平衡
                    senddata = new byte[7];
                    senddata[0] = (byte) sendIndex;
                    senddata[1] = 5;
                    senddata[2] = 5;
                    senddata[7] = (byte) 0x01;
                    break;

            }
            data = sendProtocol((byte) 0x00, cmd1, cmd2, senddata);
        }
        return data;
    }

    private byte[] shujuwei(String name) {

        byte[] senddata = null;
        if (name != null) {
            byte[] namebuff = name.getBytes();
            int len = namebuff.length;
            senddata = new byte[6 + 1 + len];
            senddata[0] = (byte) sendIndex;
            senddata[1] = 5;
            senddata[2] = 5;//1,2 相等立即执行。
            senddata[6] = (byte) len;
            System.arraycopy(namebuff, 0, senddata, 7, namebuff.length);
        } else {
            senddata = new byte[6];
            senddata[0] = (byte) sendIndex;
            senddata[1] = 5;
            senddata[2] = 5;//1,2 相等立即执行。
        }
        return senddata;
    }

    private byte[] getMessageData() {
        if (DataBuffer.managerList.size() <= 0) {
            return null;
        }
        // 格式 分组号（一个字节）延迟时间（3个字节） 文件名长度（一个字节） 文件名（15个字节） 一共20个字节
        int size = DataBuffer.managerList.size() * 20;
        byte[] bs = new byte[size];

        for (int i = 0; i < DataBuffer.managerList.size(); i++) {
            SQLBeanRobot beanRobot = DataBuffer.managerList.get(i);
            int k = Integer.valueOf(beanRobot.getGid());
            bs[i * 20 + 0] = (byte) k;
            byte[] bs2 = intToByte3(Integer.valueOf(beanRobot.getTime()));
            System.arraycopy(bs2, 0, bs, i * 20 + 1, bs2.length);
            byte[] bs3 = beanRobot.getBinfile().getBytes();
            int j = bs3.length; // 文件名的长度
            if (j > 15) {
                j = 15;
            }
            bs[i * 20 + 4] = (byte) j;
            System.arraycopy(bs3, 0, bs, i * 20 + 5, j);
        }

        return bs;
    }

    public byte[] getDateByte(String binNameStr) {// 指令打包
        Long fileCRC = FileUtils.getCRC32(DataBuffer.filePath);//
        getFileLen(DataBuffer.filePath);
        if (DataBuffer.sendbinLen < 10)
            return null;
        byte[] binNameb = DataBuffer.sendbinName.getBytes();
        int data0Len = 32 + binNameb.length;
        byte[] data0 = new byte[data0Len];
        data0[0] = (byte) 0xff;// 报头
        data0[1] = (byte) (0xff & ((data0Len - 3) >> 8));// 长度高位
        data0[2] = (byte) (0xff & (data0Len - 3));// 长度低位
        data0[3] = (byte) (DataBuffer.sendIndex++);//
        data0[4] = (byte) (0);//
        data0[5] = (byte) 33;// 指令种类
        data0[6] = (byte) 20;
        data0[8] = (byte) 3;// 机器人类型
        putInt(DataBuffer.serverIP, data0, 10);
        putLong(fileCRC, data0, 14);
        putLong(DataBuffer.sendbinLen, data0, 22);
        System.arraycopy(binNameb, 0, data0, 30, binNameb.length);
        data0[data0Len - 1] = (byte) 0xAA;// 报尾
        data0[data0Len - 2] = XORcheckSend(data0);
        return data0;
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

    public Long getFileLen(String filePath) {
        InputStream fileOutStream = null;
        long fileLen = 0;
        File file = new File(filePath);
        try {
            fileOutStream = new FileInputStream(file);
            while (fileLen == 0) {// 读取长度需要时间
                fileLen = fileOutStream.available();
            }
            DataBuffer.sendbinLen = fileLen;
            fileOutStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("SetActivity download", "SetActivity download bin文件没找到");
        }
        return fileLen;
    }

    public static int bytesToInt2(byte[] bytes, int begin) {// 两个字节转化为int
        // 高位在前低位在后
        return (int) (0x00ff & bytes[begin + 1]) | ((0x00ff & bytes[begin]) << 8);
    }

    public int putInt(int dataD, byte[] data, int index) {// 高字节在前
        for (int i = 0; i < 4; i++) {
            data[index + i] = (byte) (dataD >> (8 * (3 - i)));
        }
        index += 4;
        return index;
    }

    public int putLong(long dataD, byte[] data, int index) {// 高字节在前
        for (int i = 0; i < 8; i++) {
            data[index + i] = (byte) (dataD >> (8 * (7 - i)));
        }
        index += 8;
        return index;
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

    /**
     * int转成3个字节byte
     *
     * @param num
     * @return
     */
    public byte[] intToByte3(int num) {
        byte[] bs = new byte[3];
        bs[0] = (byte) ((num >> 16) & 0xff);
        bs[1] = (byte) ((num >> 8) & 0xff);
        bs[2] = (byte) ((num >> 0) & 0xff);
        return bs;
    }

    //这里调用这个老的协议封装。
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
    public static byte[] addProtocol(byte[] buff) {
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

//    public static void send2robot(int num){
//        byte[] senddata = getsendbuff(data);
//        try {
//            socket = new DatagramSocket(); // //首先创建一个DatagramSocket对象
//            socket.setBroadcast(true);
//            sendAddress = InetAddress.getByName("255.255.255.255");
//        } catch (Exception e) {
//            Log.i("udpBroadcast", "udpBroadcast 开设端口号出错0" + e);
//        }
//        sendpacket = new DatagramPacket(senddata, senddata.length, sendAddress, serverPort);
//        try {
//            socket.send(sendpacket); // 调用socket对象的send方法，发送数据
//        } catch (Exception e) {
//            Log.i("udpBroadcast", "udpBroadcast 发送广播出错000" + e);
//        }
//
//
//    }
}
