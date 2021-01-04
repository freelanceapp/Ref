package com.apps.ref.activities_fragments.activity_shop_map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.ref.BuildConfig;
import com.apps.ref.R;
import com.apps.ref.activities_fragments.activity_add_order_text.AddOrderTextActivity;
import com.apps.ref.activities_fragments.activity_login.LoginActivity;
import com.apps.ref.adapters.HoursAdapter;
import com.apps.ref.databinding.ActivityShopMapBinding;
import com.apps.ref.language.Language;
import com.apps.ref.models.HourModel;
import com.apps.ref.models.NearbyModel;
import com.apps.ref.models.PlaceDetailsModel;
import com.apps.ref.models.UserModel;
import com.apps.ref.preferences.Preferences;
import com.apps.ref.remote.Api;
import com.apps.ref.tags.Tags;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks, LocationListener {
    private ActivityShopMapBinding binding;
    private NearbyModel.Result placeModel;
    private String lang;
    private List<HourModel> hourModelList;
    private HoursAdapter adapter;
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private final String gps_perm = Manifest.permission.ACCESS_FINE_LOCATION;
    private final int loc_req = 22;
    private boolean canSend = false;
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shop_map);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        placeModel = (NearbyModel.Result) intent.getSerializableExtra("data");

    }

    private void initView() {
        preferences  = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        hourModelList = new ArrayList<>();
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        binding.setDistance("");
        binding.setModel(placeModel);
        binding.flBack.setOnClickListener(v -> finish());
        binding.progBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this,R.color.colorPrimary), PorterDuff.Mode.SRC_IN);



        binding.tvShow.setOnClickListener(v -> {
            if (hourModelList.size()>0){
                if (binding.expandLayout.isExpanded()){
                    binding.expandLayout.collapse(true);
                    binding.arrow2.animate().rotation(180).setDuration(500).start();
                }else {
                    binding.expandLayout.expand(true);
                    binding.arrow2.animate().rotation(0).setDuration(500).start();

                }
            }else {
                Toast.makeText(this, R.string.work_hour_not_aval, Toast.LENGTH_SHORT).show();
            }
        });

        binding.imageShare.setOnClickListener(v -> {
            Log.e("id",placeModel.getPlace_id()+"__");
            Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID+".provider",createFile());
            String url = getString(R.string.can_order)+"\n"+Tags.base_url+"place/details/"+placeModel.getPlace_id();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT,url);
            intent.putExtra(Intent.EXTRA_STREAM,uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent,"Share"));
        });

        binding.btnNext.setOnClickListener(v -> {
            canSend = true;
            if (canSend){
                if (userModel!=null){
                    Intent intent = new Intent(this, AddOrderTextActivity.class);
                    intent.putExtra("data",placeModel);
                    startActivityForResult(intent,100);
                }else {
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.putExtra("from", false);
                    startActivity(intent);
                }


            }
        });
        binding.flBack.setOnClickListener(v -> {super.onBackPressed();});
        updateUI();
    }

    private void getPlaceDetails() {

        String fields = "opening_hours,photos,reviews";

        Api.getService("https://maps.googleapis.com/maps/api/")
                .getPlaceDetails(placeModel.getPlace_id(), fields, lang, getString(R.string.map_api_key))
                .enqueue(new Callback<PlaceDetailsModel>() {
                    @Override
                    public void onResponse(Call<PlaceDetailsModel> call, Response<PlaceDetailsModel> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            updateHoursUI(response.body());
                        } else {

                            try {
                                Log.e("error_code", response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<PlaceDetailsModel> call, Throwable t) {
                        try {

                            Log.e("Error", t.getMessage());
                            Toast.makeText(ShopMapActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {

                        }
                    }
                });
    }

    private void updateHoursUI(PlaceDetailsModel body) {

        if (body.getResult().getReviews()!=null&&body.getResult().getReviews().size()>0){
            placeModel.setReviews(body.getResult().getReviews());

        }else {
            placeModel.setReviews(new ArrayList<>());
        }


        if (body.getResult().getOpening_hours() != null) {
            placeModel.setOpen(body.getResult().getOpening_hours().isOpen_now());
            if (body.getResult().getOpening_hours().getWeekday_text()!=null&&body.getResult().getOpening_hours().getWeekday_text().size()>0){
                placeModel.setWork_hours(body.getResult().getOpening_hours());

                placeModel.setOpen(true);
                binding.tvStatus.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
                binding.icon.setColorFilter(ContextCompat.getColor(this,R.color.colorPrimary));
                hourModelList.clear();
                hourModelList.addAll(getHours());


            }else {
                placeModel.setOpen(false);
                binding.tvStatus.setTextColor(ContextCompat.getColor(this,R.color.color_rose));
                binding.icon.setColorFilter(ContextCompat.getColor(this,R.color.color_rose));

            }


        } else {
            placeModel.setOpen(false);
            binding.tvStatus.setTextColor(ContextCompat.getColor(this,R.color.color_rose));
            binding.icon.setColorFilter(ContextCompat.getColor(this,R.color.color_rose));


        }
        if (placeModel.getPhotos()!=null){
            if (placeModel.getPhotos().size()>0)
            {
                String url = Tags.IMAGE_Places_URL+placeModel.getPhotos().get(0).getPhoto_reference()+"&key="+getString(R.string.map_api_key);
                Picasso.get().load(Uri.parse(url)).fit().into(binding.image);
                addMarker(url);

            }else
            {

                addMarker(placeModel.getIcon());

                Picasso.get().load(Uri.parse(placeModel.getIcon())).fit().into(binding.image);

            }
        }
        else {
            addMarker(placeModel.getIcon());
            Picasso.get().load(Uri.parse(placeModel.getIcon())).fit().into(binding.image);

        }


        binding.recView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HoursAdapter(hourModelList,this);
        binding.recView.setAdapter(adapter);

        binding.setDistance(String.format(Locale.ENGLISH,"%.2f",placeModel.getDistance()));
        binding.setModel(placeModel);

        binding.progBar.setVisibility(View.GONE);
        binding.tvShow.setVisibility(View.VISIBLE);
        binding.arrow2.setVisibility(View.VISIBLE);
        binding.tvStatus.setVisibility(View.VISIBLE);
        binding.icon.setVisibility(View.VISIBLE);
    }

    private List<HourModel> getHours() {
        List<HourModel> list = new ArrayList<>();

        for (String time: placeModel.getWork_hours().getWeekday_text()){

            String day = time.split(":", 2)[0].trim();
            String t = time.split(":",2)[1].trim();
            HourModel hourModel = new HourModel(day,t);
            list.add(hourModel);




        }

        return list;
    }

    private File createFile(){
        File file = null;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.logo_text);
        file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),System.currentTimeMillis()+".png");
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,outputStream);
            outputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private void updateUI() {

        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (googleMap != null) {
            mMap = googleMap;
            mMap.setTrafficEnabled(false);
            mMap.setBuildingsEnabled(false);
            mMap.setIndoorEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            CheckPermission();
            getPlaceDetails();


        }
    }

    private void addMarker(String url) {
        View view = LayoutInflater.from(this).inflate(R.layout.marker_shop,null);
        CircleImageView imageView = view.findViewById(R.id.image);
        TextView tvDistance = view.findViewById(R.id.tvDistance);
        TextView tvName = view.findViewById(R.id.tvName);






        Picasso.get().load(Uri.parse(url)).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                tvDistance.setText(String.format(Locale.ENGLISH,"%.2f %s",placeModel.getDistance(),getString(R.string.km)));
                tvName.setText(placeModel.getName());
                imageView.setImageBitmap(bitmap);
                IconGenerator iconGenerator = new IconGenerator(ShopMapActivity.this);
                iconGenerator.setContentPadding(2,2,2,2);
                iconGenerator.setContentView(view);


                mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon())).position(new LatLng(placeModel.getGeometry().getLocation().getLat(),placeModel.getGeometry().getLocation().getLng())));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(placeModel.getGeometry().getLocation().getLat(),placeModel.getGeometry().getLocation().getLng()),20.0f));

            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });


    }

    private void addMarkerHome(double lat ,double lng) {
        View view = LayoutInflater.from(this).inflate(R.layout.marker_my_location,null);
        IconGenerator iconGenerator = new IconGenerator(ShopMapActivity.this);
        iconGenerator.setContentPadding(2,2,2,2);
        iconGenerator.setContentView(view);


        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon())).position(new LatLng(lat,lng)));


    }



    private void initGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }
    private void CheckPermission()
    {
        if (ActivityCompat.checkSelfPermission(this, gps_perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{gps_perm}, loc_req);
        } else {
            mMap.setMyLocationEnabled(true);
            initGoogleApiClient();

        }
    }

    private void initLocationRequest()
    {
        locationRequest = LocationRequest.create();
        locationRequest.setFastestInterval(1000);
        locationRequest.setInterval(60000);
        LocationSettingsRequest.Builder request = new LocationSettingsRequest.Builder();
        request.addLocationRequest(locationRequest);
        request.setAlwaysShow(false);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, request.build());

        result.setResultCallback(result1 -> {

            Status status = result1.getStatus();
            switch (status.getStatusCode())
            {
                case LocationSettingsStatusCodes.SUCCESS:
                    startLocationUpdate();
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try {
                        status.startResolutionForResult(ShopMapActivity.this,1255);
                    }catch (Exception e)
                    {
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    Log.e("not available","not available");
                    break;
            }
        });

    }
    @SuppressLint("MissingPermission")
    private void startLocationUpdate()
    {
        locationCallback = new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        };
        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        initLocationRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (googleApiClient!=null){
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        addMarkerHome(location.getLatitude(),location.getLongitude());
        if (googleApiClient!=null){
            googleApiClient.disconnect();
        }
        if (locationCallback!=null){
            LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==1255&&resultCode==RESULT_OK){
            startLocationUpdate();
        }else if (requestCode==100&&resultCode==RESULT_OK){

        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (requestCode == loc_req)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                initGoogleApiClient();
                mMap.setMyLocationEnabled(true);

            }else
            {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }



}