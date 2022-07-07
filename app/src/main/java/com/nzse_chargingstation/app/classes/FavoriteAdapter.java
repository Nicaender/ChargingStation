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
import com.nzse_chargingstation.app.activities.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteHolder> {

    public FavoriteAdapter(Context context) {
        this.mContext = context;
    }
    private List<ChargingStation> favoriteList = new ArrayList<>();
    private final Context mContext;

    @NonNull
    @Override
    public FavoriteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new FavoriteHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteHolder holder, int position) {
        holder.myCS = favoriteList.get(holder.getAdapterPosition());
        String name = holder.myCS.getStrasse() + ' ' + holder.myCS.getHausnummer();
        holder.tvFavoriteAddress.setText(name);
        String city = holder.myCS.getPostleitzahl() + ", " + holder.myCS.getOrt();
        holder.tvFavoriteCity.setText(city);
        String distance;
        if(ContainerAndGlobal.getCurrentLocation() != null)
            distance = ContainerAndGlobal.df.format(ContainerAndGlobal.calculateLength(holder.myCS.getPosition(), ContainerAndGlobal.getCurrentLocation())) + " KM";
        else
            distance = mContext.getResources().getString(R.string.distance) + " : " + mContext.getResources().getString(R.string.unknown);
        holder.tvDistance.setText(distance);

        holder.btnRemoveFromFavorite.setOnClickListener(v -> {
            holder.btnRemoveFromFavorite.setClickable(false);
            ContainerAndGlobal.removeFavorite(holder.myCS);
            ContainerAndGlobal.saveData(1, v.getContext());
            notifyItemRemoved(holder.getAdapterPosition());
            Toast.makeText(v.getContext(), v.getContext().getResources().getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    public void setFavoriteList(List<ChargingStation> favorites) {
        this.favoriteList = favorites;
        notifyItemRangeChanged(0, favorites.size());
    }

    protected class FavoriteHolder extends RecyclerView.ViewHolder {
        private final TextView tvFavoriteAddress, tvDistance, tvFavoriteCity;
        private final Button btnRemoveFromFavorite;
        private ChargingStation myCS;

        public FavoriteHolder(View itemView) {
            super(itemView);
            tvFavoriteAddress = itemView.findViewById(R.id.textViewFavoriteAddress);
            tvDistance = itemView.findViewById(R.id.textViewDistance);
            tvFavoriteCity = itemView.findViewById(R.id.textViewFavoriteCity);
            btnRemoveFromFavorite = itemView.findViewById(R.id.buttonRemoveFromFavorite);

            itemView.setOnClickListener(v -> {
                ContainerAndGlobal.setZoomToThisChargingStation(myCS);
                ((MainActivity)mContext).switchFragment(0);
            });
        }
    }
}
