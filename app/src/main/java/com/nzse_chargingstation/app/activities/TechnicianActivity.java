package com.nzse_chargingstation.app.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;

import com.nzse_chargingstation.app.R;
import com.nzse_chargingstation.app.classes.ContainerAndGlobal;
import com.nzse_chargingstation.app.classes.DefectiveAdapter;
import com.nzse_chargingstation.app.classes.DefectiveDistanceComparator;
import com.nzse_chargingstation.app.classes.FavoriteDistanceComparator;

public class TechnicianActivity extends AppCompatActivity {

    Button btnBackTechniker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technician);

        if(ContainerAndGlobal.getCurrentLocation() != null)
            ContainerAndGlobal.getDefectiveList().sort(new DefectiveDistanceComparator());

        RecyclerView recyclerView = findViewById(R.id.rvDefectiveList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        DefectiveAdapter adapter = new DefectiveAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setDefectiveList(ContainerAndGlobal.getDefectiveList());

        btnBackTechniker = findViewById(R.id.buttonBackTechniker);

        btnBackTechniker.setOnClickListener(v -> finish());
    }
}