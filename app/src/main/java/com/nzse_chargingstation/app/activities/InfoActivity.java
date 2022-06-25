package com.nzse_chargingstation.app.activities;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nzse_chargingstation.app.R;
import com.nzse_chargingstation.app.classes.ContainerAndGlobal;
import com.nzse_chargingstation.app.classes.LocaleHelper;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Button btnInfoBack;
        TextView tvBetreiber, tvStrasse, tvHausnummer, tvAdresszusatz, tvPostleitzahl, tvOrt, tvBundesland,
                tvKreisOderKreisfreiestadt, tvBreitengrad, tvLaengengrad, tvInbetriebnahmedatum,
                tvAnschlussleitung, tvArtDerLadeeinrichung, tvAnzahlLadepunkte, tvSteckertypen1,
                tvSteckertypen2, tvSteckertypen3, tvSteckertypen4 ;

        btnInfoBack = findViewById(R.id.buttonInfoBack);
        tvBetreiber = findViewById(R.id.textViewBetreiber);
        tvStrasse = findViewById(R.id.textViewStrasse);
        tvHausnummer = findViewById(R.id.textViewHausnummer);
        tvAdresszusatz = findViewById(R.id.textViewAdresszusatz);
        tvPostleitzahl = findViewById(R.id.textViewPostleitzahl);
        tvOrt = findViewById(R.id.textViewOrt);
        tvBundesland = findViewById(R.id.textViewBundesland);
        tvKreisOderKreisfreiestadt = findViewById(R.id.textViewKreisOderKreisfreieStadt);
        tvBreitengrad = findViewById(R.id.textViewBreitengrad);
        tvLaengengrad = findViewById(R.id.textViewLaengengrad);
        tvInbetriebnahmedatum = findViewById(R.id.textViewInbetriebnahmedatum);
        tvAnschlussleitung = findViewById(R.id.textViewAnschlussleitung);
        tvArtDerLadeeinrichung = findViewById(R.id.textViewArtDerLadeeinrichung);
        tvAnzahlLadepunkte = findViewById(R.id.textViewAnzahlLadepunkte);
        tvSteckertypen1 = findViewById(R.id.textViewSteckertypen1);
        tvSteckertypen2 = findViewById(R.id.textViewSteckertypen2);
        tvSteckertypen3 = findViewById(R.id.textViewSteckertypen3);
        tvSteckertypen4 = findViewById(R.id.textViewSteckertypen4);

        btnInfoBack.setOnClickListener(v -> finish());
        String operator = getResources().getString(R.string.string_operator) + ": " + ContainerAndGlobal.getClickedChargingStation().getBetreiber();
        String street = getResources().getString(R.string.string_street) + ": " + ContainerAndGlobal.getClickedChargingStation().getStrasse();
        String houseNumber = getResources().getString(R.string.string_house_number) + ": " + ContainerAndGlobal.getClickedChargingStation().getHausnummer();
        String optionalAddress = getResources().getString(R.string.string_optional_address) + ": " + ContainerAndGlobal.getClickedChargingStation().getAdresszusatz();
        String postalCode = getResources().getString(R.string.string_postal_code) + ": " + ContainerAndGlobal.getClickedChargingStation().getPostleitzahl();
        String ort = getResources().getString(R.string.string_city) + ": " + ContainerAndGlobal.getClickedChargingStation().getOrt();
        String state = getResources().getString(R.string.string_state) + ": " + ContainerAndGlobal.getClickedChargingStation().getBundesland();
        String districtOrIndependentCity = getResources().getString(R.string.string_district_question) + ": " + ContainerAndGlobal.getClickedChargingStation().getKreis_kreisfreie_stadt();
        String latitude = getResources().getString(R.string.string_latitude) + ": " + ContainerAndGlobal.getClickedChargingStation().getBreitengrad();
        String longitude = getResources().getString(R.string.string_longitude) + ": " + ContainerAndGlobal.getClickedChargingStation().getLängengrad();
        String installationDate = getResources().getString(R.string.string_commissioned_date) + ": " + ContainerAndGlobal.getClickedChargingStation().getInbetriebnahmedatum();
        String connectingCable = getResources().getString(R.string.string_connecting_cable) + ": " + ContainerAndGlobal.getClickedChargingStation().getAnschlussleitung();
        String typeOfChargingDevice = getResources().getString(R.string.string_type_of_charging_device) + ": " + ContainerAndGlobal.getClickedChargingStation().getArtDerLadeeinrichtung();
        String numberOfChargingPoints = getResources().getString(R.string.string_number_of_charging_points) + ": " + ContainerAndGlobal.getClickedChargingStation().getAnzahlDerLadepunkte();
        String connectorType1 = getResources().getString(R.string.string_connector_type_1) + ": " + ContainerAndGlobal.getClickedChargingStation().getSteckertypen1();
        String connectorType2 = getResources().getString(R.string.string_connector_type_2) + ": " + ContainerAndGlobal.getClickedChargingStation().getSteckertypen2();
        String connectorType3 = getResources().getString(R.string.string_connector_type_3) + ": " + ContainerAndGlobal.getClickedChargingStation().getSteckertypen3();
        String connectorType4 = getResources().getString(R.string.string_connector_type_4) + ": " + ContainerAndGlobal.getClickedChargingStation().getSteckertypen4();
        tvBetreiber.setText(operator);
        tvStrasse.setText(street);
        tvHausnummer.setText(houseNumber);
        tvAdresszusatz.setText(optionalAddress);
        tvPostleitzahl.setText(postalCode);
        tvOrt.setText(ort);
        tvBundesland.setText(state);
        tvKreisOderKreisfreiestadt.setText(districtOrIndependentCity);
        tvBreitengrad.setText(latitude);
        tvLaengengrad.setText(longitude);
        tvInbetriebnahmedatum.setText(installationDate);
        tvAnschlussleitung.setText(connectingCable);
        tvArtDerLadeeinrichung.setText(typeOfChargingDevice);
        tvAnzahlLadepunkte.setText(numberOfChargingPoints);
        tvSteckertypen1.setText(connectorType1);
        tvSteckertypen2.setText(connectorType2);
        tvSteckertypen3.setText(connectorType3);
        tvSteckertypen4.setText(connectorType4);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "de"));
    }
}