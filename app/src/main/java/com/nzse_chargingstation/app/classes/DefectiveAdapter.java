package com.nzse_chargingstation.app.classes;

import android.annotation.SuppressLint;
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

public class DefectiveAdapter extends RecyclerView.Adapter<DefectiveAdapter.defectiveHolder> {
    private List<Defective> defectiveList = new ArrayList<>();

    @NonNull
    @Override
    public defectiveHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_defective, parent, false);
        return new defectiveHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull defectiveHolder holder, int position) {
        Defective currentDefective = defectiveList.get(position);
        String distance;
        holder.tvDefectiveAddress.setText(currentDefective.getDefectiveCs().getStrasse() + ' ' + currentDefective.getDefectiveCs().getHausnummer());
        String city = currentDefective.getDefectiveCs().getPostleitzahl() + ", " + currentDefective.getDefectiveCs().getOrt();
        holder.tvDefectiveCity.setText(city);
        if(ContainerAndGlobal.getCurrentLocation() != null)
            distance = ContainerAndGlobal.df.format(ContainerAndGlobal.calculateLength(currentDefective.getDefectiveCs().getLocation(), ContainerAndGlobal.getCurrentLocation())) + " KM";
        else
            distance = "Unknown distance";
        holder.tvDefectiveReason.setText(currentDefective.getReason());
        holder.tvDistance.setText(distance);
        if(!currentDefective.isMarked())
            holder.btnMarkToRepair.setText("Mark");
        else
            holder.btnMarkToRepair.setText("Marked");

        holder.btnMarkToRepair.setOnClickListener(v -> {
            if(!currentDefective.isMarked())
            {
                currentDefective.setMarked(true);
                notifyItemChanged(holder.getAdapterPosition());
                Toast.makeText(v.getContext(), "Successfully marked", Toast.LENGTH_LONG).show();
            }
            else
            {
                ContainerAndGlobal.removeDefective(currentDefective);
                ContainerAndGlobal.saveData(false, v.getContext());
                notifyItemRemoved(holder.getAdapterPosition());
                Toast.makeText(v.getContext(), "Successfully repaired", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return defectiveList.size();
    }

    public void setDefectiveList(List<Defective> defectives)
    {
        this.defectiveList = defectives;
        notifyItemRangeChanged(0, defectives.size());
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    class defectiveHolder extends RecyclerView.ViewHolder {
        private final TextView tvDefectiveAddress;
        private final TextView tvDefectiveReason;
        private final TextView tvDefectiveCity;
        private final TextView tvDistance;
        private final Button btnMarkToRepair;

        public defectiveHolder(@NonNull View itemView) {
            super(itemView);
            tvDefectiveAddress = itemView.findViewById(R.id.textViewDefectiveAddress);
            tvDefectiveReason = itemView.findViewById(R.id.textViewDefectiveReason);
            tvDefectiveCity = itemView.findViewById(R.id.textViewDefectiveCity);
            tvDistance = itemView.findViewById(R.id.textViewDistance);
            btnMarkToRepair = itemView.findViewById(R.id.buttonMarkToRepair);
        }
    }

}
