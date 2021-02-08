package com.apps.ref.activities_fragments.activity_product_family;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.apps.ref.R;
import com.apps.ref.activities_fragments.activity_family.FamilyActivity;
import com.apps.ref.adapters.FamilyAdapter;
import com.apps.ref.adapters.ProductFamilyCategoryAdapter;
import com.apps.ref.databinding.ActivityProductFamilyBinding;
import com.apps.ref.language.Language;
import com.apps.ref.models.AllFamilyModel;
import com.apps.ref.models.AllCategoryModel;
import com.apps.ref.models.FamilyModel;
import com.apps.ref.models.SingleCategoryFamilyModel;
import com.apps.ref.remote.Api;
import com.apps.ref.tags.Tags;
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen;
import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductFamilyActivity extends AppCompatActivity {


    private static final String TAG = "ProductFamilyActivity";
    private ActivityProductFamilyBinding binding;
    private String lang;
    private ProductFamilyCategoryAdapter productFamilyCategoryAdapter;
    private FamilyAdapter familyAdapter;
    private List<SingleCategoryFamilyModel> categoryList;
    private List<AllFamilyModel.Data> familyList;


    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.onAttach(newBase, Paper.book().read("lang", "ar")));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_product_family);

        initView();

    }


    private void initView() {

        Paper.init(this);
        lang = Paper.book().read("lang", Locale.getDefault().getLanguage());
        binding.setLang(lang);

        categoryList = new ArrayList<>();
        familyList = new ArrayList<>();


        productFamilyCategoryAdapter = new ProductFamilyCategoryAdapter(categoryList, this);
        binding.recViewCategory.setAdapter(productFamilyCategoryAdapter);
        binding.recViewCategory.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        familyAdapter = new FamilyAdapter(familyList, this);
        binding.recViewFamily.setAdapter(familyAdapter);
        binding.recViewFamily.setLayoutManager(new LinearLayoutManager(this));


        binding.back.setOnClickListener(view -> {

            back();
        });

        getCategories();

    }

    private void getCategories() {

        Api.getService(Tags.base_url).getCategories("off", 0, 0).enqueue(new Callback<AllCategoryModel>() {
            @Override
            public void onResponse(Call<AllCategoryModel> call, Response<AllCategoryModel> response) {
                binding.progBarCategory.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    categoryList.clear();
                    categoryList.addAll(response.body().getData());
                    if (categoryList.size() > 0) {
                        productFamilyCategoryAdapter.notifyDataSetChanged();
                        binding.tvNoData.setVisibility(View.GONE);
                    } else {
                        binding.tvNoData.setVisibility(View.VISIBLE);

                    }
                } else {
                    binding.progBarCategory.setVisibility(View.GONE);

                    switch (response.code()) {
                        case 500:
                            Toast.makeText(ProductFamilyActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(ProductFamilyActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            break;
                    }

                }
            }

            @Override
            public void onFailure(Call<AllCategoryModel> call, Throwable t) {
                try {
                    binding.progBarCategory.setVisibility(View.GONE);

                    Log.e(TAG, t.getMessage() + "__");
                    if (t.getMessage() != null) {
                        Log.e("error", t.getMessage());
                        if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                            Toast.makeText(ProductFamilyActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
                        } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                        } else {
                            Toast.makeText(ProductFamilyActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {

                }
            }
        });

    }

    private void getFamilies(int id) {

        binding.progBar.setVisibility(View.VISIBLE);
        Api.getService(Tags.base_url).getFamilies("off", 0, 0, id).enqueue(new Callback<AllFamilyModel>() {
            @Override
            public void onResponse(Call<AllFamilyModel> call, Response<AllFamilyModel> response) {
                binding.progBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    familyList.clear();
                    familyList.addAll(response.body().getData());
                    if (familyList.size() > 0) {
                        familyAdapter.notifyDataSetChanged();
                        binding.tvNoData.setVisibility(View.GONE);
                    } else {
                        binding.tvNoData.setVisibility(View.VISIBLE);

                    }
                } else {
                    binding.progBar.setVisibility(View.GONE);

                    switch (response.code()) {
                        case 500:
                            Toast.makeText(ProductFamilyActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(ProductFamilyActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            break;
                    }

                }
            }

            @Override
            public void onFailure(Call<AllFamilyModel> call, Throwable t) {
                try {
                    binding.progBar.setVisibility(View.GONE);

                    Log.e(TAG, t.getMessage() + "__");
                    if (t.getMessage() != null) {
                        Log.e("error", t.getMessage());
                        if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                            Toast.makeText(ProductFamilyActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
                        } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                        } else {
                            Toast.makeText(ProductFamilyActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {

                }
            }
        });

    }


    public void showFamilies(SingleCategoryFamilyModel category) {

        familyList.clear();
        familyAdapter.setCategoryTitle(category.getTitle());
        getFamilies(category.getId());

    }

    public void navigateToFamilyActivity(FamilyModel familyModel) {
        Intent intent = new Intent(this, FamilyActivity.class);
        intent.putExtra("data", familyModel);
        startActivity(intent);
    }

    private void back() {
        finish();
    }


}