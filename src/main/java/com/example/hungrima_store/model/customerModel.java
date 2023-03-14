package com.example.hungrima_store.model;

import java.util.Objects;

public class customerModel {
    private final String custname;
    private  String phoneno;
    private  String cust_id;

    public customerModel(String custname, String phoneno, String cust_id) {
        this.custname = custname;
        this.phoneno = phoneno;
        this.cust_id = cust_id;
    }
    public customerModel(String custname){
        this.custname = custname;
    }
    public String getCustname() {
        return custname;
    }

    public String getCust_id() {
        return cust_id;
    }


    public String getPhoneno() {
        return phoneno;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        customerModel customer = (customerModel) o;
        return getCustname().equals(customer.getCustname());
    }



    @Override
    public int hashCode() {
        return Objects.hash(getCustname());
    }
}
