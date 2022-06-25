package com.nzse_chargingstation.app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nzse_chargingstation.app.R;
import com.nzse_chargingstation.app.classes.ContainerAndGlobal;

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
        String betreiber = "Operator: " + ContainerAndGlobal.getClickedChargingStation().getBetreiber();
        String strasse = "Street: " + ContainerAndGlobal.getClickedChargingStation().getStrasse();
        String hausnummer = "House number: " + ContainerAndGlobal.getClickedChargingStation().getHausnummer();
        String adresszusatz = "Optional Address: " + ContainerAndGlobal.getClickedChargingStation().getAdresszusatz();
        String postleitzahl = "Postal code: " + ContainerAndGlobal.getClickedChargingStation().getPostleitzahl();
        String ort = "City: " + ContainerAndGlobal.getClickedChargingStation().getOrt();
        String bundesland = "State: " + ContainerAndGlobal.getClickedChargingStation().getBundesland();
        String kreisOderKreisfreieStadt = "District or independent city: " + ContainerAndGlobal.getClickedChargingStation().getKreis_kreisfreie_stadt();
        String breitengrad = "Latitude: " + ContainerAndGlobal.getClickedChargingStation().getBreitengrad();
        String laengengrad = "Longitude: " + ContainerAndGlobal.getClickedChargingStation().getLängengrad();
        String inbetriebnahmedatum = "Commissioned date: " + ContainerAndGlobal.getClickedChargingStation().getInbetriebnahmedatum();
        String anschlussleitung = "Connecting cable: " + ContainerAndGlobal.getClickedChargingStation().getAnschlussleitung();
        String artDerLadeeinrichung = "Type of charging device: " + ContainerAndGlobal.getClickedChargingStation().getArtDerLadeeinrichtung();
        String anzahlLadePunkte = "Number of charging points: " + ContainerAndGlobal.getClickedChargingStation().getAnzahlDerLadepunkte();
        String steckertypen1 = "Connector type 1: " + ContainerAndGlobal.getClickedChargingStation().getSteckertypen1();
        String steckertypen2 = "Connector type 2: " + ContainerAndGlobal.getClickedChargingStation().getSteckertypen2();
        String steckertypen3 = "Connector type 3: " + ContainerAndGlobal.getClickedChargingStation().getSteckertypen3();
        String steckertypen4 = "Connector type 4: " + ContainerAndGlobal.getClickedChargingStation().getSteckertypen4();
        tvBetreiber.setText(betreiber);
        tvStrasse.setText(strasse);
        tvHausnummer.setText(hausnummer);
        tvAdresszusatz.setText(adresszusatz);
        tvPostleitzahl.setText(postleitzahl);
        tvOrt.setText(ort);
        tvBundesland.setText(bundesland);
        tvKreisOderKreisfreiestadt.setText(kreisOderKreisfreieStadt);
        tvBreitengrad.setText(breitengrad);
        tvLaengengrad.setText(laengengrad);
        tvInbetriebnahmedatum.setText(inbetriebnahmedatum);
        tvAnschlussleitung.setText(anschlussleitung);
        tvArtDerLadeeinrichung.setText(artDerLadeeinrichung);
        tvAnzahlLadepunkte.setText(anzahlLadePunkte);
        tvSteckertypen1.setText(steckertypen1);
        tvSteckertypen2.setText(steckertypen2);
        tvSteckertypen3.setText(steckertypen3);
        tvSteckertypen4.setText(steckertypen4);
    }
}