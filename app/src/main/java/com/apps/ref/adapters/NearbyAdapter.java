package com.apps.ref.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.ref.R;
import com.apps.ref.activities_fragments.activity_shops.ShopsActivity;
import com.apps.ref.databinding.LoadMoreRowBinding;
import com.apps.ref.databinding.ShopSearchRowBinding;
import com.apps.ref.models.NearbyModel;


import java.util.List;

public class NearbyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int DATA = 1;
    private final int LOAD = 2;
    private List<NearbyModel.Result> placeModelList;
    private Context context;
    private double user_lat = 0.0, user_lng = 0.0;
    private LayoutInflater inflater;
    private AppCompatActivity activity;
    private String currency="";

    public NearbyAdapter(List<NearbyModel.Result> placeModelList, Context context, double user_lat, double user_lng,String currency) {
        this.placeModelList = placeModelList;
        this.context = context;
        this.user_lat = user_lat;
        this.user_lng = user_lng;
        inflater = LayoutInflater.from(context);
        activity = (AppCompatActivity) context;
        this.currency = currency;

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==DATA){
            ShopSearchRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.shop_search_row, parent, false);
            return new MyHolder(binding);
        }else {
            LoadMoreRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.load_more_row, parent, false);
            return new LoadMoreHolder(binding);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyHolder){
            MyHolder myHolder = (MyHolder) holder;
            NearbyModel.Result placeModel = placeModelList.get(position);
            myHolder.binding.setModel(placeModel);

            holder.itemView.setOnClickListener(v -> {
                NearbyModel.Result placeModel1 = placeModelList.get(myHolder.getAdapterPosition());

                if (activity instanceof ShopsActivity){
                    ShopsActivity shopsActivity = (ShopsActivity) activity;
                    shopsActivity.setShopData(placeModel1);

                }

            });
        }else if (holder instanceof LoadMoreHolder){
            LoadMoreHolder loadMoreHolder = (LoadMoreHolder) holder;
            loadMoreHolder.binding.prgBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(context,R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            loadMoreHolder.binding.prgBar.setIndeterminate(true);
        }

    }

    @Override
    public int getItemCount() {
        return placeModelList.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        private ShopSearchRowBinding binding;

        public MyHolder(ShopSearchRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;


        }

    }

    public static class LoadMoreHolder extends RecyclerView.ViewHolder {
        private LoadMoreRowBinding binding;

        public LoadMoreHolder(LoadMoreRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;


        }

    }

    @Override
    public int getItemViewType(int position) {
        if (placeModelList.get(position)==null){
            return LOAD;
        }else {
            return DATA;
        }
    }
}
