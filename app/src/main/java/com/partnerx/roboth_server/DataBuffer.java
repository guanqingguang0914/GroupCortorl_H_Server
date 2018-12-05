package com.partnerx.roboth_server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;

import com.partnerx.sqlite.SQLBeanRobot;

public class DataBuffer {
    public static int type = 3;//默认H3，只有H0 会设置为1.
    public static  final int H0 = 0;
    public static final int H1 = 1;
    public static final int H3 = 3;
    public static final int H5 = 5;
    public static byte sendIndex = 2;// 初始值不能为和客户端的值相同
    public static byte comID = 2;
    public static int serverIP = 0;
    public static String filePath = "";
    public static String sendbinName = "";
    public static Long sendbinLen = 0l;
    public static String binName = null;
    public static int sleepTime = 1700;
    public static int amVolume = 0;
    public static int countDownTime = 100;
    public static boolean ifOpen =  true;
    public static boolean ifdownload = false;
    public static boolean startServer = false;
    public static boolean isstart101 = false;
    public static MediaPlayer mp = new MediaPlayer();
    public static MediaPlayer mediaPlayer;
    public static boolean isBreak =  true;
    public static boolean isMovingEnd =  true;
    public static final Lock lockAddress = new ReentrantLock(); // 锁对象
    public static List<InetAddress> AddressList = new ArrayList<InetAddress>();//
    public final static String MUSICFILE = Environment.getExternalStorageDirectory() + File.separator + "GroupControl" + File.separator;
    public final static String BINFILE = Environment.getExternalStorageDirectory() + File.separator + "GroupControl";
    public final static String SKILL_BINFILE = Environment.getExternalStorageDirectory() + File.separator + "PartnerX" + File.separator + "SkillPlayer";
    public static List<String> fileList = new ArrayList<String>();
    public static String[] allFileStrArr = null;// 下载文件
    public static String[] showbinStrArr = null;// 动作选择
    public static String[] StrArr0 = new String[]{"TBboys_Start", "TBboys_End"};
    public static String[] StrArr = new String[]{"TBboys_Start.bin", "TBboys_End.bin"};
    public static List<SQLBeanRobot> managerList = Collections.synchronizedList(new ArrayList<SQLBeanRobot>());

    public static void getbinStrArr_NoEx() {
        FileUtils.getFileList_NoEx(new File(DataBuffer.BINFILE), DataBuffer.fileList);//返回filelist.
        for(int i=0;i<DataBuffer.fileList.size();i++){
            Log.e("helei 无后缀名"," "+DataBuffer.fileList.get(i));
        }
        if (!(DataBuffer.fileList.size() > 0)) {
            DataBuffer.showbinStrArr = StrArr0;
            return;
        }
        DataBuffer.allFileStrArr = new String[DataBuffer.fileList.size()];
        String[] showbinStrArr0 = new String[DataBuffer.fileList.size()];

        int ii = 0;
        if (DataBuffer.fileList.contains(StrArr[0])) {
            DataBuffer.allFileStrArr[ii] = StrArr[0];
            ii++;
        }
        if (DataBuffer.fileList.contains(StrArr[1])) {
            DataBuffer.allFileStrArr[ii] = StrArr[1];
            ii++;
        }
        int jj = 0;
        for (String str : DataBuffer.fileList) {
            if (!str.equals(StrArr[0]) && !str.equals(StrArr[1])) {
                showbinStrArr0[jj] = str;
                jj++;
                DataBuffer.allFileStrArr[ii] = str;
                ii++;
            }
        }


        DataBuffer.showbinStrArr = new String[2 + jj];
        System.arraycopy(DataBuffer.StrArr0, 0, DataBuffer.showbinStrArr, 0, 2);
        if (jj > 0)
            System.arraycopy(showbinStrArr0, 0, DataBuffer.showbinStrArr, 2, jj);
    }

    public static void getbinStrArr_HasEx() {// 有后缀名
        FileUtils.getFileList_HasEx(new File(DataBuffer.BINFILE), DataBuffer.fileList,false);
        //暂时给H56表演用，暂时屏蔽
//        FileUtils.getFileList_HasEx(new File(DataBuffer.SKILL_BINFILE), DataBuffer.fileList,true);
        for(int i=0;i<DataBuffer.fileList.size();i++){
            Log.e("helei 有后缀名"," "+DataBuffer.fileList.get(i));
        }
        if (!(DataBuffer.fileList.size() > 0)) {
            DataBuffer.showbinStrArr = StrArr0;
            return;
        }
        DataBuffer.allFileStrArr = new String[DataBuffer.fileList.size()];
        String[] showbinStrArr0 = new String[DataBuffer.fileList.size()];

        int ii = 0;
        if (DataBuffer.fileList.contains(StrArr[0])) {
            DataBuffer.allFileStrArr[ii] = StrArr[0];
            ii++;
        }
        if (DataBuffer.fileList.contains(StrArr[1])) {
            DataBuffer.allFileStrArr[ii] = StrArr[1];
            ii++;
        }
        int jj = 0;
        for (String str : DataBuffer.fileList) {
            if (!str.equals(StrArr[0]) && !str.equals(StrArr[1])) {
//                if (str.endsWith(".bin")) {
                    showbinStrArr0[jj] = FileUtils.getFileNameNoEx(str);
                    DataBuffer.allFileStrArr[jj] = str;
                    jj++;
//                }
                DataBuffer.allFileStrArr[ii] = str;
                ii++;
            }
        }
        DataBuffer.showbinStrArr = new String[jj];
        //System.arraycopy(DataBuffer.StrArr0, 0, DataBuffer.showbinStrArr, 0, 2);
        if (jj > 0)
            System.arraycopy(showbinStrArr0, 0, DataBuffer.showbinStrArr, 0, jj);
    }


    public static void nextsong() {
        String Binpath = Environment.getExternalStorageDirectory().getPath() + File.separator + "GroupControl" + File.separator + "Biaoyan" + File.separator;
        String Binname = getFileName(Binpath, false);
        if(Binname != "") {
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(Binpath + Binname);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static int nMainIndex=0, nSubIndex=0;
    static boolean bSubExist=false, bStartMedia=false;
    public static String getFileName(String path, boolean bStart)
    {
        String strName;
        File file;
        if(bStart == true) {  //第一次获取
            nMainIndex = 1;
            nSubIndex = 1;
            bSubExist = false;
            Log.e("getFileName", "start: " + path);
        }
        strName = Integer.toString(nMainIndex) + "." + Integer.toString(nSubIndex) + ".mp3";
        file = new File(path + strName);
        if(file.exists()) {    //判断子类型
            Log.e("getFileName", "sub exist: " + strName);
            nSubIndex++;
            bSubExist = true;
            return strName;
        }

        if(bSubExist == true) {
            bSubExist = false;
            nSubIndex = 1;
            nMainIndex++;
        }
        strName = Integer.toString(nMainIndex) + ".mp3";
        file = new File(path + strName);
        if(file.exists()) {    //判断主类型
            Log.e("getFileName", "main exist: " + strName);
            nMainIndex++;
            return strName;
        }

        Log.e("getFileName", "not exist: " + path);
        return "";
    }

}
