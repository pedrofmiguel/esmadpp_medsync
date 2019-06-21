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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.UUID;

import static com.example.login2.AddMed.drugDate;

public class AddPlan extends AppCompatActivity {

    EditText txtTitle, txtNamePerson, txtDose, txtDisease;
    Spinner spinnerDrugs;
    ArrayList<String> drugList = new ArrayList<String>();

    private FirebaseDatabase database;
    private DatabaseReference myDrugsRef;
    private DatabaseReference myPlansRef;

    String currentUserId;

    // Database
    private FirebaseAuth mAuth;
    FirebaseUser user;

    private int countEqualsIdDrugs;
    private int countPlans;

    Map<String, Drugs> mapa = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plan);

        //Bottom Navigation Bar
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation_add_plan);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        txtTitle = findViewById(R.id.txtTitle);
        txtNamePerson = findViewById(R.id.txtNamePerson);
        txtDisease = findViewById(R.id.txtDisease);
        txtDose= findViewById(R.id.txtDose);
        spinnerDrugs = (Spinner) findViewById(R.id.spinnerDrugs);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Firebase
        database = FirebaseDatabase.getInstance();
        myDrugsRef = database.getReference("drugs");
        myPlansRef = FirebaseDatabase.getInstance().getReference("plans");

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();


        countEqualsIdDrugs = 0;
        countPlans = 0;

        myDrugsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot drugSnapShot : dataSnapshot.getChildren()) {
                    Drugs drug = drugSnapShot.getValue(Drugs.class);

                    if(currentUserId.equals(drug.userId)){
                        countEqualsIdDrugs++;
                        drugList.add(drug.drugName);

                        mapa.put(drug.drugName, drug);
                    }
                }

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(AddPlan.this, android.R.layout.simple_dropdown_item_1line, drugList);

                spinnerDrugs.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myPlansRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot planSnapShot : dataSnapshot.getChildren()) {
                    Plans plan = planSnapShot.getValue(Plans.class);

                    if(user.getUid().equals(plan.userId)) {
                        countPlans++;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

    private void addPlanToListPlans() {
        String title = txtTitle.getText().toString();
        String namePerson = txtNamePerson.getText().toString();
        String disease = txtDisease.getText().toString();
        int dose = Integer.parseInt(txtDose.getText().toString());
        final String drugNameSelected = spinnerDrugs.getSelectedItem().toString();
        String uniqueID = UUID.randomUUID().toString();


        Plans plan = new Plans(currentUserId, uniqueID, title, disease, namePerson, drugNameSelected, dose, "Data e hora da toma", "Dias que faltam");
        myPlansRef.child(uniqueID).setValue(plan)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Drugs d = mapa.get(drugNameSelected);

                        if(drugNameSelected.equals(d.drugName)){
                            int addOnePersonUsing = d.personsUsing + 1;

                            updateDrug(d.drugId, d.userId, d.drugName, d.drugNum, d.drugDate, addOnePersonUsing);
                        }

                        Snackbar.make(findViewById(android.R.id.content), "O plano foi criado com sucesso!",Snackbar.LENGTH_LONG).setAction("Action", null).show();

                        Intent intent = new Intent(getApplicationContext(), list_planos.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(findViewById(android.R.id.content), "Ocorreu um erro na criação do plano!",Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                });

    }

    public static Date drugDate;

    public void addPlan(View view) {
        String title = txtTitle.getText().toString();
        String namePerson = txtNamePerson.getText().toString();
        String disease = txtDisease.getText().toString();
        int dose = Integer.parseInt(txtDose.getText().toString());
        final String drugNameSelected = spinnerDrugs.getSelectedItem().toString();
        String uniqueID = UUID.randomUUID().toString();

        // Convert string to date
        final SimpleDateFormat dateFormater = new SimpleDateFormat("dd/MM/yyyy");

        // Get current Date
        final Date nowDate = Calendar.getInstance().getTime();

        if(!title.equals("") && !namePerson.equals("") && !disease.equals("") && !txtDose.getText().toString().equals("") && !drugNameSelected.equals("")) {
            if(dose > 0) {
                myDrugsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot drugSnapShot : dataSnapshot.getChildren()) {
                            Drugs drug = drugSnapShot.getValue(Drugs.class);
                            if(currentUserId.equals(drug.userId)){
                                if(drugNameSelected.equals(drug.drugName)) {
                                    try {
                                        drugDate = dateFormater.parse(drug.drugDate);

                                        if(drugDate.before(nowDate)){
                                            buildDialog(AddPlan.this).show();
                                        } else {
                                            addPlanToListPlans();
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
                Snackbar.make(view, "A dose tem de ser maior que zero!",Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        } else {
            Snackbar.make(view, "Existem campos por preencher!",Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    // When need to update drug, because when a plan is added, it has a drug associated, so when need to add one element to "personsUsing"
    public boolean updateDrug(String drugId, String userId, String drugName, int drugNum, String drugDate, int personsUsing) {

        DatabaseReference drugToChange = myDrugsRef.child(drugId);

        Drugs drug = new Drugs(drugId, userId, drugName, drugNum, drugDate, personsUsing);

        drugToChange.setValue(drug);

        return true;
    }

    public AlertDialog.Builder buildDialog(Context c) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Medicamento fora de prazo!");
        builder.setMessage("O prazo de validade do medicamento selecionado expirou! Tem a certeza que o pretende adicionar ao plano?");

        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addPlanToListPlans();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder;
    }

    public void getBackAddPlan(View view) {
        if(countPlans == 0) {
            Intent intent = new Intent(getApplicationContext(), PlanoEmpty.class);
            startActivity(intent);
        } else {
            Intent intent =  new Intent(getApplicationContext(), list_planos.class);
            startActivity(intent);
        }
    }


    // Function to open calendar and create alerts for plans
    public void openCalendar(View view){
        final String drugNameSelected = spinnerDrugs.getSelectedItem().toString();
        int dose = Integer.parseInt(txtDose.getText().toString());
        String title = txtTitle.getText().toString();
        String namePerson = txtNamePerson.getText().toString();

        Calendar cal = Calendar.getInstance();
        Intent intent2 = new Intent(Intent.ACTION_EDIT);
        intent2.setType("vnd.android.cursor.item/event");
        intent2.putExtra("beginTime", cal.getTimeInMillis());
        intent2.putExtra("allDay", false);
        intent2.putExtra("rrule", "FREQ=DAILY");
        intent2.putExtra("endTime", cal.getTimeInMillis()+60601000);
        intent2.putExtra("description", namePerson + " está na hora de tomar "+ dose+ " comprimido(s) de " + drugNameSelected);
        intent2.putExtra("title", title);
        startActivity(intent2);

    }

}
