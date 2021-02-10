package com.apps.ref.models;

import java.io.Serializable;

public class SubscriptionModel implements Serializable {

    private int id;
    private String title;
    private String image;
    private String price;
    private String currency;
    private String number_of_days;
    private String number_of_freeUsed;
    private String is_free;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public String getNumber_of_days() {
        return number_of_days;
    }

    public String getNumber_of_freeUsed() {
        return number_of_freeUsed;
    }

    public String getIs_free() {
        return is_free;
    }
}
