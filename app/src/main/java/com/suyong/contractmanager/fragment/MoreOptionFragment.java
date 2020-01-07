package com.suyong.contractmanager.fragment;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.maps.model.LatLng;
import com.amap.api.track.query.entity.HistoryTrack;
import com.amap.api.track.query.entity.Point;
import com.amap.api.track.query.model.AddTerminalResponse;
import com.amap.api.track.query.model.AddTrackResponse;
import com.amap.api.track.query.model.DistanceResponse;
import com.amap.api.track.query.model.HistoryTrackRequest;
import com.amap.api.track.query.model.HistoryTrackResponse;
import com.amap.api.track.query.model.LatestPointRequest;
import com.amap.api.track.query.model.LatestPointResponse;
import com.amap.api.track.query.model.OnTrackListener;
import com.amap.api.track.query.model.ParamErrorResponse;
import com.amap.api.track.query.model.QueryTerminalResponse;
import com.amap.api.track.query.model.QueryTrackResponse;
import com.suyong.contractmanager.ContractManager;
import com.suyong.contractmanager.CustomerManagerActivity;
import com.suyong.contractmanager.LibraryActivity;
import com.suyong.contractmanager.MyApplication;
import com.suyong.contractmanager.R;
import com.suyong.contractmanager.TicketManager;
import com.suyong.contractmanager.TrackServiceActivity;
import com.suyong.contractmanager.adapter.FunctionAdapter;
import com.suyong.contractmanager.pojo.Function;
import com.suyong.contractmanager.service.TrackService;
import com.suyong.contractmanager.utils.ProgressDialogUtil;
import com.suyong.contractmanager.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class MoreOptionFragment extends Fragment {

    private MyApplication myApplication;
    private TrackService.ServiceBinder mTrackService ;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mTrackService = ( TrackService.ServiceBinder)service;
            dialog.dismiss();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private ProgressDialog dialog;
    //private Button query;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Switch switch1;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootView = inflater.from(getContext()).inflate(R.layout.moreoption,container,false);
        myApplication = (MyApplication) getActivity().getApplication();
        dialog = ProgressDialogUtil.newInstatnce(getActivity(),"提示","开启中,耐心等待...");
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        switch1 = rootView.findViewById(R.id.open_postion);
//        query = rootView.findViewById(R.id.query);
//        query.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//               // queryHistroy();
//                //queryCurrentPostion();
//                queryDataBase();
//            }
//        });
        if(myApplication.isStartService&&myApplication.isRunning) {
            switch1.setChecked(true);
        }else if(myApplication.isStartService&&!myApplication.isRunning){
            switch1.setChecked(true);
            openLocationService();
        }
        initView();
        return rootView;
    }

    private void queryDataBase() {
        startActivity(new Intent(getActivity(),TrackServiceActivity.class));
    }

    private void openLocationService() {
        dialog.show();
        Intent intent = new Intent(getActivity(),TrackService.class);
        getActivity().bindService(intent,connection, Context.BIND_AUTO_CREATE);
    }

    private void closeLocationService() {
        getActivity().unbindService(connection);
    }

    private void initView() {
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    //选中时 do some thing
                    editor = pref.edit();
                    editor.putBoolean("postion_flag",true);
                    editor.apply();
                    //开启定位服务
                    openLocationService();
                } else {
                    //非选中时 do some thing
                    editor = pref.edit();
                    editor.putBoolean("postion_flag",false);
                    editor.apply();
                    //关闭定位服务
                    closeLocationService();
                }

            }
        });
    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);
    }

    //查询当前位置
   public void queryCurrentPostion(){
       if(!myApplication.isRunning){
           Toast.makeText(MyApplication.getContext(), "定位服务未开启!",Toast.LENGTH_SHORT).show();
           return;
       }
       mTrackService.getClient().queryLatestPoint(new LatestPointRequest(mTrackService.getServiceId(), mTrackService.getTerminalId()), new OnTrackListener() {
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
                mTrackService.getServiceId(),
                mTrackService.getTerminalId(),
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
        mTrackService.getClient().queryHistoryTrack(historyTrackRequest, new OnTrackListener() {

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
                    List<LatLng> point = new ArrayList<>();
                    for(Point p :points){
                        point.add(new LatLng(p.getLat(),p.getLng()));
                    }
                    EventBus.getDefault().postSticky(point);
                    startActivity(new Intent(getActivity(), TrackServiceActivity.class));
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
