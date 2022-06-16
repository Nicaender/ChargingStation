package com.nzse_chargingstation.app.classes;

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

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.favoriteHolder> {
    private List<ChargingStation> favoriteList = new ArrayList<>();

    @NonNull
    @Override
    public favoriteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new favoriteHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull favoriteHolder holder, int position) {
        ChargingStation currentFavorite = favoriteList.get(position);
        String name = currentFavorite.getStrasse() + ' ' + currentFavorite.getHausnummer();
        holder.tvFavoriteAddress.setText(name);
        String distance;
        if(ContainerAndGlobal.getCurrentLocation() != null)
            distance = ContainerAndGlobal.df.format(ContainerAndGlobal.calculateLength(currentFavorite.getLocation(), ContainerAndGlobal.getCurrentLocation())) + " KM";
        else
            distance = "Unknown distance";
        holder.tvDistance.setText(distance);

        holder.btnUnfavorite.setOnClickListener(v -> {
            int index = ContainerAndGlobal.indexSearchFavorites(currentFavorite.getLocation());
            ContainerAndGlobal.getFavoriteList().remove(index);
            ContainerAndGlobal.addChargingStation(currentFavorite.getMyIndex(), currentFavorite);
            ContainerAndGlobal.saveData(true, v.getContext());
            notifyItemRemoved(holder.getAdapterPosition());
            Toast.makeText(v.getContext(), "Removed from favorites", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    public void setFavoriteList(List<ChargingStation> favorites)
    {
        this.favoriteList = favorites;
        notifyItemRangeChanged(0, favorites.size());
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    class favoriteHolder extends RecyclerView.ViewHolder {
        private final TextView tvFavoriteAddress;
        private final TextView tvDistance;
        private final Button btnUnfavorite;

        public favoriteHolder(View itemView)
        {
            super(itemView);
            tvFavoriteAddress = itemView.findViewById(R.id.textViewFavoriteAdress);
            tvDistance = itemView.findViewById(R.id.textViewDistance);
            btnUnfavorite = itemView.findViewById(R.id.buttonUnfavorite);
        }
    }
}
