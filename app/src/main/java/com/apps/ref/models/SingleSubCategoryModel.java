package com.apps.ref.models;

import java.io.Serializable;

public class SingleSubCategoryModel implements Serializable {
  private int id;
  private String title;
  private String desc;
  private String image;
  private int family_id;
  private int family_basic_category_id;
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

    public String getDesc() {
        return desc;
    }

    public String getImage() {
        return image;
    }

    public int getFamily_id() {
        return family_id;
    }

    public int getFamily_basic_category_id() {
        return family_basic_category_id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }
}
