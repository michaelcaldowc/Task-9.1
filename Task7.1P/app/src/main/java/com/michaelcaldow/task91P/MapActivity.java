package com.michaelcaldow.task91P;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";
    private GoogleMap mMap;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainMapLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = DatabaseHelper.getInstance(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment_container);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Map Fragment not found!");
            Toast.makeText(this, "Error loading map.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Log.i(TAG, "Map is ready.");
        mMap.getUiSettings().setZoomControlsEnabled(true);
        loadItemsOnMap();
    }

    private void loadItemsOnMap() {
        List<Item> itemList = databaseHelper.getAllItems();
        if (itemList == null || itemList.isEmpty()) {
            Toast.makeText(this, "No items to display on map.", Toast.LENGTH_SHORT).show();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-37.8136, 144.9631), 10)); // Melbourne
            return;
        }

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boolean hasValidLocations = false;

        for (Item item : itemList) {
            if (item.getLatitude() != 0.0 || item.getLongitude() != 0.0) {
                LatLng itemLocation = new LatLng(item.getLatitude(), item.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(itemLocation)
                        .title(item.getPostType() + ": " + item.getName())
                        .snippet(item.getLocation()));
                boundsBuilder.include(itemLocation);
                hasValidLocations = true;
                Log.d(TAG, "Adding marker for: " + item.getName() + " at " + item.getLatitude() + "," + item.getLongitude());
            } else {
                Log.d(TAG, "Skipping item with no location: " + item.getName());
            }
        }

        if (hasValidLocations) {
            LatLngBounds bounds = boundsBuilder.build();
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } else {
            Toast.makeText(this, "No items with valid locations found.", Toast.LENGTH_SHORT).show();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-37.8136, 144.9631), 10)); // Default to Melbourne
        }
    }
}