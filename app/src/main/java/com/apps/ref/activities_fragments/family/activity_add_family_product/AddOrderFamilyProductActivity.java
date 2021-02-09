package com.apps.ref.activities_fragments.family.activity_add_family_product;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.apps.ref.R;
import com.apps.ref.activities_fragments.activity_add_coupon.AddCouponActivity;
import com.apps.ref.activities_fragments.family.activity_map_delivery_location.MapDeliveryLocationActivity;
import com.apps.ref.adapters.AddOrderImagesAdapter;
import com.apps.ref.adapters.CartProductAdapter;
import com.apps.ref.databinding.ActivityAddFamilyOrderProductsBinding;
import com.apps.ref.databinding.DialogAlertBinding;
import com.apps.ref.databinding.DialogSelectImage2Binding;
import com.apps.ref.language.Language;
import com.apps.ref.models.AddOrderFamilyTextModel;
import com.apps.ref.models.FamilyModel;
import com.apps.ref.models.FavoriteLocationModel;
import com.apps.ref.models.SingleOrderDataModel;
import com.apps.ref.models.SingleProductModel;
import com.apps.ref.models.UserModel;
import com.apps.ref.preferences.Preferences;
import com.apps.ref.remote.Api;
import com.apps.ref.share.Common;
import com.apps.ref.tags.Tags;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddOrderFamilyProductActivity extends AppCompatActivity {
    private ActivityAddFamilyOrderProductsBinding binding;
    private String lang;
    private final String READ_PERM = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final String write_permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private final String camera_permission = Manifest.permission.CAMERA;
    private final int READ_REQ = 1, CAMERA_REQ = 2;
    private List<Uri> imagesList;
    private AlertDialog dialog;
    private AddOrderImagesAdapter addOrderImagesAdapter;
    private UserModel userModel;
    private Preferences preferences;
    private String vat="0";
    private CartProductAdapter adapter;
    private double total_cost = 0.0;
    private FamilyModel familyModel;
    private List<SingleProductModel> productModelList;
    private AddOrderFamilyTextModel addOrderTextModel;
    String distance;
    int cost;
    private String currency;

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.onAttach(newBase, Paper.book().read("lang", "ar")));
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_family_order_products);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        familyModel = (FamilyModel) intent.getSerializableExtra("data");
        total_cost = intent.getDoubleExtra("cost",0.0);
        productModelList = (List<SingleProductModel>) intent.getSerializableExtra("products");

    }

    private void initView() {
        if (productModelList==null){
            productModelList = new ArrayList<>();
        }
        addOrderTextModel = new AddOrderFamilyTextModel();
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        imagesList = new ArrayList<>();
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        binding.setPriceBeforeVat("0");
        binding.setTotalCost(String.format(Locale.ENGLISH,"%.2f",total_cost));
        binding.setModel(familyModel);
        binding.setVat(vat);
        currency = getString(R.string.sar);
        if (userModel!=null){
            currency = userModel.getUser().getCountry().getWord().getCurrency();
        }
        binding.setCurrency(currency);
        binding.recViewImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false));
        addOrderImagesAdapter = new AddOrderImagesAdapter(imagesList,this);
        binding.recViewImages.setAdapter(addOrderImagesAdapter);

        binding.imageCamera.setOnClickListener(v -> createDialogAlert());
        binding.tvAddCoupon.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddCouponActivity.class);
            startActivityForResult(intent,100);
        });
        binding.close.setOnClickListener(v -> {super.onBackPressed();});
        binding.tvAddComment.setOnClickListener(v -> {
            binding.tvAddComment.setVisibility(View.GONE);
            binding.llNotes.setVisibility(View.VISIBLE);
        });

        binding.btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapDeliveryLocationActivity.class);
            intent.putExtra("data",familyModel);
            startActivityForResult(intent,200);
        });
        binding.recViewProducts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartProductAdapter(productModelList,this);
        binding.recViewProducts.setAdapter(adapter);
        addOrderTextModel.setUser_id(userModel.getUser().getId());
        addOrderTextModel.setFamily_id(familyModel.getId());
        addOrderTextModel.setOrder_type("family");
        addOrderTextModel.setGoogle_place_id("");
        addOrderTextModel.setBill_cost(total_cost);
        addOrderTextModel.setFrom_address(familyModel.getAddress());
        addOrderTextModel.setFrom_latitude(familyModel.getLatitude());
        addOrderTextModel.setFrom_longitude(familyModel.getLongitude());
        addOrderTextModel.setFrom_name(familyModel.getName());
        addOrderTextModel.setCoupon_id("0");
        addOrderTextModel.setEnd_shipping_time("");
        addOrderTextModel.setOrder_notes("");
