package com.apps.ref.activities_fragments.activity_subscription;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.apps.ref.R;
import com.apps.ref.adapters.SubscriptionAdapter;
import com.apps.ref.databinding.ActivitySubscriptionBinding;
import com.apps.ref.interfaces.Listeners;
import com.apps.ref.language.Language;
import com.apps.ref.models.PackageResponse;
import com.apps.ref.models.SubscriptionDataModel;
import com.apps.ref.models.SubscriptionModel;
import com.apps.ref.models.UserModel;
import com.apps.ref.preferences.Preferences;
import com.apps.ref.remote.Api;
import com.apps.ref.share.Common;
import com.apps.ref.tags.Tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.apps.ref.tags.Tags.base_url;


public class SubscriptionActivity extends AppCompatActivity implements Listeners.BackListener {


    private ActivitySubscriptionBinding binding;
    private String lang;
    private SubscriptionAdapter adapter;
    private List<SubscriptionModel> subscriptionModelList;
    private UserModel userModel;
    private Preferences preferences;



    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.onAttach(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_subscription);
        initView();
    }

    private void initView() {
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        subscriptionModelList = new ArrayList<>();
        Paper.init(this);
        lang = Paper.book().read("lang", Locale.getDefault().getLanguage());
        binding.setLang(lang);
        binding.setBackListener(this);
        adapter = new SubscriptionAdapter(subscriptionModelList,this);
        binding.recView.setLayoutManager(new LinearLayoutManager(this));
        binding.recView.setAdapter(adapter);
        binding.back.setOnClickListener(view -> {

            back();
        });

        getPackage();
    }

    private void getPackage() {
        Api.getService(base_url).getSubscription()
                .enqueue(new Callback<SubscriptionDataModel>() {
                    @Override
                    public void onResponse(Call<SubscriptionDataModel> call, Response<SubscriptionDataModel> response) {
                        binding.progBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                subscriptionModelList.clear();
                                subscriptionModelList.addAll(response.body().getData());
                                if (subscriptionModelList.size()>0){
                                    binding.tvNoData.setVisibility(View.GONE);
                                    adapter.notifyDataSetChanged();
                                }else {
                                    binding.tvNoData.setVisibility(View.VISIBLE);

                                }
                            }
                        } else {
                            binding.progBar.setVisibility(View.GONE);

                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<SubscriptionDataModel> call, Throwable t) {
                        try {
                            binding.progBar.setVisibility(View.GONE);

                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(SubscriptionActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(SubscriptionActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });
    }


    public void buyPackage(SubscriptionModel model) {

        ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();

        Api.getService(Tags.base_url)
                .buyPackage(model.getId(),userModel.getUser().getId(),model.getPrice())
                .enqueue(new Callback<PackageResponse>() {
                    @Override
                    public void onResponse(Call<PackageResponse> call, Response<PackageResponse> response) {
                        dialog.dismiss();
                        if (response.isSuccessful() && response.body() != null) {
//                            Intent intent = new Intent(SubscriptionActivity.this, WebViewActivity.class);
//                            intent.putExtra("data",response.body());
//                            startActivityForResult(intent,100);
                        } else {
                            if (response.code() == 500) {
                                Toast.makeText(SubscriptionActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("ERROR", response.message() + "");

                                Toast.makeText(SubscriptionActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            }


                        }
                    }

                    @Override
                    public void onFailure(Call<PackageResponse> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            if (t.getMessage() != null) {
                                Log.e("msg_category_error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(SubscriptionActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SubscriptionActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {
                            Log.e("Error", e.getMessage() + "__");
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==100){
            if (resultCode==RESULT_OK){
                Toast.makeText(this, getString(R.string.suc), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void back() {
        finish();
    }

    @Override
    public void onBackPressed() {

        back();
    }
}