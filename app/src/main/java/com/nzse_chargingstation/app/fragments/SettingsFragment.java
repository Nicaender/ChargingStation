package com.nzse_chargingstation.app.fragments;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.nzse_chargingstation.app.R;
import com.nzse_chargingstation.app.activities.LoginActivity;
import com.nzse_chargingstation.app.classes.ContainerAndGlobal;

public class SettingsFragment extends Fragment {

    Button btnLoginTechniker, btnDarkmode;
    EditText etViewRadiusValue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnLoginTechniker =  view.findViewById(R.id.buttonLoginTechniker);
        btnDarkmode = view.findViewById(R.id.buttonDarkMode);
        etViewRadiusValue = view.findViewById(R.id.editTextViewRadiusValue);

        // Saving state of our app
        // using SharedPreferences
        SharedPreferences sharedPreferences = this.requireActivity().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", false);
        final int maxViewRange = sharedPreferences.getInt("maxViewRange", 10);

        // When user reopens the app
        // after applying dark/light mode
        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            btnDarkmode.setText(R.string.disable_darkmode);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            btnDarkmode.setText(R.string.enable_darkmode);
        }
        ContainerAndGlobal.setMaxViewRange(maxViewRange);

        etViewRadiusValue.setText(String.valueOf(ContainerAndGlobal.getMaxViewRange()));

        // Implementation of dark mode button
        btnDarkmode.setOnClickListener(v -> {
            if (isDarkModeOn) {
                // if dark mode is on it
                // will turn it off
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                // it will set isDarkModeOn
                // boolean to false
                editor.putBoolean("isDarkModeOn", false);
                editor.apply();
                // change text of Button
                btnDarkmode.setText(R.string.enable_darkmode);
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
                btnDarkmode.setText(R.string.disable_darkmode);
            }
            ContainerAndGlobal.setChangedSetting(true);
        });

        // Implementation of button to login site from techniker
        btnLoginTechniker.setOnClickListener(v -> startActivity(new Intent(getActivity(), LoginActivity.class)));

        // Implementation of button to limit max view range in map
        etViewRadiusValue.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Perform action on key press
                ContainerAndGlobal.setMaxViewRange(Integer.parseInt(etViewRadiusValue.getText().toString()));
                editor.putInt("maxViewRange", ContainerAndGlobal.getMaxViewRange());
                editor.apply();
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etViewRadiusValue.getWindowToken(), 0);
                return true;
            }
            return false;
        });
    }
}