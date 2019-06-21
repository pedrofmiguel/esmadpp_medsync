package com.example.login2;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class AddMed extends AppCompatActivity {

    EditText txtName, txtNum, txtDate;

    private FirebaseDatabase database;
    private DatabaseReference mDatabase;

    String currentUserId;

    // Database
    private FirebaseAuth mAuth;
    FirebaseUser user;

    private int countEqualsIdDrugs;
    private int countEqualsIdPlans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_med);

        //Bottom Navigation Bar
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        txtName = findViewById(R.id.txtName);
        txtNum = findViewById(R.id.txtEditNumber);
        txtDate = findViewById(R.id.txtEditDate);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        countEqualsIdDrugs = 0;
        countEqualsIdPlans = 0;

        mDatabase.child("drugs").addValueEventListener(new ValueEventListener() {
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

        //Check if user has plans in DataBase
        mDatabase.child("plans").addValueEventListener(new ValueEventListener() {
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
                        Intent intent =  new Intent(getApplicationContext(), list_planos.class);
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

    public static Date drugDate;
    private boolean nameExists;

    public void addMed(final View view) throws ParseException {
        final String name = txtName.getText().toString();
        String date = txtDate.getText().toString();
        String num = txtNum.getText().toString();
        int number = 0;
        final String uniqueID = UUID.randomUUID().toString();

        nameExists = false;

        // Convert string to date
        SimpleDateFormat dateFormater = new SimpleDateFormat("dd/MM/yyyy");

        // Get current Date
        Date nowDate = Calendar.getInstance().getTime();

        int count = 0;

        if(!name.equals("") && !num.equals("") && !date.equals("")) {
            drugDate = dateFormater.parse(date);
            number = Integer.parseInt(txtNum.getText().toString());
            if(number > 0) {
                if(drugDate.after(nowDate)) {
                    final Drugs drug = new Drugs(uniqueID, currentUserId, name, number, date, 0);

                    mDatabase.child("drugs").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot drugSnapshot : dataSnapshot.getChildren()) {
                                Drugs getDrugs = drugSnapshot.getValue(Drugs.class);

                                if(user.getUid().equals(getDrugs.userId)) {
                                    if(name.equals(getDrugs.drugName)){
                                        nameExists = true;
                                    }
                                }
                            }

                            if(nameExists == false) {
                                // Guardar as informações na base de dados, associadas ao id do utilizador que está logado
                                mDatabase.child("drugs").child(uniqueID).setValue(drug)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(view, "O medicamento foi adicionado com sucesso!",Snackbar.LENGTH_LONG).setAction("Action", null).show();

                                                // Start activity
                                                startActivity(new Intent(AddMed.this, list_inventario.class));
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Snackbar.make(view, "Ocorreu um erro ao adicionar o medicamento!",Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            }
                                        });
                            } else {
                                Snackbar.make(view, "Já existe um medicamento com esse nome! Escolha outro.",Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });



                } else {
                    Snackbar.make(view, "O prazo de validade do medicamento não pode ser anterior nem igual ao dia de hoje!",Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            } else {
                Snackbar.make(view, "Não pode adicionar medicamentos com número de comprimidos igual ou inferior a zero!",Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        } else {
            Snackbar.make(view, "Existem campos por preencher!",Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    public void getBack(View view) {
        if(countEqualsIdDrugs == 0) {
            Intent intent = new Intent(getApplicationContext(), inventario.class);
            startActivity(intent);
        } else {
            Intent intent =  new Intent(getApplicationContext(), list_inventario.class);
            startActivity(intent);
        }
    }
}
