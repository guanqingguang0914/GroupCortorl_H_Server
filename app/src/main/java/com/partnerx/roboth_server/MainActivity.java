package com.partnerx.roboth_server;

import java.io.File;
import java.io.IOException;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.abilix.dialogdemo.ShowDialog;
import com.partnerx.json.SetActionJsonActivity;

public class MainActivity extends Activity {
    private Button volume, stop, showframe, sendbin, sendbroad, btn_choisebin, fixed, release, initial, batterylevel,
            help, set;
    private Button H0,H3,H5;
    private ImageView timetext;
    // private EditText sleeptime;
    private Handler mHandler;
    private static long lasttime = 0;
    private static boolean ShowBattery = true;
    private static boolean ShowFrame = true;
    private static Toast toast = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //联网升级，升级包
        toast = new Toast(MainActivity.this);
        setContentView(R.layout.activity_xmltext);
        SharedPreferences share = getSharedPreferences("ControlConfig", Context.MODE_PRIVATE);
        DataBuffer.ifOpen = share.getBoolean("ifMusic", true);
        DataBuffer.sleepTime = share.getInt("sleepTime", 0);
        DataBuffer.countDownTime = share.getInt("countDownTime", 100);
        DataBuffer.getbinStrArr_HasEx();

        DataBuffer.mediaPlayer = new MediaPlayer();
        DataBuffer.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                DataBuffer.nextsong();
            }
        });

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 20001:
                        countdownImage(Integer.parseInt((msg.obj).toString()));
                        break;
                    case 20002:
                        countdownImage(Integer.parseInt((msg.obj).toString()));
                        if(DataBuffer.isMovingEnd){
                            sendbroad.setEnabled(true);
                            fixed.setEnabled(true);
                            release.setEnabled(true);
                            initial.setEnabled(true);
                            batterylevel.setEnabled(true);
                            volume.setEnabled(true);
                            stop.setEnabled(true);
                            showframe.setEnabled(true);
                        }
                        break;
                    case 20003:
                        if (DataBuffer.ifOpen)
                            DataBuffer.mp.start();
                        break;
                    default:
                        break;
                }
            }
        };

        //按钮变灰只需要将他们的背景图标改掉。、
        H0 = (Button) findViewById(R.id.H0);
//        H0.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //color2Gray();
//                DataBuffer.type = DataBuffer.H0;
//                H0H3H5(0);
//                color2Gray();
//            }
//        });
        H3 = (Button) findViewById(R.id.H3);
        H3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataBuffer.type = DataBuffer.H3;
                H0H3H5(3);
                color2Gray();
            }
        });
        H5 = (Button) findViewById(R.id.H5);
        H5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataBuffer.type = DataBuffer.H5;
                H0H3H5(5);
                color2nomal();
            }
        });

        set = (Button) findViewById(R.id.set);
        //这里先把设置置灰，先不检测
//        set.setEnabled(false);
//        set.setBackgroundDrawable(getResources().getDrawable(R.drawable.gray));
        set.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // 跳转界面
                Intent intent = new Intent(MainActivity.this, SetActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });
        help = (Button) findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // 跳转界面
                Intent intent = new Intent(MainActivity.this, HelpActivity.class);
//                Intent intent = new Intent(MainActivity.this, SetActionJsonActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });
        // sleeptime = (EditText)findViewById(R.id.sleeptime);
        timetext = (ImageView) findViewById(R.id.timetext);

        volume = (Button) findViewById(R.id.volume);
        //先暂时屏蔽掉
