package com.partnerx.roboth_server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class SetActivity extends Activity implements OnSeekBarChangeListener{
    private Button ifmusic, download, group;
    private SeekBar seekbar_sleeptime;
    private SeekBar seekbar_countdown;
    private TextView sleeptime;
    private TextView countdowntime;
    private Handler mHandler;
    private static Toast toast = null;
    private static boolean changMax = false;
    private AudioManager am;
    private int amVolume;

    private Button  qianjin,houtui,zuozhuan,youzhuan,stop,openbanace,closebanace,zuozhuanwan,youzhuanwan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        toast = new Toast(SetActivity.this);
        am = (AudioManager) getSystemService(AUDIO_SERVICE);

        if (!DataBuffer.startServer) {// 得提前开启，开启tcp服务器需要时间
            new Thread(new ServerRunnable(getLocalIpAddress())).start();// 打开服务器
            DataBuffer.startServer = true;
        }
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 20002:// 通知下载完毕
                        download.setEnabled(true);
//                        toast.makeText(SetActivity.this, getString(R.string.file) + DataBuffer.sendbinName + getString(R.string.downfinish), Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        };
        LogMgr.i("sleepTime = " + DataBuffer.sleepTime + ";countDownTime = " + DataBuffer.countDownTime);
        sleeptime = (TextView) findViewById(R.id.sleeptime);
        sleeptime.setText(getString(R.string.setmusicdelay) + DataBuffer.sleepTime + " ms");
        countdowntime = (TextView) findViewById(R.id.countdowntime);
        countdowntime.setText(getString(R.string.setcmddelay) + (DataBuffer.countDownTime * 50) + " ms");
        ifmusic = (Button) findViewById(R.id.ifmusic);
//        ifmusic.setEnabled(false);
        if (DataBuffer.ifOpen) {
            ifmusic.setText(R.string.closemusic);
        } else {
            ifmusic.setText(R.string.openmusic);
        }
        ifmusic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (DataBuffer.ifOpen) {
//                    if (DataBuffer.mp.isPlaying()) {
//                        DataBuffer.mp.stop();
//                    }
                    amVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                    if(DataBuffer.amVolume == 0){
                        DataBuffer.amVolume = amVolume;
                    }
                    LogMgr.i("DataBuffer.amVolume = " + DataBuffer.amVolume);
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                    DataBuffer.ifOpen = false;
                    ifmusic.setText(R.string.openmusic);
                } else {
                    LogMgr.i("DataBuffer.amVolume = " + DataBuffer.amVolume);
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, DataBuffer.amVolume, 0);
                    DataBuffer.ifOpen = true;
                    ifmusic.setText(R.string.closemusic);
                }
                SharedPreferences sharedPreferences = getSharedPreferences("ControlConfig", Context.MODE_PRIVATE); // 私有数据
                Editor editor = sharedPreferences.edit();// 获取编辑器
                editor.putBoolean("ifMusic", DataBuffer.ifOpen);
                editor.commit();// 提交修改
            }
        });
//        ifmusic.setBackgroundDrawable(getResources().getDrawable(R.drawable.gray));
        download = (Button) findViewById(R.id.download);
//        download.setEnabled(false);
//        download.setBackgroundDrawable(getResources().getDrawable(R.drawable.gray));
        download.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!(DataBuffer.fileList.size() > 0)) {
                    toast.makeText(SetActivity.this, R.string.no_find_file, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (DataBuffer.ifdownload) {
                    toast.makeText(SetActivity.this, R.string.cmd_wait_little, Toast.LENGTH_SHORT).show();
                    return;
                }
                final String[] str5 = DataBuffer.allFileStrArr;
                new AlertDialog.Builder(SetActivity.this).
                        setTitle(R.string.file_choice)// 超出长度可以自动换行
                        .setItems(str5, new DialogInterface.OnClickListener() { // content
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                download.setEnabled(false);
                                DataBuffer.filePath = DataBuffer.BINFILE + File.separator + str5[which];
                                if(!new File(DataBuffer.filePath).exists()){
                                    DataBuffer.filePath = DataBuffer.SKILL_BINFILE + File.separator + FileUtils.getFileNameNoEx(str5[which]) +".zip"+ File.separator +str5[which];
                                }
                                DataBuffer.sendbinName = str5[which];
                                DataBuffer.comID = (byte) 31;
                                new Thread(new UdpBroadcast(mHandler)).start();// 执行文件下载
                                toast.makeText(SetActivity.this, R.string.down_wait, Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // 关闭alertDialog
                        download.setEnabled(true);
                    }
                }).show();
            }
        });

        seekbar_countdown = (SeekBar) findViewById(R.id.seekbar_countdown);
        seekbar_countdown.setMax(80);
        seekbar_countdown.setProgress(DataBuffer.countDownTime - 20);
        seekbar_countdown.setOnSeekBarChangeListener(this);

        seekbar_sleeptime = (SeekBar) findViewById(R.id.seekbar_sleeptime);
        // seekbar_sleeptime.setMax(300);
        // seekbar_sleeptime.setProgress(100+DataBuffer.sleepTime/50);
        seekbar_sleeptime.setMax(DataBuffer.countDownTime + 200);
        seekbar_sleeptime.setProgress(DataBuffer.countDownTime + DataBuffer.sleepTime / 50);
        seekbar_sleeptime.setOnSeekBarChangeListener(this);

        group = (Button) findViewById(R.id.group);
