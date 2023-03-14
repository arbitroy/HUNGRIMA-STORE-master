package com.example.hungrima_store.model;

import java.time.LocalDate;

public class transModel {
    private String pname;
    private String date;
    private String t_type;
    private Integer quantity;
    private Integer s_count;

    private Integer id;



    public transModel(String pName, String date, String tType, Integer quantity, Integer sCount) {
        this.pname = pName;
        this.date = date;
        this.t_type = tType;
        this.quantity = quantity;
        this.s_count = sCount;
    }


    public String getPname() {
        return pname;
    }

    public String getQuantity() {
        return String.valueOf(quantity);
    }

    public String getS_count() {
        return String.valueOf(s_count);
    }

    public String getDate() {
        return String.valueOf(date);
    }

    public String getT_type() {
        return t_type;
    }
}
