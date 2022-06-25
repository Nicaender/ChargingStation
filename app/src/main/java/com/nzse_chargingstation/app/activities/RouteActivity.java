package com.nzse_chargingstation.app.activities;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nzse_chargingstation.app.R;
import com.nzse_chargingstation.app.classes.ContainerAndGlobal;
import com.nzse_chargingstation.app.classes.LocaleHelper;
import com.nzse_chargingstation.app.classes.RouteEachAdapter;
import com.nzse_chargingstation.app.classes.RoutePlan;

public class RouteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        Button btnRouteEachBack = findViewById(R.id.buttonRouteEachBack);
        TextView tvRoutePlanName = findViewById(R.id.textViewRoutePlanName);

        RoutePlan currentRoutePlan = ContainerAndGlobal.getSelectedRoutePlan();
        ContainerAndGlobal.setSelectedRoutePlan(null);
        if(currentRoutePlan == null)
            finish();

        assert currentRoutePlan != null;
        tvRoutePlanName.setText(currentRoutePlan.getName());
        RecyclerView recyclerView = findViewById(R.id.rvRouteEachList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        RouteEachAdapter adapter = new RouteEachAdapter(this, currentRoutePlan);
        recyclerView.setAdapter(adapter);

        adapter.setRouteEachList(currentRoutePlan.getChargingStationRoutes());

        btnRouteEachBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "de"));
    }
}