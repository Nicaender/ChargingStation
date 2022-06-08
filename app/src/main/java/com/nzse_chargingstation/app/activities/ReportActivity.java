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
    TextView tvChargingStationAddress;
    EditText etAdditionalInformation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        btnReportBack = findViewById(R.id.buttonReportBack);
        btnReportConfirm = findViewById(R.id.buttonReportConfirm);
        tvChargingStationAddress = findViewById(R.id.textViewChargingStationAddress);
        etAdditionalInformation = findViewById(R.id.editTextAdditionalInformation);

        String name = ContainerAndGlobal.getReportedChargingStation().getStrasse() + ' ' + ContainerAndGlobal.getReportedChargingStation().getHausnummer();
        tvChargingStationAddress.setText(name);

        btnReportBack.setOnClickListener(v -> finish());

        btnReportConfirm.setOnClickListener(v -> add_defective());
    }

    private void add_defective()
    {
        Defective tmp;
        int isFavorite = ContainerAndGlobal.searchInFavorites(ContainerAndGlobal.getReportedChargingStation().getLocation());
        if(isFavorite == -1)
            tmp = new Defective(ContainerAndGlobal.getReportedChargingStation(), ContainerAndGlobal.indexSearchInList(ContainerAndGlobal.getReportedChargingStation().getLocation()), null, etAdditionalInformation.getText().toString());
        else
            tmp = new Defective(null, -1, ContainerAndGlobal.getFavoriteList().get(isFavorite), etAdditionalInformation.getText().toString());
        ContainerAndGlobal.addDefective(tmp);
        ContainerAndGlobal.saveData(false, getApplicationContext());
        finish();
        Toast.makeText(this, "Charging station successfully reported", Toast.LENGTH_LONG).show();
    }
}