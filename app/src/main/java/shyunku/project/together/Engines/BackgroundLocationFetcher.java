package shyunku.project.together.Engines;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.model.LatLng;

import shyunku.project.together.Activities.MainApplication;

public class BackgroundLocationFetcher {
    private static final BackgroundLocationFetcher instance = new BackgroundLocationFetcher();

    private LocationRequest locationRequest;
    private LocationSettingsRequest.Builder builder;
    private LocationSettingsRequest locationSettingsRequest;
    private FusedLocationProviderClient locationClient;
    private LocationFetcherListener<LatLng> listener;

    private final long REQUEST_INTERVAL = 10000;
    private final long REQUEST_FASTEST_INTERVAL = 5000;

    private BackgroundLocationFetcher() {

    }

    public void updateLocation(){
        locationRequest = new LocationRequest();
        locationRequest.setInterval(REQUEST_INTERVAL);
        locationRequest.setFastestInterval(REQUEST_FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setNumUpdates(1);

        builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);

        locationSettingsRequest = builder.build();
        final LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location currentLocation = locationResult.getLastLocation();
                LatLng point = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                if (listener != null) {
                    listener.locationFetchListen(point);
                }
            }
        };

        Context context = MainApplication.getContext();
        locationClient = LocationServices.getFusedLocationProviderClient(context);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                locationClient.requestLocationUpdates(
                        locationRequest, locationCallback, Looper.myLooper()
                );
            }
        }, 0);
    }

    public void onResultFetched(LocationFetcherListener<LatLng> listener){
        this.listener = listener;
    }

    public static BackgroundLocationFetcher getInstance(){
        instance.updateLocation();
        return instance;
    }
}
