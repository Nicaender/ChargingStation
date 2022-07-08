package com.nzse_chargingstation.app.activities;

import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nzse_chargingstation.app.R;
import com.nzse_chargingstation.app.classes.ContainerAndGlobal;
import com.nzse_chargingstation.app.classes.DefectiveAdapter;
import com.nzse_chargingstation.app.classes.DefectiveDistanceComparator;
import com.nzse_chargingstation.app.classes.LocaleHelper;

/**
 * An activity class that show the lists of defective charging station. Mainly used by Technician to mark and repair the defective charging station.
 */
public class TechnicianActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technician);

        try {
            if(ContainerAndGlobal.getCurrentLocation() != null)
                ContainerAndGlobal.getDefectiveList().sort(new DefectiveDistanceComparator());

            RecyclerView recyclerView = findViewById(R.id.rvDefectiveList);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setHasFixedSize(true);

            DefectiveAdapter adapter = new DefectiveAdapter(this);
            recyclerView.setAdapter(adapter);

            adapter.setDefectiveList(ContainerAndGlobal.getDefectiveList());

            ImageView imgViewTechnicianBack = findViewById(R.id.imageViewTechnicianBack);

            imgViewTechnicianBack.setOnClickListener(v -> finish());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "de"));
    }
}