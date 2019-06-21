package com.example.login2;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EditProfile extends AppCompatActivity {

    EditText txtName, txtEmail, txtBirthDate;
    RadioGroup rgSex;
    ImageView imgEditPhoto;

    //DataBase
    private FirebaseAuth mAuth;
    FirebaseUser user;
    String currentUserEmail;
    FirebaseDatabase database;
    DatabaseReference mDatabaseDrugs, mDatabasePlans;

    private int countEqualsIdDrugs;
    private int countEqualsIdPlans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtBirthDate = findViewById(R.id.txtBirthDate);
        rgSex = findViewById(R.id.rgSex);
        imgEditPhoto = findViewById(R.id.imgEditPhoto);

        //Bottom Navigation Bar
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation_edit_profile);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        //DataBase
        mAuth = FirebaseAuth.getInstance();
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        mDatabaseDrugs = database.getReference("drugs");
        mDatabasePlans = database.getReference("plans");

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        // User info
        String userName;
        String userEmail;
        String userDateBirth;
        String userSex;
        Uri userImageUrl;

        if(extras != null) {
            userName = extras.getString("name");
            userEmail = extras.getString("email");
            userDateBirth = extras.getString("dateBirth");
            userSex = extras.getString("sex");
            userImageUrl = Uri.parse(extras.getString("imageUrl"));

            txtName.setText(userName);
            txtEmail.setText(userEmail);
            txtBirthDate.setText(userDateBirth);
            if(userSex.equals("Masculino")) {
                rgSex.check(R.id.rbMale);
            } else {
                rgSex.check(R.id.rbFemale);
            }

            Picasso.get()
                    .load(userImageUrl)
                    .into(imgEditPhoto);
        }

        countEqualsIdDrugs = 0;
        countEqualsIdPlans = 0;

        mDatabaseDrugs.addValueEventListener(new ValueEventListener() {
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
        mDatabasePlans.addValueEventListener(new ValueEventListener() {
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

    public static Date dateOfBirth;

    // Save profile
    public void save (View view) throws ParseException {
        int selectedId = rgSex.getCheckedRadioButtonId();
        RadioButton rb = findViewById(selectedId);
        String gender = rb.getText().toString();
        String name = txtName.getText().toString();
        String email = txtEmail.getText().toString();
        String birthDate = txtBirthDate.getText().toString();

        // Convert string to date
        SimpleDateFormat dateFormater = new SimpleDateFormat("dd/MM/yyyy");
        // Get current Date
        Date nowDate = Calendar.getInstance().getTime();

        Intent intentSaveForRegister = new Intent();
        if(name.equals("") || email.equals("") || birthDate.equals("") || rb.equals("")) {
            Snackbar.make(view, "Existem campos por preencher!",Snackbar.LENGTH_LONG).setAction("Action", null).show();
        } else {
            dateOfBirth = dateFormater.parse(birthDate);

            if(dateOfBirth.after(nowDate)){
                Snackbar.make(view, "O dia de aniversário tem de ser pelo menos um dia anterior ao de hoje!",Snackbar.LENGTH_LONG).setAction("Action", null).show();
            } else {
                intentSaveForRegister.putExtra("name", name);
                intentSaveForRegister.putExtra("email", email);
                intentSaveForRegister.putExtra("birthDate", birthDate);
                intentSaveForRegister.putExtra("sex", gender);
                setResult(RESULT_OK, intentSaveForRegister);
                finish();
            }
        }
    }

    // Close edit profile
    public void cancel(View view) {
        finish();
    }

    // Send e-mail to edit password
    public void resetPassword(View view) {
        mAuth.sendPasswordResetEmail(currentUserEmail);

        Intent intentResetPassword = new Intent();
        setResult(RESULT_CANCELED, intentResetPassword);
        finish();
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
                    if(countEqualsIdPlans == 0) {
                        Intent intent = new Intent(getApplicationContext(), PlanoEmpty.class);
                        startActivity(intent);
                    } else {
                        Intent intent =  new Intent(getApplicationContext(), list_planos.class);
                        startActivity(intent);
                    }
                    return true;

                case R.id.action_perfil:
                    return true;
            }
            return false;
        }
    };
}
