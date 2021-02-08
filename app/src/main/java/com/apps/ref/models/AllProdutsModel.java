package com.apps.ref.models;

import java.io.Serializable;
import java.util.List;

public class AllProdutsModel implements Serializable {
    private List<SingleProductModel> data;
private int status;
    public List<SingleProductModel> getData() {
        return data;
    }

    public int getStatus() {
        return status;
    }
}