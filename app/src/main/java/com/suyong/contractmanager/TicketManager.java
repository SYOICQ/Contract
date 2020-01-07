package com.suyong.contractmanager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.suyong.contractmanager.ThreadPool.MyThreadPool;
import com.suyong.contractmanager.adapter.TicketAdapter;
import com.suyong.contractmanager.pojo.Contract;
import com.suyong.contractmanager.pojo.Ticket;
import com.suyong.contractmanager.utils.DBUtils;
import com.suyong.contractmanager.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Result;

public class TicketManager extends AppCompatActivity {

    private RecyclerView recyclerView ;
    private TicketAdapter adapter;
    private List<Ticket> data = new ArrayList<>();
    private ProgressBar queryProgressBar;
    private ImageButton search_ticket;
    private ImageButton add_ticket;
    private EditText ticket_id_forsearch;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_manager_layout);
        init();
    }

    private void init() {
        recyclerView = findViewById(R.id.ticket_recycler);
        adapter = new TicketAdapter(data);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new TicketAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String tag,Ticket ticket) {
                if(tag.equals("partyA")){
                    showQueryPartyA(ticket);
                }else if(tag.equals("partyB")){
                    showQueryPartyB(ticket);
                }else if(tag.equals("contract")){
                    showQueryContract(ticket);
                }else{
                    EventBus.getDefault().postSticky(ticket);
                    startActivity(new Intent(TicketManager.this,TicketDetail.class));
                }
            }
        });
        adapter.setOnItemLongClickListener(new TicketAdapter.OnItemLongClickListener() {
            @Override
            public void onItemClick(String tag, Ticket ticket) {
                deleteTicket(ticket);
            }
        });
        GridLayoutManager layoutManager = new GridLayoutManager(this,1);
        recyclerView.setLayoutManager(layoutManager);
        queryProgressBar = findViewById(R.id.query_process2);
        ticket_id_forsearch = findViewById(R.id.ticket_id_forsearch);
        search_ticket = findViewById(R.id.btn_search_ticket);
        search_ticket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryTicketById();
            }
        });
        add_ticket = findViewById(R.id.btn_add_ticket);
        add_ticket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Ticket ticket = new Ticket();
                ticket.setContract_id("!@#");
                EventBus.getDefault().postSticky(ticket);
                startActivity(new Intent(TicketManager.this,TicketDetail.class));
            }
        });
    }

    private void showQueryContract(final Ticket ticket) {
        final View view = getLayoutInflater().inflate(R.layout.contract_info,null);
        final TextView t1 = view.findViewById(R.id.contract_info_id);
        final TextView t2 = view.findViewById(R.id.contract_info_name);
        final TextView t3 = view.findViewById(R.id.contract_info_partyA);
        final TextView t4 = view.findViewById(R.id.contract_info_partyB);
        final TextView t5 = view.findViewById(R.id.contract_info_status);
        final TextView t6 = view.findViewById(R.id.contract_info_start_date);
        final TextView t7 = view.findViewById(R.id.contract_info_end_date);
        final TextView t8 = view.findViewById(R.id.contract_info_money);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Connection con = DBUtils.getConnection();
                PreparedStatement ps = null;
                ResultSet set = null;
                try {
                    String sql = "select * from contract where id = ?";
                    ps = con.prepareStatement(sql);
                    ps.setString(1,ticket.getContract_id());
                    set = ps.executeQuery();
                    set.first();
                    final String s1 = set.getString("id");
                    final String s2 = set.getString("name");
                    final String s3 = set.getString("partyA");
                    final String s4 = set.getString("partyB");
                    final String s5 = set.getString("status");
                    final String s6 = formatter.format(set.getDate("start_time"));
                    final String s7 = formatter.format(set.getDate("end_time"));
                    final String s8 = set.getBigDecimal("money").toString();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            t1.setText(s1);t2.setText(s2);t3.setText(s3);t4.setText(s4);
                            t5.setText(s5);t6.setText(s6);t7.setText(s7);t8.setText(s8);
                            new AlertDialog.Builder(TicketManager.this)
                                    .setTitle("合同:")
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

    private void deleteTicket(final Ticket ticket) {
        new AlertDialog.Builder(this)
                .setTitle("警告！")
                .setIcon(R.drawable.warn)
                .setMessage("你正在删除编号为:"+ticket.getTicket_sno()+"的发票")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                Connection con = DBUtils.getConnection();
                                PreparedStatement ps = null;
                                try{
                                    String sql = "delete from ticket where id=?";
                                    ps = con.prepareStatement(sql);
                                    ps.setString(1,ticket.getTicket_sno());
                                    ps.executeUpdate();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastUtil.showToast(MyApplication.getContext(),"删除成功!");
                                            startQuery();
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
                })
                .setNegativeButton("取消",null)
                .show();
    }


    private void queryTicketById() {
        data.clear();
        queryProgressBar.setVisibility(View.VISIBLE);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Connection con = DBUtils.getConnection();
                PreparedStatement ps = null;
                ResultSet set = null;
                try{
                    ps = con.prepareStatement("SELECT t.id as tId,t.date,t.type,t.t_money,c.id as cId,c.partyA,c.partyB FROM ticket t INNER JOIN contract c ON t.contract_id = c.id ");
                    set = ps.executeQuery();
                    while(set.next()){
                        if(TextUtils.isEmpty(ticket_id_forsearch.getText().toString().trim())) {startQuery();return;}
                        if((!TextUtils.isEmpty(ticket_id_forsearch.getText().toString().trim()))&&(set.getString("tId").equals(ticket_id_forsearch.getText().toString().trim()))){
                            PreparedStatement ps1 = null;
                            ResultSet set1 = null;
                            Ticket ticket = new Ticket();
                            ticket.setTicket_date(set.getDate("date"));
                            ticket.setTicket_type(set.getString("type"));
                            ticket.setTicket_sno(set.getString("tId"));
                            ticket.setMoney_lowprase(set.getBigDecimal("t_money"));
                            ticket.setContract_id(set.getString("cId"));
                            ticket.setPartyA_id(set.getString("partyA"));
                            ticket.setPartyB_id(set.getString("partyB"));
                            ps1 = con.prepareStatement("select * from customer where id = ?");
                            ps1.setString(1,ticket.getPartyA_id());
                            set1 = ps1.executeQuery();
                            while(set1.next()){
                                ticket.setPartyA_name(set1.getString("name"));
                            }
                            ps1 = con.prepareStatement("select * from customer where id = ?");
                            ps1.setString(1,ticket.getPartyB_id());
                            set1 = ps1.executeQuery();
                            while(set1.next()){
                                ticket.setPartyB_name(set1.getString("name"));
                                ticket.setAddress(set1.getString("address"));
                                ticket.setTelephone(set1.getString("phone"));
                            }
                            DBUtils.close(null,set1,ps1);
                            data.add(ticket);
                        }
                    }


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            queryProgressBar.setVisibility(View.GONE);
                            if(data.size()==0) ToastUtil.showToast(TicketManager.this,"暂无发票数据!");
                        }
                    });
                }catch(Exception e){
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            queryProgressBar.setVisibility(View.GONE);
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
    protected void onResume(){
        super.onResume();
        startQuery();
    }

    private void startQuery() {
        queryProgressBar.setVisibility(View.VISIBLE);
        data.clear();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Connection con = DBUtils.getConnection();
                PreparedStatement ps = null;
                ResultSet set = null;
                try{
                    ps = con.prepareStatement("SELECT t.id as tId,t.date,t.type,t.t_money,c.id as cId,c.partyA,c.partyB FROM ticket t INNER JOIN contract c ON t.contract_id = c.id");
                    set = ps.executeQuery();
                    int row = 0;
                    while(set.next()){
                        PreparedStatement ps1 = null;
                        ResultSet set1 = null;
                        Ticket ticket = new Ticket();
                        ticket.setTicket_date(set.getDate("date"));
                        ticket.setTicket_type(set.getString("type"));
                        ticket.setTicket_sno(set.getString("tId"));
                        ticket.setMoney_lowprase(set.getBigDecimal("t_money"));
                        ticket.setContract_id(set.getString("cId"));
                        ticket.setPartyA_id(set.getString("partyA"));
                        ticket.setPartyB_id(set.getString("partyB"));
                        ps1 = con.prepareStatement("select * from customer where id = ?");
                        ps1.setString(1,ticket.getPartyA_id());
                        set1 = ps1.executeQuery();
                        while(set1.next()){
                            ticket.setPartyA_name(set1.getString("name"));
                        }
                        ps1 = con.prepareStatement("select * from customer where id = ?");
                        ps1.setString(1,ticket.getPartyB_id());
                        set1 = ps1.executeQuery();
                        while(set1.next()){
                            ticket.setPartyB_name(set1.getString("name"));
                            ticket.setAddress(set1.getString("address"));
                            ticket.setTelephone(set1.getString("phone"));
                        }
                        DBUtils.close(null,set1,ps1);
                        data.add(ticket);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            queryProgressBar.setVisibility(View.GONE);
                            if(data.size()==0) ToastUtil.showToast(TicketManager.this,"暂无发票数据!");
                        }
                    });
                }catch(Exception e){
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            queryProgressBar.setVisibility(View.GONE);
                        }
                    });
                }finally{
                    DBUtils.close(con,set,ps);
                }
            }
        };
        MyThreadPool.getInstance().submit(runnable);
    }

    private void showQueryPartyA(final Ticket ticket) {
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
                    ps.setString(1,ticket.getPartyA_id());
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
                            new AlertDialog.Builder(TicketManager.this)
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
    private void showQueryPartyB(final Ticket ticket) {
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
                    ps.setString(1,ticket.getPartyB_id());
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
                            new AlertDialog.Builder(TicketManager.this)
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
