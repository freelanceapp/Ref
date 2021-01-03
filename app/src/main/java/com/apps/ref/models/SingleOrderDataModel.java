package com.apps.ref.models;

import java.io.Serializable;

public class SingleOrderDataModel implements Serializable {
    private OrderModel order;

    public OrderModel getOrder() {
        return order;
    }
}
