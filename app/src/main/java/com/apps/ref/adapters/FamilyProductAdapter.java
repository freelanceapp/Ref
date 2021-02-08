package com.apps.ref.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;


import com.apps.ref.R;
import com.apps.ref.activities_fragments.activity_family.FamilyActivity;
import com.apps.ref.databinding.ItemFamilyOrderBinding;
import com.apps.ref.models.ProductModel;
import com.apps.ref.models.SingleProductModel;

import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;

public class FamilyProductAdapter extends RecyclerView.Adapter<FamilyProductAdapter.FamilyOrderAdapterVH> {

    private List<SingleProductModel> list;
    private Context context;
    private LayoutInflater inflater;
    private String lang;
    private FamilyActivity activity;


    public FamilyProductAdapter(List<SingleProductModel> list, Context context) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        Paper.init(context);
        lang = Paper.book().read("lang", "ar");
        activity = (FamilyActivity) context;


    }

    @NonNull
    @Override
    public FamilyOrderAdapterVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFamilyOrderBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_family_order, parent, false);
        return new FamilyOrderAdapterVH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FamilyOrderAdapterVH holder, int position) {

        SingleProductModel productModel = list.get(position);
        holder.binding.setModel(productModel);
        holder.binding.setLang(lang);
        holder.binding.tvOldPrice.setText(String.format(Locale.ENGLISH, "%s %s", productModel.getOld_price(), context.getString(R.string.sar)));

        if (!productModel.getHave_offer().equals("without_offer")) {
            holder.binding.tvOldPrice.setPaintFlags(holder.binding.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        holder.binding.imageIncrease.setOnClickListener(view -> {
            SingleProductModel model = list.get(holder.getAdapterPosition());
            int count = model.getCount()+1;
            model.setCount(count);
            list.set(holder.getAdapterPosition(),model);
            activity.updateProduct(model,holder.getAdapterPosition());

        });

        holder.binding.imageDecrease.setOnClickListener(view -> {
            SingleProductModel model = list.get(holder.getAdapterPosition());

            if (model.getCount()>1){
                int count = model.getCount()-1;
                model.setCount(count);
                list.set(holder.getAdapterPosition(),model);
                activity.updateProduct(model,holder.getAdapterPosition());

            }


        });


//        holder.binding.imageAddToCard.setOnClickListener(view -> {
//            ProductModel model = list.get(holder.getAdapterPosition());
//            activity.addToCart(model,holder.getAdapterPosition());
//        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class FamilyOrderAdapterVH extends RecyclerView.ViewHolder {
        public ItemFamilyOrderBinding binding;

        public FamilyOrderAdapterVH(@NonNull ItemFamilyOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }


}
