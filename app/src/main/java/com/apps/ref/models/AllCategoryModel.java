package com.apps.ref.models;

import java.io.Serializable;
import java.util.List;

public class AllCategoryModel implements Serializable {
    private List<SingleCategoryFamilyModel> data;
    private int status;

    public List<SingleCategoryFamilyModel> getData() {
        return data;
    }

    public int getStatus() {
        return status;
    }
}