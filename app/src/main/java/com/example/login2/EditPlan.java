package com.example.login2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditPlan extends AppCompatActivity {

    EditText txtTitle, txtDisease, txtNamePerson, txtEditDose;
    Spinner spinnerDrugs;
    ArrayList<String> drugList = new ArrayList<String>();

    // Database
    private FirebaseAuth mAuth;
    FirebaseUser user;
    private DatabaseReference myPlansRef, myDrugsRef;

    // request code
    private int REQUEST_CODE = 1;

    String userId;
    String planId;
    String planTitle;
    String disease;
    String personName;
    String drugName;
    int drugNum;
    String drugDate;
    String drugDays;

    private int countEqualsIdDrugs;

    Map<String, Drugs> mapa = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_plan);

        txtTitle = findViewById(R.id.txtTitle);
        txtNamePerson = findViewById(R.id.txtNamePerson);
        txtDisease = findViewById(R.id.txtDisease);
        txtEditDose = findViewById(R.id.txtEditDose);
        spinnerDrugs = findViewById(R.id.spinnerDrugs);

        //Bottom Navigation Bar
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation_edit_plan);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        myPlansRef = FirebaseDatabase.getInstance().getReference("plans");
        myDrugsRef = FirebaseDatabase.getInstance().getReference("drugs");

        countEqualsIdDrugs = 0;

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if(extras != null) {
            userId = extras.getString("userId");
            planId = extras.getString("planId");
            planTitle = extras.getString("planTitle");
            disease = extras.getString("disease");
            personName = extras.getString("personName");
            drugName = extras.getString("drugName");
            drugNum = extras.getInt("drugNum");
            drugDate = extras.getString("drugDate");
            drugDays = extras.getString("drugDays");

            String doseNum = String.valueOf(drugNum);

            // Fill inputs with drug's info
            txtTitle.setText(planTitle);
            txtNamePerson.setText(personName);
            txtDisease.setText(disease);
            txtEditDose.setText(doseNum);


            myDrugsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for(DataSnapshot drugSnapShot : dataSnapshot.getChildren()) {
                        Drugs drug = drugSnapShot.getValue(Drugs.class);

                        if(user.getUid().equals(drug.userId)){
                            countEqualsIdDrugs++;
                            drugList.add(drug.drugName);

                            mapa.put(drug.drugName, drug);
                        }
                    }

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(EditPlan.this, android.R.layout.simple_dropdown_item_1line, drugList);

                    spinnerDrugs.setAdapter(arrayAdapter);

                    spinnerDrugs.setSelection(arrayAdapter.getPosition(drugName));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void editPlanConfirmed() {
        String title = txtTitle.getText().toString();
        String disease = txtDisease.getText().toString();
        String namePerson = txtNamePerson.getText().toString();
        int dose = Integer.parseInt(txtEditDose.getText().toString());
        String drugNameSelected = spinnerDrugs.getSelectedItem().toString();

        final Intent intentEditPlan = new Intent();

        intentEditPlan.putExtra("userId", userId);
        intentEditPlan.putExtra("planId", planId);
        intentEditPlan.putExtra("planTitle", title);
        intentEditPlan.putExtra("disease", disease);
        intentEditPlan.putExtra("personName", namePerson);
        intentEditPlan.putExtra("drugName", drugNameSelected);
        intentEditPlan.putExtra("lastDrugName", drugName);
        intentEditPlan.putExtra("drugNum", dose);
        intentEditPlan.putExtra("drugDate", drugDate);
        intentEditPlan.putExtra("drugDays", drugDays);
        setResult(RESULT_OK, intentEditPlan);
        finish();
    }

    public static Date selectedDrugDate;

    public void editPlan(View view) {
        String title = txtTitle.getText().toString();
        String disease = txtDisease.getText().toString();
        String namePerson = txtNamePerson.getText().toString();
        int dose = Integer.parseInt(txtEditDose.getText().toString());
        final String drugNameSelected = spinnerDrugs.getSelectedItem().toString();

        // Convert string to date
        final SimpleDateFormat dateFormater = new SimpleDateFormat("dd/MM/yyyy");

        // Get current Date
        final Date nowDate = Calendar.getInstance().getTime();


        if(!title.equals("") && !disease.equals("") && !namePerson.equals("") && !txtEditDose.getText().toString().equals("") && !drugNameSelected.equals("")){
            myDrugsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot drugSnapShot : dataSnapshot.getChildren()) {
                        Drugs drug = drugSnapShot.getValue(Drugs.class);

                        if(user.getUid().equals(drug.userId)){
                            if(drugNameSelected.equals(drug.drugName)) {
                                try {
                                    selectedDrugDate = dateFormater.parse(drug.drugDate);

                                    if(selectedDrugDate.before(nowDate)){
                                        buildDialog(EditPlan.this).show();
                                    } else {
                                        editPlanConfirmed();
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            Snackbar.make(view, "Existem campos por preencher!",Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    public AlertDialog.Builder buildDialog(Context c) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Medicamento fora de prazo!");
        builder.setMessage("O prazo de validade do medicamento selecionado expirou! Tem a certeza que o pretende adicionar ao plano?");

        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editPlanConfirmed();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder;
    }

    // Check what activity should be shown, by user icon clicked
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            switch (menuItem.getItemId()) {
                case R.id.action_drugs:
                    if(countEqualsIdDrugs == 0) {
                        Intent intent = new Intent(getApplicationContext(), inventario.class);
                        startActivity(intent);
                    } else {
                        Intent intent =  new Intent(getApplicationContext(), list_inventario.class);
                        startActivity(intent);
                    }
                    return true;

                case R.id.action_planos:
                    return true;

                case R.id.action_perfil:
                    Intent intent = new Intent(getApplicationContext(), Profile.class);
                    startActivity(intent);
                    return true;
            }
            return false;
        }
    };

    public void getBackFromEdit(View view) {
        Intent goToListPlans = new Intent(EditPlan.this, list_planos.class);
        startActivity(goToListPlans);
    }
}
