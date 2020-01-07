package com.suyong.contractmanager;

import android.os.Bundle;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.mysql.jdbc.interceptors.ResultSetScannerInterceptor;
import com.suyong.contractmanager.Interfaces.loginInterface;
import com.suyong.contractmanager.Interfaces.registerInterface;
import com.suyong.contractmanager.ThreadPool.MyThreadPool;
import com.suyong.contractmanager.utils.DBUtils;
import com.suyong.contractmanager.utils.StorgeUtils;
import com.suyong.contractmanager.utils.ToastUtil;


public class LoginActivity extends AppCompatActivity implements OnClickListener{

    private EditText username;
    private EditText password;
    private Button login;
    private TextView edit_password;

    private void initView(){
        edit_password = findViewById(R.id.edit_password);
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        login = (Button)findViewById(R.id.login);
        login.setOnClickListener(this);
        edit_password.setOnClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        StorgeUtils util = new StorgeUtils(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        switch (arg0.getId()){
            case R.id.login :
                login(new loginInterface() {
                    @Override
                    public void Sucess(String username,String password) {
                        MyApplication.getCurrentIUser().setName(username);
                        MyApplication.getCurrentIUser().setPassword(password);
                        startActivity(new Intent(LoginActivity.this,MainActivity.class));
                        finish();
                    }

                    @Override
                    public void failed() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(LoginActivity.this,"账号密码错误或不存在");
                            }
                        });

                    }
                });
                break;
            case R.id.edit_password:
                editPassword();
                break;
        }
    }

    /**
     * 修改密码
     */
    private void editPassword() {
        View view = getLayoutInflater().inflate(R.layout.edit_passwd1,null);
        final EditText username = view.findViewById(R.id.username);
        final EditText newPassword = view.findViewById(R.id.newPassword);

        new AlertDialog.Builder(this)
                .setTitle("修改密码:")
                .setView(view)
                .setIcon(R.drawable.edit_password)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                Connection con = DBUtils.getConnection();
                                PreparedStatement ps = null;
                                try{
                                    ps = con.prepareStatement("update admin set password =? where username =?");
                                    ps.setString(1,newPassword.getText().toString());
                                    ps.setString(2,username.getText().toString());
                                    ps.executeUpdate();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastUtil.showToast(MyApplication.getContext(),"修改密码成功！");
                                        }
                                    });
                                }catch(Exception e){
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastUtil.showToast(MyApplication.getContext(),"修改密码失败！");
                                        }
                                    });
                                }finally{
                                    DBUtils.close(con,null,ps);
                                }
                            }
                        };
                        MyThreadPool.getInstance().submit(runnable);
                    }
                }).setNegativeButton("取消",null)
                .show();
    }


    /**
     * 登陆
     * @param loginInterface
     */
    private void login(final loginInterface loginInterface) {
        // TODO Auto-generated method stub
        if (!TextUtils.isEmpty(username.getText().toString().trim()) && !TextUtils.isEmpty(password.getText().toString().trim())) {
            final String name = username.getText().toString().trim();
            final String pwd= password.getText().toString().trim();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Connection con = DBUtils.getConnection();
                    PreparedStatement ps = null;
                    ResultSet set = null;
                    try {
                        String sql = "select username,password from admin where username =? and password = ?";
                        ps = con.prepareStatement(sql);
                        ps.setString(1,name);
                        ps.setString(2,pwd);
                        set = ps.executeQuery();
                        if(set.first()){
                            loginInterface.Sucess(name,pwd);
                        }else {
                            loginInterface.failed();
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }finally{
                        DBUtils.close(con,set,ps);
                    }
                }
            };
            MyThreadPool.getInstance().submit(runnable);
        }
    }

    /**
     * 注册
     * @param listener
     */
    private void signup(final registerInterface listener) {
        // TODO Auto-generated method stub
        if(!TextUtils.isEmpty(username.getText().toString().trim())&& !TextUtils.isEmpty(password.getText().toString().trim())) {
            final String name = username.getText().toString().trim();
            final String pwd = password.getText().toString().trim();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Connection con = DBUtils.getConnection();
                    PreparedStatement ps = null;
                    ResultSet set = null;
                    try {
                        //查询该账户是否已被注册
                        String sql = "select * from where username=?";
                        ps = con.prepareStatement(sql);
                        ps.setString(1,name);
                        set = ps.executeQuery();
                        if(set.first()){
                            listener.failed("用户已被注册");
                            return;
                        }
                        //开始注册
                        ps = con.prepareStatement("insert into admin values(?,?)");
                        ps.setString(1,name);
                        ps.setString(2,pwd);
                        ps.executeUpdate();
                        listener.Sucess();
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.failed("注册失败");
                    } finally {
                        DBUtils.close(con,set,ps);
                    }
                }
            };
            MyThreadPool.getInstance().submit(runnable);
        }
    }
}

