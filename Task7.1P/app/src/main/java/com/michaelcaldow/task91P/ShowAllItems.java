package com.michaelcaldow.task91P;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast; // For showing messages

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList; // For List
import java.util.List;     // For List


public class ShowAllItems extends AppCompatActivity implements ItemAdapter.OnItemClickListener {

    private RecyclerView itemsRecyclerView;
    private ItemAdapter itemAdapter;
    private List<Item> itemList;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_all_items);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        itemsRecyclerView = findViewById(R.id.items_recyclerView);
        itemList = new ArrayList<>();

        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialise the adapter
        itemAdapter = new ItemAdapter(this, itemList, this);
        itemsRecyclerView.setAdapter(itemAdapter);

        loadItems();

        // Initialize DatabaseHelper
        databaseHelper = DatabaseHelper.getInstance(this); // Uses singleton instance

        itemsRecyclerView = findViewById(R.id.items_recyclerView);
        itemList = new ArrayList<>();

        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new ItemAdapter(this, itemList, this);
        itemsRecyclerView.setAdapter(itemAdapter);

    }
    @Override
    protected void onResume() {
        super.onResume();
        loadItemsFromDatabase();
    }

    private void loadItemsFromDatabase() {
        itemList.clear();
        List<Item> itemsFromDb = databaseHelper.getAllItems();
        if (itemsFromDb != null && !itemsFromDb.isEmpty()) {
            itemList.addAll(itemsFromDb);
            Log.i("ShowAllItems", "Loaded " + itemsFromDb.size() + " items from database.");
        } else {
            Log.i("ShowAllItems", "No items found in database.");
            Toast.makeText(this, "No items found.", Toast.LENGTH_SHORT).show();
        }

        itemAdapter.setItems(itemList);
    }
    private void loadItems() {
        itemList.clear();
        itemAdapter.setItems(itemList);
    }

    @Override
    public void onItemClick(Item item) {
        Intent intent = new Intent(ShowAllItems.this, DisplayItem.class);
        intent.putExtra("ITEM_ID", item.getId());
        startActivity(intent);
    }

}