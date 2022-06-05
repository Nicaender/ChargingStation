package com.nzse_chargingstation.app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nzse_chargingstation.app.R;
import com.nzse_chargingstation.app.classes.ContainerAndGlobal;
import com.nzse_chargingstation.app.classes.Defective;

public class ReportActivity extends AppCompatActivity {

    Button btn_report_back, btn_report_confirm;
    TextView tv_charging_station_address;
    EditText et_additional_information;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        btn_report_back = findViewById(R.id.button_report_back);
        btn_report_confirm = findViewById(R.id.button_report_confirm);
        tv_charging_station_address = findViewById(R.id.textview_charging_station_address);
        et_additional_information = findViewById(R.id.edittext_additional_information);

        tv_charging_station_address.setText("-");
        tv_charging_station_address.setText(ContainerAndGlobal.getReportedChargingStation().getStrasse());

        btn_report_back.setOnClickListener(v -> finish());

        btn_report_confirm.setOnClickListener(v -> add_defective());
    }

    private void add_defective()
    {
        Defective tmp = new Defective(ContainerAndGlobal.getReportedChargingStation(), et_additional_information.getText().toString(), ContainerAndGlobal.isAlreadyFavorite(ContainerAndGlobal.getReportedChargingStation()));
        if(ContainerAndGlobal.addOrRemoveDefective(tmp, true) == -1)
            return;

        finish();
        Toast.makeText(this, "Charging station successfully reported", Toast.LENGTH_LONG).show();
    }
}