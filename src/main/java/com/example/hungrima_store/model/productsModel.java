package com.example.hungrima_store.model;



import java.util.Objects;

public class productsModel {

    private final String pname;
    private String price;
    private String p_id;

    public productsModel(String pname, String price , String p_id) {

        this.pname = pname;
        this.price = price;
        this.p_id = p_id;
    }
    public productsModel(String pname) {
        this.pname = pname;
    }



    public String getPname() {
        return pname;
    }

    public String getP_id() {
        return p_id;
    }


    public String getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
       productsModel person = (productsModel) o;
        return getPname().equals(person.getPname());
    }



    @Override
    public int hashCode() {
        return Objects.hash(getPname());
    }
}
