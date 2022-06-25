package com.nzse_chargingstation.app.classes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.nzse_chargingstation.app.R;

public class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final Context mContext;

    public InfoWindowAdapter(Context context) {
        this.mContext = context.getApplicationContext();
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        return null;
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View v =  inflater.inflate(R.layout.custom_info_window, null);

        ChargingStation myCS = ContainerAndGlobal.searchChargingStation(marker.getPosition());
        String address, distance, fastCharging;

        TextView tvChargingStationAddress = v.findViewById(R.id.textViewChargingStationAddress);
        TextView tvChargingStationDistance = v.findViewById(R.id.textViewChargingStationDistance);
        TextView tvChargingStationFastCharging = v.findViewById(R.id.textViewChargingStationFastCharging);
        TextView tvInfo = v.findViewById(R.id.textViewInfo);
        TextView tvHoldInfo = v.findViewById(R.id.textViewHoldInfo);
        ImageView imgViewChargingStation = v.findViewById(R.id.imageViewChargingStation);
        imgViewChargingStation.setImageResource(mContext.getResources().getIdentifier("ic_baseline_electrical_services_24", "drawable", mContext.getPackageName()));

        assert myCS != null;
        address = myCS.getStrasse() + ' ' + myCS.getHausnummer();
        tvChargingStationAddress.setText(address);

        if(ContainerAndGlobal.getCurrentLocation() != null)
        {
            if(LocaleHelper.getLanguage(mContext).equals("en"))
                distance = mContext.getResources().getString(R.string.distance) + " : " + ContainerAndGlobal.df.format(ContainerAndGlobal.calculateLength(myCS.getLocation(), ContainerAndGlobal.getCurrentLocation())) + " KM";
            else
                distance = mContext.getResources().getString(R.string.distance_auf_deutsch) + " : " + ContainerAndGlobal.df.format(ContainerAndGlobal.calculateLength(myCS.getLocation(), ContainerAndGlobal.getCurrentLocation())) + " KM";
        }
        else
        {
            if(LocaleHelper.getLanguage(mContext).equals("en"))
                distance = mContext.getResources().getString(R.string.distance) + " : " + mContext.getResources().getString(R.string.unknown);
            else
                distance = mContext.getResources().getString(R.string.distance_auf_deutsch) + " : " + mContext.getResources().getString(R.string.unknown_auf_deutsch);
        }

        tvChargingStationDistance.setText(distance);

        if(myCS.getArtDerLadeeinrichtung().equals("Schnellladeeinrichtung"))
        {
            if(LocaleHelper.getLanguage(mContext).equals("en"))
                fastCharging = mContext.getString(R.string.fast_charging);
            else
                fastCharging = mContext.getString(R.string.fast_charging_auf_deutsch);
            imgViewChargingStation.setColorFilter(mContext.getColor(R.color.icon_color));
        }
        else
        {
            if(LocaleHelper.getLanguage(mContext).equals("en"))
                fastCharging = mContext.getString(R.string.fast_charging_no);
            else
                fastCharging = mContext.getString(R.string.fast_charging_no_auf_deutsch);
            if(ContainerAndGlobal.isDarkmode())
                imgViewChargingStation.setColorFilter(mContext.getColor(R.color.white));
            else
                imgViewChargingStation.setColorFilter(mContext.getColor(R.color.black));
        }

        tvChargingStationFastCharging.setText(fastCharging);

        if(LocaleHelper.getLanguage(mContext).equals("en"))
        {
            tvInfo.setText(mContext.getString(R.string.click_to_show_navigation_route));
            tvHoldInfo.setText(mContext.getString(R.string.hold_for_more_info));
        }
        else
        {
            tvInfo.setText(mContext.getString(R.string.click_to_show_navigation_route_auf_deutsch));
            tvHoldInfo.setText(mContext.getString(R.string.hold_for_more_info_auf_deutsch));
        }

        if(ContainerAndGlobal.isDarkmode())
        {
            v.setBackground(AppCompatResources.getDrawable(mContext, R.drawable.item_curved_dark));
            tvChargingStationAddress.setTextColor(mContext.getColor(R.color.white));
            tvChargingStationDistance.setTextColor(mContext.getColor(R.color.white));
            tvChargingStationFastCharging.setTextColor(mContext.getColor(R.color.white));
            tvInfo.setTextColor(mContext.getColor(R.color.white));
            tvHoldInfo.setTextColor(mContext.getColor(R.color.white));
        }
        else
        {
            v.setBackground(AppCompatResources.getDrawable(mContext, R.drawable.item_curved_light));
            tvChargingStationAddress.setTextColor(mContext.getColor(R.color.black));
            tvChargingStationDistance.setTextColor(mContext.getColor(R.color.black));
            tvChargingStationFastCharging.setTextColor(mContext.getColor(R.color.black));
            tvInfo.setTextColor(mContext.getColor(R.color.black));
            tvHoldInfo.setTextColor(mContext.getColor(R.color.black));
        }

        return v;
    }
}
