package com.example.login2;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MyAdapterRecycler extends RecyclerView.Adapter<MyAdapterRecycler.ViewHolder> {

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
        void OnEditClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) { mListener = listener; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView titleMed, txtCompNumber, txtPrazo, txtConsumo;
        ImageView imageDeleteDrug, imageEditDrug;

        public ViewHolder(View itemView, final MyAdapterRecycler.OnItemClickListener listener) {
            super(itemView);
            titleMed = itemView.findViewById(R.id.titleMed);
            txtCompNumber = itemView.findViewById(R.id.txtCompNumber);
            txtPrazo = itemView.findViewById(R.id.txtPrazo);
            txtConsumo = itemView.findViewById(R.id.txtConsumo);
            imageDeleteDrug = itemView.findViewById(R.id.imageDeleteDrug);
            imageEditDrug = itemView.findViewById(R.id.imageEditDrug);

            // Delete drug
            imageDeleteDrug.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });

            // Edit drug
            imageEditDrug.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) {
                            listener.OnEditClick(position);
                        }
                    }
                }
            });
        }
    }

    private ArrayList<Drugs> myDrugsList;

    public MyAdapterRecycler(ArrayList<Drugs> myDrugsList) {
        this.myDrugsList = myDrugsList;
    }

    @NonNull
    @Override
    public MyAdapterRecycler.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.meds_list, viewGroup, false);
        return new ViewHolder(itemView, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapterRecycler.ViewHolder viewHolder, int i) {
        Drugs item = myDrugsList.get(i);
        viewHolder.titleMed.setText(item.getDrugName());
        viewHolder.txtCompNumber.setText(String.valueOf(item.getDrugNum()));
        viewHolder.txtPrazo.setText(item.getDrugDate());
        viewHolder.txtConsumo.setText(String.valueOf(item.getPersonsUsing()));
    }

    @Override
    public int getItemCount() {
        return myDrugsList.size();
    }

    public void filterDrug(ArrayList<Drugs> filteredDrugs) {
        myDrugsList = filteredDrugs;
        notifyDataSetChanged();
    }
}
