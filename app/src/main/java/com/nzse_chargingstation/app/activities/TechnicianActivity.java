package com.nzse_chargingstation.app.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;

import com.nzse_chargingstation.app.R;
import com.nzse_chargingstation.app.classes.ContainerAndGlobal;
import com.nzse_chargingstation.app.classes.DefectiveAdapter;

public class TechnicianActivity extends AppCompatActivity {

    Button btn_back_techniker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technician);

        RecyclerView recyclerView = findViewById(R.id.rv_defective_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        DefectiveAdapter adapter = new DefectiveAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setDefective_list(ContainerAndGlobal.getDefectiveList());

        btn_back_techniker = findViewById(R.id.button_back_techniker);

        btn_back_techniker.setOnClickListener(v -> finish());
    }
}