//        volume.setEnabled(false);
//        volume.setBackgroundDrawable(getResources().getDrawable(R.drawable.gray));
        volume.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) { // 本机时钟
                if ((System.currentTimeMillis() - lasttime > DataBuffer.countDownTime * 50) && !DataBuffer.ifdownload) {
                    String[] str5 = {getString(R.string.open_voice), getString(R.string.close_voice)};
                    new AlertDialog.Builder(MainActivity.this).setTitle(R.string.is_close_voice)// 超出长度可以自动换行
                            .setItems(str5, new DialogInterface.OnClickListener() { // content
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            DataBuffer.comID = (byte) 7;//
                                            volume.setText(getString(R.string.open_voice));
                                            break;
                                        case 1:
                                            DataBuffer.comID = (byte) 8;
                                            volume.setText(getString(R.string.close_voice));
                                            break;
                                    }
                                    new Thread(new UdpBroadcast(mHandler)).start();
                                    lasttime = System.currentTimeMillis();
                                    sendbroad.setEnabled(false);
                                    fixed.setEnabled(false);
                                    release.setEnabled(false);
                                    initial.setEnabled(false);
                                    batterylevel.setEnabled(false);
                                    volume.setEnabled(false);
                                    stop.setEnabled(false);
                                    showframe.setEnabled(false);
//                                     sendbin.setEnabled(false);
                                }
                            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss(); // 关闭alertDialog
                        }
                    }).show();
                } else{
                    toast.makeText(MainActivity.this, R.string.toast_notice, Toast.LENGTH_SHORT).show();
                }

            }
        });

        stop = (Button) findViewById(R.id.stop);
        stop.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) { // 本机时钟
                if ((System.currentTimeMillis() - lasttime > DataBuffer.countDownTime * 50) && !DataBuffer.ifdownload) {
                    DataBuffer.isMovingEnd = true;
                    DataBuffer.comID = (byte) 6;
                    new Thread(new UdpBroadcast(mHandler)).start();
                    lasttime = System.currentTimeMillis();
//                    sendbroad.setEnabled(false);
//                    fixed.setEnabled(false);
//                    release.setEnabled(false);
//                    initial.setEnabled(false);
//                    batterylevel.setEnabled(false);
//                    volume.setEnabled(false);
//                    stop.setEnabled(false);
//                    showframe.setEnabled(false);
                    color2nomal();
                    DataBuffer.mediaPlayer.reset();
                    DataBuffer.bStartMedia = false;
                    //关闭音乐。
                    if (DataBuffer.mp.isPlaying()) {
                        DataBuffer.mp.stop();
                    }
                    DataBuffer.ifOpen = true;
                } else{
                    toast.makeText(MainActivity.this, R.string.toast_notice, Toast.LENGTH_SHORT).show();
                }
            }
        });

        showframe = (Button) findViewById(R.id.showframe);
        showframe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((System.currentTimeMillis() - lasttime > DataBuffer.countDownTime * 50) && !DataBuffer.ifdownload) {
//                    if(ShowFrame){
//                        DataBuffer.comID = (byte) 12;
//                        ShowFrame = false;
//                        showframe.setText("显示组号");
//                    }else {
//                        DataBuffer.comID = (byte) 13;
//                        ShowFrame = true;
//                        showframe.setText("设置组号");
//                    }
                    DataBuffer.comID = (byte) 12;
                    new Thread(new UdpBroadcast(mHandler)).start();
                    lasttime = System.currentTimeMillis();
                    sendbroad.setEnabled(false);
                    fixed.setEnabled(false);
                    release.setEnabled(false);
                    initial.setEnabled(false);
                    batterylevel.setEnabled(false);
                    volume.setEnabled(false);
                    stop.setEnabled(false);
                    showframe.setEnabled(false);
//                     sendbin.setEnabled(false);
                } else{
                    toast.makeText(MainActivity.this, R.string.toast_notice, Toast.LENGTH_SHORT).show();
                }
            }
        });
