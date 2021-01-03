package com.apps.ref.adapters.shop_products_adapters;

import com.apps.ref.databinding.GroupTitleRowBinding;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

public class ShopGroupViewHolder extends GroupViewHolder {
    public GroupTitleRowBinding binding;
    public ShopGroupViewHolder(GroupTitleRowBinding binding) {
        super(binding.getRoot());
        this.binding =binding;
    }


}
