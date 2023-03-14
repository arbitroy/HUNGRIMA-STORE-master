package com.example.hungrima_store.model;

public class billModel {
    private String pname;
    private Integer quantity;
    private Integer price;
    private Integer amount;
    public billModel(String pname, Integer quantity, Integer price, Integer amount){
        this.pname = pname;
        this.quantity = quantity;
        this.price = price;
        this.amount = amount;
    }

    public String getPname() {
        return pname;
    }

    public String getQuantity() {
        return String.valueOf(quantity);
    }

    public String getAmount() {
        return String.valueOf(amount);
    }

    public String getPrice() {
        return String.valueOf(price);
    }
}
