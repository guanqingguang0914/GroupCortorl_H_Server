package com.abilix.dialogdemo;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e("quhw", "进入监听");
		int code = intent.getIntExtra("id", -1);
		if(code != -1){
			Log.e("quhw", "进入关闭事件");
//			NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
//			notificationManager.cancel(code);
			DownFileThread.interupted = true;
			ShowDialog.getDialog().cancelDownNotification();
		}
	}
	
}
