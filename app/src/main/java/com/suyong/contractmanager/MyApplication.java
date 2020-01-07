package com.suyong.contractmanager;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.amap.api.track.AMapTrackClient;
import com.suyong.contractmanager.pojo.CurrentIUser;

public class MyApplication extends Application {

    public SharedPreferences trackConf = null;
    //
    public boolean isStartService ;
    public boolean isRunning;
    //当前登陆的用户
    private static CurrentIUser currentIUser;

    private static Context context;
    public long ServiceId;
    public long TerminalId;
    public AMapTrackClient Client;

    @Override
    public void onCreate() {
        super.onCreate();
        currentIUser = new CurrentIUser();
        context = getApplicationContext();
        trackConf = getSharedPreferences("track_conf", MODE_PRIVATE);
        isStartService = trackConf.getBoolean("is_trace_started",false);
        isRunning = false;
    }

    public static CurrentIUser getCurrentIUser(){
        return currentIUser;
    }

    public static Context getContext(){
        return context;
    }
}
