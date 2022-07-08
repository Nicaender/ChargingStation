package com.nzse_chargingstation.app.activities;

import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nzse_chargingstation.app.R;
import com.nzse_chargingstation.app.classes.ChargingStation;
import com.nzse_chargingstation.app.classes.ContainerAndGlobal;
import com.nzse_chargingstation.app.classes.LocaleHelper;
import com.nzse_chargingstation.app.classes.SearchAdapter;

import java.util.ArrayList;

/**
 * An activity class that will be used when user wants to search a charging station from the search bar. This class contains all charging stations except defective charging stations.
 */
public class SearchActivity extends AppCompatActivity {

    private SearchAdapter searchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        try {
            SearchView svChargingStation;
            svChargingStation = findViewById(R.id.searchViewChargingStation);
            ImageView imgViewSearchBack;
            imgViewSearchBack = findViewById(R.id.imageViewSearchBack);

            // Implementation of back button
            imgViewSearchBack.setOnClickListener(v -> {
                finish();
                overridePendingTransition(0, 0);
            });

            // Implementation of search bar
            RecyclerView recyclerView = findViewById(R.id.rvSearchList);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setHasFixedSize(true);

            ContainerAndGlobal.getFilteredList().clear();
            ContainerAndGlobal.getFilteredList().addAll(ContainerAndGlobal.getChargingStationList());
            ContainerAndGlobal.getFilteredList().addAll(ContainerAndGlobal.getFavoriteList());
            for(int i = 0; i < ContainerAndGlobal.getFilteredList().size(); i++) {
                if(ContainerAndGlobal.isInDefective(ContainerAndGlobal.getFilteredList().get(i))) {
                    ContainerAndGlobal.getFilteredList().remove(i);
                    i--;
                }
            }
            searchAdapter = new SearchAdapter(this,  ContainerAndGlobal.getFilteredList());
            recyclerView.setAdapter(searchAdapter);

            svChargingStation.onActionViewExpanded();

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "de"));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    /**
     * Filtering the list that contains the word from the searched words
     * @param text is the current searched address
     */
    private void filter(String text) {
        try {
            // creating a new array list to filter our data.
            ArrayList<ChargingStation> filteredList = new ArrayList<>();

            // running a for loop to compare elements.
            for (ChargingStation item : ContainerAndGlobal.getFilteredList()) {
                // checking if the entered string matched with any item of our recycler view.
                String name = item.getStrasse() + " " + item.getHausnummer();
                String city = item.getPostleitzahl() + ", " + item.getOrt();
                if (name.toLowerCase().contains(text.toLowerCase()) || city.toLowerCase().contains(text.toLowerCase())) {
                    // if the item is matched we are
                    // adding it to our filtered list.
                    filteredList.add(item);
                }
            }
            searchAdapter.filterList(filteredList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}