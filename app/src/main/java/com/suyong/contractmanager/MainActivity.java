package com.suyong.contractmanager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.suyong.contractmanager.ThreadPool.MyThreadPool;
import com.suyong.contractmanager.adapter.MyFragmentPagerAdapter;
import com.suyong.contractmanager.fragment.BlankFragment;
import com.suyong.contractmanager.fragment.FunctionFragment;
import com.suyong.contractmanager.fragment.MoreOptionFragment;
import com.suyong.contractmanager.fragment.MyInfoFragment;
import com.suyong.contractmanager.utils.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 1;
    private int notificationId = 1;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private ViewPager mViewPager;
    private RadioGroup mTabRadioGroup;

    private List<Fragment> mFragments;
    private FragmentPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        check();
        initView();
        checkOutDateContract();
    }
    private Map<String, String> permissionHintMap = new HashMap<>();
    private void check() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Map<String, String> requiredPermissions = new HashMap<>();
                requiredPermissions.put(Manifest.permission.ACCESS_FINE_LOCATION, "定位");
                requiredPermissions.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, "存储");
                requiredPermissions.put(Manifest.permission.READ_PHONE_STATE, "读取设备信息");
                for (String permission : requiredPermissions.keySet()) {
                    if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                        permissionHintMap.put(permission, requiredPermissions.get(permission));
                    }
                }
                if (!permissionHintMap.isEmpty()) {
                    requestPermissions(permissionHintMap.keySet().toArray(new String[0]), REQUEST_CODE_PERMISSION);
                }
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<String> failPermissions = new LinkedList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                failPermissions.add(permissions[i]);
            }
        }
        if (!failPermissions.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String permission : failPermissions) {
                sb.append(permissionHintMap.get(permission)).append("、");
            }
            sb.deleteCharAt(sb.length() - 1);
            String hint = "未授予必要权限: " +
                    sb.toString() +
                    "，请前往设置页面开启权限";
            new AlertDialog.Builder(this)
                    .setMessage(hint)
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    }).setPositiveButton("设置", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    System.exit(0);
                }
            }).show();
        }
    }
    private void checkOutDateContract() {
        final Date date = new Date();
        final String now = formatter.format(date);
        Log.d("MainActivity", "yyy"+now);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Connection con = DBUtils.getConnection();
                PreparedStatement ps = null;
                ResultSet set = null;
                try{
                    ps = con.prepareStatement("select * from contract where end_time < ?");
                    ps.setDate(1,  java.sql.Date.valueOf(now));
                    set = ps.executeQuery();
                    while(set.next()){
                        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                        String channelId = "contractId";
                        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
                         NotificationChannel channel = new NotificationChannel(channelId,"contractId",NotificationManager.IMPORTANCE_DEFAULT);
                        manager.createNotificationChannel(channel);
                        }
                        Intent intent = new Intent(MainActivity.this,outDateContract.class);
                        intent.putExtra("id",set.getString("id"));
                       PendingIntent pi = PendingIntent.getActivity(MainActivity.this,notificationId,intent,0);
                        Notification notification = new NotificationCompat.Builder(MainActivity.this,channelId)
                        .setContentTitle("合同过期提醒")
                         .setContentText("过期合同编号:"+set.getString("id")+",详情请点击!")
                         .setDefaults(NotificationCompat.DEFAULT_ALL)
                          .setPriority(NotificationCompat.PRIORITY_MAX)
                          .setContentIntent(pi)
                         .setAutoCancel(true)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.contract_manager)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.contract_manager))
                        .build();
                        manager.notify(notificationId++,notification);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    DBUtils.close(con,set,ps);
                }
            }
        };
        MyThreadPool.getInstance().submit(runnable);
    }

    private void initView() {
        // find view
        mViewPager = findViewById(R.id.fragment_vp);
        mTabRadioGroup = findViewById(R.id.tabs_rg);
        // init fragment
        mFragments = new ArrayList<>(3);
        mFragments.add(new MyInfoFragment());
        mFragments.add(new FunctionFragment());
        mFragments.add(new MoreOptionFragment());
        // init view pager
        mAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), mFragments);
        mViewPager.setAdapter(mAdapter);
        // register listener
        mViewPager.addOnPageChangeListener(mPageChangeListener);
        mTabRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewPager.removeOnPageChangeListener(mPageChangeListener);
    }

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            RadioButton radioButton = (RadioButton) mTabRadioGroup.getChildAt(position);
            radioButton.setChecked(true);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private RadioGroup.OnCheckedChangeListener mOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            for (int i = 0; i < group.getChildCount(); i++) {
                if (group.getChildAt(i).getId() == checkedId) {
                    mViewPager.setCurrentItem(i);
                    return;
                }
            }
        }
    };


}
