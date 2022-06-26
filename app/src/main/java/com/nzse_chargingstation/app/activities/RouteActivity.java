package com.nzse_chargingstation.app.activities;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
        Button btnRouteRemove = findViewById(R.id.buttonRouteRemove);
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

        btnRouteRemove.setOnClickListener(v -> {
            for(int i = 0; i < ContainerAndGlobal.getRoutePlanList().size(); i++)
            {
                if(currentRoutePlan.equals(ContainerAndGlobal.getRoutePlanList().get(i))) {
                    ContainerAndGlobal.getRoutePlanList().remove(i);
                    Toast.makeText(this, getString(R.string.route_plan_is_removed), Toast.LENGTH_SHORT).show();
                    ContainerAndGlobal.saveData(3, this);
                    finish();
                    break;
                }
            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "de"));
    }
}