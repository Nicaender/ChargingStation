package com.nzse_chargingstation.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nzse_chargingstation.app.R;
import com.nzse_chargingstation.app.classes.ChargingStation;
import com.nzse_chargingstation.app.classes.ContainerAndGlobal;
import com.nzse_chargingstation.app.classes.SearchAdapter;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private SearchAdapter searchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        SearchView svChargingStation;
        svChargingStation = findViewById(R.id.searchViewChargingStation);
        Button btnSearchBack;
        btnSearchBack = findViewById(R.id.buttonSearchBack);

        svChargingStation.performClick();

        // Implementation of back button
        btnSearchBack.setOnClickListener(v -> finish());

        // Implementation of search bar
        RecyclerView recyclerView = findViewById(R.id.rvSearchList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        ContainerAndGlobal.getFilteredList().clear();
        ContainerAndGlobal.getFilteredList().addAll(ContainerAndGlobal.getChargingStationList());
        ContainerAndGlobal.getFilteredList().addAll(ContainerAndGlobal.getFavoriteList());
        searchAdapter = new SearchAdapter(this,  ContainerAndGlobal.getFilteredList());
        recyclerView.setAdapter(searchAdapter);

        svChargingStation.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });
    }

    /**
     * Filtering the list that contains the word from the searched words
     * @param text is the current searched address
     */
    private void filter(String text)
    {
        // creating a new array list to filter our data.
        ArrayList<ChargingStation> filteredList = new ArrayList<>();

        // running a for loop to compare elements.
        for (ChargingStation item : ContainerAndGlobal.getFilteredList()) {
            // checking if the entered string matched with any item of our recycler view.
            String name = item.getStrasse() + " " + item.getHausnummer();
            if (name.toLowerCase().contains(text.toLowerCase())) {
                // if the item is matched we are
                // adding it to our filtered list.
                filteredList.add(item);
            }
        }
        searchAdapter.filterList(filteredList);
    }
}