package com.nzse_chargingstation.app.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
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

        try {
            ImageView imgViewRouteBack = findViewById(R.id.imageViewRouteBack);
            Button btnRouteNameRename = findViewById(R.id.buttonRouteNameRename);
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

            // Implementation of back image button
            imgViewRouteBack.setOnClickListener(v -> finish());

            // Implementation of rename button
            btnRouteNameRename.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(R.string.route_plan_name_question);

                // Set up the input
                final EditText input = new EditText(v.getContext());
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton(getString(R.string.builder_positive_button), (dialog, which) -> {
                    if(!input.getText().toString().isEmpty())
                    {
                        currentRoutePlan.setName(input.getText().toString());
                        ContainerAndGlobal.saveData(3, v.getContext());
                        Toast.makeText(v.getContext(), getString(R.string.successfully_renamed), Toast.LENGTH_SHORT).show();
                        tvRoutePlanName.setText(currentRoutePlan.getName());
                    }
                });
                builder.setNegativeButton(getString(R.string.builder_negative_button), (dialog, which) -> dialog.cancel());
                AlertDialog dialog = builder.create();
                dialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(v.getContext(), R.drawable.item_curved));
                dialog.show();
            });

            // Implementation of remove button
            btnRouteRemove.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(R.string.delete_route_plan_question);
                // Set up the buttons
                builder.setPositiveButton(getString(R.string.builder_positive_button), (dialog, which) -> {
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
                builder.setNegativeButton(getString(R.string.builder_negative_button), (dialog, which) -> dialog.cancel());
                AlertDialog dialog = builder.create();
                dialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(v.getContext(), R.drawable.item_curved));
                dialog.show();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "de"));
    }
}