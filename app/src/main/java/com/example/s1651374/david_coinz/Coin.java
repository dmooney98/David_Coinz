package com.example.s1651374.david_coinz;

public class Coin {

    private String id;
    private String currency;
    private Double value;

    public Coin(String id, String currency, Double value) {
        this.id = id;
        this.currency = currency;
        this.value = value;
    }

    public Double getValue() {
        return value;
    }

    public String getCurrency() {
        return currency;
    }

    public String getId() {
        return id;
    }

}
