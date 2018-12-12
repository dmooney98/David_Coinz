package com.example.s1651374.david_coinz;

//==================================================================================================
// This class is created as it allows a far more efficient way of storing the coins which are used
// so frequently throughout the app.  It stores their id, currency, and value
public class Coin {

    //==============================================================================================
    // Set up the required 3 variables
    private String id;
    private String currency;
    private Double value;

    //==============================================================================================
    // Create constructor, which will always require the id, currency, and value of the coin
    Coin(String id, String currency, Double value) {
        this.id = id;
        this.currency = currency;
        this.value = value;
    }

    //==============================================================================================
    // Create appropriate getter methods.  Setter methods are not required for this class, as a Coin
    // is never changed after being first created, so these would be redundant
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
