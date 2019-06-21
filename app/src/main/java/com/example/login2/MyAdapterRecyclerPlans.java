package com.example.login2;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyAdapterRecyclerPlans extends RecyclerView.Adapter<MyAdapterRecyclerPlans.ViewHolder> {

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
        void onEditClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) { mListener = listener; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView titlePlan, txtPessoaNome, txtNomezito, txtComprimidoNum, txtTimes, txtDaysLeft, txtDoenca1;
        ImageView deleteImage;
        ImageView editPlan;

        public ViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            titlePlan = itemView.findViewById(R.id.titlePlan);
            txtPessoaNome = itemView.findViewById(R.id.txtPessoaNome);
            txtNomezito = itemView.findViewById(R.id.txtNomezito);
            txtComprimidoNum = itemView.findViewById(R.id.txtComprimidoNum);
            txtTimes = itemView.findViewById(R.id.txtTimes);
            txtDaysLeft = itemView.findViewById(R.id.txtDaysLeft);
            txtDoenca1 = itemView.findViewById(R.id.txtDoenca1);
            deleteImage = itemView.findViewById(R.id.deleteImage);
            editPlan = itemView.findViewById(R.id.editPlan);

            // Delete plan
            deleteImage.setOnClickListener(new View.OnClickListener(){
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

            //Edit plan
            editPlan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) {
                            listener.onEditClick(position);
                        }
                    }
                }
            });
        }
    }

    private ArrayList<Plans> myPlansList;

    public MyAdapterRecyclerPlans(ArrayList<Plans> myPlansList) {
        this.myPlansList = myPlansList;
    }

    @NonNull
    @Override
    public MyAdapterRecyclerPlans.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plans_list, viewGroup, false);
        //MyAdapterRecyclerPlans mrp = new MyAdapterRecyclerPlans(itemView, mListener);
        return new ViewHolder(itemView, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapterRecyclerPlans.ViewHolder viewHolder, int i) {
        Plans item = myPlansList.get(i);
        String dose = String.valueOf(item.drugNum);

        viewHolder.titlePlan.setText(item.planTitle);
        viewHolder.txtPessoaNome.setText(item.personName);
        viewHolder.txtNomezito.setText(item.drugName);
        viewHolder.txtComprimidoNum.setText(dose);
//        viewHolder.txtTimes.setText(item.drugDate);
//        viewHolder.txtDaysLeft.setText(item.drugDays);
        viewHolder.txtDoenca1.setText(item.disease);
    }

    @Override
    public int getItemCount() {return myPlansList.size();}

    public void filterPlans(ArrayList<Plans> filteredPlans) {
        myPlansList = filteredPlans;
        notifyDataSetChanged();
    }
}
