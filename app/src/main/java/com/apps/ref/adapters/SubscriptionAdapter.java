package com.apps.ref.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;


import com.apps.ref.R;
import com.apps.ref.activities_fragments.activity_subscription.SubscriptionActivity;
import com.apps.ref.databinding.ItemSubscritionBinding;
import com.apps.ref.models.SubscriptionModel;

import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.SubscriptionAdapterVH> {

    private List<SubscriptionModel> list;
    private Context context;
    private LayoutInflater inflater;
    private String lang;
    private SubscriptionActivity activity;


    public SubscriptionAdapter(List<SubscriptionModel> subscriptionList, Context context) {
        this.list = subscriptionList;
        this.context = context;
        inflater = LayoutInflater.from(context);
        Paper.init(context);
        lang = Paper.book().read("lang", Locale.getDefault().getLanguage());
        activity = (SubscriptionActivity) context;

    }

    @NonNull
    @Override
    public SubscriptionAdapterVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSubscritionBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_subscrition, parent, false);
        return new SubscriptionAdapterVH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionAdapterVH holder, int position) {
        SubscriptionModel model = list.get(position);
        holder.binding.setModel(model);
        holder.binding.setLang(lang);
        holder.binding.btnBuy.setOnClickListener(view -> {
            SubscriptionModel model2 = list.get(holder.getAdapterPosition());

            //.buyPackage(model2);
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class SubscriptionAdapterVH extends RecyclerView.ViewHolder {
        public ItemSubscritionBinding binding;

        public SubscriptionAdapterVH(@NonNull ItemSubscritionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

}