//        group.setEnabled(false);
//        group.setBackgroundDrawable(getResources().getDrawable(R.drawable.gray));
        group.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                startActivity(new Intent(SetActivity.this, SetDanceActivity.class));
            }
        });

        qianjin = (Button) findViewById(R.id.qianjin);
        qianjin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DataBuffer.comID = (byte) 80;
                new Thread(new UdpBroadcast(mHandler)).start();
                //UDPsend.getInstance().send(80);
               // UdpBroadcast.send2robot(80);
            }
        });
        houtui =  (Button) findViewById(R.id.houtui);
        houtui.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DataBuffer.comID = (byte) 82;
                new Thread(new UdpBroadcast(mHandler)).start();
            }
        });
        zuozhuan =  (Button) findViewById(R.id.zuozhuan);
        zuozhuan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DataBuffer.comID = (byte) 81;
                new Thread(new UdpBroadcast(mHandler)).start();
            }
        });
        youzhuan =  (Button) findViewById(R.id.youzhuan);
        youzhuan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                DataBuffer.comID = (byte) 83;
               new Thread(new UdpBroadcast(mHandler)).start();

            }
        });
        stop =  (Button) findViewById(R.id.stop);
        stop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                DataBuffer.comID = (byte) 84;
                new Thread(new UdpBroadcast(mHandler)).start();
            }
        });
        openbanace = (Button) findViewById(R.id.openbannace);

        openbanace.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                DataBuffer.comID = (byte) 78;
                new Thread(new UdpBroadcast(mHandler)).start();
            }
        });
        closebanace = (Button) findViewById(R.id.closebanace);
        closebanace.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                DataBuffer.comID = (byte) 79;
                new Thread(new UdpBroadcast(mHandler)).start();
            }
        });
        zuozhuanwan = (Button) findViewById(R.id.zuozhuanwan);

        zuozhuanwan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                DataBuffer.comID = (byte) 77;
                new Thread(new UdpBroadcast(mHandler)).start();
            }
        });
        youzhuanwan = (Button) findViewById(R.id.youzhuanwan);
        youzhuanwan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                DataBuffer.comID = (byte) 76;
                new Thread(new UdpBroadcast(mHandler)).start();
            }
        });
    }
    /* 获取WIFI下ip地址 */
    private String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        // 获取32位整型IP地址
        int ipAddress = wifiInfo.getIpAddress();
        DataBuffer.serverIP = ipAddress;
        // 返回整型地址转换成“*.*.*.*”地址
        return String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));
    }
    /**
     * 去掉文件名后缀
     */
    private String getExtensionName(String n) {
        if (n != null && n.length() > 0) {
            int dot = n.lastIndexOf(".");
            if ((dot > -1) && (dot < (n.length() - 1))) {
                return n.substring(0, dot);
            }
        }
        return n;
    }
    protected void onDestroy() {
        super.onDestroy();
        // 保存配置信息
        SharedPreferences sharedPreferences = getSharedPreferences("ControlConfig", Context.MODE_PRIVATE); // 私有数据
        Editor editor = sharedPreferences.edit();// 获取编辑器
        editor.putInt("sleepTime", DataBuffer.sleepTime);
        editor.putInt("countDownTime", DataBuffer.countDownTime);
        editor.putBoolean("ifMusic", DataBuffer.ifOpen);
        editor.commit();// 提交修改
        finish();
    }
    private void getFileList(File pathFile, HashMap<String, String> fileList) {// 获取指定路径下的文件信息
        if (!pathFile.exists())
            return;
        List<String> listFile = new ArrayList<String>();
        if (pathFile.isDirectory()) {// 如果是文件夹的话
            // 返回文件夹中有的数据
            File[] files = pathFile.listFiles();
            // 先判断下有没有权限，如果没有权限的话，就不执行了
            if (null == files)
                return;
            for (int i = 0; i < files.length; i++) {
                getFileList(files[i], fileList);
            }
        } else {// 如果是文件的话直接加入
            // //进行文件的处理
            // String filePath = pathFile.getAbsolutePath();
            // //文件名
            // String fileName =
            // filePath.substring(filePath.lastIndexOf("/")+1);
            // //添加
            // fileList.put(fileName, filePath);//map
            // if(!listFile.contains(fileName))listFile.add(fileName);//list
        }
    }
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {// 监听seekbar的值
            case R.id.seekbar_sleeptime:
                // DataBuffer.sleepTime = (progress-100)*50;
                if (!changMax) {
                    DataBuffer.sleepTime = (progress - DataBuffer.countDownTime) * 50;
//                    DataBuffer.sleepTime = (progress - 100) * 50;
                    sleeptime.setText(getString(R.string.setmusicdelay) + DataBuffer.sleepTime + " " +
                            "ms");
                }
                break;
            case R.id.seekbar_countdown:
                DataBuffer.countDownTime = progress + 20;
                countdowntime.setText(getString(R.string.setcmddelay) + (DataBuffer.countDownTime * 50) + " ms");
                break;
            default:
                break;
        }
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {// 监听seekbar的值
            case R.id.seekbar_countdown:
                changMax = true;
                seekbar_sleeptime.setMax(DataBuffer.countDownTime + 200);// 改变大小后会走onProgressChanged
                if ((DataBuffer.sleepTime / 50 + DataBuffer.countDownTime) <= 0) {
                    DataBuffer.sleepTime = -DataBuffer.countDownTime * 50;
                    seekbar_sleeptime.setProgress(0);
                } else {
                    seekbar_sleeptime.setProgress(DataBuffer.countDownTime + DataBuffer.sleepTime / 50);
                }
                sleeptime.setText(getString(R.string.setmusicdelay) + DataBuffer.sleepTime + " ms");
                changMax = false;
                break;
            case R.id.seekbar_sleeptime:

                break;
            default:
                break;
        }
        SharedPreferences sharedPreferences = getSharedPreferences("ControlConfig", Context.MODE_PRIVATE); // 私有数据
        Editor editor = sharedPreferences.edit();// 获取编辑器
        editor.putInt("sleepTime", DataBuffer.sleepTime);
        editor.putInt("countDownTime", DataBuffer.countDownTime);
        editor.commit();// 提交修改
    }

