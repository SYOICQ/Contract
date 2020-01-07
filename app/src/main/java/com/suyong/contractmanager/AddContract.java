package com.suyong.contractmanager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.suyong.contractmanager.ThreadPool.MyThreadPool;
import com.suyong.contractmanager.utils.DBUtils;
import com.suyong.contractmanager.utils.ToastUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddContract extends AppCompatActivity {

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private AlertDialog alertDialog1; //信息框
    Calendar calendar= Calendar.getInstance(Locale.CHINA);
    private EditText id;
    private EditText name;
    private TextView partyA;
    private TextView partyB;
    private EditText status;
    private TextView start_time;
    private TextView end_time;
    private EditText money;
    private Button btn_sumbit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contract);
        init();
    }

    private void init() {
        id = findViewById(R.id.contract_id);
        name = findViewById(R.id.contract_name);
        partyA = findViewById(R.id.contract_partyA);
        partyA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQueryCustmoer((TextView) v);
            }
        });
        partyB = findViewById(R.id.contract_partyB);
        partyB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQueryCustmoer((TextView) v);
            }
        });
        status = findViewById(R.id.contract_status);
        start_time = findViewById(R.id.contract_start_time);
        start_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(AddContract.this,0,(TextView) v,calendar);
            }
        });
        end_time = findViewById(R.id.contract_end_time);
        end_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(AddContract.this,0,(TextView) v,calendar);
            }
        });
        money = findViewById(R.id.contract_money);
        btn_sumbit = findViewById(R.id.btn_add_contract);
        btn_sumbit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContract();
            }
        });
    }

    private void addContract() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Connection con = DBUtils.getConnection();
                PreparedStatement ps = null;
                ResultSet set =null;
                try{
                    if(TextUtils.isEmpty(id.getText().toString().trim())||
                            TextUtils.isEmpty(name.getText().toString().trim())||
                            TextUtils.isEmpty(partyA.getText().toString().trim())||
                            TextUtils.isEmpty(partyB.getText().toString().trim())||
                            TextUtils.isEmpty(status.getText().toString().trim())||
                            TextUtils.isEmpty(start_time.getText().toString().trim())||
                            TextUtils.isEmpty(end_time.getText().toString().trim())||
                                    TextUtils.isEmpty(money.getText().toString().trim())

                    ){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(AddContract.this,"都是必填项,请全部填写!");
                            }
                        });
                        return;
                    }

                    java.util.Date startTime = formatter.parse(start_time.getText().toString().trim());
                    java.util.Date endTime = formatter.parse(end_time.getText().toString().trim());
                    if(startTime.after(endTime)){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(AddContract.this,"开始日期必须小于结束日期!");
                            }
                        });
                        return;
                    }


                    String str = "select name from contract where id = ?";
                    ps = con.prepareStatement(str);
                    ps.setString(1,id.getText().toString().trim());
                    set = ps.executeQuery();
                    if(set.first()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(AddContract.this,"合同编号已存在!");
                            }
                        });
                        return;
                    }
                    //更新
                    String sql = "insert into contract values(?,?,?,?,?,?,?,?)";
                    ps = con.prepareStatement(sql);
                    ps.setString(1,id.getText().toString().trim());
                    ps.setString(2,name.getText().toString().trim());
                    ps.setString(3,partyA.getText().toString().trim());
                    ps.setString(4,partyB.getText().toString().trim());
                    ps.setString(5,status.getText().toString().trim());
                    ps.setDate(6, Date.valueOf(start_time.getText().toString().trim()));
                    ps.setDate(7, Date.valueOf(end_time.getText().toString().trim()));
                    ps.setBigDecimal(8,new BigDecimal(money.getText().toString().trim()));
                    ps.executeUpdate();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(MyApplication.getContext(),"添加成功!");
                            finish();
                        }
                    });
                }catch(Exception e){
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(MyApplication.getContext(),"添加失败!");
                        }
                    });
                }finally{
                    DBUtils.close(con,set,ps);
                }
            }
        };
        MyThreadPool.getInstance().submit(runnable);
    }

    private void showQueryCustmoer(final TextView v) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Connection con = DBUtils.getConnection();
                PreparedStatement ps = null;
                ResultSet set = null;
                try {
                    String str = "select count(*)  as NumberOfProducts from customer";
                    ps = con.prepareStatement(str);
                    set = ps.executeQuery();
                    set.next();
                    int rowCount = set.getInt("NumberOfProducts");
                    Log.d("AddContract", "rowCount:" + rowCount);
                    final String [] data = new String[rowCount];
                    String sql = "select id,name from customer";
                    ps = con.prepareStatement(sql);
                    set = ps.executeQuery();
                    if(set.first()){
                        int k =0;
                        do{
                            Log.d("AddContract", "ccca!"+"[" + set.getString("id") + "]" + set.getString("name"));
                            data[k] = "["+set.getString("id")+"]"+set.getString("name");
                            k++;
                        }while(set.next());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showList(data,v);
                            }
                        });
                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(AddContract.this,"暂无客户，请先添加!");
                                finish();
                            }
                        });
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
    public void showList(final String [] items,final TextView v){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(AddContract.this);
        alertBuilder.setTitle("客户:");
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Toast.makeText(AddContract.this, items[i], Toast.LENGTH_SHORT).show();
                String str = items[i].substring(1,items[i].lastIndexOf("]"));
                v.setText(str);
                alertDialog1.dismiss();
            }
        });
        alertDialog1 = alertBuilder.create();
        alertDialog1.show();
    }

    public static void showDatePickerDialog(Activity activity, int themeResId, final TextView tv, Calendar calendar) {
        // 直接创建一个DatePickerDialog对话框实例，并将它显示出来
        new DatePickerDialog(activity, themeResId, new DatePickerDialog.OnDateSetListener() {
            // 绑定监听器(How the parent is notified that the date is set.)
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // 此处得到选择的时间，可以进行你想要的操作
                tv.setText( year + "-" + (monthOfYear + 1) + "-" + dayOfMonth );
            }
        }
                // 设置初始日期
                , calendar.get(Calendar.YEAR)
                , calendar.get(Calendar.MONTH)
                , calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
}