//        showframe.setEnabled(false);
//        showframe.setBackgroundDrawable(getResources().getDrawable(R.drawable.gray));

        release = (Button) findViewById(R.id.release);
        release.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) { // 本机时钟
                if ((System.currentTimeMillis() - lasttime > DataBuffer.countDownTime * 50) && !DataBuffer.ifdownload) {
                    DataBuffer.comID = (byte) 0;
                    new Thread(new UdpBroadcast(mHandler)).start();
                    lasttime = System.currentTimeMillis();
                    sendbroad.setEnabled(false);
                    fixed.setEnabled(false);
                    release.setEnabled(false);
                    initial.setEnabled(false);
                    batterylevel.setEnabled(false);
                    volume.setEnabled(false);
                    stop.setEnabled(false);
                    showframe.setEnabled(false);
//                     sendbin.setEnabled(false);
                } else{
                    toast.makeText(MainActivity.this, R.string.toast_notice, Toast.LENGTH_SHORT).show();
                }
            }
        });

        fixed = (Button) findViewById(R.id.fixed);
        fixed.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) { // 本机时钟
                if ((System.currentTimeMillis() - lasttime > DataBuffer.countDownTime * 50) && !DataBuffer.ifdownload) {
                    DataBuffer.comID = (byte) 1;
                    new Thread(new UdpBroadcast(mHandler)).start();
                    lasttime = System.currentTimeMillis();
                    sendbroad.setEnabled(false);
                    fixed.setEnabled(false);
                    release.setEnabled(false);
                    initial.setEnabled(false);
                    batterylevel.setEnabled(false);
                    volume.setEnabled(false);
                    stop.setEnabled(false);
                    showframe.setEnabled(false);
//                     sendbin.setEnabled(false);
                } else{
                    toast.makeText(MainActivity.this, R.string.toast_notice, Toast.LENGTH_SHORT).show();
                }
            }
        });

        initial = (Button) findViewById(R.id.initial);
        initial.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) { // 本机时钟
                if ((System.currentTimeMillis() - lasttime > DataBuffer.countDownTime * 50) && !DataBuffer.ifdownload) {
                    DataBuffer.comID = (byte) 2;
                    new Thread(new UdpBroadcast(mHandler)).start();
                    lasttime = System.currentTimeMillis();
                    sendbroad.setEnabled(false);
                    fixed.setEnabled(false);
                    release.setEnabled(false);
                    initial.setEnabled(false);
                    batterylevel.setEnabled(false);
                    volume.setEnabled(false);
                    stop.setEnabled(false);
                    showframe.setEnabled(false);
                    DataBuffer.mediaPlayer.reset();
                    DataBuffer.bStartMedia = false;
                    //关闭音乐。
                    if (DataBuffer.mp.isPlaying()) {
                        DataBuffer.mp.stop();
                    }
                    DataBuffer.ifOpen = true;
                } else{
                    toast.makeText(MainActivity.this, R.string.toast_notice,Toast.LENGTH_SHORT).show();
                }
            }
        });
        batterylevel = (Button) findViewById(R.id.batterylevel);
