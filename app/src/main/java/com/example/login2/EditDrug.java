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

public class EditDrug extends AppCompatActivity {

    EditText txtEditName, txtEditNumber, txtEditDate;

    //DataBase
    private FirebaseAuth mAuth;
    FirebaseUser user;
    private DatabaseReference myDrugsRef;
    private DatabaseReference myPlansRef;

    private int countEqualsIdPlans;

    String drugId;
    String userId;
    String drugName;
    int drugNum;
    String drugDate;
    int personsUsing;

    int number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_drug);

        txtEditName = findViewById(R.id.txtEditName);
        txtEditNumber = findViewById(R.id.txtEditNumber);
        txtEditDate = findViewById(R.id.txtEditDate);

        //Bottom Navigation Bar
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation_edit_drug);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        myDrugsRef = FirebaseDatabase.getInstance().getReference("drugs");
        myPlansRef = FirebaseDatabase.getInstance().getReference("plans");


        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if(extras != null) {
            drugId = extras.getString("drugId");
            userId = extras.getString("userId");
            drugName = extras.getString("drugName");
            drugNum = extras.getInt("drugNum");
            drugDate = extras.getString("drugDate");
            personsUsing = extras.getInt("personsUsing");

            // Fill inputs with drug's info
            txtEditName.setText(drugName);
            txtEditNumber.setText(String.valueOf(drugNum));
            txtEditDate.setText(drugDate);
        }

        countEqualsIdPlans = 0;

        //Check if user has plans in DataBase
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

    public static Date drugEditDate;
    private boolean nameExists;

    public void editMed(final View view) throws ParseException {
        final String name = txtEditName.getText().toString();

        String num = txtEditNumber.getText().toString();
        number = 0;
        final String date = txtEditDate.getText().toString();

        nameExists = false;

        // Convert string to date
        SimpleDateFormat dateFormater = new SimpleDateFormat("dd/MM/yyyy");
        // Get current Date
        Date nowDate = Calendar.getInstance().getTime();

        final Intent intentEdit = new Intent();
        if(!name.equals("") && !num.equals("") && !date.equals("")) {
            drugEditDate = dateFormater.parse(date);
            number = Integer.parseInt(txtEditNumber.getText().toString());
            if(number > 0) {
                if(drugEditDate.after(nowDate)) {
                    myDrugsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot drugSnapshot : dataSnapshot.getChildren()) {
                                Drugs getDrugs = drugSnapshot.getValue(Drugs.class);

                                if(user.getUid().equals(getDrugs.userId)) {
                                    if(name.equals(getDrugs.drugName) && !getDrugs.drugName.equals(drugName)){
                                        nameExists = true;
                                    }
                                }
                            }

                            if(nameExists == false) {
                                Log.d("BARULHO", "DRUGID - " + drugId + " USERID - " + userId);
                                intentEdit.putExtra("drugId", drugId);
                                intentEdit.putExtra("userId", userId);
                                intentEdit.putExtra("drugName", name);
                                intentEdit.putExtra("lastDrugName", drugName);
                                intentEdit.putExtra("drugNum", number);
                                intentEdit.putExtra("drugDate", date);
                                intentEdit.putExtra("personsUsing", personsUsing);
                                setResult(RESULT_OK, intentEdit);
                                finish();
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

    public void getEditBack(View view) {
            Intent intent =  new Intent(getApplicationContext(), list_inventario.class);
            startActivity(intent);
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
}
