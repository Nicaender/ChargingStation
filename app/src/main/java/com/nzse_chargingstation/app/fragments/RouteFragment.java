package com.nzse_chargingstation.app.fragments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nzse_chargingstation.app.R;
import com.nzse_chargingstation.app.classes.ContainerAndGlobal;
import com.nzse_chargingstation.app.classes.FavoriteAdapter;
import com.nzse_chargingstation.app.classes.RouteAdapter;
import com.nzse_chargingstation.app.classes.RoutePlan;

public class RouteFragment extends Fragment {

    private RouteAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_route, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.rvRouteList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(true);

        adapter = new RouteAdapter(requireContext());
        recyclerView.setAdapter(adapter);

        adapter.setRouteList(ContainerAndGlobal.getRoutePlanList());

        Button btnAddRoute = view.findViewById(R.id.buttonAddRoute);

        btnAddRoute.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(R.string.route_plan_name_question);

            // Set up the input
            final EditText input = new EditText(requireContext());
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton(getString(R.string.builder_positive_button), (dialog, which) -> {
                if(!input.getText().toString().isEmpty())
                {
                    ContainerAndGlobal.getRoutePlanList().add(new RoutePlan(input.getText().toString()));
                    ContainerAndGlobal.saveData(3, requireContext());
                    Toast.makeText(requireContext(), getString(R.string.successfully_added), Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton(getString(R.string.builder_negative_button), (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        if(adapter != null)
            adapter.notifyDataSetChanged();
    }
}