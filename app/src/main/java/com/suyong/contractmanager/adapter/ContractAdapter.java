package com.suyong.contractmanager.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.signature.StringSignature;
import com.suyong.contractmanager.R;
import com.suyong.contractmanager.pojo.Contract;
import com.suyong.contractmanager.pojo.Function;
import com.suyong.contractmanager.utils.BitmapUtils;

import org.w3c.dom.Text;

import java.util.Date;
import java.util.List;

public class ContractAdapter extends RecyclerView.Adapter<ContractAdapter.ViewHolder> {
    private Context mContext;
    private List<Contract> mContractList;
    private Date nowDate;

    public interface OnItemClickListener {
        void onItemClick(Contract contract,int position,String flag);
    }
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public void setOnItemClickListener(OnItemClickListener listener){
        onItemClickListener= listener;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Contract contract,int position);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener){
        onItemLongClickListener= listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(mContext==null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.contract_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Contract contract = mContractList.get(position);
        Date contractDate = new Date(contract.getEnd_time().getTime());
        long l = -1;
        if(contractDate.getTime() > nowDate.getTime()) {
            l = (contractDate.getTime() - nowDate.getTime()) / 3600/24/1000;
        }
        if(l<=30&&l>=0){
            holder.flag.setVisibility(View.VISIBLE);
            holder.flag.setImageResource(R.drawable.icon_incoming_outdate);
        }else if(contractDate.before(nowDate)){
            holder.flag.setVisibility(View.VISIBLE);
            holder.flag.setImageResource(R.drawable.already_outdate);
        }
        holder.progressBar.setVisibility(View.VISIBLE);
        holder.imageView.setVisibility(View.GONE);
        holder.id.setTag(position);
        holder.id.setText(contract.getId());
        holder.id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(contract,position,"id");
            }
        });
        holder.name.setText(contract.getName());
        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if((int)holder.id.getTag()==position) {
                    onItemLongClickListener.onItemLongClick(contract, position);
                }
                return true;
            }
        });
        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(contract,position,"name");
            }
        });
        String updateTime = String.valueOf(System.currentTimeMillis());
        Glide.with(mContext)
                .load("http://49.234.92.110:8080/MyWebDemo/DownLoadServlet?filename="+contract.getId()+".jpg")
                .error(R.drawable.contract_default)
                .signature(new StringSignature(updateTime))
                .into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        if((int)holder.id.getTag()!=position)  return;
                        contract.setBitmap(BitmapUtils.drawableToBitamp(resource));
                        holder.progressBar.setVisibility(View.GONE);
                        holder.imageView.setVisibility(View.VISIBLE);
                        holder.imageView.setImageBitmap(contract.getBitmap());
                    }
                    @Override
                    public void onStart() {
                        super.onStart();

                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable){
                        holder.progressBar.setVisibility(View.GONE);
                        holder.imageView.setVisibility(View.VISIBLE);
                        holder.imageView.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(),R.drawable.contract_default));
                        super.onLoadFailed(e,errorDrawable);
                    }

                });
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(contract,position,"image");
            }
        });

    }



    @Override
    public int getItemCount() {
        return mContractList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        ImageView imageView;
        ImageView flag;
        TextView id;
        TextView name;
        ProgressBar progressBar;
        public ViewHolder(View view){
            super(view);
            cardView = (CardView)view;
            imageView = view.findViewById(R.id.contract_image);
            id = view.findViewById(R.id.contract_id);
            name = view.findViewById(R.id.contract_name);
            progressBar = view.findViewById(R.id.contract_process);
            flag = view.findViewById(R.id.contract_flag_outdate);
        }
    }

    public ContractAdapter(List<Contract> mContractList){
        this.mContractList = mContractList;
        nowDate = new Date();
    }

}
