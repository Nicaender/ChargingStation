package com.nzse_chargingstation.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.nzse_chargingstation.app.R;
import com.nzse_chargingstation.app.activities.LoginActivity;
import com.nzse_chargingstation.app.classes.ContainerAndGlobal;

public class SettingsFragment extends Fragment {

    Button btn_login_techniker, btn_darkmode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btn_login_techniker =  view.findViewById(R.id.button_login_techniker);
        btn_darkmode = view.findViewById(R.id.button_darkmode);

        // Saving state of our app
        // using SharedPreferences
        SharedPreferences sharedPreferences = this.requireActivity().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", false);

        // When user reopens the app
        // after applying dark/light mode
        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            btn_darkmode.setText(R.string.disable_darkmode);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            btn_darkmode.setText(R.string.enable_darkmode);
        }

        // Implementation of dark mode button
        btn_darkmode.setOnClickListener(v -> {
            if (isDarkModeOn) {
                // if dark mode is on it
                // will turn it off
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                // it will set isDarkModeOn
                // boolean to false
                editor.putBoolean("isDarkModeOn", false);
                editor.apply();
                // change text of Button
                btn_darkmode.setText(R.string.enable_darkmode);
            }
            else {
                // if dark mode is off
                // it will turn it on
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                // it will set isDarkModeOn
                // boolean to true
                editor.putBoolean("isDarkModeOn", true);
                editor.apply();
                // change text of Button
                btn_darkmode.setText(R.string.disable_darkmode);
            }
            ContainerAndGlobal.setChangedSetting(true);
        });

        // Implementation of button to login site from techniker
        btn_login_techniker.setOnClickListener(v -> startActivity(new Intent(getActivity(), LoginActivity.class)));
    }
}