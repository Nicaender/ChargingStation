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
    private List<Defective> defective_list = new ArrayList<>();

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
        Defective current_defective = defective_list.get(position);
        holder.tv_defective_address.setText(current_defective.getDefective_cs().getAddress());
        holder.tv_defective_reason.setText(current_defective.getReason());
        String tmp = ContainerAndGlobal.df.format(ContainerAndGlobal.calculateLength(current_defective.getDefective_cs().getLocation(), ContainerAndGlobal.getCurrent_location())) + " KM";
        holder.tv_distance.setText(tmp);
        if(!current_defective.isMarked())
            holder.btn_mark_to_repair.setText("Mark");
        else
            holder.btn_mark_to_repair.setText("Marked");

        holder.btn_mark_to_repair.setOnClickListener(v -> {
            if(!current_defective.isMarked())
            {
                current_defective.setMarked(true);
                notifyItemChanged(holder.getAdapterPosition());
                Toast.makeText(v.getContext(), "Successfully marked", Toast.LENGTH_LONG).show();
            }
            else
            {
                ContainerAndGlobal.add_or_remove_defective(current_defective, false);
                notifyItemRemoved(holder.getAdapterPosition());
                Toast.makeText(v.getContext(), "Successfully repaired", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return defective_list.size();
    }

    public void setDefective_list(List<Defective> defectives)
    {
        this.defective_list = defectives;
        notifyItemRangeChanged(0, defectives.size());
    }

    class defectiveHolder extends RecyclerView.ViewHolder {
        private TextView tv_defective_address, tv_defective_reason, tv_distance;
        private Button btn_mark_to_repair;

        public defectiveHolder(@NonNull View itemView) {
            super(itemView);
            tv_defective_address = itemView.findViewById(R.id.textview_defective_address);
            tv_defective_reason = itemView.findViewById(R.id.textview_defective_reason);
            tv_distance = itemView.findViewById(R.id.textview_distance);
            btn_mark_to_repair = itemView.findViewById(R.id.button_mark_to_repair);
        }
    }

}
