package com.michaelcaldow.task91P;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    Button createAdvertButton, showAllItemsButton, showOnMapButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        createAdvertButton = findViewById(R.id.createAdvert_button);
        showAllItemsButton = findViewById(R.id.showAllItems_button);
        showOnMapButton = findViewById(R.id.showOnMap_button);

        createAdvertButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), CreateNewAdvert.class);
            startActivity(intent);
        });

        showAllItemsButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ShowAllItems.class);
            startActivity(intent);
        });

        showOnMapButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), MapActivity.class);
            startActivity(intent);
        });
    }
}