package com.nzse_chargingstation.app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nzse_chargingstation.app.R;
import com.nzse_chargingstation.app.classes.ContainerAndGlobal;
import com.nzse_chargingstation.app.classes.Defective;
import com.nzse_chargingstation.app.classes.LocaleHelper;

import org.w3c.dom.Text;

public class ReportActivity extends AppCompatActivity {

    EditText etAdditionalInformation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        try {
            ImageView imgViewReportBack = findViewById(R.id.imageViewReportBack);
            Button btnReportConfirm = findViewById(R.id.buttonReportConfirm);
            TextView tvChargingStationAddress = findViewById(R.id.textViewChargingStationAddress);
            TextView tvChargingStationCity = findViewById(R.id.textViewChargingStationCity);
            etAdditionalInformation = findViewById(R.id.editTextAdditionalInformation);

            String name = ContainerAndGlobal.getReportedChargingStation().getStrasse() + ' ' + ContainerAndGlobal.getReportedChargingStation().getHausnummer();
            tvChargingStationAddress.setText(name);
            String city = ContainerAndGlobal.getReportedChargingStation().getPostleitzahl() + ", " + ContainerAndGlobal.getReportedChargingStation().getOrt();
            tvChargingStationCity.setText(city);

            imgViewReportBack.setOnClickListener(v -> finish());

            btnReportConfirm.setOnClickListener(v -> add_defective());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "de"));
    }

    /**
     * Adding a new defective charging station
     */
    private void add_defective() {
        try {
            Defective tmp;
            tmp = new Defective(ContainerAndGlobal.getReportedChargingStation(), ContainerAndGlobal.isInFavorite(ContainerAndGlobal.getReportedChargingStation()), etAdditionalInformation.getText().toString());
            ContainerAndGlobal.addDefective(tmp);
            ContainerAndGlobal.saveData(2, getApplicationContext());
            finish();
            Toast.makeText(this, getResources().getString(R.string.charging_station_successfully_reported), Toast.LENGTH_SHORT).show();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }
}