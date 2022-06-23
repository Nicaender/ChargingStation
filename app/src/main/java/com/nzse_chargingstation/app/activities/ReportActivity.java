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

    Button btnReportBack, btnReportConfirm;
    TextView tvChargingStationAddress, tvChargingStationCity;
    EditText etAdditionalInformation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        btnReportBack = findViewById(R.id.buttonReportBack);
        btnReportConfirm = findViewById(R.id.buttonReportConfirm);
        tvChargingStationAddress = findViewById(R.id.textViewChargingStationAddress);
        tvChargingStationCity = findViewById(R.id.textViewChargingStationCity);
        etAdditionalInformation = findViewById(R.id.editTextAdditionalInformation);

        String name = ContainerAndGlobal.getReportedChargingStation().getStrasse() + ' ' + ContainerAndGlobal.getReportedChargingStation().getHausnummer();
        tvChargingStationAddress.setText(name);
        String city = ContainerAndGlobal.getReportedChargingStation().getPostleitzahl() + ", " + ContainerAndGlobal.getReportedChargingStation().getOrt();
        tvChargingStationCity.setText(city);

        btnReportBack.setOnClickListener(v -> finish());

        btnReportConfirm.setOnClickListener(v -> add_defective());
    }

    /**
     * Adding a new defective charging station
     */
    private void add_defective()
    {
        Defective tmp;
        tmp = new Defective(ContainerAndGlobal.getReportedChargingStation(), ContainerAndGlobal.isInFavorite(ContainerAndGlobal.getReportedChargingStation()), etAdditionalInformation.getText().toString());
        ContainerAndGlobal.addDefective(tmp);
        ContainerAndGlobal.saveData(false, getApplicationContext());
        finish();
        Toast.makeText(this, "Charging station successfully reported", Toast.LENGTH_LONG).show();
    }
}