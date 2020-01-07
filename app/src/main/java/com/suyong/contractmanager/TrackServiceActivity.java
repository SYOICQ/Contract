package com.suyong.contractmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.track.query.entity.HistoryTrack;
import com.amap.api.track.query.entity.Point;
import com.amap.api.track.query.model.AddTerminalResponse;
import com.amap.api.track.query.model.AddTrackResponse;
import com.amap.api.track.query.model.DistanceResponse;
import com.amap.api.track.query.model.HistoryTrackRequest;
import com.amap.api.track.query.model.HistoryTrackResponse;
import com.amap.api.track.query.model.LatestPointResponse;
import com.amap.api.track.query.model.OnTrackListener;
import com.amap.api.track.query.model.ParamErrorResponse;
import com.amap.api.track.query.model.QueryTerminalResponse;
import com.amap.api.track.query.model.QueryTrackResponse;
import com.suyong.contractmanager.Interfaces.registerInterface;
import com.suyong.contractmanager.ThreadPool.MyThreadPool;
import com.suyong.contractmanager.utils.DBUtils;
import com.suyong.contractmanager.utils.ProgressDialogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TrackServiceActivity extends AppCompatActivity {

    private MyApplication myApplication;
    private ProgressDialog dialog;
    private Date currentTime;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    MapView mMapView = null;
    AMap aMap = null;
    MyLocationStyle myLocationStyle = null;
    private List<LatLng> data = new ArrayList<>();

   /* @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void messageEventBus(List<LatLng> data){
        Log.e("suyong","messageEventBus:"+data.size());
      if(data.size()>0) {
            Log.e("suyong","postion:"+data.size());
            this.data=data;
        }
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("suyong","onCreate:");
        setContentView(R.layout.activity_track_service);
        myApplication = (MyApplication)getApplication();
        dialog = ProgressDialogUtil.newInstatnce(this,"加载中!","请耐心等待!");
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        aMap.showIndoorMap(true);
       // EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        queryDataBase(new registerInterface() {
            @Override
            public void Sucess() {
                aMap.addMarker(new MarkerOptions().position(data.get(0)).title("end").snippet("终点"));
                aMap.addMarker(new MarkerOptions().position(data.get(data.size() - 1)).title("start").snippet("起点"));
                aMap.addPolyline(new PolylineOptions().
                        addAll(data).width(10).color(Color.argb(255, 0, 221, 238)));
                showMyLocation();
                dialog.dismiss();
                Log.e("suyong","onResume:"+data.size());
            }

            @Override
            public void failed(String returnCode) {
                showMyLocation();
                dialog.dismiss();
            }
        });
//        queryHistroy();
//        aMap.addMarker(new MarkerOptions().position(data.get(0)).title("end").snippet("终点"));
//        aMap.addMarker(new MarkerOptions().position(data.get(data.size() - 1)).title("start").snippet("起点"));
//        aMap.addPolyline(new PolylineOptions().
//                addAll(data).width(10).color(Color.argb(255, 0, 221, 238)));
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    private void queryDataBase(final registerInterface listener) {
        data.clear();
        dialog.show();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Connection con = DBUtils.getConnection();
                PreparedStatement ps = null;
                ResultSet set = null;
                try{
                    currentTime = new Date();
                    String now = formatter.format(currentTime);
                    Date pretime = formatter.parse(now);
                    Long time = pretime.getTime() - 43200000;
                    String sql = "select * from position where time >? and user =?";
                    ps = con.prepareStatement(sql);
                    ps.setLong(1,time);
                    ps.setString(2,MyApplication.getCurrentIUser().getName());
                    set = ps.executeQuery();
                    set.first();
                    while(set.next()){
                        //纬度  经度
                        data.add(new LatLng(set.getLong("lat"),set.getLong("lng")));
                    }
                    listener.Sucess();
                }catch(Exception e){
                    e.printStackTrace();
                    DBUtils.close(con,set,ps);
                    listener.failed("error");
                }
            }
        };
        MyThreadPool.getInstance().submit(runnable);
    }

    private void showMyLocation() {
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE);//连续定位、且将视角移动到地图中心点，地图依照设备方向旋转，定位点会跟随设备移动。（1秒1次定位）
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        //EventBus.getDefault().unregister(this);
        //data.clear();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }
    //查询历史轨迹
    public void queryHistroy(){
        // 搜索最近12小时以内上报的轨迹
        if(!myApplication.isRunning){
            Toast.makeText(MyApplication.getContext(), "定位服务未开启!",Toast.LENGTH_SHORT).show();
            return;
        }
//        Log.e("suyong","query:" +mTrackService.getServiceId()+":"+
//                mTrackService.getTerminalId());
        HistoryTrackRequest historyTrackRequest = new HistoryTrackRequest(
                myApplication.ServiceId,
                myApplication.TerminalId,
                System.currentTimeMillis() - 12 * 60 * 60 * 1000,
                System.currentTimeMillis(),
                1,      // 不绑路
                0,      // 不做距离补偿
                5000,   // 距离补偿阈值，只有超过5km的点才启用距离补偿
                0,  // 由旧到新排序
                1,  // 返回第1页数据
                100,    // 一页不超过100条
                ""  // 暂未实现，该参数无意义，请留空
        );
        myApplication.Client.queryHistoryTrack(historyTrackRequest, new OnTrackListener() {

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

            }

            @Override
            public void onHistoryTrackCallback(HistoryTrackResponse historyTrackResponse) {
                if (historyTrackResponse.isSuccess()) {
                    // historyTrack中包含终端轨迹信息
                    HistoryTrack historyTrack = historyTrackResponse.getHistoryTrack();
                    List<Point> points = historyTrack.getPoints();
                    Log.e("suyong",":"+points.size());
                    for(Point p :points){
                        data.add(new LatLng(p.getLat(),p.getLng()));
                    }

                } else {
                    // 查询失败
                    Toast.makeText(MyApplication.getContext(), "查询历史轨迹失败!，", Toast.LENGTH_SHORT).show();
                }
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
}
