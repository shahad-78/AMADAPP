package com.example.amadapp.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.amadapp.R;
import com.example.amadapp.ReportAreaActivity;
import com.example.amadapp.TreeRecommendationsActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * A simple {@link Fragment} subclass.
 */
public class TreeFragment extends Fragment implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap googleMap;
    private Button reportAreaButton, findTreesButton;

    private LatLng selectedLocation;

    private static final int LOCATION_PERMISSION_CODE = 1001;

    public TreeFragment() {
        // Required empty public constructor
    }

    public static TreeFragment newInstance(String param1, String param2) {
        TreeFragment fragment = new TreeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tree, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        Toast.makeText(getContext(),"Make sure you activate GPS on your phone",Toast.LENGTH_LONG).show();

        mapView = view.findViewById(R.id.mapView);
        reportAreaButton = view.findViewById(R.id.reportAreaButton);
        findTreesButton = view.findViewById(R.id.findTreesButton);

        // Initialize the MapView.
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Set click listeners
        reportAreaButton.setOnClickListener(v -> {
            if (selectedLocation != null) {
                Intent intent = new Intent(getActivity(), ReportAreaActivity.class);
                intent.putExtra("lat", String.valueOf(selectedLocation.latitude));
                intent.putExtra("lng", String.valueOf(selectedLocation.longitude));
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Please tap on the map to select a location first.", Toast.LENGTH_SHORT).show();
            }
        });

        findTreesButton.setOnClickListener(v -> {
            if (selectedLocation != null) {
                Intent intent = new Intent(getActivity(), TreeRecommendationsActivity.class);
                intent.putExtra("lat", String.valueOf(selectedLocation.latitude));
                intent.putExtra("lng", String.valueOf(selectedLocation.longitude));
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Please tap on the map to select a location first.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Allow user to tap anywhere to select a location
        googleMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            updateMapLocation(selectedLocation, "Selected Location");
            Toast.makeText(getContext(), "Location selected!", Toast.LENGTH_SHORT).show();
        });

        // --- NEW: Load current location from GPS ---
        getCurrentLocation();
    }

    private void getCurrentLocation() {
        if (getContext() == null) return;

        // 1. Check Permissions
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
            return;
        }

        try {
            // Enable the "My Location" blue dot layer
            googleMap.setMyLocationEnabled(true);

            // 2. Get Location Manager
            LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            Location location = null;

            // 3. Try to get last known location from GPS or Network
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            // 4. Update Map
            if (location != null) {
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                selectedLocation = userLatLng;
                updateMapLocation(userLatLng, "Current Location");
            } else {
                // Fallback to default (Riyadh) if location not available immediately
                LatLng riyadh = new LatLng(24.7136, 46.6753);
                updateMapLocation(riyadh, "Riyadh (Default)");
                Toast.makeText(getContext(), "Waiting for location...", Toast.LENGTH_SHORT).show();
            }

        } catch (SecurityException e) {
            Log.e("TreeFragment", "Location Permission Error: " + e.getMessage());
        }
    }

    // Handle Permission Result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(getContext(), "Permission denied. Using default location.", Toast.LENGTH_SHORT).show();
                LatLng riyadh = new LatLng(24.7136, 46.6753);
                updateMapLocation(riyadh, "Riyadh (Default)");
            }
        }
    }

    private void updateMapLocation(LatLng location, String title) {
        if (googleMap != null) {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(location).title(title));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
        }
    }

    // --- Lifecycle Methods for MapView ---
    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mapView != null) mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mapView != null) mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }
}