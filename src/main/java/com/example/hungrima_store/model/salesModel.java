package com.example.hungrima_store.model;

public class salesModel {
    private String custname;
    private String modePayment;
    private String collection;
    private String remarks;
    private String sales_date;
    private Integer t_amount;
    private Integer s_id;


    public salesModel(String custname, String modePayment, String collection, String remarks, String sales_date, Integer t_amount, Integer s_id){
        this.custname = custname;
        this.modePayment = modePayment;
        this.collection = collection;
        this.remarks = remarks;
        this.sales_date = sales_date;
        this.t_amount = t_amount;
        this.s_id = s_id;
    }
    public String getT_amount() {
        return String.valueOf(t_amount);
    }

    public String getCustname() {
        return custname;
    }

    public String getModePayment() {
        return modePayment;
    }

    public String getCollection() {
        return collection;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getSales_date() {
        return String.valueOf(sales_date);
    }

    public String getS_id() {
        return String.valueOf(s_id);
    }
}
