package com.apps.ref.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;


import com.apps.ref.R;
import com.apps.ref.activities_fragments.family.activity_add_family_product.AddOrderFamilyProductActivity;
import com.apps.ref.databinding.CartProductRowBinding;
import com.apps.ref.models.SingleProductModel;

import java.util.List;

import io.paperdb.Paper;

public class CartProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SingleProductModel> list;
    private Context context;
    private LayoutInflater inflater;
    private String lang;
    private AddOrderFamilyProductActivity activity;

    public CartProductAdapter(List<SingleProductModel> list, Context context) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        Paper.init(context);
        lang = Paper.book().read("lang","ar");
        activity = (AddOrderFamilyProductActivity) context;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        CartProductRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.cart_product_row, parent, false);
        return new MyHolder(binding);


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MyHolder myHolder = (MyHolder) holder;
        SingleProductModel model = list.get(position);
        myHolder.binding.setModel(model);
        myHolder.binding.tvIncrease.setOnClickListener(v -> {
            int pos = myHolder.getAdapterPosition();
            SingleProductModel productModel = list.get(pos);
            int count = productModel.getCount()+1;
            productModel.setCount(count);
            myHolder.binding.setModel(productModel);
            activity.updateItemCount(productModel,pos);
            notifyItemChanged(pos);

        });

        myHolder.binding.tvDecrease.setOnClickListener(v -> {
            int pos = myHolder.getAdapterPosition();
            SingleProductModel productModel = list.get(pos);
            int count = productModel.getCount();
            if (count>1){
                count -=1;
                productModel.setCount(count);
                myHolder.binding.setModel(productModel);
                activity.updateItemCount(productModel,pos);
                notifyItemChanged(pos);

            }

        });



    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        public CartProductRowBinding binding;

        public MyHolder(@NonNull CartProductRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }




}
