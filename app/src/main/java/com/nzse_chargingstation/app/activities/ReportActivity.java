package com.nzse_chargingstation.app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
        tv_charging_station_address.setText(ContainerAndGlobal.getReported_charging_station().getAddress());

        btn_report_back.setOnClickListener(v -> finish());

        btn_report_confirm.setOnClickListener(v -> add_defective());
    }

    private void add_defective()
    {

        for(int i = 0; i < ContainerAndGlobal.getCharging_station_list().size(); i++)
        {
            if(ContainerAndGlobal.getCharging_station_list().get(i).equals(ContainerAndGlobal.getReported_charging_station()))
            {
                Defective tmp = new Defective(ContainerAndGlobal.getReported_charging_station(), et_additional_information.getText().toString());
                ContainerAndGlobal.getDefective_list().add(tmp);
                ContainerAndGlobal.getCharging_station_list().remove(i);
                ContainerAndGlobal.setReported_charging_station(null);
                return;
            }
        }
        for(int i = 0; i < ContainerAndGlobal.getCharging_station_list_filtered().size(); i++)
        {
            if(ContainerAndGlobal.getCharging_station_list_filtered().get(i).equals(ContainerAndGlobal.getReported_charging_station()))
            {
                Defective tmp = new Defective(ContainerAndGlobal.getReported_charging_station(), et_additional_information.getText().toString());
                ContainerAndGlobal.getDefective_list().add(tmp);
                ContainerAndGlobal.getCharging_station_list_filtered().remove(i);
                ContainerAndGlobal.setReported_charging_station(null);
                return;
            }
        }
    }
}