package com.suyong.contractmanager.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.amap.api.track.AMapTrackClient;
import com.amap.api.track.ErrorCode;
import com.amap.api.track.OnTrackLifecycleListener;
import com.amap.api.track.TrackParam;
import com.amap.api.track.query.entity.LocationMode;
import com.amap.api.track.query.entity.Point;
import com.amap.api.track.query.model.AddTerminalRequest;
import com.amap.api.track.query.model.AddTerminalResponse;
import com.amap.api.track.query.model.AddTrackResponse;
import com.amap.api.track.query.model.DistanceResponse;
import com.amap.api.track.query.model.HistoryTrackResponse;
import com.amap.api.track.query.model.LatestPointRequest;
import com.amap.api.track.query.model.LatestPointResponse;
import com.amap.api.track.query.model.OnTrackListener;
import com.amap.api.track.query.model.ParamErrorResponse;
import com.amap.api.track.query.model.QueryTerminalRequest;
import com.amap.api.track.query.model.QueryTerminalResponse;
import com.amap.api.track.query.model.QueryTrackResponse;
import com.suyong.contractmanager.MyApplication;
import com.suyong.contractmanager.R;
import com.suyong.contractmanager.ThreadPool.MyThreadPool;
import com.suyong.contractmanager.TrackServiceActivity;
import com.suyong.contractmanager.utils.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Timer;
import java.util.TimerTask;

public class TrackService extends Service {

    private Timer timer = new Timer();
    private TimerTask task;
    private MyApplication myApplication;
    private static final String CHANNEL_ID_SERVICE_RUNNING = "CHANNEL_ID_SERVICE_RUNNING";
    private ServiceBinder mBinder = new ServiceBinder();
    private long trackId;
    private  AMapTrackClient aMapTrackClient = null;
    private OnTrackLifecycleListener onTrackLifecycleListener = null;
    private  long terminalId;
    final long serviceId = 99058;  // 这里填入你创建的服务id
    final String terminalName = MyApplication.getCurrentIUser().getName();   // 唯一标识某个用户或某台设备的名称
    public TrackService() {
    }

