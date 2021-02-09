package com.apps.ref.models;

import java.io.Serializable;

public class AddOrderFamilyTextModel implements Serializable {
    private int user_id;
    private int family_id = 0;
    private String order_type="";
    private String google_place_id = "";
    private double bill_cost = 0.0;
    private String to_address="";
    private double to_latitude=0.0;
    private double to_longitude=0.0;
    private String from_address="";
    private String from_name="";
    private double from_latitude=0.0;
    private double from_longitude=0.0;
    private String end_shipping_time="";
    private String coupon_id = "0";
    private String order_description="";
    private String order_notes="";
    private String payment_method = "";
    private String hour_arrival_time="";

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getFamily_id() {
        return family_id;
    }

    public void setFamily_id(int family_id) {
        this.family_id = family_id;
    }

    public String getOrder_type() {
        return order_type;
    }

    public void setOrder_type(String order_type) {
        this.order_type = order_type;
    }

    public String getGoogle_place_id() {
        return google_place_id;
    }

    public void setGoogle_place_id(String google_place_id) {
        this.google_place_id = google_place_id;
    }

    public double getBill_cost() {
        return bill_cost;
    }

    public void setBill_cost(double bill_cost) {
        this.bill_cost = bill_cost;
    }

    public String getTo_address() {
        return to_address;
    }

    public void setTo_address(String to_address) {
        this.to_address = to_address;
    }

    public double getTo_latitude() {
        return to_latitude;
    }

    public void setTo_latitude(double to_latitude) {
        this.to_latitude = to_latitude;
    }

    public double getTo_longitude() {
        return to_longitude;
    }

    public void setTo_longitude(double to_longitude) {
        this.to_longitude = to_longitude;
    }

    public String getFrom_address() {
        return from_address;
    }

    public void setFrom_address(String from_address) {
        this.from_address = from_address;
    }

    public String getFrom_name() {
        return from_name;
    }

    public void setFrom_name(String from_name) {
        this.from_name = from_name;
    }

    public double getFrom_latitude() {
        return from_latitude;
    }

    public void setFrom_latitude(double from_latitude) {
        this.from_latitude = from_latitude;
    }

    public double getFrom_longitude() {
        return from_longitude;
    }

    public void setFrom_longitude(double from_longitude) {
        this.from_longitude = from_longitude;
    }

    public String getEnd_shipping_time() {
        return end_shipping_time;
    }

    public void setEnd_shipping_time(String end_shipping_time) {
        this.end_shipping_time = end_shipping_time;
    }

    public String getCoupon_id() {
        return coupon_id;
    }

    public void setCoupon_id(String coupon_id) {
        this.coupon_id = coupon_id;
    }

    public String getOrder_description() {
        return order_description;
    }

    public void setOrder_description(String order_description) {
        this.order_description = order_description;
    }

    public String getOrder_notes() {
        return order_notes;
    }

    public void setOrder_notes(String order_notes) {
        this.order_notes = order_notes;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }

    public String getHour_arrival_time() {
        return hour_arrival_time;
    }

    public void setHour_arrival_time(String hour_arrival_time) {
        this.hour_arrival_time = hour_arrival_time;
    }
}
