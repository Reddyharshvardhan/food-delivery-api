package com.harsha.food_delivery.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartResponse {
    private Integer id;
    private List<CartItemResponse> items = new ArrayList<>();
    private BigDecimal totalCartPrice = BigDecimal.ZERO;

    public CartResponse() {
    }

    public CartResponse(Integer id, List<CartItemResponse> items, BigDecimal totalCartPrice) {
        this.id = id;
        this.items = items;
        this.totalCartPrice = totalCartPrice;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items;
    }

    public BigDecimal getTotalCartPrice() {
        return totalCartPrice;
    }

    public void setTotalCartPrice(BigDecimal totalCartPrice) {
        this.totalCartPrice = totalCartPrice;
    }
}
