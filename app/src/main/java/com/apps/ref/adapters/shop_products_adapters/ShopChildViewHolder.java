package com.apps.ref.adapters.shop_products_adapters;

import com.apps.ref.databinding.ProductChildRowBinding;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

public class ShopChildViewHolder extends ChildViewHolder {
    public ProductChildRowBinding binding;
    public ShopChildViewHolder(ProductChildRowBinding binding) {
        super(binding.getRoot());
        this.binding =binding;
    }
}
