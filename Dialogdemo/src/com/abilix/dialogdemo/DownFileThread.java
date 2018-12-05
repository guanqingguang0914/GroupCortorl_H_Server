package com.abilix.dialogdemo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.zip.CRC32;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class DownFileThread extends Thread implements Constant{
	
	public final static String TAG = "DownFileThread";

	private Context context;
	private String apkName;//文件名称
	private String appName;//应用名称
	private UpdateInfo updateInfo;
	private boolean installApk; // 是否需要自动安装
	private  ProgressDialog pd;

	private File apkFile;
//	private String urlStr;
	
	boolean isFinished; 
	public static boolean interupted = false; //是否停止下载
	
	public static NotificationManager mNotificationManager ;
	public Notification.Builder mBuilder ; 
	public static  int notifyCode;

	
	@SuppressLint("NewApi") 
	public DownFileThread(Context context, String appName, String apkName, UpdateInfo updateInfo, boolean installApk, ProgressDialog pd) {
		this.context = context;
		this.apkName = apkName;
		this.appName = appName;
		this.updateInfo = updateInfo;
		this.installApk = installApk;
		this.pd = pd;

		Log.e("quhw", "下载地址为:"+updateInfo.getApk_url());
		
        String sdpath = Environment.getExternalStorageDirectory() + "/";
        String mSavePath = sdpath + "Android/data/Abilix";
        
        File mSave = new File(mSavePath);
        if(!mSave.exists()){
        	mSave.mkdirs();
		}

		apkFile = new File(mSavePath+"/"+apkName);
		isFinished = false;
		
		notifyCode = (int) System.currentTimeMillis();
		
		Intent delIntent = new Intent();
		delIntent.setClass(context, NotificationReceiver.class);
		delIntent.putExtra("id", notifyCode);
		delIntent.setAction("del_action");
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, delIntent, 0);
		
		
		mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//		mBuilder = new NotificationCompat.Builder(context);
		mBuilder = new Notification.Builder(context);
		mBuilder.setContentTitle(appName)
			.setContentText(context.getResources().getString(R.string.update_lib_down_ing))
			.setWhen(System.currentTimeMillis())
//			.setPriority(Notification.PRIORITY_DEFAULT)
			.setOngoing(true)//不能实现滑动删除
//			.setAutoCancel(true)//点击通知栏会自动被取消消失
			.setProgress(100, 0, false)
			.setDeleteIntent(pendingIntent)
			.setSmallIcon(R.drawable.update_lib_ic_launcher);
		
	}

	public File getApkFile() {
		if (isFinished)
			return apkFile;
		else
			return null;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void interuptThread() {
		Log.e("quhw", "执行删除操作");
		interupted = true;
		mNotificationManager.cancel(notifyCode);
	}

	@SuppressLint("NewApi") 
	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (Environment.getExternalStorageState().equals(//检查sdcard是否存在，并可读写
				Environment.MEDIA_MOUNTED)) {
			URL url = null;
			HttpURLConnection conn = null;
			InputStream iStream = null;
			FileOutputStream fos = null;
			BufferedInputStream bis = null;
			try {
//				StrictMode
//				.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//						.detectDiskReads().detectDiskWrites()
//						.detectNetwork()
//						.penaltyLog().build());
//				StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//				.detectLeakedSqlLiteObjects()
//				.detectLeakedClosableObjects().penaltyLog()
//				.penaltyDeath().build());
				url = new URL(updateInfo.getApk_url());
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5000);
				conn.setReadTimeout(20000);
				iStream = conn.getInputStream();
				fos = new FileOutputStream(apkFile);
				
				if(conn.getResponseCode() == 404){
					throw new Exception("404fail!");
				}
				
				bis = new BufferedInputStream(iStream);
				byte[] buffer = new byte[1024];
				int len = 0;
				//获取下载文件的size
				int length = conn.getContentLength();
				double rate = (double) 100 / length;
				
				int down_step = 3;
				int downloadCount = 0;//已经下载好的大小
				int updateCount = 0;//已经上传的文件大小
				while (false == interupted && ((len = bis.read(buffer)) != -1)) {
					fos.write(buffer, 0, len);
					downloadCount += len;
					
					int p = (int) (downloadCount * rate);
//					Log.e("quhw", "progress:"+p);
//					if((p-oldP) >=0){
					if(updateCount==0 || (downloadCount*rate - down_step)>=updateCount){
						updateCount += down_step;
						
						mBuilder.setProgress(100, p, false);
						mBuilder.setContentText(context.getResources().getString(R.string.update_lib_down_ing)+p+"%");
						mNotificationManager.notify(notifyCode, mBuilder.build());
					}

				}
			
				
				if (downloadCount == length) {
					isFinished = true;
					mBuilder.setProgress(100, 100, false);
					
					mNotificationManager.cancel(notifyCode);
					if(installApk){
						// 安装
						int force_update = updateInfo.getForce_update();
						if(force_update == 1){
							installApk();
//							boolean isInstall = isAppOnForeground();
//							if(isInstall == true){
								System.exit(0);
//							}
						}else{
							installApk();
						}


					}else{
						String sdpath = Environment.getExternalStorageDirectory() + "/";
				        String mSavePath = sdpath + "Android/data/Abilix";
				        String myapkname = "";
				        if(apkName.length() > 0){
							String[] apkNames = apkName.split("/");
							if(apkNames.length == 4){
								myapkname = apkNames[3];
							}else{
								myapkname = apkName;
							}
						}
				    	File apkfile = new File(mSavePath, myapkname);
						long size = getCRC32(apkfile);
//						if(FileCrc == size){
//							// 不安装，发通知
//							String mPath = Environment.getExternalStorageDirectory() + "/" + "Android/data/Abilix/" + apkName;
//							ShowDialog.getDialog().checkupdateshow(mPath);
//						}else{
//							ShowDialog.getDialog().checkfailshow("CRC大小不匹配");
//						}
						
						
					}
				}
				
			}catch(Exception e){
//				e.printStackTrace();
				ShowDialog.getDialog().checkfailshow("" + e);
			} finally{
				if(bis != null){
					try {
						bis.close();
						mNotificationManager.cancel(notifyCode);
					} catch (IOException e) {
						// TODO Auto-generated catch block
//						e.printStackTrace();
						ShowDialog.getDialog().checkfailshow("BufferedInputStream 关闭异常");
					}
				}
				if(fos != null){
					try {
						fos.close();

					} catch (IOException e) {
						// TODO Auto-generated catch block
//						e.printStackTrace();
						ShowDialog.getDialog().checkfailshow("FileOutputStream 关闭异常");
					}
				}
				if(iStream != null){
					try {
						iStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				mNotificationManager.cancel(notifyCode);
			}
			
		} else {
//			Log.e("quhw", "下载失败：sdcard异常");
			ShowDialog.getDialog().checkfailshow("下载失败：sdcard异常");
		}
	}



	/*
     * 安装apk文件
     */
    private void installApk(){
    	String sdpath = Environment.getExternalStorageDirectory() + "/";
        String mSavePath = sdpath + "Android/data/Abilix";
        String myapkname = "";
        if(apkName.length() > 0){
			String[] apkNames = apkName.split("/");
			if(apkNames.length == 4){
				myapkname = apkNames[3];
			}else{
				myapkname = apkName;
			}
		}
    	File apkfile = new File(mSavePath, myapkname);
    	if(!apkfile.exists()){
    		return;
    	}
    	//通过intent安装apk文件
    	Intent i = new Intent(Intent.ACTION_VIEW);
    	i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
    	i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	context.startActivity(i);
    	
    }
	/*
	 * 判断当前应用是否在前台运行
	 */

	private boolean isAppOnForeground(){
		if(pd != null){
			pd.dismiss();
		}
		ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		String packageName = context.getApplicationContext().getPackageName();
		List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
		if (appProcesses == null)
			return false;
		for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
		// The name of the process that this object is associated with.
			if(appProcess.processName.equals(packageName)&&appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
			{
				return true;
			}
		}
		return false;
	}


	public static void cancelNotification(){
		if(mNotificationManager != null){
			mNotificationManager.cancel(notifyCode);
		}
		
	}
	
	/**
	 * 获取文件的CRC值
	 * 
	 * @param f
	 * @return
	 */
	public static long getCRC32(File f) {
		try {
			FileInputStream in = new FileInputStream(f);
			CRC32 crc = new CRC32();
			byte[] bytes = new byte[1024];
			int byteCount;
			crc.reset();
			while ((byteCount = in.read(bytes)) > 0) {
				crc.update(bytes, 0, byteCount);
			}
			in.close();
			long sum = crc.getValue();
			return sum;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}
}
