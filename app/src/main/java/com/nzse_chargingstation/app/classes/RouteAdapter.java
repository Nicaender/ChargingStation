package com.nzse_chargingstation.app.classes;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nzse_chargingstation.app.R;
import com.nzse_chargingstation.app.activities.MainActivity;
import com.nzse_chargingstation.app.activities.RouteActivity;

import java.util.ArrayList;
import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteHolder> {

    public RouteAdapter(Context context)
    {
        this.mContext = context;
    }
    private List<RoutePlan> routeList = new ArrayList<>();
    private final Context mContext;

    @NonNull
    @Override
    public RouteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route, parent, false);
        return new RouteHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteHolder holder, int position) {
        holder.tvRouteName.setText(routeList.get(holder.getAdapterPosition()).getName());

        holder.btnRouteEdit.setOnClickListener(v -> {
            ContainerAndGlobal.setSelectedRoutePlan(routeList.get(holder.getAdapterPosition()));
            mContext.startActivity(new Intent(mContext, RouteActivity.class));
        });

        holder.btnRouteNavigate.setOnClickListener(v -> {
            if(routeList.get(holder.getAdapterPosition()).getChargingStationRoutes().size() != 0) {
                ContainerAndGlobal.setNavigateRoutePlan(routeList.get(holder.getAdapterPosition()));
                ((MainActivity)mContext).switchFragment(0);
            }
            else
                Toast.makeText(mContext, R.string.this_route_plan_has_0_charging_station, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    public void setRouteList(List<RoutePlan> routeList)
    {
        this.routeList = routeList;
        notifyItemRangeChanged(0, routeList.size());
    }

    class RouteHolder extends RecyclerView.ViewHolder {
        private final TextView tvRouteName;
        private final Button btnRouteEdit, btnRouteNavigate;

        public RouteHolder(View itemView)
        {
            super(itemView);
            tvRouteName = itemView.findViewById(R.id.textViewRouteName);
            btnRouteEdit = itemView.findViewById(R.id.buttonRouteEdit);
            btnRouteNavigate = itemView.findViewById(R.id.buttonRouteNavigate);
        }
    }
}
