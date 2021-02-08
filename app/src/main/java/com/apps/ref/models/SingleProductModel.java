package com.apps.ref.models;

import java.io.Serializable;
import java.util.List;

public class SingleProductModel implements Serializable {

    public int id;
    public String title;
    public String desc;
    public String main_image;
    public int family_id;
    public int category_id;
    public double price;
    public double old_price;
    public String have_offer;
    public String offer_type;
    public int offer_value;
    public String offer_started_at;
    public String offer_finished_at;
    public double rating_value;
    public String is_shown;
    public String created_at;
    public String updated_at;
    private List<ImageModel> product_images;
    private int count = 1;

    public static class ImageModel implements Serializable {
        private int id;
        private String image;
        private int product_id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public int getProduct_id() {
            return product_id;
        }

        public void setProduct_id(int product_id) {
            this.product_id = product_id;
        }
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

    public String getMain_image() {
        return main_image;
    }

    public int getFamily_id() {
        return family_id;
    }

    public int getCategory_id() {
        return category_id;
    }

    public double getPrice() {
        return price;
    }

    public double getOld_price() {
        return old_price;
    }

    public String getHave_offer() {
        return have_offer;
    }

    public String getOffer_type() {
        return offer_type;
    }

    public int getOffer_value() {
        return offer_value;
    }

    public String getOffer_started_at() {
        return offer_started_at;
    }

    public String getOffer_finished_at() {
        return offer_finished_at;
    }

    public double getRating_value() {
        return rating_value;
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

    public List<ImageModel> getProduct_images() {
        return product_images;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
