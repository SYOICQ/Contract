package com.suyong.contractmanager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.drm.DrmStore;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.suyong.contractmanager.ThreadPool.MyThreadPool;
import com.suyong.contractmanager.pojo.Contract;
import com.suyong.contractmanager.utils.DBUtils;
import com.suyong.contractmanager.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ContractDetail extends AppCompatActivity {

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private TextView openEdit;
    private EditText id;
    private EditText name;
    private TextView partyA;
    private TextView partyB;
    private EditText status;
    private TextView start_time;
    private TextView end_time;
    private EditText money;
    private Button btn_confirm;

    Calendar calendar= Calendar.getInstance(Locale.CHINA);

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void messageEventBus(Contract contract){
        id.setText(contract.getId());
        name.setText(contract.getName());
        partyA.setText(contract.getPartyA());
        partyB.setText(contract.getPartyB());
        status.setText(contract.getStatus());
        money.setText(contract.getMoney().toString());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        start_time.setText(formatter.format(contract.getStart_time()));
        end_time.setText(formatter.format(contract.getEnd_time()));
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contract_detail);
        init();
        EventBus.getDefault().register(this);//注册
    }

    private void init() {
        id = findViewById(R.id.detail_id);
        name = findViewById(R.id.detail_name);
        partyA = findViewById(R.id.detail_partyA);
        partyA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQueryPartyA();
            }
        });
        partyB = findViewById(R.id.detail_partyB);
        partyB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQueryPartyB();
            }
        });
        status = findViewById(R.id.detail_contract_status);
        start_time = findViewById(R.id.detail_start_time);
        start_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(ContractDetail.this,0,(TextView) v,calendar);
            }
        });
        end_time = findViewById(R.id.detail_end_time);
        end_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(ContractDetail.this,0,(TextView) v,calendar);
            }
        });
        money = findViewById(R.id.detail_money);
        btn_confirm = findViewById(R.id.btn_edit_detailcontract);
        openEdit = findViewById(R.id.edit_contract_detail);
        openEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name.setEnabled(true);
                partyA.setEnabled(true);
                partyB.setEnabled(true);
                status.setEnabled(true);
                start_time.setEnabled(true);
                end_time.setEnabled(true);
                money.setEnabled(true);
                btn_confirm.setEnabled(true);
            }
        });
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editContract();
            }
        });

    }

    private void showQueryPartyA() {
        final View view = getLayoutInflater().inflate(R.layout.contract_customer_info,null);
        final TextView t1 = view.findViewById(R.id.customer_id);
        final TextView t2 = view.findViewById(R.id.customer_name);
        final TextView t3 = view.findViewById(R.id.customer_sex);
        final TextView t4 = view.findViewById(R.id.customer_telephone);
        final TextView t5 = view.findViewById(R.id.customer_company);
        final TextView t6 = view.findViewById(R.id.customer_email);
        final TextView t7 = view.findViewById(R.id.customer_address);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Connection con = DBUtils.getConnection();
                PreparedStatement ps = null;
                ResultSet set = null;
                try {
                    String sql = "select * from customer where id = ?";
                    ps = con.prepareStatement(sql);
                    ps.setString(1,partyA.getText().toString().trim());
                    set = ps.executeQuery();
                    set.first();
                    final String s1 = set.getString("id");
                    final String s2 = set.getString("name");
                    final String s3 = set.getString("sex");
                    final String s4 = set.getString("phone");
                    final String s5 = set.getString("company");
                    final String s6 = set.getString("email");
                    final String s7 = set.getString("address");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            t1.setText(s1);t2.setText(s2);t3.setText(s3);t4.setText(s4);
                            t5.setText(s5);t6.setText(s6);t7.setText(s7);
                            new AlertDialog.Builder(ContractDetail.this)
                                    .setTitle("甲方:")
                                    .setView(view)
                                    .setIcon(R.drawable.add_customer_icon)
                                    .setPositiveButton("确定", null)
                                    .show();
                        }
                    });
                }catch(Exception e){
                    e.printStackTrace();
                }finally{
                    DBUtils.close(con,set,ps);
                }
            }
        };
        MyThreadPool.getInstance().submit(runnable);
    }
    private void showQueryPartyB() {
        final View view = getLayoutInflater().inflate(R.layout.contract_customer_info,null);
        final TextView t1 = view.findViewById(R.id.customer_id);
        final TextView t2 = view.findViewById(R.id.customer_name);
        final TextView t3 = view.findViewById(R.id.customer_sex);
        final TextView t4 = view.findViewById(R.id.customer_telephone);
        final TextView t5 = view.findViewById(R.id.customer_company);
        final TextView t6 = view.findViewById(R.id.customer_email);
        final TextView t7 = view.findViewById(R.id.customer_address);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Connection con = DBUtils.getConnection();
                PreparedStatement ps = null;
                ResultSet set = null;
                try {
                    String sql = "select * from customer where id = ?";
                    ps = con.prepareStatement(sql);
                    ps.setString(1,partyB.getText().toString().trim());
                    set = ps.executeQuery();
                    set.first();
                    final String s1 = set.getString("id");
                    final String s2 = set.getString("name");
                    final String s3 = set.getString("sex");
                    final String s4 = set.getString("phone");
                    final String s5 = set.getString("company");
                    final String s6 = set.getString("email");
                    final String s7 = set.getString("address");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            t1.setText(s1);t2.setText(s2);t3.setText(s3);t4.setText(s4);
                            t5.setText(s5);t6.setText(s6);t7.setText(s7);
                            new AlertDialog.Builder(ContractDetail.this)
                                    .setTitle("乙方:")
                                    .setView(view)
                                    .setIcon(R.drawable.add_customer_icon)
                                    .setPositiveButton("确定", null)
                                    .show();
                        }
                    });
                }catch(Exception e){
                    e.printStackTrace();
                }finally{
                    DBUtils.close(con,set,ps);
                }
            }
        };
        MyThreadPool.getInstance().submit(runnable);
    }

    private void editContract() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Connection con = DBUtils.getConnection();
                PreparedStatement ps = null;
                try{
                    if(TextUtils.isEmpty(name.getText().toString().trim())||
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
                                ToastUtil.showToast(ContractDetail.this,"都是必填项,请全部填写!");
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
                                ToastUtil.showToast(ContractDetail.this,"开始日期必须小于结束日期!");
                            }
                        });
                        return;
                    }

                    //更新
                    String sql = "update contract set name =?,partyA=?,partyB=?,status=?,start_time=?,end_time=?,money=? where id=?";
                    ps = con.prepareStatement(sql);
                    ps.setString(1,name.getText().toString().trim());
                    ps.setString(2,partyA.getText().toString().trim());
                    ps.setString(3,partyB.getText().toString().trim());
                    ps.setString(4,status.getText().toString().trim());
                    ps.setDate(5, Date.valueOf(start_time.getText().toString().trim()));
                    ps.setDate(6, Date.valueOf(end_time.getText().toString().trim()));
                    ps.setBigDecimal(7,new BigDecimal(money.getText().toString().trim()));
                    ps.setString(8,id.getText().toString().trim());
                    ps.executeUpdate();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(MyApplication.getContext(),"修改成功!");
                        }
                    });
                }catch(Exception e){
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(MyApplication.getContext(),"修改失败!");
                        }
                    });
                }finally{
                    DBUtils.close(con,null,ps);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                }
            }
        };
        MyThreadPool.getInstance().submit(runnable);
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

        @Override
    public void onDestroy(){
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
