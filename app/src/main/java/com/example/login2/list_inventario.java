package com.example.login2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class list_inventario extends AppCompatActivity {

    //Search field
    EditText txtSearchDrugs;

    private RecyclerView recycleMeds;
    private ArrayList<Drugs> drugs;
    private MyAdapterRecycler drugAdapter;

    Button button2;

    //DataBase
    private FirebaseAuth mAuth;
    FirebaseUser user;

    FirebaseDatabase database;
    DatabaseReference myDrugsRef, myPlansRef;

    String currentUserId;

    private int countEqualsIdPlans;
    private int countDrugsExists;

    // request code
    private int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_inventario);

        txtSearchDrugs = findViewById(R.id.txtSearchDrug);
        txtSearchDrugs.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filterDrugs(s.toString());
            }
        });

        //Bottom Navigation Bar
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        button2 = findViewById(R.id.button2);
        recycleMeds = findViewById(R.id.recycleMeds);
        recycleMeds.setHasFixedSize(true);
        recycleMeds.setLayoutManager(new LinearLayoutManager(this));

        database = FirebaseDatabase.getInstance();
        myDrugsRef = database.getReference("drugs");
        myPlansRef = database.getReference("plans");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        user = mAuth.getCurrentUser();

        drugs = new ArrayList<Drugs>();

        // Read from the database
        myDrugsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                for (DataSnapshot drugSnapShot : dataSnapshot.getChildren()) {
                    Drugs drug = drugSnapShot.getValue(Drugs.class);

                    // Add drug to arraylist
                    if (currentUserId.equals(drug.userId)) {
                        drugs.add(
                                new Drugs(drug.drugId, drug.userId, drug.drugName, drug.drugNum, drug.drugDate, drug.personsUsing));

                        drugAdapter = new MyAdapterRecycler(drugs);
                        recycleMeds.setAdapter(drugAdapter);
                    }
                }

                // When a user try to delete a drug
                drugAdapter.setOnItemClickListener(new MyAdapterRecycler.OnItemClickListener(){

                    //Delete drug
                    @Override
                    public void onDeleteClick(final int position) {

                        final Drugs drugPosition = drugs.get(position);
                        countDrugsExists = 0;

                        // Search in the data base for all the drugs
                        myDrugsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (final DataSnapshot drugSnapShot : dataSnapshot.getChildren()) {
                                    final Drugs drugToDelete = drugSnapShot.getValue(Drugs.class);

                                    // Compare the drugs with the one that the user clicked
                                    if(currentUserId.equals(drugToDelete.userId)){
                                        countDrugsExists++;
                                    }

                                    if(drugToDelete.drugId.equals(drugPosition.drugId)) {

                                        // Check if there's plans using this drug
                                        if(drugToDelete.personsUsing != 0) {
                                            Snackbar.make(findViewById(android.R.id.content), "Existem planos com esse medicamento. Apague-os antes de apagar o medicamento.",Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                        } else {
                                            drugSnapShot.getRef().removeValue()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            removeItem(position);
                                                            Snackbar.make(findViewById(android.R.id.content), "O medicamento foi apagado com sucesso!",Snackbar.LENGTH_LONG).setAction("Action", null).show();

                                                            // If user deleted the last drug, the empty state will be shown
                                                            if(countDrugsExists == 1) {
                                                                Intent goToDrugsEmptyState = new Intent(getApplicationContext(), inventario.class);
                                                                startActivity(goToDrugsEmptyState);
                                                            }
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Snackbar.make(findViewById(android.R.id.content), "Houve um problema e o medicamento não foi apagado! Tente novamente.",Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                                        }
                                                    });
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    // Edit drug
                    @Override
                    public void OnEditClick(int position) {
                        final Drugs drugPosition = drugs.get(position);

                        myDrugsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (final DataSnapshot drugSnapShot : dataSnapshot.getChildren()) {
                                    final Drugs drugToEdit = drugSnapShot.getValue(Drugs.class);

                                    if(drugToEdit.drugId.equals(drugPosition.drugId)){
                                        Intent intent = new Intent(getApplicationContext(), EditDrug.class);
                                        intent.putExtra("drugId", drugToEdit.drugId);
                                        intent.putExtra("userId", drugToEdit.userId);
                                        intent.putExtra("drugName", drugToEdit.drugName);
                                        intent.putExtra("drugNum", drugToEdit.drugNum);
                                        intent.putExtra("drugDate", drugToEdit.drugDate);
                                        intent.putExtra("personsUsing", drugToEdit.personsUsing);
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

        countEqualsIdPlans = 0;

        //Check if user has plans in database
        myPlansRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot planSnapShot : dataSnapshot.getChildren()) {
                    Plans plan = planSnapShot.getValue(Plans.class);

                    if(user.getUid().equals(plan.userId)) {
                        countEqualsIdPlans++;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Log", "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            final String id = data.getExtras().getString("drugId");
            final String user = data.getExtras().getString("userId");
            final String name = data.getExtras().getString("drugName");
            final String lastDrugName = data.getExtras().getString("lastDrugName");
            final int num = data.getExtras().getInt("drugNum");
            final String date = data.getExtras().getString("drugDate");
            final int personsUsing = data.getExtras().getInt("personsUsing");

            // Search for the plans that use the drug that will be edited, and update the name of the drug in the plans
            myPlansRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot planSnapShot : dataSnapshot.getChildren()) {
                        Plans plan = planSnapShot.getValue(Plans.class);

                        if (currentUserId.equals(plan.userId)) {
                            if(lastDrugName.equals(plan.drugName) && !name.equals(plan.drugName)) {
                                updatePlanInfo(plan.userId, plan.planId, plan.planTitle, plan.disease, plan.personName, name, plan.drugNum, plan.drugDate, plan.drugDays);
                            }

                        }
                    }

                    updateDrugInfo(id, user, name, num, date, personsUsing);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            finish();
            startActivity(getIntent());

        }
    }

    private boolean updateDrugInfo(String drugId, String userId, String drugName, int drugNum, String drugDate, int personsUsing) {

        DatabaseReference drugToChange = myDrugsRef.child(drugId);

        Drugs drug = new Drugs(drugId, userId, drugName, drugNum, drugDate, personsUsing);

        drugToChange.setValue(drug);

        return true;
    }

    private boolean updatePlanInfo(String userId, String planId, String planTitle, String disease, String personName, String drugName, int drugNum, String drugDate, String drugDays) {

        DatabaseReference planToChange = myPlansRef.child(planId);

        Plans plan = new Plans(userId, planId, planTitle, disease, personName, drugName, drugNum, drugDate, drugDays);

        planToChange.setValue(plan);

        return true;
    }

    public void removeItem(int position){
        drugs.remove(position);
        drugAdapter.notifyItemRemoved(position);
    }

    // Check what activity should be shown, by user icon clicked
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            switch (menuItem.getItemId()) {
                case R.id.action_drugs:
                    return true;

                case R.id.action_planos:
                    if(countEqualsIdPlans == 0) {
                        Intent intent = new Intent(getApplicationContext(), PlanoEmpty.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(getApplicationContext(), list_planos.class);
                        startActivity(intent);
                    }
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
        Intent intent = new Intent(this, AddMed.class);
        startActivity(intent);
    }

    private void filterDrugs(String text) {
        ArrayList<Drugs> filteredDrugs = new ArrayList<>();

        for(Drugs drug: drugs) {
            if(drug.drugName.toLowerCase().contains(text.toLowerCase())) {
                filteredDrugs.add(drug);
            }
        }

        drugAdapter.filterDrug(filteredDrugs);
    }
}


