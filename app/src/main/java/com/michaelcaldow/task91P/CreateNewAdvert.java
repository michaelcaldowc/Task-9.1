package com.michaelcaldow.task91P;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateNewAdvert extends AppCompatActivity {

    private static final String TAG = "CreateNewAdvert";

    private RadioGroup postTypeRadioGroup;
    private RadioButton lostRadioButton, foundRadioButton;
    private TextView locationTitleTextView;
    private EditText nameEditText, phoneEditText, descriptionEditText, dateEditText, locationEditText;
    private Button saveButton, getCurrentLocationButton;
    private final Calendar calendar = Calendar.getInstance();
    private DatabaseHelper databaseHelper;

    private FusedLocationProviderClient fusedLocationClient;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;

    // ActivityResultLauncher for Places Autocomplete
    private ActivityResultLauncher<Intent> placesAutocompleteLauncher;

    // ActivityResultLauncher for Location Permission
    private ActivityResultLauncher<String> requestLocationPermissionLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_new_advert);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = DatabaseHelper.getInstance(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyA-t-QXbc9emzVsSLN3uoqootnbLeM9JyA");
        }

        postTypeRadioGroup = findViewById(R.id.radioGroup);
        lostRadioButton = findViewById(R.id.lost_radioButton);
        foundRadioButton = findViewById(R.id.found_radioButton);
        nameEditText = findViewById(R.id.name_editText);
        phoneEditText = findViewById(R.id.phone_editText);
        descriptionEditText = findViewById(R.id.description_editText);
        dateEditText = findViewById(R.id.date_editText);
        locationTitleTextView = findViewById(R.id.locationTitle_textView);
        locationEditText = findViewById(R.id.location_editText);
        saveButton = findViewById(R.id.save_button);
        getCurrentLocationButton = findViewById(R.id.getCurrentLocation_button);

        setupDatepicker();
        setupRadioGroupListener();
        setupSaveButton();
        setupLocationFeatures();

        lostRadioButton.setChecked(true);
        toggleLocationVisibility(false);
    }

    private void setupDatepicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateInView();
        };
        dateEditText.setOnClickListener(view -> new DatePickerDialog(CreateNewAdvert.this, dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                .show());
    }

    private void setupRadioGroupListener() {
        postTypeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            toggleLocationVisibility(checkedId == R.id.found_radioButton);
        });
    }

    private void toggleLocationVisibility(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        locationTitleTextView.setVisibility(visibility);
        locationEditText.setVisibility(visibility);
        getCurrentLocationButton.setVisibility(visibility);
        if (!show) {
            locationEditText.setText("");
            currentLatitude = 0.0;
            currentLongitude = 0.0;
        }
    }

    private void setupLocationFeatures() {
        // Initialize the permission launcher
        requestLocationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        fetchCurrentLocation();
                    } else {
                        Toast.makeText(this, "Cannot get current location.", Toast.LENGTH_LONG).show();
                    }
                });

        getCurrentLocationButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });

        // Initialize the Places Autocomplete launcher
        placesAutocompleteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());
                        locationEditText.setText(place.getAddress());
                        if (place.getLatLng() != null) {
                            currentLatitude = place.getLatLng().latitude;
                            currentLongitude = place.getLatLng().longitude;
                            Log.i(TAG, "Place selected: " + place.getName() + ", Lat: " + currentLatitude + ", Lng: " + currentLongitude);
                        }
                   }
                });

        locationEditText.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(this);
            placesAutocompleteLauncher.launch(intent);
        });
    }

    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Cannot get current location", Toast.LENGTH_SHORT).show();
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();
                        Geocoder geocoder = new Geocoder(CreateNewAdvert.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address address = addresses.get(0);
                                locationEditText.setText(address.getAddressLine(0));
                                Log.i(TAG, "Current Location: " + address.getAddressLine(0) + " Lat: " + currentLatitude + " Lng: " + currentLongitude);
                            } else {
                                locationEditText.setText("Lat: " + String.format(Locale.US, "%.6f", currentLatitude) + ", Lng: " + String.format(Locale.US, "%.6f", currentLongitude));
                                Log.w(TAG, "No address found for current location.");
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Geocoder error", e);
                            locationEditText.setText("Lat: " + String.format(Locale.US, "%.6f", currentLatitude) + ", Lng: " + String.format(Locale.US, "%.6f", currentLongitude));
                            Toast.makeText(CreateNewAdvert.this, "Error getting address", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CreateNewAdvert.this, "Could not get current location. Please ensure location is enabled.", Toast.LENGTH_LONG).show();
                        Log.w(TAG, "FusedLocationClient returned null location.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting location", e);
                    Toast.makeText(CreateNewAdvert.this, "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void updateDateInView() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        dateEditText.setText(sdf.format(calendar.getTime()));
    }

    private void setupSaveButton() {
        saveButton.setOnClickListener(v -> saveAdvert());
    }

    private void saveAdvert() {
        String postType = lostRadioButton.isChecked() ? "Lost" : "Found";
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();
        String locationString = locationEditText.getText().toString().trim();

        boolean isFound = postType.equals("Found");

        if (name.isEmpty() || phone.isEmpty() || description.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isFound && locationString.isEmpty()) {
            Toast.makeText(this, "Please provide the location for a found item.", Toast.LENGTH_SHORT).show();
            return;
        }

        Item newItem = new Item(postType, name, phone, description, date, locationString, currentLatitude, currentLongitude);
        long result = databaseHelper.addItem(newItem);

        if (result != -1) {
            Toast.makeText(this, "Advert saved successfully!", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Saved item with ID: " + result + " Name: " + name + " Lat: " + currentLatitude + " Lng: " + currentLongitude);
            finish();
        } else {
            Toast.makeText(this, "Error saving advert. Please try again.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Database insertion failed for item: " + name);
        }
    }
}
