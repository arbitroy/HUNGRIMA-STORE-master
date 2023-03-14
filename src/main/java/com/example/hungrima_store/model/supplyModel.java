package com.example.hungrima_store.model;

public class supplyModel {
    private final String supply_date;
    private final String cost;
    private final String quantity;
    private final String p_name;
    private final String supply_id;

        public supplyModel(String supply_date, String cost, String quantity, String p_name, String supply_id) {
        this.supply_date = supply_date;
        this.cost = cost;
        this.quantity = quantity;
        this.p_name = p_name;
        this.supply_id = supply_id;
    }

    public String getSupply_date() {
        return supply_date;
    }

    public String getCost() {
        return cost;
    }

    public String getP_name() {
        return p_name;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getSupply_id() {
        return supply_id;
    }
}
