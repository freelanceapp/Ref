package com.apps.ref.models;

import java.io.Serializable;
import java.util.List;

public class FamilyModel implements Serializable {

    private int id;
    private String name;
    private String phone_code;
    private String phone;
    private String user_type;
    private String email;
    private String logo;
    private String banner;
    private String iban_number;
    private String address;
    private double latitude;
    private double longitude;
    private String receive_notifications;
    private String show_phone;
    private String software_type;
    private String gender;
    private int String_of_birth;
    private String hoppy;
    private String nationality;
    private String car_type;
    private String car_model;
    private String manufacturing_year;
    private String account_number;
    private String owner_account;
    private String resident_identity_img;
    private String driving_license_img;
    private String the_car_from_the_front_img;
    private String the_car_from_the_back_img;
    private double rate;
    private String register_code;
    private String register_link;
    private int id_country;
    private String is_login;
    private int last_logout;
    private String is_confirmed;
    private String confirmed_code;
    private String forget_password_code;
    private String is_block;
    private String package_finished_at;
    private String email_verified_at;
    private String created_at;
    private String updated_at;
private List<SingleSubCategoryModel>  family_categories;
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone_code() {
        return phone_code;
    }

    public String getPhone() {
        return phone;
    }

    public String getUser_type() {
        return user_type;
    }

    public String getEmail() {
        return email;
    }

    public String getLogo() {
        return logo;
    }

    public String getBanner() {
        return banner;
    }

    public String getIban_number() {
        return iban_number;
    }

    public String getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getReceive_notifications() {
        return receive_notifications;
    }

    public String getShow_phone() {
        return show_phone;
    }

    public String getSoftware_type() {
        return software_type;
    }

    public String getGender() {
        return gender;
    }

    public int getString_of_birth() {
        return String_of_birth;
    }

    public String getHoppy() {
        return hoppy;
    }

    public String getNationality() {
        return nationality;
    }

    public String getCar_type() {
        return car_type;
    }

    public String getCar_model() {
        return car_model;
    }

    public String getManufacturing_year() {
        return manufacturing_year;
    }

    public String getAccount_number() {
        return account_number;
    }

    public String getOwner_account() {
        return owner_account;
    }

    public String getResident_identity_img() {
        return resident_identity_img;
    }

    public String getDriving_license_img() {
        return driving_license_img;
    }

    public String getThe_car_from_the_front_img() {
        return the_car_from_the_front_img;
    }

    public String getThe_car_from_the_back_img() {
        return the_car_from_the_back_img;
    }

    public double getRate() {
        return rate;
    }

    public String getRegister_code() {
        return register_code;
    }

    public String getRegister_link() {
        return register_link;
    }

    public int getId_country() {
        return id_country;
    }

    public String getIs_login() {
        return is_login;
    }

    public int getLast_logout() {
        return last_logout;
    }

    public String getIs_confirmed() {
        return is_confirmed;
    }

    public String getConfirmed_code() {
        return confirmed_code;
    }

    public String getForget_password_code() {
        return forget_password_code;
    }

    public String getIs_block() {
        return is_block;
    }

    public String getPackage_finished_at() {
        return package_finished_at;
    }

    public String getEmail_verified_at() {
        return email_verified_at;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public List<SingleSubCategoryModel> getFamily_categories() {
        return family_categories;
    }
}
