package com.suyong.contractmanager.pojo;

import java.math.BigDecimal;
import java.sql.Date;

public class Ticket {
    private Date ticket_date;
    private String ticket_type;
    private String ticket_sno;
    private String contract_id;
    private String partyA_name;
    private String partyA_id;
    private BigDecimal money_lowprase;
    private String partyB_name;
    private String partyB_id;
    private String address;
    private String telephone;

    public Date getTicket_date() {
        return ticket_date;
    }

    public void setTicket_date(Date ticket_date) {
        this.ticket_date = ticket_date;
    }

    public String getTicket_type() {
        return ticket_type;
    }

    public void setTicket_type(String ticket_type) {
        this.ticket_type = ticket_type;
    }

    public String getTicket_sno() {
        return ticket_sno;
    }

    public void setTicket_sno(String ticket_sno) {
        this.ticket_sno = ticket_sno;
    }

    public String getContract_id() {
        return contract_id;
    }

    public void setContract_id(String contract_id) {
        this.contract_id = contract_id;
    }

    public String getPartyA_name() {
        return partyA_name;
    }

    public void setPartyA_name(String partyA_name) {
        this.partyA_name = partyA_name;
    }

    public String getPartyA_id() {
        return partyA_id;
    }

    public void setPartyA_id(String partyA_id) {
        this.partyA_id = partyA_id;
    }

    public BigDecimal getMoney_lowprase() {
        return money_lowprase;
    }

    public void setMoney_lowprase(BigDecimal money_lowprase) {
        this.money_lowprase = money_lowprase;
    }

    public String getPartyB_name() {
        return partyB_name;
    }

    public void setPartyB_name(String partyB_name) {
        this.partyB_name = partyB_name;
    }

    public String getPartyB_id() {
        return partyB_id;
    }

    public void setPartyB_id(String partyB_id) {
        this.partyB_id = partyB_id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}
