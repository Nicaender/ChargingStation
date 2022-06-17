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

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.favoriteHolder> {

    public FavoriteAdapter(Context context)
    {
        this.mContext = context;
    }
    private List<ChargingStation> favoriteList = new ArrayList<>();
    private final Context mContext;

    @NonNull
    @Override
    public favoriteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new favoriteHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull favoriteHolder holder, int position) {
        holder.myCS = favoriteList.get(position);
        String name = holder.myCS.getStrasse() + ' ' + holder.myCS.getHausnummer();
        holder.tvFavoriteAddress.setText(name);
        String distance;
        if(ContainerAndGlobal.getCurrentLocation() != null)
            distance = ContainerAndGlobal.df.format(ContainerAndGlobal.calculateLength(holder.myCS.getLocation(), ContainerAndGlobal.getCurrentLocation())) + " KM";
        else
            distance = "Unknown distance";
        holder.tvDistance.setText(distance);

        holder.btnUnfavorite.setOnClickListener(v -> {
            holder.btnUnfavorite.setClickable(false);
            int index = ContainerAndGlobal.indexSearchFavorites(holder.myCS.getLocation());
            ContainerAndGlobal.getFavoriteList().remove(index);
            ContainerAndGlobal.addChargingStation(holder.myCS.getMyIndex(), holder.myCS);
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
        private ChargingStation myCS;

        public favoriteHolder(View itemView)
        {
            super(itemView);
            tvFavoriteAddress = itemView.findViewById(R.id.textViewFavoriteAddress);
            tvDistance = itemView.findViewById(R.id.textViewDistance);
            btnUnfavorite = itemView.findViewById(R.id.buttonUnfavorite);

            itemView.setOnClickListener(v -> {
                ContainerAndGlobal.setZoomToThisChargingStation(myCS);
                ((MainActivity)mContext).switchFragment(0);
            });
        }
    }
}
