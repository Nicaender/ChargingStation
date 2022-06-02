package com.nzse_chargingstation.app.classes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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

    @Override
    public void onBindViewHolder(@NonNull defectiveHolder holder, int position) {
        Defective current_defective = defective_list.get(position);
        holder.tv_defective_address.setText(current_defective.getDefective_cs().getAddress());
        holder.tv_defective_reason.setText(current_defective.getReason());
        holder.btn_mark_to_repair.setText(current_defective.getTechnician());
    }

    @Override
    public int getItemCount() {
        return defective_list.size();
    }

    public void setDefective_list(List<Defective> defectives)
    {
        this.defective_list = defectives;
        notifyDataSetChanged();
    }

    class defectiveHolder extends RecyclerView.ViewHolder {
        private TextView tv_defective_address, tv_defective_reason;
        private Button btn_mark_to_repair;

        public defectiveHolder(@NonNull View itemView) {
            super(itemView);
            tv_defective_address = itemView.findViewById(R.id.textview_defective_address);
            tv_defective_reason = itemView.findViewById(R.id.textview_defective_reason);
            btn_mark_to_repair = itemView.findViewById(R.id.button_mark_to_repair);
        }
    }

}
