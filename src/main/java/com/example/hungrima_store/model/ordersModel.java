package com.example.hungrima_store.model;

public class ordersModel {
    private String pname;
    private Integer price;
    private Integer amount;
    private Integer quantity;

    public ordersModel(String pname, Integer quantity, Integer price, Integer amount){
        this.pname = pname;
        this.quantity = quantity;
        this.price = price;
        this.amount = amount;
    }

    public String getnam() {
        return pname;
    }

    public String getQuant() {
        return String.valueOf(quantity);
    }

    public String getAmount() {
        return String.valueOf(amount);
    }

    public String getPrice() {
        return String.valueOf(price);
    }
}
