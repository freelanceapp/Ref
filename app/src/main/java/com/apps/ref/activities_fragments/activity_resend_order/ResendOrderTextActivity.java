package com.apps.ref.activities_fragments.activity_resend_order;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.apps.ref.R;
import com.apps.ref.activities_fragments.activity_add_coupon.AddCouponActivity;
import com.apps.ref.activities_fragments.activity_chat.ChatActivity;
import com.apps.ref.activities_fragments.activity_map_search.MapSearchActivity;
import com.apps.ref.adapters.AddOrderImagesAdapter;
import com.apps.ref.databinding.ActivityResendOrderTextBinding;
import com.apps.ref.databinding.DialogSelectImage2Binding;
import com.apps.ref.language.Language;
import com.apps.ref.models.AddOrderTextModel;
import com.apps.ref.models.CouponModel;
import com.apps.ref.models.FavoriteLocationModel;
import com.apps.ref.models.OrderModel;
import com.apps.ref.models.SingleOrderDataModel;
import com.apps.ref.models.UserModel;
import com.apps.ref.preferences.Preferences;
import com.apps.ref.remote.Api;
import com.apps.ref.share.Common;
import com.apps.ref.tags.Tags;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResendOrderTextActivity extends AppCompatActivity {
    private ActivityResendOrderTextBinding binding;
    private OrderModel orderModel;
    private String lang;
    private boolean canSend = false;
    private final String READ_PERM = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final String write_permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private final String camera_permission = Manifest.permission.CAMERA;
    private final int READ_REQ = 1, CAMERA_REQ = 2;
    private List<Uri> imagesList;
    private AlertDialog dialog;
    private AddOrderImagesAdapter addOrderImagesAdapter;
    private AddOrderTextModel addOrderTextModel;
    private Preferences preferences;
    private UserModel userModel;

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.onAttach(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_resend_order_text);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent()
    {
        Intent intent = getIntent();
        orderModel = (OrderModel) intent.getSerializableExtra("data");

    }
    private void initView()
    {

        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        addOrderTextModel = new AddOrderTextModel();
        imagesList = new ArrayList<>();
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        binding.setModel(orderModel);
        binding.recViewImages.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        addOrderImagesAdapter = new AddOrderImagesAdapter(imagesList,this);
        binding.recViewImages.setAdapter(addOrderImagesAdapter);

        binding.edtOrder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().trim().isEmpty()){
                    canSend = true;
                }else {
                    canSend = false;
                }

                updateBtnUI();
            }
        });
        binding.imageCamera.setOnClickListener(v -> createDialogAlert());
        binding.tvAddCoupon.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddCouponActivity.class);
            startActivityForResult(intent,100);
        });
        binding.tvChangeLocation.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapSearchActivity.class);
            intent.putExtra("type", 1);
            startActivityForResult(intent, 200);
        });
        binding.close.setOnClickListener(v -> {super.onBackPressed();});

        binding.btnNext.setOnClickListener(v -> {
            if (canSend){
                String order_text = binding.edtOrder.getText().toString();
                addOrderTextModel.setOrder_text(order_text);
                if (imagesList.size()>0){

                    sendOrderTextWithImage();
                }else {
                    sendOrderTextWithoutImage();
                }
            }


        });

        addOrderTextModel.setOrder_type(orderModel.getOrder_type());
        addOrderTextModel.setUser_id(userModel.getUser().getId());
        if (orderModel.getMarket_id()!=null){
            addOrderTextModel.setMarket_id(Integer.parseInt(orderModel.getMarket_id()));

        }else {
            addOrderTextModel.setMarket_id(0);

        }
        addOrderTextModel.setPlace_id(orderModel.getGoogle_palce_id());
        addOrderTextModel.setPlace_name(orderModel.getMarket_name());
        addOrderTextModel.setPlace_address(orderModel.getMarket_address());
        addOrderTextModel.setPlace_lat(Double.parseDouble(orderModel.getMarket_latitude()));
        addOrderTextModel.setPlace_lng(Double.parseDouble(orderModel.getMarket_longitude()));
        addOrderTextModel.setPayment(orderModel.getPayment_method());
        addOrderTextModel.setCoupon_id("0");
        addOrderTextModel.setComments(orderModel.getNotes());
        addOrderTextModel.setTo_address(orderModel.getClient_address());
        addOrderTextModel.setTo_lat(Double.parseDouble(orderModel.getClient_latitude()));
        addOrderTextModel.setTo_lng(Double.parseDouble(orderModel.getClient_longitude()));



    }
    private void updateBtnUI()
    {
        if (canSend){
            binding.btnNext.setBackgroundResource(R.color.colorPrimary);
        }else {
            binding.btnNext.setBackgroundResource(R.color.gray8);

        }
    }
    private void sendOrderTextWithoutImage()
    {
        ProgressDialog dialog = Common.createProgressDialog(this,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url)
                .sendTextOrder(userModel.getUser().getToken(),userModel.getUser().getId(),addOrderTextModel.getOrder_type(),addOrderTextModel.getMarket_id(),addOrderTextModel.getPlace_id(),"0",addOrderTextModel.getTo_address(),addOrderTextModel.getTo_lat(),addOrderTextModel.getTo_lng(),addOrderTextModel.getPlace_name(),addOrderTextModel.getPlace_address(),addOrderTextModel.getPlace_lat(),addOrderTextModel.getPlace_lng(),"1",addOrderTextModel.getCoupon_id(),addOrderTextModel.getOrder_text(),addOrderTextModel.getComments())
                .enqueue(new Callback<SingleOrderDataModel>() {
                    @Override
                    public void onResponse(Call<SingleOrderDataModel> call, Response<SingleOrderDataModel> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()&&response.body()!=null)
                        {
                            Intent intent =new Intent(ResendOrderTextActivity.this, ChatActivity.class);
                            intent.putExtra("order_id",response.body().getOrder().getId());
                            startActivity(intent);
                        }else
                        {
                            if (response.code()==500)
                            {
                                Toast.makeText(ResendOrderTextActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                            }else if(response.code()==406){
                                Toast.makeText(ResendOrderTextActivity.this, R.string.no_courier, Toast.LENGTH_SHORT).show();
                            } else
                            {
                                Toast.makeText(ResendOrderTextActivity.this,getString(R.string.failed), Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(ResendOrderTextActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ResendOrderTextActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
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
        addOrderTextModel.setOrder_text(binding.edtOrder.getText().toString().trim());
        RequestBody user_id_part = Common.getRequestBodyText(String.valueOf(userModel.getUser().getId()));
        RequestBody order_type_part = Common.getRequestBodyText(addOrderTextModel.getOrder_type());

        RequestBody market_id_part = Common.getRequestBodyText(String.valueOf(addOrderTextModel.getMarket_id()));
        RequestBody google_place_id_part = Common.getRequestBodyText(addOrderTextModel.getPlace_id());
        RequestBody bill_cost_part = Common.getRequestBodyText("0");
        RequestBody client_address_part = Common.getRequestBodyText(addOrderTextModel.getTo_address());
        RequestBody client_lat_part = Common.getRequestBodyText(String.valueOf(addOrderTextModel.getTo_lat()));
        RequestBody client_lng_part = Common.getRequestBodyText(String.valueOf(addOrderTextModel.getTo_lng()));
        RequestBody market_name_part = Common.getRequestBodyText(addOrderTextModel.getPlace_name());
        RequestBody market_address_part = Common.getRequestBodyText(addOrderTextModel.getPlace_address());
        RequestBody market_lat_part = Common.getRequestBodyText(String.valueOf(addOrderTextModel.getPlace_lat()));
        RequestBody market_lng_part = Common.getRequestBodyText(String.valueOf(addOrderTextModel.getPlace_lng()));
        RequestBody arrival_time_part = Common.getRequestBodyText(orderModel.getOrder_time_arrival());
        RequestBody coupon_id_part = Common.getRequestBodyText(addOrderTextModel.getCoupon_id());
        RequestBody details_part = Common.getRequestBodyText(addOrderTextModel.getOrder_text());
        RequestBody notes_part = Common.getRequestBodyText(addOrderTextModel.getComments());


        Api.getService(Tags.base_url)
                .sendTextOrderWithImage(userModel.getUser().getToken(),user_id_part,order_type_part,market_id_part,google_place_id_part,bill_cost_part,client_address_part,client_lat_part,client_lng_part,market_name_part,market_address_part,market_lat_part,market_lng_part,arrival_time_part,coupon_id_part,details_part,notes_part,getMultiPartImages())
                .enqueue(new Callback<SingleOrderDataModel>() {
                    @Override
                    public void onResponse(Call<SingleOrderDataModel> call, Response<SingleOrderDataModel> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()&&response.body()!=null)
                        {
                            Intent intent =new Intent(ResendOrderTextActivity.this, ChatActivity.class);
                            intent.putExtra("order_id",response.body().getOrder().getId());
                            startActivity(intent);
                        }else
                        {
                            if (response.code()==500)
                            {
                                Toast.makeText(ResendOrderTextActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                            }else if(response.code()==406){
                                Toast.makeText(ResendOrderTextActivity.this, R.string.no_courier, Toast.LENGTH_SHORT).show();
                            } else
                            {
                                Toast.makeText(ResendOrderTextActivity.this,getString(R.string.failed), Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(ResendOrderTextActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ResendOrderTextActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
            CouponModel couponModel = (CouponModel) data.getSerializableExtra("data");

            addOrderTextModel.setCoupon_id(String.valueOf(couponModel.getId()));
            if (couponModel.getCoupon_type().equals("per")){
                String discount = getString(R.string.you_got)+" "+couponModel.getCoupon_value()+"% "+getString(R.string.discount)+" "+getString(R.string.on_delivery);;
                binding.tvCoupon.setText(discount);
            }else {
                String discount = getString(R.string.you_got)+" "+couponModel.getCoupon_value()+" "+userModel.getUser().getCountry().getWord().getCurrency()+" "+getString(R.string.discount)+" "+getString(R.string.on_delivery);;
                binding.tvCoupon.setText(discount);

            }



        }else if (requestCode == 200 && resultCode == Activity.RESULT_OK && data != null){
            FavoriteLocationModel model = (FavoriteLocationModel) data.getSerializableExtra("data");
            addOrderTextModel.setTo_address(model.getAddress());
            addOrderTextModel.setTo_lat(model.getLat());
            addOrderTextModel.setTo_lng(model.getLng());
            binding.tvDropOff.setText(model.getAddress());

        }



    }

    private void cropImage(Uri uri) {

        CropImage.activity(uri).setAspectRatio(1,1).setGuidelines(CropImageView.Guidelines.ON).start(this);

    }

    private Uri getUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        return Uri.parse(MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, "", ""));
    }


    public void delete(int adapterPosition) {
        imagesList.remove(adapterPosition);
        if (imagesList.size()==1){
            imagesList.clear();
            addOrderImagesAdapter.notifyDataSetChanged();
        }else {
            addOrderImagesAdapter.notifyItemRemoved(adapterPosition);
        }
    }
}