//    private volatile  int id = 0;
//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//
//        //消息种类
//         switch(v.getId()){
//             case R.id.qianjin:
//                 id = 80;
//                 break;
//             case R.id.zuozhuan:
//                 id = 81;
//                 break;
//             case R.id.houtui:
//                 id = 82;
//                 break;
//             case R.id.youzhuan:
//                 id = 83;
//                 break;
//         }
//
//         switch(event.getAction()){
//             case MotionEvent.ACTION_DOWN:
//                // StartTimer();
//                 break;
//             case MotionEvent.ACTION_UP:
////                 StopTimer();
////                 id = 84;
////                 task();
//                 DataBuffer.comID = (byte) 80;
//                new Thread(new UdpBroadcast(mHandler)).start();
//                 break;
//         }
//        return false;
//    }
    //用定时器发送。
//    private Timer timer= null;
//    private TimerTask timerTask = null;
//    private int timedelay = 1000;
//    private void StartTimer(){
//        StopTimer();
//        timer = new Timer();
//        timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                task();
//            }
//        };
//        timer.schedule(timerTask,0,timedelay);
//    }
//    private void task(){
//
//       // new Thread(new UdpBroadcast(mHandler)).start();
//    }
//    private void StopTimer(){
//        if(timer != null){
//            timer.cancel();
//            timer = null;
//        }
//        if(timerTask != null){
//            timerTask.cancel();
//            timerTask = null;
//        }
//    }


}