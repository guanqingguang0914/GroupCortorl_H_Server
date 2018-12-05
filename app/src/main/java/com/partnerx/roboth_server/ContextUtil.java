package com.partnerx.roboth_server;

import android.app.Application;
import android.content.Context;

public class ContextUtil extends Application {
    private static Application instance;
    private Context context;
    private String pathName = "";
    public static Application getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        context = getApplicationContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                pathName = "GroupControl";
                FileUtils.saveSoundFileToSdCard(context, FileUtils.MOVEBIN, pathName);
            }
        }).start();
    }
}


