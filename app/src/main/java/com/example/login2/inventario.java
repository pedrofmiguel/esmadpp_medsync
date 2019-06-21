package com.example.login2;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class inventario extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseUser user;
    private DatabaseReference mDatabasePlans;

    private int countEqualsIdPlans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario);

        //Bottom Navigation Bar
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        // Database
        mAuth = FirebaseAuth.getInstance();
        mDatabasePlans = FirebaseDatabase.getInstance().getReference("plans");
        user = mAuth.getCurrentUser();

        countEqualsIdPlans = 0;

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

    public void addFirstDrug(View view) {
        Intent intent = new Intent(this, AddMed.class);
        startActivity(intent);
    }
}
