package com.nzse_chargingstation.app.classes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nzse_chargingstation.app.R;

import java.util.ArrayList;

/**
 * A Search adapter that implements recycler view to show the list of all charging stations except defective ones.
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchHolder> {

    // creating a variable for array list and context.
    private ArrayList<ChargingStation> chargingStationList;
    private final Context mContext;

    public SearchAdapter(Context context, ArrayList<ChargingStation> chargingStationList) {
        this.chargingStationList = chargingStationList;
        this.mContext = context;
    }

    // method for filtering our recyclerview items.
    @SuppressLint("NotifyDataSetChanged")
    public void filterList(ArrayList<ChargingStation> filteredList) {
        // below line is to add our filtered
        // list in our course array list.
        chargingStationList = filteredList;
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SearchHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search, parent, false);
        return new SearchHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchHolder holder, int position) {
        // setting data to our views of recycler view.
        holder.myCS = chargingStationList.get(holder.getAdapterPosition());
        String name = holder.myCS.getStrasse() + ' ' + holder.myCS.getHausnummer();
        String city = holder.myCS.getPostleitzahl() + ", " + holder.myCS.getOrt();
        holder.tvSearchAddress.setText(name);
        String distance;
        if(ContainerAndGlobal.getCurrentLocation() != null)
            distance = ContainerAndGlobal.df.format(ContainerAndGlobal.calculateLength(holder.myCS.getPosition(), ContainerAndGlobal.getCurrentLocation())) + " KM";
        else
            distance = mContext.getResources().getString(R.string.distance) + " : " + mContext.getResources().getString(R.string.unknown);
        holder.tvDistance.setText(distance);
        holder.tvSearchCity.setText(city);
    }

    @Override
    public int getItemCount() {
        return chargingStationList.size();
    }

    protected class SearchHolder extends RecyclerView.ViewHolder {
        // creating variables for our views.
        private final TextView tvSearchAddress, tvDistance, tvSearchCity;
        private ChargingStation myCS;

        public SearchHolder(@NonNull View itemView) {
            super(itemView);
            // initializing our views with their ids.
            tvSearchAddress = itemView.findViewById(R.id.textViewSearchAddress);
            tvDistance = itemView.findViewById(R.id.textViewDistance);
            tvSearchCity = itemView.findViewById(R.id.textViewSearchCity);

            itemView.setOnClickListener(v -> {
                ContainerAndGlobal.setZoomToThisChargingStationOnPause(myCS);
                ((Activity)mContext).finish();
            });
        }
    }
}