//        batterylevel.setEnabled(false);
//        batterylevel.setBackgroundDrawable(getResources().getDrawable(R.drawable.gray));
        batterylevel.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) { // 本机时钟
                if ((System.currentTimeMillis() - lasttime > DataBuffer.countDownTime * 50) && !DataBuffer.ifdownload) {
                    if (ShowBattery) {
                        DataBuffer.comID = (byte) 4;
                        ShowBattery = false;
                        batterylevel.setText(R.string.cloce_battery);
                    } else {
                        DataBuffer.comID = (byte) 5;
                        ShowBattery = true;
                        batterylevel.setText(R.string.openbattery);
                    }
                    new Thread(new UdpBroadcast(mHandler)).start();
                    lasttime = System.currentTimeMillis();
                    sendbroad.setEnabled(false);
                    fixed.setEnabled(false);
                    batterylevel.setEnabled(false);
                    release.setEnabled(false);
                    initial.setEnabled(false);
                    volume.setEnabled(false);
                    stop.setEnabled(false);
                    showframe.setEnabled(false);
                } else{
                    toast.makeText(MainActivity.this, R.string.toast_notice, Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_choisebin = (Button) findViewById(R.id.btn_choisebin);
        if(DataBuffer.binName != null){
            btn_choisebin.setText(DataBuffer.binName);//目前只是H5群控版本
//            btn_choisebin.setText(FileUtils.returnTranName(DataBuffer.binName));
        }
        btn_choisebin.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) { // 本机时钟
                final String[] str5 = DataBuffer.showbinStrArr;
//                final String[] chNameStr = FileUtils.returnChName(str5);
                final String[] chNameStr = str5;
                new AlertDialog.Builder(MainActivity.this).setTitle(R.string.binchoice)// 超出长度可以自动换行
                        .setItems(chNameStr, new DialogInterface.OnClickListener() { // content
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DataBuffer.binName = str5[which];
                                btn_choisebin.setText(chNameStr[which]);
                            }
                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // 关闭alertDialog
                    }
                }).show();
            }
        });

        sendbroad = (Button) findViewById(R.id.sendbroad);
        sendbroad.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) { // 本机时钟
//                LogMgr.i("btn_choisebin.getText().toString().trim()).equals(R.string.binchoice == "+(DataBuffer.binName == null)+";"+ (DataBuffer.binName.equals(""))+";"+(TextUtils.isEmpty(btn_choisebin.getText().toString().trim()))+"" +(btn_choisebin.getText().toString().trim()).equals(R.string.binchoice) );
                if ((System.currentTimeMillis() - lasttime > DataBuffer.countDownTime * 50) && !DataBuffer.ifdownload) {
                    if(!ShowBattery){//true 电量不检测
                        new AlertDialog.Builder(MainActivity.this)// 超出长度可以自动换行
                                .setMessage(R.string.notice_battery)
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss(); // 关闭alertDialog
                                    }
                                })
                                .show();
                    }else {//false  电量检测中
                        if ((System.currentTimeMillis() - lasttime > DataBuffer.countDownTime * 50) && !DataBuffer.ifdownload) {
                            if (DataBuffer.binName == null || (btn_choisebin.getText().toString().trim()).equals("动作选择") ||
                                    (btn_choisebin.getText().toString().trim()).equals("Action selection") ||
                                    (btn_choisebin.getText().toString().trim()).equals("動作選擇")) {
                                toast.makeText(MainActivity.this, R.string.choixe_move_bin, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            DataBuffer.comID = (byte) 3;
                            new Thread(new UdpBroadcast(mHandler)).start();
                            lasttime = System.currentTimeMillis();
//                            sendbroad.setEnabled(false);
//                            fixed.setEnabled(false);
//                            release.setEnabled(false);
//                            initial.setEnabled(false);
//                            batterylevel.setEnabled(false);
//                            volume.setEnabled(false);
//                            stop.setEnabled(false);
//                            showframe.setEnabled(false);
                            DataBuffer.isMovingEnd = false;
                            color2Gray();
                            try {
                                try {
                                    if (DataBuffer.mp != null) {
                                        DataBuffer.mp.reset();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                String musicpath1 = DataBuffer.MUSICFILE + FileUtils.getFileNameNoEx(DataBuffer.binName);
                                if (new File(musicpath1 + ".wav").exists()) {// 文件存在
                                    musicpath1 = musicpath1 + ".wav";
                                } else if (new File(musicpath1 + ".mp3").exists()) {
                                    musicpath1 = musicpath1 + ".mp3";
                                } else if (new File(musicpath1 + ".mp4").exists()) {
                                    musicpath1 = musicpath1 + ".mp4";
                                } else
                                    musicpath1 = null;
                                if (musicpath1 != null) {
                                    Log.e("helei","path: "+musicpath1);
                                    DataBuffer.mp.setDataSource(musicpath1);
                                    DataBuffer.mp.prepare();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }else {
                    toast.makeText(MainActivity.this, R.string.toast_notice, Toast.LENGTH_SHORT).show();
                }
            }
        });


        ShowDialog.getDialog().loadVersion(MainActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

        ShowDialog.getDialog().cancelDownNotification();

//         if(DataBuffer.mp.isPlaying()){//横竖屏切换时音乐会关掉
//         DataBuffer.mp.stop();
//         }
//        DataBuffer.mp.release();
//        DataBuffer.mp = null;
        DataBuffer.mediaPlayer.release();;
        DataBuffer.mediaPlayer = null;
    }

    public void countdownImage(int num) {
        switch (num) {
            case 0:
                timetext.setImageResource(R.drawable.countdown0);
                break;
            case 1:
                timetext.setImageResource(R.drawable.countdown1);
                break;
            case 2:
                timetext.setImageResource(R.drawable.countdown2);
                break;
            case 3:
                timetext.setImageResource(R.drawable.countdown3);
                break;
            case 4:
                timetext.setImageResource(R.drawable.countdown4);
                break;
            case 5:
                timetext.setImageResource(R.drawable.countdown5);
                break;
            case 6:
                timetext.setImageResource(R.drawable.countdown6);
                break;
            case 7:
                timetext.setImageResource(R.drawable.countdown7);
                break;
            case 8:
                timetext.setImageResource(R.drawable.countdown8);
                break;
            case 9:
                timetext.setImageResource(R.drawable.countdown9);
                break;
            case 10:
                timetext.setImageResource(R.drawable.countdown10);
                break;
            default:
                break;
        }
    }
    //添加选择H3时按钮变灰
    public void color2Gray(){
        volume.setBackgroundDrawable(getResources().getDrawable(R.drawable.gray));
        volume.setEnabled(false);
        showframe.setBackgroundDrawable(getResources().getDrawable(R.drawable.gray));
        showframe.setEnabled(false);
        batterylevel.setBackgroundDrawable(getResources().getDrawable(R.drawable.gray));
        batterylevel.setEnabled(false);
        release.setBackgroundDrawable(getResources().getDrawable(R.drawable.gray));
        release.setEnabled(false);
        fixed.setBackgroundDrawable(getResources().getDrawable(R.drawable.gray));
        fixed.setEnabled(false);
        //initial
        initial.setBackgroundDrawable(getResources().getDrawable(R.drawable.gray));
        initial.setEnabled(false);
    }

    public void color2nomal(){

        volume.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_btn));
        volume.setEnabled(true);
        showframe.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_btn));
        showframe.setEnabled(true);
        batterylevel.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_btn));
        batterylevel.setEnabled(true);
        release.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_btn));
        release.setEnabled(true);
        fixed.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_btn));
        fixed.setEnabled(true);
        initial.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_btn));
        initial.setEnabled(true);
    }
    public void H0H3H5(int num){
        switch(num){
            case 0:
                H0.setBackgroundDrawable(getResources().getDrawable(R.drawable.selectdown));
                H3.setBackgroundDrawable(getResources().getDrawable(R.drawable.typeselect));
                H5.setBackgroundDrawable(getResources().getDrawable(R.drawable.typeselect));
                break;
            case 3:
                H3.setBackgroundDrawable(getResources().getDrawable(R.drawable.selectdown));
                H0.setBackgroundDrawable(getResources().getDrawable(R.drawable.typeselect));
                H5.setBackgroundDrawable(getResources().getDrawable(R.drawable.typeselect));
                break;
            case 5:
                H5.setBackgroundDrawable(getResources().getDrawable(R.drawable.selectdown));
                H0.setBackgroundDrawable(getResources().getDrawable(R.drawable.typeselect));
                H3.setBackgroundDrawable(getResources().getDrawable(R.drawable.typeselect));
                break;
        }


    }

//	/**
//	 * 申请权限
//	 */
//	private void applyPermission() {
//		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
//
//		}
//	}
}
