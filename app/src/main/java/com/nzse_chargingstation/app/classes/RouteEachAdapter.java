package com.nzse_chargingstation.app.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nzse_chargingstation.app.R;

import java.util.ArrayList;
import java.util.List;

public class RouteEachAdapter extends RecyclerView.Adapter<RouteEachAdapter.RouteEachHolder> {

    public RouteEachAdapter(Context context, RoutePlan routePlan)
    {
        this.mContext = context;
        this.routePlan = routePlan;
    }
    private List<ChargingStation> routeEachList = new ArrayList<>();
    private final RoutePlan routePlan;
    private final Context mContext;

    @NonNull
    @Override
    public RouteEachHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_each, parent, false);
        return new RouteEachHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteEachHolder holder, int position) {
        String name = routeEachList.get(holder.getAdapterPosition()).getStrasse() + ' ' + routeEachList.get(holder.getAdapterPosition()).getHausnummer();
        holder.tvRouteEachAddress.setText(name);
        String city = routeEachList.get(holder.getAdapterPosition()).getPostleitzahl() + ", " + routeEachList.get(holder.getAdapterPosition()).getOrt();
        holder.tvRouteEachCity.setText(city);
        String distance;
        if(ContainerAndGlobal.getCurrentLocation() != null)
            distance = ContainerAndGlobal.df.format(ContainerAndGlobal.calculateLength(routeEachList.get(holder.getAdapterPosition()).getLocation(), ContainerAndGlobal.getCurrentLocation())) + " KM";
        else
            distance = mContext.getResources().getString(R.string.distance) + " : " + mContext.getResources().getString(R.string.unknown);
        holder.tvRouteEachDistance.setText(distance);

        holder.btnRouteEachRemove.setOnClickListener(v -> {
            holder.btnRouteEachRemove.setClickable(false);
            routePlan.getChargingStationRoutes().remove(holder.getAdapterPosition());
            ContainerAndGlobal.saveData(3, v.getContext());
            notifyItemRemoved(holder.getAdapterPosition());
            Toast.makeText(v.getContext(), v.getResources().getString(R.string.removed_from_this_route_plan), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return routeEachList.size();
    }

    public void setRouteEachList(List<ChargingStation> routeEachList)
    {
        this.routeEachList = routeEachList;
        notifyItemRangeChanged(0, routeEachList.size());
    }

    class RouteEachHolder extends RecyclerView.ViewHolder {

        private final TextView tvRouteEachAddress, tvRouteEachCity, tvRouteEachDistance;
        private final Button btnRouteEachRemove;

        public RouteEachHolder(View itemView)
        {
            super(itemView);
            tvRouteEachAddress = itemView.findViewById(R.id.textViewRouteEachAddress);
            tvRouteEachCity = itemView.findViewById(R.id.textViewRouteEachCity);
            tvRouteEachDistance = itemView.findViewById(R.id.textViewRouteEachDistance);
            btnRouteEachRemove = itemView.findViewById(R.id.buttonRouteEachRemove);
        }
    }
}
