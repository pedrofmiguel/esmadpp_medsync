package com.example.login2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.login2.DrugDateService.CHANNEL_ID;

public class PlanoEmpty extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseUser user;
    private DatabaseReference mDatabaseDrugs;

    private int countEqualsIdDrugs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plano_empty);

        //Create channel for notifications
        createNotificationChannel();

        //Bottom Navigation Bar
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        //DataBase
        mAuth = FirebaseAuth.getInstance();
        mDatabaseDrugs = FirebaseDatabase.getInstance().getReference("drugs");
        user = mAuth.getCurrentUser();

        countEqualsIdDrugs = 0;

        //Check if user has drugs in DataBase
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
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, DrugDateService.class);
        startService(intent);
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

    public void addPlan(View view) {
        if(countEqualsIdDrugs == 0) {
            Snackbar.make(view, "Ainda não criou nenhum medicamento! Crie um antes de criar um plano.",Snackbar.LENGTH_LONG).setAction("Action", null).show();
        } else {
            Intent intent = new Intent(this, AddPlan.class);
            startActivity(intent);
        }

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
