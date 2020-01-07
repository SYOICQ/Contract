package com.suyong.contractmanager.pojo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.suyong.contractmanager.R;

import java.math.BigDecimal;
import java.sql.Date;

public class Contract {
    private Context mContext;
    private String id;
    private String name;
    private String partyA;
    private String partyB;
    private Date start_time;
    private Date end_time;
    private BigDecimal money;
    private String status;
    private Bitmap bitmap;

    public Contract(Context c){
        mContext = c;
        bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.contract_default);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPartyA() {
        return partyA;
    }

    public void setPartyA(String partyA) {
        this.partyA = partyA;
    }

    public String getPartyB() {
        return partyB;
    }

    public void setPartyB(String partyB) {
        this.partyB = partyB;
    }

    public Date getStart_time() {
        return start_time;
    }

    public void setStart_time(Date start_time) {
        this.start_time = start_time;
    }

    public Date getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Date end_time) {
        this.end_time = end_time;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public String toString() {
        return "Contract{" +
                "mContext=" + mContext +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", partyA='" + partyA + '\'' +
                ", partyB='" + partyB + '\'' +
                ", start_time=" + start_time +
                ", end_time=" + end_time +
                ", money=" + money +
                ", status='" + status + '\'' +
                ", bitmap=" + bitmap +
                '}';
    }
}