//        binding.cash.setVisibility(View.GONE);
//        binding.frcard.setBackgroundResource(R.drawable.small_stroke_primary2);
//        binding.frcash.setBackgroundResource(0);
//        binding.iconcash.setVisibility(View.GONE);
//        binding.iconcard.setVisibility(View.VISIBLE);
        addOrderTextModel.setPayment_method("online");
//        binding.card.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                binding.frcard.setBackgroundResource(R.drawable.small_stroke_primary2);
//                binding.frcash.setBackgroundResource(0);
//                binding.iconcash.setVisibility(View.GONE);
//                binding.iconcard.setVisibility(View.VISIBLE);
//                addOrderTextModel.setPayment_method("online");
//            }
//        });
//        binding.cash.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                binding.frcash.setBackgroundResource(R.drawable.small_stroke_primary2);
//                binding.frcard.setBackgroundResource(0);
//                binding.iconcash.setVisibility(View.VISIBLE);
//                binding.iconcard.setVisibility(View.GONE);
//                addOrderTextModel.setPayment_method("cash");
//
//            }
//        });
        getVAT();


    }
//    private void getDelevryCost(int distance2)
//    {
//        ProgressDialog progressDialog= Common.createProgressDialog(this,getString(R.string.wait));
//        progressDialog.setCancelable(false);
//        progressDialog.setCanceledOnTouchOutside(false);
//        progressDialog.show();
//        Api.getService(Tags.base_url)
//                .getDeleveryCost(distance2)
//                .enqueue(new Callback<DeleveryCostModel>() {
//                    @Override
//                    public void onResponse(Call<DeleveryCostModel> call, Response<DeleveryCostModel> response) {
//                        progressDialog.dismiss();
//
//                        if (response.isSuccessful() && response.body() != null) {
//                            cost=response.body().getDelivery_cost();
//                            CreateDialogAlert3(AddOrderFamilyProductActivity.this,getString(R.string.delevery_cost)+" = "+cost+" "+getString(R.string.sar));
//
//                            Log.e("ddddddd",response.body().getDelivery_cost()+"");
//                        } else {
//                            progressDialog.dismiss();
//
//                            try {
//                                Log.e("error_code", response.errorBody().string());
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//
//                    }
//
//                    @Override
//                    public void onFailure(Call<DeleveryCostModel> call, Throwable t) {
//                        progressDialog.dismiss();
//
//                        try {
//                            Log.e("3", "3");
//
//                            if (t.getMessage() != null) {
//                                Log.e("error", t.getMessage());
//                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
//                                    Toast.makeText(AddOrderFamilyProductActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
//                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
//                                } else {
//                                    Toast.makeText(AddOrderFamilyProductActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        } catch (Exception e) {
//
//                        }
//                    }
//                });
//    }
    private void getVAT()
    {
        calculateTotalPrice();
       /* ProgressDialog dialog = Common.createProgressDialog(this,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Api.getService(Tags.base_url).getOfferSetting().enqueue(new Callback<OfferSettingModel>() {
            @Override
            public void onResponse(Call<OfferSettingModel> call, Response<OfferSettingModel> response) {
                dialog.dismiss();

                if (response.isSuccessful()) {
                    vat = response.body().getTax();
                    calculateTotalPrice();
                } else {
                    dialog.dismiss();
                    try {
                        Log.e("error_code vat",response.code()+ response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }

            @Override
            public void onFailure(Call<OfferSettingModel> call, Throwable t) {



                try {
                    dialog.dismiss();
                    if (t.getMessage() != null) {
                        Log.e("error", t.getMessage() + "__");

                        if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                            Toast.makeText(AddOrderProductActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                        }else if (t.getMessage().toLowerCase().contains("socket")||t.getMessage().toLowerCase().contains("canceled")){}
                        else {
                            Toast.makeText(AddOrderProductActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                        }
                    }


                } catch (Exception e) {

                }
            }
        });*/

    }
    private void sendOrderTextWithoutImage()
    {

        ProgressDialog dialog = Common.createProgressDialog(this,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();

        String notes = binding.edtComment.getText().toString();
        String order_text =convertOrderToText();
        if (!notes.isEmpty()){
         order_text = order_text+"\n"+"ملاحظات:-"+"\n"+notes;
        }
        addOrderTextModel.setOrder_description(order_text);
        addOrderTextModel.setOrder_notes(notes);
        Api.getService(Tags.base_url)
                .sendFamilyTextOrder("Bearer "+userModel.getUser().getToken(),userModel.getUser().getId(),addOrderTextModel.getFamily_id(), String.valueOf(addOrderTextModel.getBill_cost()),addOrderTextModel.getTo_address(),addOrderTextModel.getTo_latitude(),addOrderTextModel.getTo_longitude(),addOrderTextModel.getFrom_name(),addOrderTextModel.getFrom_address(),addOrderTextModel.getFrom_latitude(),addOrderTextModel.getFrom_longitude(),addOrderTextModel.getEnd_shipping_time(),addOrderTextModel.getCoupon_id(),addOrderTextModel.getOrder_notes(),addOrderTextModel.getPayment_method())
                .enqueue(new Callback<SingleOrderDataModel>() {
                    @Override
                    public void onResponse(Call<SingleOrderDataModel> call, Response<SingleOrderDataModel> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()&&response.body()!=null)
                        {
                            Intent intent = getIntent();
                            intent.putExtra("order_id",response.body().getOrder().getId());
                            setResult(RESULT_OK,intent);
                            finish();
                        }else
                        {
                            if (response.code()==500)
                            {
                                Toast.makeText(AddOrderFamilyProductActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                            } else
                            {
                                Toast.makeText(AddOrderFamilyProductActivity.this,getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            }

                            try {
                                Log.e("error",response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<SingleOrderDataModel> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            if (t.getMessage() != null) {
                                Log.e("msg_category_error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(AddOrderFamilyProductActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AddOrderFamilyProductActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }catch (Exception e)
                        {
                            Log.e("Error",e.getMessage()+"__");
                        }
                    }
                });
    }
    private void sendOrderTextWithImage()
    {
        ProgressDialog dialog = Common.createProgressDialog(this,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();
        String notes = binding.edtComment.getText().toString();
        String order_text =convertOrderToText();
        if (!notes.isEmpty()){
            order_text = order_text+"\n"+"ملاحظات:-"+"\n"+notes;
        }
        addOrderTextModel.setOrder_description(order_text);
        addOrderTextModel.setOrder_notes(notes);


        Log.e("order_type",addOrderTextModel.getOrder_type()+"_");
        RequestBody user_id_part = Common.getRequestBodyText(String.valueOf(addOrderTextModel.getUser_id()));
        RequestBody order_type_part = Common.getRequestBodyText(addOrderTextModel.getOrder_type());
        RequestBody family_id_part = Common.getRequestBodyText(String.valueOf(addOrderTextModel.getFamily_id()));
        RequestBody google_place_id_part = Common.getRequestBodyText(addOrderTextModel.getGoogle_place_id());
        RequestBody bill_cost_part = Common.getRequestBodyText(String.valueOf(addOrderTextModel.getBill_cost()));
        RequestBody from_address_part = Common.getRequestBodyText(addOrderTextModel.getFrom_address());
        RequestBody from_lat_part = Common.getRequestBodyText(String.valueOf(addOrderTextModel.getFrom_latitude()));
        RequestBody from_lng_part = Common.getRequestBodyText(String.valueOf(addOrderTextModel.getFrom_longitude()));
        RequestBody from_name_part = Common.getRequestBodyText(addOrderTextModel.getFrom_name());
        RequestBody to_address_part = Common.getRequestBodyText(addOrderTextModel.getTo_address());
        RequestBody to_lat_part = Common.getRequestBodyText(String.valueOf(addOrderTextModel.getTo_latitude()));
        RequestBody to_lng_part = Common.getRequestBodyText(String.valueOf(addOrderTextModel.getTo_longitude()));
        RequestBody arrival_time_part = Common.getRequestBodyText(String.valueOf(addOrderTextModel.getEnd_shipping_time()));
        RequestBody coupon_id_part = Common.getRequestBodyText(addOrderTextModel.getCoupon_id());
        RequestBody details_part = Common.getRequestBodyText(addOrderTextModel.getOrder_description());
        RequestBody notes_part = Common.getRequestBodyText(addOrderTextModel.getOrder_notes());
        RequestBody payment_part = Common.getRequestBodyText(addOrderTextModel.getPayment_method());
        RequestBody hours_part = Common.getRequestBodyText(addOrderTextModel.getHour_arrival_time());
        RequestBody delevery_cost_part = Common.getRequestBodyText(cost+"");


        Api.getService(Tags.base_url)
                .sendFamilyTextOrderWithImage("Bearer "+userModel.getUser().getToken(),user_id_part,family_id_part,bill_cost_part,to_address_part,to_lat_part,to_lng_part,from_name_part,from_address_part,from_lat_part,from_lng_part,arrival_time_part,coupon_id_part,notes_part,payment_part,getMultiPartImages())
                .enqueue(new Callback<SingleOrderDataModel>() {
                    @Override
                    public void onResponse(Call<SingleOrderDataModel> call, Response<SingleOrderDataModel> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()&&response.body()!=null)
                        {

                            Intent intent = getIntent();
                            intent.putExtra("order_id",response.body().getOrder().getId());
                            setResult(RESULT_OK,intent);
                            finish();
                        }else
                        {
                            if (response.code()==500)
                            {
                                Toast.makeText(AddOrderFamilyProductActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                            } else
                            {
                                Toast.makeText(AddOrderFamilyProductActivity.this,getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            }

                            try {
                                Log.e("error",response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<SingleOrderDataModel> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            if (t.getMessage() != null) {

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(AddOrderFamilyProductActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AddOrderFamilyProductActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }catch (Exception e)
                        {
                            Log.e("Error",e.getMessage()+"__");
                        }
                    }
                });
    }
    private List<MultipartBody.Part> getMultiPartImages()
    {
        List<MultipartBody.Part> parts = new ArrayList<>();
        for (Uri uri :imagesList){
            if (uri!=null){
                MultipartBody.Part part = Common.getMultiPartImage(this,uri,"images[]");
                parts.add(part);
            }

        }
        return parts;
    }
    private String convertOrderToText()
    {
        String order = "";
        for (int index = 0;index<productModelList.size();index++){
            SingleProductModel model = productModelList.get(index);
            order = order+model.getTitle()+"("+model.getCount()+")"+"\n";

        }
        return order;
    }

    private void calculateTotalPrice()
    {
        //double tax = addOrderProductsModel.getTotal_cost()*(Double.parseDouble(vat)/100);

        double tax = 0;
        binding.setVat(String.format(Locale.ENGLISH,"%.2f",tax));
        double priceBeforeVat = total_cost-tax;
        binding.setPriceBeforeVat(String.format(Locale.ENGLISH,"%.2f",priceBeforeVat));

    }
    public void createDialogAlert()
    {
        dialog = new AlertDialog.Builder(this)
                .create();

        DialogSelectImage2Binding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_select_image2, null, false);
        binding.llCamera.setOnClickListener(v -> checkCameraPermission());
        binding.llGallery.setOnClickListener(v -> checkReadPermission());

        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_congratulation_animation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
    }
    public void checkReadPermission()
    {
        if (ActivityCompat.checkSelfPermission(this, READ_PERM) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{READ_PERM}, READ_REQ);
        } else {
            SelectImage(READ_REQ);
        }
    }
    public void checkCameraPermission()
    {


        if (ContextCompat.checkSelfPermission(this, write_permission) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, camera_permission) == PackageManager.PERMISSION_GRANTED
        ) {
            SelectImage(CAMERA_REQ);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{camera_permission, write_permission}, CAMERA_REQ);
        }
    }
    private void SelectImage(int req)
    {

        Intent intent = new Intent();

        if (req == READ_REQ) {
            intent.setAction(Intent.ACTION_PICK);
            intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/*");
            startActivityForResult(intent, req);

        } else if (req == CAMERA_REQ) {
            try {
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, req);
            } catch (SecurityException e) {
                Toast.makeText(this, R.string.perm_image_denied, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, R.string.perm_image_denied, Toast.LENGTH_SHORT).show();

            }


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_REQ) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SelectImage(requestCode);
            } else {
                Toast.makeText(this, getString(R.string.perm_image_denied), Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == CAMERA_REQ) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                SelectImage(requestCode);
            } else {
                Toast.makeText(this, getString(R.string.perm_image_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_REQ && resultCode == Activity.RESULT_OK && data != null) {

            Uri uri = data.getData();
            cropImage(uri);


        }
        else if (requestCode == CAMERA_REQ && resultCode == Activity.RESULT_OK && data != null) {

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            Uri uri = getUriFromBitmap(bitmap);
            if (uri != null) {
                cropImage(uri);

            }


        }
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri uri = result.getUri();

                if (imagesList.size()>0){
                    imagesList.add(imagesList.size()-1,uri);
                    addOrderImagesAdapter.notifyItemInserted(imagesList.size()-1);

                }else {
                    imagesList.add(uri);
                    imagesList.add(null);
                    addOrderImagesAdapter.notifyItemRangeInserted(0,imagesList.size());
                }


                dialog.dismiss();

                binding.recViewImages.postDelayed(()->{
                    binding.recViewImages.smoothScrollToPosition(imagesList.size()-1);
                },100);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        else if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null){
            //coupon

        }
        else if (requestCode == 200 && resultCode == Activity.RESULT_OK && data != null){
            FavoriteLocationModel favoriteLocationModel = (FavoriteLocationModel) data.getSerializableExtra("data");
            addOrderTextModel.setTo_address(favoriteLocationModel.getAddress());
            addOrderTextModel.setTo_latitude(favoriteLocationModel.getLat());
            addOrderTextModel.setTo_longitude(favoriteLocationModel.getLng());
            distance = String.format(Locale.ENGLISH, "%s", String.format(Locale.ENGLISH, "%.0f", (SphericalUtil.computeDistanceBetween(new LatLng(addOrderTextModel.getTo_latitude(), addOrderTextModel.getTo_longitude()), new LatLng(addOrderTextModel.getFrom_latitude(), addOrderTextModel.getFrom_longitude())) / 1000)));

            int distance2=Integer.parseInt(distance);

            Log.e("ldldl",distance+"----"+distance2);
           // getDelevryCost(distance2);

            int time = data.getIntExtra("time",1);
            Calendar calendar = Calendar.getInstance();
            switch (time){
                case 1:
                    addOrderTextModel.setHour_arrival_time("1");
                    calendar.add(Calendar.HOUR_OF_DAY,1);
                    break;
                case 2:
                    addOrderTextModel.setHour_arrival_time("2");
                    calendar.add(Calendar.HOUR_OF_DAY,2);
                    break;
                case 3:
                    addOrderTextModel.setHour_arrival_time("3");
                    calendar.add(Calendar.HOUR_OF_DAY,3);

                    break;
                case 4:
                    addOrderTextModel.setHour_arrival_time("4");

                    calendar.add(Calendar.DAY_OF_MONTH,1);

                    break;
                case 5:
                    addOrderTextModel.setHour_arrival_time("5");
                    calendar.add(Calendar.DAY_OF_MONTH,2);

                    break;
                case 6:
                    addOrderTextModel.setHour_arrival_time("6");

                    calendar.add(Calendar.DAY_OF_MONTH,3);

                    break;
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
            String timeArrival =dateFormat.format(new Date(calendar.getTimeInMillis()));
            addOrderTextModel.setEnd_shipping_time(timeArrival);

            if (imagesList.size()>0){
                sendOrderTextWithImage();
            }else {
                sendOrderTextWithoutImage();
            }

        }

    }
    private void CreateDialogAlert3(Context context,String msg) {
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .create();

        DialogAlertBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_alert, null, false);

        binding.tvMsg.setText(msg);
        binding.btnCancel.setText(getString(R.string.send));
        binding.btnCancel.setOnClickListener(v ->{
            if (imagesList.size()>0){
                sendOrderTextWithImage();
            }else {
                sendOrderTextWithoutImage();
            }
                    dialog.dismiss();
                }

        );
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_congratulation_animation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
    }

    private void cropImage(Uri uri)
    {

        CropImage.activity(uri).setAspectRatio(1,1).setGuidelines(CropImageView.Guidelines.ON).start(this);

    }
    private Uri getUriFromBitmap(Bitmap bitmap)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        return Uri.parse(MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, "", ""));
    }
    public void delete(int adapterPosition)
    {
        imagesList.remove(adapterPosition);
        if (imagesList.size()==1){
            imagesList.clear();
            addOrderImagesAdapter.notifyDataSetChanged();
        }else {
            addOrderImagesAdapter.notifyItemRemoved(adapterPosition);
        }
    }
    public void updateItemCount(SingleProductModel productModel, int pos)
    {
        productModelList.set(pos,productModel);
        total_cost = getTotalOrderCost(productModelList);
        addOrderTextModel.setBill_cost(total_cost);

        binding.setTotalCost(String.format(Locale.ENGLISH,"%.2f",total_cost));
        calculateTotalPrice();
    }
    private double getTotalOrderCost(List<SingleProductModel> productModelList)
    {
        double total=0.0;
        for (SingleProductModel model:productModelList){
            total +=model.getPrice()*model.getCount();
        }

        return total;
    }


}