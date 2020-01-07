package com.suyong.contractmanager.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.suyong.contractmanager.ContractManager;
import com.suyong.contractmanager.CustomerManagerActivity;
import com.suyong.contractmanager.LibraryActivity;
import com.suyong.contractmanager.MyApplication;
import com.suyong.contractmanager.R;
import com.suyong.contractmanager.TicketManager;
import com.suyong.contractmanager.TrackServiceActivity;
import com.suyong.contractmanager.adapter.FunctionAdapter;
import com.suyong.contractmanager.pojo.Function;

import java.util.ArrayList;
import java.util.List;

public class FunctionFragment extends Fragment {

    private MyApplication myApplication;
    private List<Function> functionList = new ArrayList<>();
    private FunctionAdapter adapter;
    private RecyclerView recyclerView;
    private Function[] functions ={
            new Function("合同管理", R.drawable.contracts),
            new Function("客户信息管理", R.drawable.customer),
            new Function("发票信息管理", R.drawable.bill),
            new Function("实验室", R.drawable.library),
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootView = inflater.from(getContext()).inflate(R.layout.functionfragment_layout,container,false);
        initView(rootView);
        return rootView;
    }

    private void initView(View view) {
        myApplication = (MyApplication)getActivity().getApplication();
        for(int i=0;i<functions.length;i++) functionList.add(functions[i]);
        recyclerView = view.findViewById(R.id.reycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(),3);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new FunctionAdapter(functionList);
        adapter.setOnItemClickListener(new FunctionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                switch (position){
                    case 0:
                        startActivity(new Intent(getActivity(), ContractManager.class));
                        break;
                    case 1:
                        startActivity(new Intent(getActivity(), CustomerManagerActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(getActivity(), TicketManager.class));
                        break;
                    case 3:
                        if(!myApplication.isRunning){
                            Toast.makeText(MyApplication.getContext(), "定位服务未开启!",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        startActivity(new Intent(getActivity(), TrackServiceActivity.class));
                        break;
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);
    }
}
