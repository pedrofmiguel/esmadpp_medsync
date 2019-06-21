package com.example.login2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

public class Profile extends AppCompatActivity {


    private TabLayout tabLayout;
    private AppBarLayout appBarLayout;
    private ViewPager viewPager;

    ImageView imgProfile;

    //DataBase
    private FirebaseAuth mAuth;
    FirebaseUser user;

    FirebaseDatabase database;
    DatabaseReference myUsersRef, myUploadsRef, mDatabaseDrugs, mDatabasePlans;

    //User info
    String userName, userDateBirth, userSex, userImageUrl;

    //FirebaseUser currentUser;
    String currentUserId, currentUserEmail;

    //Count drugs and plans
    private int countEqualsIdDrugs;
    private int countEqualsIdPlans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Fragments from profile and plans history
        tabLayout = (TabLayout) findViewById(R.id.tablayout_id);
        appBarLayout = (AppBarLayout) findViewById(R.id.appbarid);
        viewPager = (ViewPager) findViewById(R.id.viewpager_id);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Add the fragments
        adapter.AddFragment(new FragmentPerfil(),"Perfil");
        adapter.AddFragment(new FragmentHistorico(),"Historico");

        // Adapter setup
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);


        //Bottom Navigation Bar
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        //DataBase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myUsersRef = database.getReference("users");
        myUploadsRef = database.getReference("uploads");
        mDatabaseDrugs = database.getReference("drugs");
        mDatabasePlans = database.getReference("plans");

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        imgProfile = findViewById(R.id.imgProfile);

        user = mAuth.getCurrentUser();

        // Fill page with user information
        fillPageWithInfo();

        countEqualsIdDrugs = 0;
        countEqualsIdPlans = 0;

        //Check if user has drugs in database
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

        //Check if user has plans in database
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

    private void fillPageWithInfo() {
        // Get user e-mail
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Get users from the database
        myUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot userSnapShot : dataSnapshot.getChildren()) {
                    Users user = userSnapShot.getValue(Users.class);

                    if (currentUserId.equals(user.userId)) {
                        //Show Image
                        Picasso.get()
                                .load(user.userImageUrl)
                                .into(imgProfile);

                        // Save user info for edit profile
                        userName = user.userName;
                        userDateBirth = user.userBirthDate;
                        userSex = user.userSex;
                        userImageUrl = user.userImageUrl;
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
