package com.michaelcaldow.task91P; // Use your package name

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;


public class DisplayItem extends AppCompatActivity {

    // UI elements
    TextView nameTextView, phoneTextView, descriptionTextView, dateTextView, locationTextView;
    TextView itemDetailsHeadingTextView, locationTitleTextView;
    Button removeButton;

    // Database Helper
    private DatabaseHelper databaseHelper;

    // Item data
    private Item currentItem;
    private long itemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_display_item);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialise database
        databaseHelper = DatabaseHelper.getInstance(this);

        itemDetailsHeadingTextView = findViewById(R.id.item_details_heading_textView);
        nameTextView = findViewById(R.id.value_name);
        phoneTextView = findViewById(R.id.value_phone);
        descriptionTextView = findViewById(R.id.value_description);
        dateTextView = findViewById(R.id.value_date);
        locationTitleTextView = findViewById(R.id.label_location);
        locationTextView = findViewById(R.id.value_location);
        removeButton = findViewById(R.id.remove_button);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("ITEM_ID")) {
            itemId = intent.getLongExtra("ITEM_ID", -1);
            Log.d("DisplayItem", "Item ID: " + itemId);
        } else {
            Toast.makeText(this, "Error: Item not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Load and Display Item
        if (itemId != -1) {
            loadItemData(itemId);
            Log.i("DisplayItem", "Successfully loaded item: " + itemId);
        } else {
            Toast.makeText(this, "Error: Invalid Item ID.", Toast.LENGTH_LONG).show();
            Log.e("DisplayItem", "Unable to load item: " + itemId);
            finish();
            return;
        }

        // OnClick Listener
        removeButton.setOnClickListener(v -> {
            removeItem();
        });
    }

    private void loadItemData(long id) {
        Log.d("DisplayItem", "Attempting to load item data for ID: " + id);
        currentItem = databaseHelper.getItemById(id);

        if (currentItem != null) {
            String headingText = currentItem.getPostType() + ": " + currentItem.getName();
            itemDetailsHeadingTextView.setText(headingText);

            nameTextView.setText(currentItem.getName());
            phoneTextView.setText(currentItem.getPhone());
            descriptionTextView.setText(currentItem.getDescription());
            if ("Lost".equalsIgnoreCase(currentItem.getPostType())) {
                // Hide location label and value if the item is Lost
                locationTitleTextView.setVisibility(View.GONE);
                locationTextView.setVisibility(View.GONE);
            } else {
                // Show location label and value if the item is Found
                locationTitleTextView.setVisibility(View.VISIBLE);
                locationTextView.setVisibility(View.VISIBLE);
                locationTextView.setText(currentItem.getLocation());
            }
            dateTextView.setText(currentItem.getDate());
            Log.i("DisplayItem", "Successfully loaded and displayed item: " + currentItem.getName());
        } else {
            Log.e("DisplayItem", "Item with ID " + id + " not found in database.");
            Toast.makeText(this, "Error: Item details could not be loaded.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void removeItem() {
        if (currentItem != null) {
            Log.d("DisplayItem", "Attempting to remove item ID: " + currentItem.getId());
            int rowsDeleted = databaseHelper.deleteItem(currentItem.getId());

            if (rowsDeleted > 0) {
                Toast.makeText(this, "Item removed successfully.", Toast.LENGTH_SHORT).show();
                Log.i("DisplayItem", "Successfully removed item ID: " + currentItem.getId());
                finish();
            } else {
                Toast.makeText(this, "Error: Could not remove item.", Toast.LENGTH_SHORT).show();
                Log.e("DisplayItem", "Failed to remove item ID: " + currentItem.getId() + ". Rows affected: " + rowsDeleted);
            }
        } else {
            Toast.makeText(this, "Error: No item data available to remove.", Toast.LENGTH_SHORT).show();
            Log.e("DisplayItem", "Attempted to remove item, but currentItem was null.");
        }
    }

}