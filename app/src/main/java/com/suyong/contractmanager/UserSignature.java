package com.suyong.contractmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.suyong.contractmanager.ThreadPool.MyThreadPool;
import com.suyong.contractmanager.utils.DBUtils;
import com.suyong.contractmanager.utils.ToastUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserSignature extends AppCompatActivity {

    private ImageButton btn_edit_signature;
    private EditText new_signature;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_signature);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RestoreView();
    }

    private void init() {
        btn_edit_signature = findViewById(R.id.btn_edit_signature);
        new_signature = findViewById(R.id.new_signature);
        btn_edit_signature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editSignature();
            }
        });
    }

    /**
     * 修改个性签名
     */
    private void editSignature() {
        final String newString = new_signature.getText().toString().trim();
        if(!TextUtils.isEmpty(newString)){
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Connection con = DBUtils.getConnection();
                    PreparedStatement ps = null;
                    try{
                        ps = con.prepareStatement("update admin set signature =? where username =?");
                        ps.setString(1,newString);
                        ps.setString(2,MyApplication.getCurrentIUser().getName());
                        ps.executeUpdate();
                        RestoreView();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(MyApplication.getContext(),"更新成功！");
                                finish();
                            }
                        });
                    }catch(Exception e){
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(MyApplication.getContext(),"更新失败！");
                                finish();
                            }
                        });
                    }finally{
                        DBUtils.close(con,null,ps);
                    }
                }
            };
            MyThreadPool.getInstance().submit(runnable);
        }else{
           finish();
        }
    }

    /**
     * 刷新界面
     */
    private void RestoreView() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Connection con = DBUtils.getConnection();
                PreparedStatement ps = null;
                ResultSet set = null;
                try{
                    String sql = "select password,nickname,sex,region,signature from admin where username =?";
                    ps = con.prepareStatement(sql);
                    ps.setString(1, MyApplication.getCurrentIUser().getName());
                    set = ps.executeQuery();
                    set.first();
                    final String Signature = set.getString("signature");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new_signature.setText(Signature.isEmpty()?"未设置个性签名":Signature);
                        }
                    });
                }catch (Exception e){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(MyApplication.getContext(),"更新用户信息失败！");
                        }
                    });
                }finally{
                    DBUtils.close(con,set,ps);
                }
            }
        };
        MyThreadPool.getInstance().submit(runnable);
    }

}
