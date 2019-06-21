package com.example.login2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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

import java.util.ArrayList;

import static com.example.login2.DrugDateService.CHANNEL_ID;

public class list_planos extends AppCompatActivity{

    //Search field
    EditText txtSearchPlan;

    //RecyclerView
    private RecyclerView recycleMeds;
    private ArrayList<Plans> plans;
    private MyAdapterRecyclerPlans planAdapter;

    //DataBase
    private FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference myDrugsRef, myPlansRef;

    // Count
    private int countEqualsIdDrugs;
    private int countPlansLeft;

    // request code
    private int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_planos);

        // Create channel for notifications
        createNotificationChannel();

        txtSearchPlan = findViewById(R.id.txtSearchPlan);
        txtSearchPlan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filterPlans(s.toString());
            }
        });

        // Bottom Navigation Bar
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        // RecyclerView
        recycleMeds = findViewById(R.id.recyclePlans);
        recycleMeds.setHasFixedSize(true);
        recycleMeds.setLayoutManager(new LinearLayoutManager(this));

        // Database
        database = FirebaseDatabase.getInstance();
        myDrugsRef = database.getReference("drugs");
        myPlansRef = database.getReference("plans");
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        countEqualsIdDrugs = 0;

        plans = new ArrayList<Plans>();

        // Read from the database
        myPlansRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot planSnapShot : dataSnapshot.getChildren()) {
                    Plans plan = planSnapShot.getValue(Plans.class);

                    // Add plan to ArrayList
                    if (user.getUid().equals(plan.userId)) {
                        plans.add(
                                new Plans(plan.userId, plan.planId, plan.planTitle, plan.disease, plan.personName, plan.drugName, plan.drugNum, plan.drugDate, plan.drugDays));

                        planAdapter = new MyAdapterRecyclerPlans(plans);
                        recycleMeds.setAdapter(planAdapter);
                    }
                }

                planAdapter.setOnItemClickListener(new MyAdapterRecyclerPlans.OnItemClickListener() {
                    // Delete plan
                    @Override
                    public void onDeleteClick(final int position) {
                        final Plans planPosition = plans.get(position);
                        countPlansLeft = 0;

                        // Serach for plans in database
                        myPlansRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot planSnapShot : dataSnapshot.getChildren()) {
                                    final Plans planToDelete = planSnapShot.getValue(Plans.class);

                                    // Count the number of plans that the user has
                                    if (user.getUid().equals(planToDelete.userId)){
                                        countPlansLeft++;
                                    }

                                    // If the searched plan is the same that the user clicked
                                    if(planToDelete.planId.equals(planPosition.planId)) {
                                        planSnapShot.getRef().removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // Remove item from the recycler view
                                                        removeItem(position);

                                                        // Check if the plan deleted is the last one, and if it is, the activity that will be shown is the empty state
                                                        myDrugsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                for(DataSnapshot drugSnapShot : dataSnapshot.getChildren()) {
                                                                    final Drugs drug = drugSnapShot.getValue(Drugs.class);

                                                                    if(user.getUid().equals(drug.userId)) {
                                                                        if(planToDelete.drugName.equals(drug.drugName)) {
                                                                            final int personsUsingThatDrug = drug.personsUsing - 1;
                                                                            updateDrug(drug.drugId, drug.userId, drug.drugName, drug.drugNum, drug.drugDate, personsUsingThatDrug);
                                                                            if(countPlansLeft == 1){
                                                                                Intent goToPlansEmptyState = new Intent(getApplicationContext(), PlanoEmpty.class);
                                                                                startActivity(goToPlansEmptyState);
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                Toast.makeText(list_planos.this, "Houve um problema na sua ação! Tente novamente.", Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(list_planos.this, "Houve um problema e o plano não foi apagado! Tente novamente.", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    // Edit plan
                    @Override
                    public void onEditClick(int position) {
                        final Plans planPosition = plans.get(position);

                        myPlansRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (final DataSnapshot planSnapShot : dataSnapshot.getChildren()) {
                                    final Plans planToEdit = planSnapShot.getValue(Plans.class);

                                    if(planToEdit.planId.equals(planPosition.planId)){
                                        Intent intent = new Intent(getApplicationContext(), EditPlan.class);
                                        intent.putExtra("userId", planToEdit.userId);
                                        intent.putExtra("planId", planToEdit.planId);
                                        intent.putExtra("planTitle", planToEdit.planTitle);
                                        intent.putExtra("disease", planToEdit.disease);
                                        intent.putExtra("personName", planToEdit.personName);
                                        intent.putExtra("drugName", planToEdit.drugName);
                                        intent.putExtra("drugNum", planToEdit.drugNum);
                                        intent.putExtra("drugDate", planToEdit.drugDate);
                                        intent.putExtra("drugDays", planToEdit.drugDays);
                                        startActivityForResult(intent, REQUEST_CODE);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Log", "Failed to read value.", error.toException());
            }
        });

        // Check for drugs in database
        myDrugsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot drugSnapShot : dataSnapshot.getChildren()) {
                    Drugs drug = drugSnapShot.getValue(Drugs.class);

                    if(user.getUid().equals(drug.userId)) {
                        countEqualsIdDrugs++;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Failed to read value
                Log.w("Log", "Failed to read value.", databaseError.toException());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start service that is responsible to inform user if any drug's date is expired
        Intent intent = new Intent(this, DrugDateService.class);
        startService(intent);
    }

    public void removeItem(int position){
        plans.remove(position);
        planAdapter.notifyItemRemoved(position);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            final String userId = data.getExtras().getString("userId");
            final String planId = data.getExtras().getString("planId");
            final String planTitle = data.getExtras().getString("planTitle");
            final String disease = data.getExtras().getString("disease");
            final String personName = data.getExtras().getString("personName");
            final String drugName = data.getExtras().getString("drugName");
            final String lastDrugName = data.getExtras().getString("lastDrugName");
            final int drugNum = data.getExtras().getInt("drugNum");
            final String drugDate = data.getExtras().getString("drugDate");
            final String drugDays = data.getExtras().getString("drugDays");

            if(!lastDrugName.equals(drugName)) {
                // When some plan is updated, the drug that he's got need to be updated in the parameter personsUsing
                myDrugsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot drugSnapShot : dataSnapshot.getChildren()) {
                            Drugs drug = drugSnapShot.getValue(Drugs.class);

                            if(user.getUid().equals(drug.userId)) {
                                if(lastDrugName.equals(drug.drugName)) {
                                    int pUsingLastDrug = drug.personsUsing - 1;
                                    updateDrug(drug.drugId, drug.userId, drug.drugName, drug.drugNum, drug.drugDate, pUsingLastDrug);
                                }
                                if(drugName.equals(drug.drugName)){
                                    int pUsingNewDrug = drug.personsUsing + 1;
                                    updateDrug(drug.drugId, drug.userId, drug.drugName, drug.drugNum, drug.drugDate, pUsingNewDrug);
                                }
                            }
                        }
                        updatePlan(userId, planId, planTitle, disease, personName, drugName, drugNum, drugDate, drugDays);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } else {
                updatePlan(userId, planId, planTitle, disease, personName, drugName, drugNum, drugDate, drugDays);
            }

            finish();
            startActivity(getIntent());

        }
    }

    private boolean updatePlan(String userId, String planId, String planTitle, String disease, String personName, String drugName, int drugNum, String drugDate, String drugDays) {

        DatabaseReference planToChange = myPlansRef.child(planId);

        Plans plan = new Plans(userId, planId, planTitle, disease, personName, drugName, drugNum, drugDate, drugDays);

        planToChange.setValue(plan);

        return true;
    }

    public boolean updateDrug(String drugId, String userId, String drugName, int drugNum, String drugDate, int personsUsing) {

        DatabaseReference drugToChange = myDrugsRef.child(drugId);

        Drugs drug = new Drugs(drugId, userId, drugName, drugNum, drugDate, personsUsing);

        drugToChange.setValue(drug);

        return true;
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
                        Intent intent = new Intent(getApplicationContext(), list_inventario.class);
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

    public void addDrug(View view) {
        Intent intent = new Intent(this, AddPlan.class);
        startActivity(intent);
    }

    private void filterPlans(String text) {
        ArrayList<Plans> filteredPlans = new ArrayList<>();

        for(Plans plan: plans) {
            if(plan.planTitle.toLowerCase().contains(text.toLowerCase()) || plan.drugName.toLowerCase().contains(text.toLowerCase()) ||
            plan.personName.toLowerCase().contains(text.toLowerCase()) || plan.disease.toLowerCase().contains(text.toLowerCase())) {
                filteredPlans.add(plan);
            }
        }

        planAdapter.filterPlans(filteredPlans);
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "MyChannel";
            String description = "MyChannelDescription";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register channel in system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
