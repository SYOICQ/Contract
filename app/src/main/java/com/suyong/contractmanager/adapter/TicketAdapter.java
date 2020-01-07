package com.suyong.contractmanager.adapter;

import android.content.Context;
import android.nfc.NfcAdapter;
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
import com.suyong.contractmanager.pojo.Ticket;

import java.text.SimpleDateFormat;
import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.ViewHolder> {
    private Context mContext;
    private List<Ticket> mTicketList;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    public interface OnItemClickListener {
        void onItemClick(String tag,Ticket ticket);
    }
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener){
        onItemClickListener= listener;
    }

    public interface OnItemLongClickListener {
        void onItemClick(String tag,Ticket ticket);
    }
    private OnItemLongClickListener onItemLongClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener listener){
        onItemLongClickListener= listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(mContext==null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.ticket_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Ticket ticket = mTicketList.get(position);
        holder.address.setText(ticket.getAddress());
        holder.contract_id.setText(ticket.getContract_id());
        holder.contract_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick("contract",ticket);
            }
        });
        holder.money_lowprase.setText(ticket.getMoney_lowprase().toString());
        holder.partyA_id.setText(ticket.getPartyA_id());
        holder.partyA_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick("partyA",ticket);
            }
        });
        holder.partyB_id.setText(ticket.getPartyB_id());
        holder.partyB_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick("partyB",ticket);
            }
        });
        holder.partyA_name.setText(ticket.getPartyA_name());
        holder.partyB_name.setText(ticket.getPartyB_name());
        holder.telephone.setText(ticket.getTelephone());
        holder.ticket_date.setText(formatter.format(ticket.getTicket_date()));
        holder.ticket_type.setText(ticket.getTicket_type());
        holder.ticket_sno.setText(ticket.getTicket_sno());
        holder.ticket.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onItemLongClickListener.onItemClick("",ticket);
                return true;
            }
        });
        holder.ticket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick("",ticket);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTicketList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView ticket_date;
        TextView ticket_type;
        TextView ticket_sno;
        TextView contract_id;
        TextView partyA_name;
        TextView partyA_id;
        TextView money_lowprase;
        TextView partyB_name;
        TextView partyB_id;
        TextView address;
        TextView telephone;
        CardView ticket;
        public ViewHolder(View view){
            super(view);
            ticket_date = view.findViewById(R.id.ticket_date);
            ticket_type = view.findViewById(R.id.ticket_type);
            ticket_sno = view.findViewById(R.id.ticket_sno);
            contract_id = view.findViewById(R.id.contract_id);
            partyA_name = view.findViewById(R.id.partyA_name);
            partyA_id = view.findViewById(R.id.partyA_id);
            money_lowprase = view.findViewById(R.id. money_lowprase);
            partyB_name = view.findViewById(R.id.partyB_name);
            partyB_id = view.findViewById(R.id.partyB_id);
            address = view.findViewById(R.id.address);
            telephone = view.findViewById(R.id.telephone);
            ticket = view.findViewById(R.id.ticket);
        }
    }

    public TicketAdapter(List<Ticket> mTicketList){
        this.mTicketList = mTicketList;
    }

}
