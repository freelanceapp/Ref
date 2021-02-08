package com.apps.ref.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;


import com.apps.ref.R;
import com.apps.ref.activities_fragments.activity_product_family.ProductFamilyActivity;
import com.apps.ref.databinding.ItemProductFamilyBinding;
import com.apps.ref.models.AllFamilyModel;
import com.apps.ref.models.FamilyModel;

import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;

public class FamilyAdapter extends RecyclerView.Adapter<FamilyAdapter.ProductFamilyAdapterVH> {

    private List<AllFamilyModel.Data> familyList;
    private Context context;
    private LayoutInflater inflater;
    private String lang;
    private ProductFamilyActivity activity;
    private String categoryTitle;

    public void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
    }

    public FamilyAdapter(List<AllFamilyModel.Data> familyList, Context context) {
        this.familyList = familyList;
        this.context = context;
        inflater = LayoutInflater.from(context);
        activity = (ProductFamilyActivity) context;
        Paper.init(context);
        lang = Paper.book().read("lang", Locale.getDefault().getLanguage());

    }

    @NonNull
    @Override
    public ProductFamilyAdapterVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductFamilyBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_product_family, parent, false);
        return new ProductFamilyAdapterVH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductFamilyAdapterVH holder, int position) {

        holder.binding.setModel(familyList.get(position).getFamilyModel());
        holder.binding.setCategoryTitle(categoryTitle);
        holder.binding.setLang(lang);
        holder.itemView.setOnClickListener(view -> {
            FamilyModel familyModel = familyList.get(holder.getAdapterPosition()).getFamilyModel();
            activity.navigateToFamilyActivity(familyModel);


        });


    }

    @Override
    public int getItemCount() {
        return familyList.size();
    }

    public class ProductFamilyAdapterVH extends RecyclerView.ViewHolder {
        public ItemProductFamilyBinding binding;

        public ProductFamilyAdapterVH(@NonNull ItemProductFamilyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }


}
