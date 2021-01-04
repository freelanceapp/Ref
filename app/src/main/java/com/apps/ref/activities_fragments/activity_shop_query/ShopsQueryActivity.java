package com.apps.ref.activities_fragments.activity_shop_query;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.apps.ref.R;
import com.apps.ref.activities_fragments.activity_shop_details.ShopDetailsActivity;
import com.apps.ref.activities_fragments.activity_shop_map.ShopMapActivity;
import com.apps.ref.adapters.NearbyAdapter3;
import com.apps.ref.databinding.ActivityShopsQueryBinding;
import com.apps.ref.language.Language;
import com.apps.ref.models.CategoryModel;
import com.apps.ref.models.CustomPlaceDataModel;
import com.apps.ref.models.NearbyModel;
import com.apps.ref.models.UserModel;
import com.apps.ref.preferences.Preferences;
import com.apps.ref.remote.Api;
import com.apps.ref.tags.Tags;
import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopsQueryActivity extends AppCompatActivity {
    private ActivityShopsQueryBinding binding;
    private List<NearbyModel.Result> resultList;
    private NearbyAdapter3 adapter;
    private double user_lat;
    private double user_lng;
    private SkeletonScreen skeletonScreen;
    private String lang;
    private boolean hasManyPages = false;
    private boolean isLoading = false;
    private String query = "";
    private String next_page="";
    private CategoryModel categoryModel;
    private Preferences preferences;
    private UserModel userModel;
    private List<NearbyModel.Result> resultListFiltered;

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.onAttach(newBase,Paper.book().read("lang","ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_shops_query);
        getDataFromIntent();
        initView();
    }

    private void initView() {
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        if (lang.equals("ar")){
            binding.setQuery(categoryModel.getTitle_ar());

        }else {
            binding.setQuery(categoryModel.getTitle_en());

        }
        resultList = new ArrayList<>();
        binding.recView.setLayoutManager(new LinearLayoutManager(this));

        String currency=getString(R.string.sar);
        if (userModel!=null){
            currency = userModel.getUser().getCountry().getWord().getCurrency();
        }
        adapter = new NearbyAdapter3(resultList,this,user_lat,user_lng,currency);
        binding.recView.setAdapter(adapter);

        skeletonScreen = Skeleton.bind(binding.recView)
                .adapter(adapter)
                .count(5)
                .frozen(false)
                .shimmer(true)
                .show();

        binding.recView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy>0){
                    int totalItem = adapter.getItemCount();
                    LinearLayoutManager manager = (LinearLayoutManager) binding.recView.getLayoutManager();
                    int pos = manager.findLastCompletelyVisibleItemPosition();
                    if (hasManyPages&&totalItem>=20&&(totalItem-pos==2)&&!isLoading){
                        isLoading = true;
                        loadMore();
                    }
                }
            }
        });


        binding.close.setOnClickListener(v -> super.onBackPressed());
        getShops(query);
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        user_lat = intent.getDoubleExtra("lat",0.0);
        user_lng = intent.getDoubleExtra("lng",0.0);
        categoryModel = (CategoryModel) intent.getSerializableExtra("data");

        query = categoryModel.getKeyword_google();
    }


    private void getShops(String query) {
        resultList.clear();
        adapter.notifyDataSetChanged();
        binding.tvNoData.setVisibility(View.GONE);
        skeletonScreen.show();

        String loc = user_lat+","+user_lng;
        Log.e("query",query);
        Log.e("loc2",loc);
        Api.getService("https://maps.googleapis.com/maps/api/")
                .nearbyPlaceRankBy(loc,query,"distance",lang,"",getString(R.string.map_api_key))
                .enqueue(new Callback<NearbyModel>() {
                    @Override
                    public void onResponse(Call<NearbyModel> call, Response<NearbyModel> response) {
                        if (response.isSuccessful()&&response.body()!=null)
                        {
                            if (response.body().getStatus().equals("OK")){

                                if (response.body().getNext_page_token()!=null){
                                    hasManyPages = true;
                                    next_page = response.body().getNext_page_token();
                                }else {
                                    hasManyPages = false;
                                    next_page = "";
                                }

                                if (response.body().getResults().size()>0)
                                {
                                    calculateDistance(response.body().getResults());
                                    binding.tvNoData.setVisibility(View.GONE);

                                }else
                                {
                                    skeletonScreen.hide();
                                    binding.tvNoData.setVisibility(View.VISIBLE);

                                }
                            }else {
                                skeletonScreen.hide();
                                binding.tvNoData.setVisibility(View.VISIBLE);

                            }

                        }else
                        {

                            skeletonScreen.hide();

                            try {
                                Log.e("error_code",response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<NearbyModel> call, Throwable t) {
                        try {


                        if (t.getMessage() != null) {
                                Log.e("error", t.getMessage());
                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(ShopsQueryActivity.this, R.string.something, Toast.LENGTH_SHORT).show();
                                }
                                else if (t.getMessage().toLowerCase().contains("socket")||t.getMessage().toLowerCase().contains("canceled")){ }

                                else {
                                    Toast.makeText(ShopsQueryActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            Log.e("Error",t.getMessage());
                            skeletonScreen.hide();
                        }catch (Exception e)
                        {

                        }
                    }
                });
    }

    private void loadMore() {

        resultList.add(null);
        adapter.notifyItemInserted(resultList.size()-1);


        String loc = user_lat+","+user_lng;
        Log.e("loc",loc);
        Api.getService("https://maps.googleapis.com/maps/api/")
                .nearbyPlaceRankBy(loc,query,"distance",lang,next_page,getString(R.string.map_api_key))
                .enqueue(new Callback<NearbyModel>() {
                    @Override
                    public void onResponse(Call<NearbyModel> call, Response<NearbyModel> response) {


                        if (response.isSuccessful()&&response.body()!=null)
                        {
                            if (response.body().getStatus().equals("OK")){

                                if (response.body().getNext_page_token()!=null){
                                    hasManyPages = true;
                                    next_page = response.body().getNext_page_token();
                                }else {
                                    hasManyPages = false;
                                    next_page = "";
                                }
                                if (response.body().getResults().size()>0)
                                {

                                    calculateDistanceLoadMore(response.body().getResults());
                                }else {
                                    if (resultList.get(resultList.size()-1)==null){
                                        resultList.remove(resultList.size()-1);
                                        adapter.notifyItemRemoved(resultList.size()-1);
                                    }
                                }
                            }

                        }else
                        {


                            try {
                                Log.e("error_code",response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<NearbyModel> call, Throwable t) {
                        try {
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage());
                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(ShopsQueryActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
                                }
                                else if (t.getMessage().toLowerCase().contains("socket")||t.getMessage().toLowerCase().contains("canceled")){ }
                                else {
                                    Toast.makeText(ShopsQueryActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }catch (Exception e)
                        {

                        }
                    }
                });




    }

    private void calculateDistance(List<NearbyModel.Result> results){
        resultListFiltered = new ArrayList<>();

        for (int i =0 ;i<results.size();i++){
            NearbyModel.Result result = results.get(i);

            if (result!=null){


                LatLng user_location = new LatLng(user_lat,user_lng);
                LatLng place_location = new LatLng(result.getGeometry().getLocation().getLat(),result.getGeometry().getLocation().getLng());
                double distance = getDistance(user_location,place_location);
                result.setDistance(distance);
                resultListFiltered.add(result);
            }

        }


        if (resultListFiltered.size()>0){
            getPlaceDataByGooglePlaceId(0);

        }else {
            binding.tvNoData.setVisibility(View.VISIBLE);

        }

    }

    private void calculateDistanceLoadMore(List<NearbyModel.Result> results){

        List<NearbyModel.Result> resultListFiltered = new ArrayList<>();

        for (int i =0 ;i<results.size();i++){
            NearbyModel.Result result = results.get(i);

            if (result!=null){

                LatLng user_location = new LatLng(user_lat,user_lng);
                LatLng place_location = new LatLng(result.getGeometry().getLocation().getLat(),result.getGeometry().getLocation().getLng());
                double distance = getDistance(user_location,place_location);
                Log.e("dist",distance+"___"+user_lat+"__"+user_lng+result.getGeometry().getLocation().getLat()+"__"+result.getGeometry().getLocation().getLng());
                result.setDistance(distance);
                resultListFiltered.add(result);
            }

        }


       getPlaceDataByGooglePlaceIdLoadMore(0,resultListFiltered);


        //sortData();

    }

    private void sortData(){
        Collections.sort(resultList, (o1, o2) -> {



            if (o1!=null&&o2!=null){
                if (o1.getDistance()<o2.getDistance()){
                    return -1;
                }else if (o1.getDistance()>o2.getDistance()){
                    return 1;
                }else{
                    return 0;

                }
            }else {
                return 0;
            }

        });

        adapter.notifyDataSetChanged();


    }

    private double getDistance(LatLng latLng1,LatLng latLng2){
        return SphericalUtil.computeDistanceBetween(latLng1,latLng2)/1000;
    }

    public void setShopData(NearbyModel.Result placeModel) {
        if (isRestaurant(placeModel)){
            //if has menu image or products

            Intent intent = new Intent(this, ShopDetailsActivity.class);
            intent.putExtra("data",placeModel);
            startActivity(intent);


        }else {
            Intent intent = new Intent(this, ShopMapActivity.class);
            intent.putExtra("data",placeModel);
            startActivity(intent);
        }

    }


    private boolean isRestaurant(NearbyModel.Result result){

        for (String type :result.getTypes()){
            if (type.equals("restaurant")){
                return true;
            }
        }

        return false;
    }


    private void getPlaceDataByGooglePlaceId(int index)
    {
        if (index<resultListFiltered.size()){

            Api.getService(Tags.base_url)
                    .getCustomPlaceByGooglePlaceId(resultListFiltered.get(index).getPlace_id())
                    .enqueue(new Callback<CustomPlaceDataModel>() {
                        @Override
                        public void onResponse(Call<CustomPlaceDataModel> call, Response<CustomPlaceDataModel> response) {
                            if (response.isSuccessful()) {

                                NearbyModel.Result result = resultListFiltered.get(index);
                                result.setCustomPlaceModel(response.body().getData());
                                resultListFiltered.set(index,result);

                                int newIndex = index+1;
                                getPlaceDataByGooglePlaceId(newIndex);


                            } else {

                                int newIndex = index+1;
                                getPlaceDataByGooglePlaceId(newIndex);

                                try {
                                    Log.e("error_code", response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }


                        }

                        @Override
                        public void onFailure(Call<CustomPlaceDataModel> call, Throwable t) {
                            try {
                                skeletonScreen.hide();

                                int newIndex = index+1;
                                getPlaceDataByGooglePlaceId(newIndex);

                                Log.e("Error", t.getMessage());
                                Toast.makeText(ShopsQueryActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
                            } catch (Exception e) {

                            }
                        }
                    });
        }else {
            skeletonScreen.hide();
            resultList.clear();
            resultList.addAll(resultListFiltered);
            adapter.notifyDataSetChanged();
            //sortData();
        }
    }


    private void getPlaceDataByGooglePlaceIdLoadMore(int index, List<NearbyModel.Result> results)
    {

        if (index<results.size()){

            Api.getService(Tags.base_url)
                    .getCustomPlaceByGooglePlaceId(results.get(index).getPlace_id())
                    .enqueue(new Callback<CustomPlaceDataModel>() {
                        @Override
                        public void onResponse(Call<CustomPlaceDataModel> call, Response<CustomPlaceDataModel> response) {
                            if (response.isSuccessful()) {

                                NearbyModel.Result result = results.get(index);
                                result.setCustomPlaceModel(response.body().getData());
                                results.set(index,result);

                                int newIndex = index+1;
                                getPlaceDataByGooglePlaceIdLoadMore(newIndex,results);


                            } else {

                                int newIndex = index+1;
                                getPlaceDataByGooglePlaceIdLoadMore(newIndex,results);

                                try {
                                    Log.e("error_code", response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }


                        }

                        @Override
                        public void onFailure(Call<CustomPlaceDataModel> call, Throwable t) {
                            try {
                                int newIndex = index+1;
                                getPlaceDataByGooglePlaceIdLoadMore(newIndex,results);

                                Log.e("Error", t.getMessage());
                                Toast.makeText(ShopsQueryActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
                            } catch (Exception e) {

                            }
                        }
                    });
        }else {
            isLoading = false;
            if (resultList.get(resultList.size()-1)==null){
                resultList.remove(resultList.size()-1);
                adapter.notifyItemRemoved(resultList.size()-1);
            }
            int oldPos = resultList.size();
            resultList.addAll(results);

            int newPos = resultList.size();
            adapter.notifyItemRangeChanged(oldPos,newPos);
        }
    }
}