package com.suyong.contractmanager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.suyong.contractmanager.ThreadPool.MyThreadPool;
import com.suyong.contractmanager.adapter.ContractAdapter;
import com.suyong.contractmanager.pojo.Contract;
import com.suyong.contractmanager.utils.DBUtils;
import com.suyong.contractmanager.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ContractManager extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Contract> data = new ArrayList<>();
    private ContractAdapter adapter;
    private ProgressBar queryProgressBar;
    private ImageButton search_contract;
    private ImageButton add_contract;
    private EditText contract_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contract_manager_layout);
        init();
    }

    private void init() {
        contract_id = findViewById(R.id.contract_id_forsearch);
        queryProgressBar = findViewById(R.id.query_process1);
        recyclerView = findViewById(R.id.contract_recycler);
        adapter = new ContractAdapter(data);
        adapter.setOnItemLongClickListener(new ContractAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(Contract contract, int position) {
                showMyDialog(contract);
            }
        });
        adapter.setOnItemClickListener(new ContractAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final Contract contract , int position, String flag) {
                if("image".equals(flag)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            EventBus.getDefault().postSticky(contract);
                            startActivity(new Intent(ContractManager.this,PhotoDetailActivity.class));
                        }
                    });
                }else{
                    EventBus.getDefault().postSticky(contract);
                    startActivity(new Intent(ContractManager.this,ContractDetail.class));
                }
            }
        });
        recyclerView.setAdapter(adapter);
        GridLayoutManager layoutManager = new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(layoutManager);
        add_contract = findViewById(R.id.btn_add_contract);
        add_contract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ContractManager.this,AddContract.class));
            }
        });
        search_contract = findViewById(R.id.btn_search_contract);
        search_contract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryByContractId();
            }
        });
    }

    private void showMyDialog(final Contract contract) {
        new AlertDialog.Builder(this)
                .setTitle("警告！")
                .setIcon(R.drawable.warn)
                .setMessage("你正在删除编号为:"+contract.getId()+"的合同")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                Connection con = DBUtils.getConnection();
                                PreparedStatement ps = null;
                                ResultSet set = null;
                                try{
                                    ps = con.prepareStatement("select * from ticket where contract_id = ?");
                                    ps.setString(1,contract.getId());
                                    set = ps.executeQuery();
                                    if(set.first()){
                                        String sql = "delete from ticket where contract_id=?";
                                        ps = con.prepareStatement(sql);
                                        ps.setString(1,contract.getId());
                                        ps.executeUpdate();
                                    }

                                    String sql = "delete from contract where id=?";
                                    ps = con.prepareStatement(sql);
                                    ps.setString(1,contract.getId());
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
                                    Log.d("ContractManager", e.getMessage());
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastUtil.showToast(MyApplication.getContext(),"删除失败!");
                                        }
                                    });
                                }finally{
                                    DBUtils.close(con,set,ps);
                                }
                            }
                        };
                        MyThreadPool.getInstance().submit(runnable);
                    }
                })
                .setNegativeButton("取消",null)
                .show();
    }


    private void queryByContractId() {
        queryProgressBar.setVisibility(View.VISIBLE);
        data.clear();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Connection con = DBUtils.getConnection();
                PreparedStatement ps = null;
                ResultSet set = null;
                try{
                    if(TextUtils.isEmpty(contract_id.getText().toString().trim())){
                        ps = con.prepareStatement("select * from contract");
                    }else{
                        ps = con.prepareStatement("select * from contract where id like '%"+contract_id.getText().toString().trim()+"%'");
                        //ps.setString(1,contract_id.getText().toString().trim());
                    }
                    set = ps.executeQuery();
                    while(set.next()){
                        Contract contract = new Contract(ContractManager.this);
                        contract.setId(set.getString("id"));
                        contract.setName(set.getString("name"));
                        contract.setStart_time(set.getDate("start_time"));
                        contract.setEnd_time(set.getDate("end_time"));
                        contract.setPartyA(set.getString("partyA"));
                        contract.setPartyB(set.getString("partyB"));
                        contract.setStatus(set.getString("status"));
                        contract.setMoney(set.getBigDecimal("money"));
                        data.add(contract);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            queryProgressBar.setVisibility(View.GONE);
                            if(data.size()==0) ToastUtil.showToast(ContractManager.this,"暂无合同数据!");
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
                    ps = con.prepareStatement("select * from contract");
                    set = ps.executeQuery();
                    while(set.next()){
                        Contract contract = new Contract(ContractManager.this);
                        contract.setId(set.getString("id"));
                        contract.setName(set.getString("name"));
                        contract.setStart_time(set.getDate("start_time"));
                        contract.setEnd_time(set.getDate("end_time"));
                        contract.setPartyA(set.getString("partyA"));
                        contract.setPartyB(set.getString("partyB"));
                        contract.setStatus(set.getString("status"));
                        contract.setMoney(set.getBigDecimal("money"));
                        data.add(contract);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            queryProgressBar.setVisibility(View.GONE);
                            if(data.size()==0) ToastUtil.showToast(ContractManager.this,"暂无合同数据!");
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
    protected void onDestroy(){
        super.onDestroy();
        //Glide.get(this).clearDiskCache();
        //Glide.get(this).clearMemory();
    }
}
