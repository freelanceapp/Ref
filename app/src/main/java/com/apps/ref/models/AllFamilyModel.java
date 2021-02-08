package com.apps.ref.models;

import android.widget.LinearLayout;

import java.io.Serializable;
import java.util.List;

public class AllFamilyModel implements Serializable {
    private List<Data> data;

    public List<Data> getData() {
        return data;
    }

    public class Data implements Serializable{
       private FamilyModel family;

       public FamilyModel getFamilyModel() {
           return family;
       }
   }
}