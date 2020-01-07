package com.suyong.contractmanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.suyong.contractmanager.R;

import com.suyong.contractmanager.pojo.Function;

import java.util.List;

public class FunctionAdapter extends RecyclerView.Adapter<FunctionAdapter.ViewHolder> {
    private Context mContext;
    private List<Function> mFunctionList;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener){
        onItemClickListener= listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(mContext==null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.function_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Function function = mFunctionList.get(position);
        holder.imageView.setImageResource(function.getImageId());
        if (onItemClickListener!=null){
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(position);
                }
            });
        }
        holder.textView.setText(function.getName());
    }

    @Override
    public int getItemCount() {
        return mFunctionList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        ImageView imageView;
        TextView textView;
        public ViewHolder(View view){
            super(view);
            cardView = (CardView)view;
            imageView = (ImageView)view.findViewById(R.id.function_image);
            textView = (TextView)view.findViewById(R.id.function_title);
        }
    }

    public FunctionAdapter(List<Function> functionList){
        mFunctionList = functionList;
    }

}
