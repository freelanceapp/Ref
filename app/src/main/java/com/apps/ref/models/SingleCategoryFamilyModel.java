package com.apps.ref.models;

import java.io.Serializable;
import java.util.List;

public class SingleCategoryFamilyModel {
    private int id;
    private String title;
    private Object desc;
    private String image;
    private Object parent_id;
    private String level;
    private String is_shown;
    private String created_at;
    private String updated_at;

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Object getDesc() {
        return desc;
    }

    public String getImage() {
        return image;
    }

    public Object getParent_id() {
        return parent_id;
    }

    public String getLevel() {
        return level;
    }

    public String getIs_shown() {
        return is_shown;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }


}