    @Override
    public void onCreate(){
        Log.e("suyong","TrackService started");
        aMapTrackClient = new AMapTrackClient(MyApplication.getContext());
        initListener();
        myApplication = (MyApplication) getApplication();
        myApplication.Client = aMapTrackClient;
        // 在Android 6.0及以上系统，若定制手机使用到doze模式，请求将应用添加到白名单。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getApplicationContext().getPackageName();
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
            boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(packageName);
            //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
            if (!hasIgnored) {
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }

        aMapTrackClient.setInterval(5, 30);
        aMapTrackClient.setCacheSize(20);
        aMapTrackClient.setLocationMode(LocationMode.DEVICE_SENSORS);
        aMapTrackClient.setOnTrackListener(onTrackLifecycleListener);
        aMapTrackClient.queryTerminal(new QueryTerminalRequest(serviceId, terminalName), new OnTrackListener() {
            @Override
            public void onQueryTerminalCallback(QueryTerminalResponse queryTerminalResponse) {
                if (queryTerminalResponse.isSuccess()) {
                    if (queryTerminalResponse.getTid() <= 0) {
                        // terminal还不存在，先创建
                        aMapTrackClient.addTerminal(new AddTerminalRequest(terminalName, serviceId), new OnTrackListener() {

                            @Override
                            public void onQueryTerminalCallback(QueryTerminalResponse queryTerminalResponse) {

                            }

                            @Override
                            public void onCreateTerminalCallback(AddTerminalResponse addTerminalResponse) {
                                if (addTerminalResponse.isSuccess()) {
                                    // 创建完成，开启猎鹰服务
                                    terminalId = addTerminalResponse.getTid();
                                    myApplication.TerminalId = terminalId;
                                    myApplication.ServiceId = serviceId;
                                    TrackParam param = new TrackParam(serviceId, terminalId);
                                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        param.setNotification(createNotification());
                                    }else {
                                        startForeground(10, createNotification());
                                    }
                                    aMapTrackClient.startTrack(param, onTrackLifecycleListener);
                                } else {
                                    // 请求失败
                                    Toast.makeText(MyApplication.getContext(), "请求失败!，" + addTerminalResponse.getErrorMsg(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onDistanceCallback(DistanceResponse distanceResponse) {

                            }

                            @Override
                            public void onLatestPointCallback(LatestPointResponse latestPointResponse) {

                            }

                            @Override
                            public void onHistoryTrackCallback(HistoryTrackResponse historyTrackResponse) {

                            }

                            @Override
                            public void onQueryTrackCallback(QueryTrackResponse queryTrackResponse) {

                            }

                            @Override
                            public void onAddTrackCallback(AddTrackResponse addTrackResponse) {
                                if (addTrackResponse.isSuccess()) {
                                    trackId = addTrackResponse.getTrid();
                                }
                            }

                            @Override
                            public void onParamErrorCallback(ParamErrorResponse paramErrorResponse) {

                            }
                        });
                    } else {
                        // terminal已经存在，直接开启猎鹰服务
                        terminalId = queryTerminalResponse.getTid();
                        myApplication.TerminalId = terminalId;
                        myApplication.ServiceId = serviceId;
                        TrackParam param = new TrackParam(serviceId, terminalId);
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            param.setNotification(createNotification());
                        }else {
                            startForeground(10, createNotification());
                        }
                        aMapTrackClient.startTrack(param, onTrackLifecycleListener);
                    }
                } else {
                    // 请求失败
                    Toast.makeText(MyApplication.getContext(), "请求失败!!，" + queryTerminalResponse.getErrorMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCreateTerminalCallback(AddTerminalResponse addTerminalResponse) {

            }

            @Override
            public void onDistanceCallback(DistanceResponse distanceResponse) {

            }

            @Override
            public void onLatestPointCallback(LatestPointResponse latestPointResponse) {

            }

            @Override
            public void onHistoryTrackCallback(HistoryTrackResponse historyTrackResponse) {

            }

            @Override
            public void onQueryTrackCallback(QueryTrackResponse queryTrackResponse) {

            }

            @Override
            public void onAddTrackCallback(AddTrackResponse addTrackResponse) {
                if (addTrackResponse.isSuccess()) {
                    trackId = addTrackResponse.getTrid();
                }
            }

            @Override
            public void onParamErrorCallback(ParamErrorResponse paramErrorResponse) {

            }
        });
        super.onCreate();
    }

    private void initListener() {
        onTrackLifecycleListener = new OnTrackLifecycleListener() {
            @Override
            public void onBindServiceCallback(int i, String s) {

            }

            @Override
            public void onStartGatherCallback(int status, String msg) {
                if (status == ErrorCode.TrackListen.START_GATHER_SUCEE ||
                        status == ErrorCode.TrackListen.START_GATHER_ALREADY_STARTED) {
                    Toast.makeText(MyApplication.getContext(), "定位采集开启成功！", Toast.LENGTH_SHORT).show();
                    if(task==null) {
                        task = new TimerTask() {
                            public void run() {
                                //每次需要执行的代码放到这里面。
                                queryCurrentPostion();
                            }
                        };
                        timer.schedule(task, 0,5000);
                    }
                } else {
                    Toast.makeText(MyApplication.getContext(), "定位采集启动异常，" + msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onStartTrackCallback(int status, String msg) {
                if (status == ErrorCode.TrackListen.START_TRACK_SUCEE ||
                        status == ErrorCode.TrackListen.START_TRACK_SUCEE_NO_NETWORK ||
                        status == ErrorCode.TrackListen.START_TRACK_ALREADY_STARTED) {
                    // 服务启动成功，继续开启收集上报
                    aMapTrackClient.startGather(onTrackLifecycleListener);
                    SharedPreferences.Editor editor = myApplication.trackConf.edit();
                    myApplication.isStartService = true;
                    myApplication.isRunning = true;
                    editor.putBoolean("is_trace_started", true);
                    editor.apply();
                } else {
                    Toast.makeText(MyApplication.getContext(), "轨迹上报服务服务启动异常，" + msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onStopGatherCallback(int i, String s) {

            }

            @Override
            public void onStopTrackCallback(int i, String s) {
                if (i == ErrorCode.TrackListen.STOP_TRACK_SUCCE) {
                    Log.e("suyong", "Trackservice_destroy");
                    SharedPreferences.Editor editor = myApplication.trackConf.edit();
                    myApplication.isStartService = false;
                    myApplication.isRunning = false;
                    editor.putBoolean("is_trace_started", false);
                    editor.apply();
                    if(timer!=null) {
                        timer.cancel();
                    }
                    Toast.makeText(MyApplication.getContext(), "关闭成功，" + s, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }
    public class ServiceBinder extends Binder {
        public AMapTrackClient getClient(){
            return aMapTrackClient;
        }
        public Long getServiceId(){
            return serviceId;
        }
        public Long getTerminalId(){
            return terminalId;
        }
    }

    private Notification createNotification() {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_SERVICE_RUNNING, "app service", NotificationManager.IMPORTANCE_LOW);
            nm.createNotificationChannel(channel);
            builder = new Notification.Builder(getApplicationContext(), CHANNEL_ID_SERVICE_RUNNING);
        } else {
            builder = new Notification.Builder(getApplicationContext());
        }
        Intent nfIntent = new Intent(this, TrackServiceActivity.class);
        nfIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0))
                .setSmallIcon(R.drawable.bill)
                .setContentTitle("猎鹰sdk运行中")
                .setContentText("猎鹰sdk运行中");
        Notification notification = builder.build();
        return notification;
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        TrackParam param = new TrackParam(serviceId, terminalId);
        aMapTrackClient.stopTrack(param,onTrackLifecycleListener);
        Log.e("suyong","service_destroy");
    }

    //查询当前位置
    public void queryCurrentPostion(){
        if(!myApplication.isRunning){
            Toast.makeText(MyApplication.getContext(), "定位服务未开启!",Toast.LENGTH_SHORT).show();
            return;
        }
        aMapTrackClient.queryLatestPoint(new LatestPointRequest(serviceId ,terminalId), new OnTrackListener() {
            @Override
            public void onQueryTerminalCallback(QueryTerminalResponse queryTerminalResponse) {

            }

            @Override
            public void onCreateTerminalCallback(AddTerminalResponse addTerminalResponse) {

            }

            @Override
            public void onDistanceCallback(DistanceResponse distanceResponse) {

            }

            @Override
            public void onLatestPointCallback(LatestPointResponse latestPointResponse) {
                if (latestPointResponse.isSuccess()) {
                    Point point = latestPointResponse.getLatestPoint().getPoint();
                    // 查询实时位置成功，point为实时位置信息
                    //lat 维度  lng 经度
                    Log.e("postion:",point.getLat()+":"+point.getLng());
                    uploadCurrentPostion(point.getLat(),point.getLng());
                } else {
                    // 查询实时位置失败
                    Toast.makeText(MyApplication.getContext(), "查询当前位置失败!，", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onHistoryTrackCallback(HistoryTrackResponse historyTrackResponse) {

            }

            @Override
            public void onQueryTrackCallback(QueryTrackResponse queryTrackResponse) {

            }

            @Override
            public void onAddTrackCallback(AddTrackResponse addTrackResponse) {

            }

            @Override
            public void onParamErrorCallback(ParamErrorResponse paramErrorResponse) {

            }
        });
    }

    private void uploadCurrentPostion(final double lat, final double lng) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Connection con = DBUtils.getConnection();
                PreparedStatement ps = null;
                try{
                    String sql = "insert into position values(?,?,?,?)";
                    ps = con.prepareStatement(sql);
                    ps.setDouble(1,lng);
                    ps.setDouble(2,lat);
                    ps.setString(3,myApplication.getCurrentIUser().getName());
                    Log.e("suyong",""+System.currentTimeMillis());
                    ps.setLong(4,System.currentTimeMillis());
                    ps.executeUpdate();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    DBUtils.close(con,null,ps);
                }
            }
        };
        MyThreadPool.getInstance().submit(runnable);
    }

}
