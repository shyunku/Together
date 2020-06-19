package shyunku.project.together.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import shyunku.project.together.Constants.Global;
import shyunku.project.together.Engines.FirebaseManageEngine;
import shyunku.project.together.Engines.LocationUpdateNotificationManager;
import shyunku.project.together.Engines.LogEngine;
import shyunku.project.together.Objects.User;
import shyunku.project.together.R;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback{
    private GoogleMap gmap = null;
    private Marker currentMarker = null, oppMarker = null;

    private static final String TAG = "google_map";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 30000;                //업데이트 주기 = 30초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 12000;        // 최소 업데이트 주기 = 12초

    private static final int PERMISSONS_REQUEST_CODE = 100;
    boolean needRequest = false;
    boolean isAutoFocus = true;

    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    Location mCurrentLocation;
    LatLng currentPosition;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;

    private View mLayout;

    User me = new User(), opp = new User();

    TextView myCurLocView, oppCurLocView, distView;
    Button updateOpp, showOpp;
    Switch allowUpdate;

    public static LocationUpdateNotificationManager man;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.view_location);

        myCurLocView = (TextView) findViewById(R.id.my_location_view);
        oppCurLocView = (TextView) findViewById(R.id.opp_location_view);
        distView = (TextView)findViewById(R.id.distance_between);
        updateOpp = (Button)findViewById(R.id.show_my_pos);
        showOpp = (Button)findViewById(R.id.show_opp_pos);
        allowUpdate = (Switch)findViewById(R.id.auto_location_update);

        DatabaseReference mr = FirebaseManageEngine.getFreshLocalDB().getReference(Global.rootName+"/users/"+Global.curDeviceID).child("location_share");
        mr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean allowed = dataSnapshot.getValue(Boolean.class);
                allowUpdate.setChecked(allowed);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //my info
        final DatabaseReference uref = FirebaseManageEngine.getFreshLocalDB().getReference(Global.rootName+"/users");

        // Listen My Info
        uref.child(Global.curDeviceID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    // Already Exists
                    me = dataSnapshot.getValue(User.class);

                    myCurLocView.setText("내 위치 : "+toAddressString(me.latitude, me.longitude));
                    distView.setText("사이 거리 : "+getDistance(me, opp));
                    allowUpdate.setChecked(me.allowLocShare);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //opp info
        uref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> users = new ArrayList<>();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String iteratingDeviceID = (String) snapshot.child("deviceID").getValue();
                    String username = (String) snapshot.child("name").getValue();
                    users.add(username);

                    if(!iteratingDeviceID.equals(Global.curDeviceID)){
                        opp = snapshot.getValue(User.class);

                        oppCurLocView.setText("상대 위치 : "+toAddressString(opp.latitude, opp.longitude));
                        distView.setText("서로 간의 거리 : "+getDistance(me, opp));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mLayout = findViewById(R.id.google_map_layout);

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map_fragment);
        mapFragment.getMapAsync(this);

        updateOpp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseManageEngine.sendLocationRequestMessage();
            }
        });

        showOpp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(new LatLng(opp.latitude, opp.longitude));
                gmap.moveCamera(cameraUpdate);
            }
        });

        allowUpdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean b) {
                me.allowLocShare = b;

                Map<String, Object> postVal = me.toMap();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(Global.rootName+"/users/"+Global.curDeviceID, postVal);

                FirebaseManageEngine.getFreshLocalDBref().updateChildren(childUpdates);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady : ");
        gmap = googleMap;

        setDefaultLocation();

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED){
            startLocationUpdates();
        }else{
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])){
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(LocationActivity.this, REQUIRED_PERMISSIONS, PERMISSONS_REQUEST_CODE);
                    }
                }).show();
            }else{
                ActivityCompat.requestPermissions(this,REQUIRED_PERMISSIONS, PERMISSONS_REQUEST_CODE);
            }
        }

        gmap.getUiSettings().setMyLocationButtonEnabled(true);
        gmap.animateCamera(CameraUpdateFactory.zoomTo(15));
        gmap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d(TAG, "onMapClick : ");
            }
        });
    }

    LocationCallback locationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult){
            super.onLocationResult(locationResult);
            List<Location> locationList = locationResult.getLocations();
            if(locationList.size()>0){
                location = locationList.get(locationList.size() - 1);
                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

                new LogEngine().sendLog(location.getLatitude()+", "+location.getLongitude());
                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도 : " + String.valueOf(location.getLatitude()) + ", 경도 : "+String.valueOf(location.getLongitude());

                //Log.d(TAG, "onLocationResult : "+markerSnippet);

                updateMyLocationStatusOnFirebase();

                setCurrentLocation(location, markerTitle, markerSnippet);
                mCurrentLocation = location;
            }
        }
    };

    private void startLocationUpdates() {
        if(!checkLocationServicesStatus()){
            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        }else{
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

            if(hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }
            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if(checkPermission())
                gmap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected  void onStart(){
        super.onStart();
        Log.d(TAG, "onStart");
        if(checkPermission()){
            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            if(gmap!=null)
                gmap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected  void onStop(){
        super.onStop();
        if(mFusedLocationClient != null){
            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public String getCurrentAddress(LatLng latlng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 5);
            new LogEngine().sendLog("Address size "+addresses.size());
        } catch (IOException e) {
            //네트워크 문제
            Toast.makeText(this, "GeoCoder service unable", Toast.LENGTH_LONG).show();
            return "GeoCoder service unable";
        }catch(IllegalArgumentException e){
            Toast.makeText(this, "Wrong GPS Coordinate", Toast.LENGTH_LONG).show();
            return "Wrong GPS Coordinate";
        }

        if(addresses == null || addresses.size() == 0){
            Toast.makeText(this, "Address Not Found", Toast.LENGTH_SHORT).show();
            return "Address Not Found";
        }else{
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        if(currentMarker != null)currentMarker.remove();
        if(oppMarker != null)oppMarker.remove();
        LatLng myCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng oppCurrentLatLng = new LatLng(opp.latitude, opp.longitude);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(myCurrentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.alpha(0.8f);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        currentMarker = gmap.addMarker(markerOptions);

        if(isAutoFocus) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(myCurrentLatLng);
            gmap.moveCamera(cameraUpdate);
            isAutoFocus = false;
        }


        markerOptions.position(oppCurrentLatLng);
        markerOptions.title(toAddressString(oppCurrentLatLng.latitude, oppCurrentLatLng.longitude));
        markerOptions.snippet(toAddressSnippet(oppCurrentLatLng.latitude, oppCurrentLatLng.longitude));
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        oppMarker = gmap.addMarker(markerOptions);
    }

    private void setDefaultLocation() {
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치 정보 가져올 수 없음";
        String marketSnippet = "위치 권한과 GPS 활성 여부 확인 바람";

        if(currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(marketSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = gmap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        gmap.moveCamera(cameraUpdate);
    }

    private boolean checkPermission() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        return hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grandResults){
        if(permsRequestCode == PERMISSONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length){
            boolean check_result = true;
            for(int result : grandResults){
                if(result != PackageManager.PERMISSION_GRANTED){
                    check_result = false;
                    break;
                }
            }
            if(check_result)startLocationUpdates();
            else{
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0]) || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])){
                    Snackbar.make(mLayout, "Permission Denied. Please allow permission after restart app.", Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }else{
                    Snackbar.make(mLayout, "Permission Denied. Please allow permission in app setting.", Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }
            }
        }
    }

    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LocationActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPS_ENABLE_REQUEST_CODE) {
            if (checkLocationServicesStatus()) {
                if (checkLocationServicesStatus()) {
                    Log.d(TAG, "onActivityResult : GPS 활성화 되있음");
                    needRequest = true;
                    return;
                }
            }
        }
    }

    private void updateMyLocationStatusOnFirebase(){
        me.latitude = location.getLatitude();
        me.longitude = location.getLongitude();

        Map<String, Object> postVal = me.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(Global.rootName+"/users/"+Global.curDeviceID, postVal);

        FirebaseManageEngine.getFreshLocalDBref().updateChildren(childUpdates);
    }

    private String toAddressString(double latitude, double longitude){
        new LatLng(latitude,longitude);
        new LogEngine().sendLog(latitude+", "+longitude);
        return getCurrentAddress(new LatLng(latitude, longitude));
    }

    private String toAddressSnippet(double latitude, double longitude){
        return "위도 : " + latitude + ", 경도 : "+longitude;
    }

    private String getDistance(User u1, User u2){
        Location l1 = new Location("a");
        Location l2 = new Location("b");
        l1.setLatitude(u1.latitude);
        l1.setLongitude(u1.longitude);
        l2.setLatitude(u2.latitude);
        l2.setLongitude(u2.longitude);
        float dist = l1.distanceTo(l2);

        return "약 "+dist+" m";
    }

    private void updateOpp(){

    }
}
