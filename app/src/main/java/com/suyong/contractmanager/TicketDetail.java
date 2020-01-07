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

import com.suyong.contractmanager.ThreadPool.MyThreadPool;
import com.suyong.contractmanager.pojo.Ticket;
import com.suyong.contractmanager.utils.DBUtils;
import com.suyong.contractmanager.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TicketDetail extends AppCompatActivity {

    Calendar calendar= Calendar.getInstance(Locale.CHINA);
    String bId = "";
    String bName="";
    String bAddress = "";
    String bPhone = "";
    String aName="";
    String aId = "";
    private String option = "";
    private AlertDialog alertDialog1; //信息框
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private TextView ticket_detail_date;
    private EditText ticket_detail_type;
    private EditText ticket_detail_sno;
    private TextView detail_contract_id;
    private TextView partyA_detail_name;
    private TextView partyA_detail_id;
    private EditText money_detail_lowprase;
    private TextView partyB_detail_name;
    private TextView partyB_detail_id;
    private TextView ticket_detail_address;
    private TextView detail_ticket_telephone;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_detail);
        init();
        EventBus.getDefault().register(this);//注册
    }

    private void init() {
        button = findViewById(R.id.confirm_ticket);
        ticket_detail_date = findViewById(R.id.ticket_detail_date);
        ticket_detail_type = findViewById(R.id.ticket_detail_type);
        ticket_detail_sno = findViewById(R.id.ticket_detail_sno);
        detail_contract_id = findViewById(R.id.detail_contract_id);
        partyA_detail_name = findViewById(R.id.partyA_detail_name);
        partyA_detail_id = findViewById(R.id.partyA_detail_id);
        money_detail_lowprase = findViewById(R.id.money_detail_lowprase);
        partyB_detail_name = findViewById(R.id.partyB_detail_name);
        partyB_detail_id = findViewById(R.id.partyB_detail_id);
        ticket_detail_address = findViewById(R.id.ticket_detail_address);
        detail_ticket_telephone = findViewById(R.id.detail_ticket_telephone);
        detail_contract_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQueryContract((TextView) v);
            }
        });
    }

    private void showQueryContract(final TextView v) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Connection con = DBUtils.getConnection();
                PreparedStatement ps = null;
                ResultSet set = null;
                try {
                    String str = "select count(*)  as NumberOfProducts from contract";
                    ps = con.prepareStatement(str);
                    set = ps.executeQuery();
                    set.next();
                    int rowCount = set.getInt("NumberOfProducts");
                    Log.d("Contract", "rowCount:" + rowCount);
                    final String [] data = new String[rowCount];
                    String sql = "select id,name,partyA,partyB from contract ";
                    ps = con.prepareStatement(sql);
                    set = ps.executeQuery();
                    if(set.first()){
                        int k =0;
                        do{
                            data[k] = "["+set.getString("id")+"] : "+set.getString("name");
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
                                ToastUtil.showToast(TicketDetail.this,"暂无合同，请先添加!");
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

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void messageEventBus(Ticket ticket){
        if(!"!@#".equals(ticket.getContract_id())){
            ticket_detail_date .setText(formatter.format(ticket.getTicket_date()));
            ticket_detail_type .setText(ticket.getTicket_type());
            ticket_detail_sno .setText(ticket.getTicket_sno());
            ticket_detail_sno.setEnabled(false);
            detail_contract_id .setText(ticket.getContract_id());
            partyA_detail_name .setText(ticket.getPartyA_name());
            partyA_detail_id .setText(ticket.getPartyA_id());
            money_detail_lowprase .setText(ticket.getMoney_lowprase().toString());
            partyB_detail_name.setText(ticket.getPartyB_name());
            partyB_detail_id .setText(ticket.getPartyB_id());
            ticket_detail_address .setText(ticket.getAddress());
            detail_ticket_telephone.setText(ticket.getTelephone());
            button.setText("提交");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editTicket();
                }
            });
        }else{
            button.setText("添加");
            ticket_detail_date.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePickerDialog(TicketDetail.this,0,(TextView) v,calendar);
                }
            });
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addticket();
                }
            });
        }
    }

    private void editTicket() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Connection con = DBUtils.getConnection();
                ResultSet set = null;
                PreparedStatement ps = null;
                try{
                    if( TextUtils.isEmpty(ticket_detail_sno.getText().toString().trim())||
                            TextUtils.isEmpty(ticket_detail_type.getText().toString().trim())||
                            TextUtils.isEmpty(money_detail_lowprase.getText().toString().trim())) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(TicketDetail.this,"都是必填项,请全部填写!");
                            }
                        });
                        return;
                    }
                    //更新
                    String sql = "update ticket set type=?,date=?,contract_id=?,t_money = ? where id=?";
                    ps = con.prepareStatement(sql);
                    ps.setString(1,ticket_detail_type.getText().toString().trim());
                    ps.setDate(2, Date.valueOf(ticket_detail_date.getText().toString().trim()));
                    ps.setString(3,detail_contract_id.getText().toString().trim());
                    ps.setBigDecimal(4,new BigDecimal(money_detail_lowprase.getText().toString().trim()));
                    ps.setString(5,ticket_detail_sno.getText().toString().trim());
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
                    DBUtils.close(con,set,ps);
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

    private void addticket() {
        Runnable runnable  = new Runnable() {
            @Override
            public void run() {
                Connection con = DBUtils.getConnection();
                PreparedStatement ps = null;
                ResultSet set =null;
                try{
                    if( TextUtils.isEmpty(ticket_detail_sno.getText().toString().trim())||
                            TextUtils.isEmpty(ticket_detail_type.getText().toString().trim())||
                            TextUtils.isEmpty(money_detail_lowprase.getText().toString().trim())) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(TicketDetail.this,"都是必填项,请全部填写!");
                            }
                        });
                        return;
                    }
                    String str = "select type from ticket where id = ?";
                    ps = con.prepareStatement(str);
                    ps.setString(1,ticket_detail_sno.getText().toString().trim());
                    set = ps.executeQuery();
                    if(set.first()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(TicketDetail.this,"发票编号已存在!");
                            }
                        });
                        return;
                    }
                    //更新
                    String sql = "insert into ticket values(?,?,?,?,?)";
                    ps = con.prepareStatement(sql);
                    ps.setString(1,ticket_detail_sno.getText().toString().trim());
                    ps.setString(2,ticket_detail_type.getText().toString().trim());
                    ps.setDate(3, Date.valueOf(ticket_detail_date.getText().toString().trim()));
                    ps.setString(4,detail_contract_id.getText().toString().trim());
                    ps.setBigDecimal(5,new BigDecimal(money_detail_lowprase.getText().toString().trim()));
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


    @Override
    public void onDestroy(){
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void showList(final String [] items,final TextView v){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(TicketDetail.this);
        alertBuilder.setTitle("合同:");
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Toast.makeText(AddContract.this, items[i], Toast.LENGTH_SHORT).show();
                option = items[i];
                String str = option.substring(3,option.indexOf(";"));
                v.setText(str);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        String[] n = option.split(";");
                        aId = n[1].substring(n[1].indexOf(":")+1);
                        bId = n[2].substring(n[2].indexOf(":")+1);
                        Log.d("TicketDetail", "aaa"+aId);
                        Connection con = DBUtils.getConnection();
                        PreparedStatement ps = null;
                        ResultSet set = null;
                        try{
                            ps = con.prepareStatement("select * from customer where id = ?");
                            ps.setString(1,aId);
                            set = ps.executeQuery();
                            while(set.next()){
                                aName = set.getString("name");
                            }
                            ps = con.prepareStatement("select * from customer where id = ?");
                            ps.setString(1,bId);
                            set = ps.executeQuery();
                            while(set.next()){
                                bName = set.getString("name");
                                bAddress = set.getString("address");
                                bPhone = set.getString("phone");
                            }
                            DBUtils.close(null,set,ps);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    partyA_detail_name.setText(aName);
                                    partyA_detail_id.setText(aId);
                                    partyB_detail_name.setText(bName);
                                    partyB_detail_id.setText(bId);
                                    ticket_detail_address.setText(bAddress);
                                    detail_ticket_telephone.setText(bPhone);
                                    alertDialog1.dismiss();
                                }
                            });
                        }catch(Exception e){
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    alertDialog1.dismiss();
                                }
                            });
                        }finally{
                            DBUtils.close(con,set,ps);
                        }
                    }
                };
                MyThreadPool.getInstance().submit(runnable);
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
