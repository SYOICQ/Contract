package com.suyong.contractmanager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.mysql.jdbc.StringUtils;
import com.suyong.contractmanager.Interfaces.TextviewListener;
import com.suyong.contractmanager.Interfaces.registerInterface;
import com.suyong.contractmanager.ThreadPool.MyThreadPool;
import com.suyong.contractmanager.pojo.Customer;
import com.suyong.contractmanager.pojo.MessageEvent;
import com.suyong.contractmanager.utils.DBUtils;
import com.suyong.contractmanager.utils.MyTextListener;
import com.suyong.contractmanager.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.w3c.dom.Text;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomerManagerActivity extends AppCompatActivity {

    private Handler handler = new Handler(Looper.myLooper());
   private TableLayout tableLayout;
   private String []rows={"用户编号","姓名","性别","电话","公司名称","邮箱","地址"};
   private ProgressBar downlaodProcess ;
   private ImageButton btn_add_customer;
   private ImageButton btn_search_customer;
   private EditText data_for_search;

   @Subscribe(threadMode = ThreadMode.MAIN)
   public void onReceive(MessageEvent messageEvent){
       downlaodProcess.setVisibility(View.GONE);
       String data = messageEvent.getMessage();
       Log.d("CustomerManagerActivity", data);
       String []d = data.split(",");
       TableRow row = new TableRow(this);
       for(int i=0;i<d.length;i++){
           View view = LayoutInflater.from(this).inflate(R.layout.text,null,false);
           TextView t = view.findViewById(R.id.title_t);
           t.setText(d[i]);
           row.addView(view);
       }
       Customer customer = new Customer(d[0],d[1],d[2],d[3],d[4],d[5],d[6]);
       row.setOnClickListener(new myListener(customer));
       row.setOnLongClickListener(new MyLonglistener(customer));
       tableLayout.addView(row);
   }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_manager_layout);
        init();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        queryCustomerInfo(null);
    }
    private void init() {
        data_for_search = findViewById(R.id.data_for_search);
        btn_add_customer = findViewById(R.id.btn_add_customer);
        btn_add_customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCustomer();
            }
        });
        btn_search_customer = findViewById(R.id.btn_search_customer);
        btn_search_customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = data_for_search.getText().toString().trim();
                if(!TextUtils.isEmpty(s)){
                    queryCustomerInfo(s);
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(CustomerManagerActivity.this,"编号为空！");
                        }
                    });
                }
            }
        });
        downlaodProcess = findViewById(R.id.query_process);
        tableLayout = findViewById(R.id.table1);
        tableLayout.setStretchAllColumns(true);
        //设置分割线为中间显示
        tableLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        addTitle();
    }


    /**
     * 添加客户
     */
    private void addCustomer() {
        View view = getLayoutInflater().inflate(R.layout.add_customer,null);
        final EditText t1 = view.findViewById(R.id.customer_id);
        final EditText t2 = view.findViewById(R.id.customer_name);
        final EditText t3 = view.findViewById(R.id.customer_sex);
        final EditText t4 = view.findViewById(R.id.customer_telephone);
        final EditText t5 = view.findViewById(R.id.customer_company);
        final EditText t6 = view.findViewById(R.id.customer_email);
        final EditText t7 = view.findViewById(R.id.customer_address);
        final TextView t1_flag = view.findViewById(R.id.customer_id_flag);
        final TextView t2_flag = view.findViewById(R.id.customer_name_flag);
        final TextView t3_flag = view.findViewById(R.id.customer_sex_flag);
        final TextView t4_flag = view.findViewById(R.id.customer_telephone_flag);
        final TextView t5_flag = view.findViewById(R.id.customer_company_flag);
        final TextView t6_flag = view.findViewById(R.id.customer_email_flag);
        final TextView t7_flag = view.findViewById(R.id.customer_address_flag);
        t1.addTextChangedListener(new MyTextListener(new TextviewListener() {
            @Override
            public void before() {
                t1_flag.setText("必填项");
            }

            @Override
            public void after(String msg) {
                if(checkName(msg)){
                    t1_flag.setText("必须为纯数字!");
                }else if(TextUtils.isEmpty(msg)){
                    t1_flag.setText("不能为空!");
                } else{
                    t1_flag.setText("");
                }
            }
        }));
        t2.addTextChangedListener(new MyTextListener(new TextviewListener() {
            @Override
            public void before() {
                t2_flag.setText("必填项");
            }

            @Override
            public void after(String msg) {
                if(!checkName(msg)){
                    t2_flag.setText("不能有数字!");
                }else{
                    t2_flag.setText("");
                }
            }
        }));
        t3.addTextChangedListener(new MyTextListener(new TextviewListener() {
            @Override
            public void before() {
                t3_flag.setText("必填项");
            }

            @Override
            public void after(String msg) {
                if(checxSex(msg)){
                    t3_flag.setText("");
                }else{
                    t3_flag.setText("性别非法输入!");
                }
            }
        }));
        t4.addTextChangedListener(new MyTextListener(new TextviewListener() {
            @Override
            public void before() {
                t4_flag.setText("必填项");
            }

            @Override
            public void after(String msg) {
                if(checkTel(msg)){
                    t4_flag.setText("");
                }else{
                    t4_flag.setText("电话非法输入!");
                }
            }
        }));
        t5.addTextChangedListener(new MyTextListener(new TextviewListener() {
            @Override
            public void before() {
                t5_flag.setText("必填项");
            }

            @Override
            public void after(String msg) {
                if(!checkName(msg)){
                    t5_flag.setText("公司名称不符合规范！");
                }else{
                    t5_flag.setText("");
                }
            }
        }));
        t6.addTextChangedListener(new MyTextListener(new TextviewListener() {
            @Override
            public void before() {
                t6_flag.setText("必填项");
            }

            @Override
            public void after(String msg) {
                if(checkEmail(msg)){
                    t6_flag.setText("");
                }else{
                    t6_flag.setText("邮箱非法输入!");
                }
            }
        }));
        t7.addTextChangedListener(new MyTextListener(new TextviewListener() {
            @Override
            public void before() {
                t7_flag.setText("必填项");
            }

            @Override
            public void after(String msg) {
                t7_flag.setText("");
            }
        }));
        new AlertDialog.Builder(CustomerManagerActivity.this)
                .setTitle("添加客户:")
                .setView(view)
                .setIcon(R.drawable.add_customer_icon)
                .setPositiveButton("提交", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                Connection con = DBUtils.getConnection();
                                PreparedStatement ps = null;
                                ResultSet set =null;
                                try{

                                    if(!"".equals(t1_flag.getText().toString().trim())||!"".equals(t2_flag.getText().toString().trim())||!"".equals(t3_flag.getText().toString().trim())||!"".equals(t4_flag.getText().toString().trim())||!"".equals(t5_flag.getText().toString().trim())||!"".equals(t6_flag.getText().toString().trim())||!"".equals(t7_flag.getText().toString().trim())){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ToastUtil.showToast(CustomerManagerActivity.this,"请按要求填写!");
                                            }
                                        });
                                        return;
                                    }

                                    if(TextUtils.isEmpty(t1.getText().toString().trim())||
                                            TextUtils.isEmpty(t2.getText().toString().trim())||
                                            TextUtils.isEmpty(t3.getText().toString().trim())||
                                            TextUtils.isEmpty(t4.getText().toString().trim())||
                                            TextUtils.isEmpty(t5.getText().toString().trim())||
                                            TextUtils.isEmpty(t6.getText().toString().trim())||
                                            TextUtils.isEmpty(t7.getText().toString().trim())
                                    ){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ToastUtil.showToast(CustomerManagerActivity.this,"都是必填项,请全部填写!");
                                            }
                                        });
                                        return;
                                    }
                                    String str = "select name,sex,phone from customer where id = ?";
                                    ps = con.prepareStatement(str);
                                    ps.setString(1,t1.getText().toString().trim());
                                    set = ps.executeQuery();
                                    if(set.first()) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ToastUtil.showToast(CustomerManagerActivity.this,"用户编号已存在!");
                                            }
                                        });
                                        return;
                                    }
                                    //更新
                                    String sql = "insert into customer values(?,?,?,?,?,?,?)";
                                    ps = con.prepareStatement(sql);
                                    ps.setString(1,t1.getText().toString().trim());
                                    ps.setString(2,t2.getText().toString().trim());
                                    ps.setString(3,t3.getText().toString().trim());
                                    ps.setString(4,t4.getText().toString().trim());
                                    ps.setString(5,t5.getText().toString().trim());
                                    ps.setString(6,t6.getText().toString().trim());
                                    ps.setString(7,t7.getText().toString().trim());
                                    ps.executeUpdate();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastUtil.showToast(MyApplication.getContext(),"添加成功!");
                                            queryCustomerInfo(null);
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
                }).setNegativeButton("取消",null)
                .show();
    }


    private boolean checkEmail(String string) {
        if (TextUtils.isEmpty(string)) {
            return false;
        }
        String regEx1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern p;
        Matcher m;
        p = Pattern.compile(regEx1);
        m = p.matcher(string);
        if (m.matches())
            return true;
        else
            return false;

    }

    private boolean checkTel(String str) {
        Pattern p = null;
        Matcher m = null;
        boolean b = false;
        String s2="((13[0-9])|(14[579])|(15([0-3,5-9]))|(16[6])|(17[0135678])|(18[0-9]|19[89]))\\d{8}$";// 验证手机号
        if(!TextUtils.isEmpty(str)){
            p = Pattern.compile(s2);
            m = p.matcher(str);
            b = m.matches();
        }
        return b;
    }

    private boolean checkName(String str) {
        for(int i =0;i<str.length();i++){
            if(!(str.charAt(i) >= 19968 && str.charAt(i) <= 171941)){
                return false;
            }
        }
        return true;
    }

    private boolean checxSex(String str) {
        if("男".equals(str)||"女".equals(str)) {
            return true;
        }
        return false;
    }

    /**
     * 添加表格标题
     */
    private void addTitle() {
        TableRow row = new TableRow(this);
        for(int i=0;i<rows.length;i++){
            View view = LayoutInflater.from(this).inflate(R.layout.text,null,false);
            TextView t = view.findViewById(R.id.title_t);
            t.setText(rows[i]);
            t.setTextColor(Color.rgb(255, 0, 0));
            row.addView(view);
        }
        tableLayout.addView(row);
    }

    /**
     *  查询所有客户信息
     */
    private void queryCustomerInfo(final String s){
       tableLayout.removeAllViews();
        addTitle();
        downlaodProcess.setVisibility(View.VISIBLE);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Connection con = DBUtils.getConnection();
                PreparedStatement ps = null;
                ResultSet set = null;
                try{
                    String sql = "";
                    if(s==null){
                        sql = "select * from customer";
                        ps = con.prepareStatement(sql);
                    }else{
                        sql = "select * from customer where id like '%"+s+"%'";
                        ps = con.prepareStatement(sql);
                        //ps.setString(1,s);
                    }
                    set = ps.executeQuery();
                    while(set.next()){
                        String id= set.getString("id");
                        String name= set.getString("name");
                        String sex= set.getString("sex");
                        String phone= set.getString("phone");
                        String company= set.getString("company");
                        String email= set.getString("email");
                        String address= set.getString("address");
                        String str = id+","+name+","+sex+","+phone+","+company+","+email+","+address;
                        EventBus.getDefault().post(new MessageEvent(str));
                    }
                }catch (Exception e){
                    // dialog.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(MyApplication.getContext(),"查询出错!");
                            downlaodProcess.setVisibility(View.GONE);
                        }
                    });
                }finally{
                    DBUtils.close(con,set,ps);
                }
            }
        };
        MyThreadPool.getInstance().submit(runnable);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private class MyLonglistener implements View.OnLongClickListener {

        Customer customer;
        public MyLonglistener(Customer customer) {
            super();
            this.customer = customer;
        }

        @Override
        public boolean onLongClick(View v) {
                View view = getLayoutInflater().inflate(R.layout.delete_customer,null);
                final TextView t1 = view.findViewById(R.id.delete_id);
                t1.setText("确定要删除编号为"+customer.getId()+"的用户吗？");
                new AlertDialog.Builder(CustomerManagerActivity.this)
                        .setTitle("删除？")
                        .setView(view)
                        .setIcon(R.drawable.delete_customer_icon)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Runnable runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        Connection con = DBUtils.getConnection();
                                        PreparedStatement ps = null;
                                        try{
                                            String sql = "delete from customer where id=?";
                                            ps = con.prepareStatement(sql);
                                            ps.setString(1,customer.getId());
                                            ps.executeUpdate();
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ToastUtil.showToast(MyApplication.getContext(),"删除成功!");
                                                    queryCustomerInfo(null);
                                                }
                                            });
                                        }catch(Exception e){
                                            e.printStackTrace();
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ToastUtil.showToast(MyApplication.getContext(),"删除失败!");
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

            return true;
        }
    }

    private class myListener implements View.OnClickListener {

        Customer customer;
        public myListener(Customer customer) {
            super();
            this.customer = customer;
        }

        @Override
        public void onClick(View v) {
            Log.e("myListener",customer.toString());
            View view = getLayoutInflater().inflate(R.layout.edit_customer,null);
            final EditText t1 = view.findViewById(R.id.customer_id);
            t1.setText(customer.getId());
            final EditText t2 = view.findViewById(R.id.customer_name);
            t2.setText(customer.getName());
            final EditText t3 = view.findViewById(R.id.customer_sex);
            t3.setText(customer.getSex());
            final EditText t4 = view.findViewById(R.id.customer_telephone);
            t4.setText(customer.getTelephone());
            final EditText t5 = view.findViewById(R.id.customer_company);
            t5.setText(customer.getCompany());
            final EditText t6 = view.findViewById(R.id.customer_email);
            t6.setText(customer.getEmail());
            final EditText t7 = view.findViewById(R.id.customer_address);
            t7.setText(customer.getAddress());
            final TextView t2_flag = view.findViewById(R.id.customer_name_flag);
            final TextView t3_flag = view.findViewById(R.id.customer_sex_flag);
            final TextView t4_flag = view.findViewById(R.id.customer_telephone_flag);
            final TextView t5_flag = view.findViewById(R.id.customer_company_flag);
            final TextView t6_flag = view.findViewById(R.id.customer_email_flag);
            final TextView t7_flag = view.findViewById(R.id.customer_address_flag);
            t2.addTextChangedListener(new MyTextListener(new TextviewListener() {
                @Override
                public void before() {
                    t2_flag.setText("必填项");
                }

                @Override
                public void after(String msg) {
                    if(!checkName(msg)){
                        t2_flag.setText("不能有数字!");
                    }else{
                        t2_flag.setText("");
                    }
                }
            }));
            t3.addTextChangedListener(new MyTextListener(new TextviewListener() {
                @Override
                public void before() {
                    t3_flag.setText("必填项");
                }

                @Override
                public void after(String msg) {
                    if(checxSex(msg)){
                        t3_flag.setText("");
                    }else{
                        t3_flag.setText("性别非法输入!");
                    }
                }
            }));
            t4.addTextChangedListener(new MyTextListener(new TextviewListener() {
                @Override
                public void before() {
                    t4_flag.setText("必填项");
                }

                @Override
                public void after(String msg) {
                    if(checkTel(msg)){
                        t4_flag.setText("");
                    }else{
                        t4_flag.setText("电话非法输入!");
                    }
                }
            }));
            t5.addTextChangedListener(new MyTextListener(new TextviewListener() {
                @Override
                public void before() {
                    t5_flag.setText("必填项");
                }

                @Override
                public void after(String msg) {
                    if(!checkName(msg)){
                        t5_flag.setText("公司名称不符合规范！");
                    }else{
                        t5_flag.setText("");
                    }
                }
            }));
            t6.addTextChangedListener(new MyTextListener(new TextviewListener() {
                @Override
                public void before() {
                    t6_flag.setText("必填项");
                }

                @Override
                public void after(String msg) {
                    if(checkEmail(msg)){
                        t6_flag.setText("");
                    }else{
                        t6_flag.setText("邮箱非法输入!");
                    }
                }
            }));
            t7.addTextChangedListener(new MyTextListener(new TextviewListener() {
                @Override
                public void before() {
                    t7_flag.setText("必填项");
                }

                @Override
                public void after(String msg) {
                    t7_flag.setText("");
                }
            }));
            new AlertDialog.Builder(CustomerManagerActivity.this)
                    .setTitle("修改:")
                    .setView(view)
                    .setIcon(R.drawable.edit)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    if(!"".equals(t2_flag.getText().toString().trim())||!"".equals(t3_flag.getText().toString().trim())||!"".equals(t4_flag.getText().toString().trim())||!"".equals(t5_flag.getText().toString().trim())||!"".equals(t6_flag.getText().toString().trim())||!"".equals(t7_flag.getText().toString().trim())){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ToastUtil.showToast(CustomerManagerActivity.this,"请按要求填写!");
                                            }
                                        });
                                        return;
                                    }

                                    if(     TextUtils.isEmpty(t2.getText().toString().trim())||
                                            TextUtils.isEmpty(t3.getText().toString().trim())||
                                            TextUtils.isEmpty(t4.getText().toString().trim())||
                                            TextUtils.isEmpty(t5.getText().toString().trim())||
                                            TextUtils.isEmpty(t6.getText().toString().trim())||
                                            TextUtils.isEmpty(t7.getText().toString().trim())
                                    ){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ToastUtil.showToast(CustomerManagerActivity.this,"都是必填项,请全部填写!");
                                            }
                                        });
                                        return;
                                    }

                                    Connection con = DBUtils.getConnection();
                                    PreparedStatement ps = null;
                                    try{
                                        String sql = "update customer set name =?,sex=?,phone=?,company=?,email=?,address=? where id=?";
                                        ps = con.prepareStatement(sql);
                                        ps.setString(1,t2.getText().toString().trim());
                                        ps.setString(2,t3.getText().toString().trim());
                                        ps.setString(3,t4.getText().toString().trim());
                                        ps.setString(4,t5.getText().toString().trim());
                                        ps.setString(5,t6.getText().toString().trim());
                                        ps.setString(6,t7.getText().toString().trim());
                                        ps.setString(7,t1.getText().toString().trim());
                                        ps.executeUpdate();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ToastUtil.showToast(MyApplication.getContext(),"更新成功!");
                                                queryCustomerInfo(null);
                                            }
                                        });
                                    }catch(Exception e){
                                        e.printStackTrace();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ToastUtil.showToast(MyApplication.getContext(),"更新失败!");
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
    }
}
