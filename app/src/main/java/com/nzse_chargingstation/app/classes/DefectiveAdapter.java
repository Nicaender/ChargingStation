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
import com.nzse_chargingstation.app.activities.TechnicianActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * A defective adapter that implements recycler view to show the list of defective charging station.
 */
public class DefectiveAdapter extends RecyclerView.Adapter<DefectiveAdapter.DefectiveHolder> {

    public DefectiveAdapter(Context context) {
        this.mContext = context;
    }
    private List<Defective> defectiveList = new ArrayList<>();
    private final Context mContext;

    @NonNull
    @Override
    public DefectiveHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_defective, parent, false);
        return new DefectiveHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DefectiveHolder holder, int position) {
        holder.myDefective = defectiveList.get(holder.getAdapterPosition());
        String distance;
        String name = holder.myDefective.getDefectiveCs().getStrasse() + ' ' + holder.myDefective.getDefectiveCs().getHausnummer();
        holder.tvDefectiveAddress.setText(name);
        String city = holder.myDefective.getDefectiveCs().getPostleitzahl() + ", " + holder.myDefective.getDefectiveCs().getOrt();
        holder.tvDefectiveCity.setText(city);
        if(ContainerAndGlobal.getCurrentLocation() != null)
            distance = ContainerAndGlobal.df.format(ContainerAndGlobal.calculateLength(holder.myDefective.getDefectiveCs().getPosition(), ContainerAndGlobal.getCurrentLocation())) + " KM";
        else
            distance = mContext.getResources().getString(R.string.distance) + " : " + mContext.getResources().getString(R.string.unknown);
        holder.tvDefectiveReason.setText(holder.myDefective.getReason());
        holder.tvDistance.setText(distance);
        if(!holder.myDefective.isMarked())
            holder.btnMarkToRepair.setText(mContext.getResources().getString(R.string.mark));
        else
            holder.btnMarkToRepair.setText(mContext.getResources().getString(R.string.finish));

        holder.btnMarkToRepair.setOnClickListener(v -> {
            if(!holder.myDefective.isMarked()) {
                holder.myDefective.setMarked(true);
                notifyItemChanged(holder.getAdapterPosition());
                Toast.makeText(v.getContext(), mContext.getResources().getString(R.string.successfully_marked), Toast.LENGTH_SHORT).show();
            }
            else {
                ContainerAndGlobal.removeDefective(holder.myDefective);
                ContainerAndGlobal.saveData(2, v.getContext());
                notifyItemRemoved(holder.getAdapterPosition());
                Toast.makeText(v.getContext(), mContext.getResources().getString(R.string.successfully_repaired), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return defectiveList.size();
    }

    public void setDefectiveList(List<Defective> defectives) {
        this.defectiveList = defectives;
        notifyItemRangeChanged(0, defectives.size());
    }

    protected class DefectiveHolder extends RecyclerView.ViewHolder {
        private final TextView tvDefectiveAddress, tvDefectiveReason, tvDefectiveCity, tvDistance;
        private final Button btnMarkToRepair;
        private Defective myDefective;

        public DefectiveHolder(@NonNull View itemView) {
            super(itemView);
            tvDefectiveAddress = itemView.findViewById(R.id.textViewDefectiveAddress);
            tvDefectiveReason = itemView.findViewById(R.id.textViewDefectiveReason);
            tvDefectiveCity = itemView.findViewById(R.id.textViewDefectiveCity);
            tvDistance = itemView.findViewById(R.id.textViewDistance);
            btnMarkToRepair = itemView.findViewById(R.id.buttonRepair);

            itemView.setOnClickListener(v -> {
                ContainerAndGlobal.setZoomToThisChargingStationOnDefective(myDefective.getDefectiveCs());
                ((TechnicianActivity)mContext).finish();
            });
        }
    }

}
