package com.apps.ref.models;

import java.io.Serializable;
import java.util.List;

public class OffersDataModel implements Serializable {
    private int current_page;
    private List<OffersModel> data;

    public int getCurrent_page() {
        return current_page;
    }

    public List<OffersModel> getData() {
        return data;
    }
}
