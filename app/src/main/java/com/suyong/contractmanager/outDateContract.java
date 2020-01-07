package com.suyong.contractmanager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.suyong.contractmanager.ThreadPool.MyThreadPool;
import com.suyong.contractmanager.pojo.Contract;
import com.suyong.contractmanager.utils.DBUtils;
import com.suyong.contractmanager.utils.ToastUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

public class outDateContract extends AppCompatActivity {

    private String ContractId;
    private TextView id;
    private TextView name;
    private TextView partyA;
    private TextView partyB;
    private TextView status;
    private TextView start_time;
    private TextView end_time;
    private TextView money;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out_date_contract);
        Intent intent = getIntent();
        ContractId = intent.getStringExtra("id");
        Log.d("outDateContract", "rrr"+ContractId);
        init();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Connection con = DBUtils.getConnection();
                PreparedStatement ps = null;
                ResultSet set = null;
                try{
                    ps = con.prepareStatement("select * from contract where id = ?");
                    ps.setString(1,ContractId);
                    set = ps.executeQuery();
                    final Contract contract = new Contract(outDateContract.this);
                    while(set.next()){
                        contract.setId(set.getString("id"));
                        contract.setName(set.getString("name"));
                        contract.setStart_time(set.getDate("start_time"));
                        contract.setEnd_time(set.getDate("end_time"));
                        contract.setPartyA(set.getString("partyA"));
                        contract.setPartyB(set.getString("partyB"));
                        contract.setStatus(set.getString("status"));
                        contract.setMoney(set.getBigDecimal("money"));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                           setData(contract);
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

    private void setData(Contract contract) {
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

    private void init() {
        id = findViewById(R.id.detail_id);
        name = findViewById(R.id.detail_name);
        partyA = findViewById(R.id.detail_partyA1);
        partyA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQueryPartyA();
            }
        });
        partyB = findViewById(R.id.detail_partyB1);
        partyB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQueryPartyB();
            }
        });
        status = findViewById(R.id.detail_contract_status);
        start_time = findViewById(R.id.detail_start_time);
        end_time = findViewById(R.id.detail_end_time);
        money = findViewById(R.id.detail_money);
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
                            new AlertDialog.Builder(outDateContract.this)
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
                            new AlertDialog.Builder(outDateContract.this)
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
}
