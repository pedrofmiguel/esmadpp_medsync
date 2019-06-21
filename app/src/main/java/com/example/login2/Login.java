package com.example.login2;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.InetAddress;

public class Login extends AppCompatActivity {

    EditText txtEmail, txtPassword;
    private static String TAG = "LOGIN";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("plans");

        // Decide what to show if internet is available or not
        boolean connection = isInternetAvailable();
        if(connection == false) {
            try {
                buildDialog(Login.this).show();
            }
            catch (Exception e) {
                Log.d("Error", "Show Dialog: " + e.getMessage());
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if internet is available
        boolean connection = isInternetAvailable();
        if(connection == false) {
            try {
                buildDialog(Login.this).show();
            }
            catch (Exception e) {
                Log.d("Error", "Show Dialog: " + e.getMessage());
            }

        }
    }

    // Function that will check if internet is available
    public boolean isInternetAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    // Creation of the alert dialog that will be shown if internet is not available
    public AlertDialog.Builder buildDialog(Context c) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Sem conexão à internet");
        builder.setMessage("Precisa de usar Wi-Fi ou dados móveis!");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        return builder;
    }

    // Login
    public void login(final View view)
    {
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();
        boolean correctEmail = false;
        int count = 0;


        if(!email.equals("")) {
            for(int i = 0; i < email.length(); i++) {
                if(String.valueOf(email.charAt(i)).equals(" ")) {
                    count++;
                }
            }
            if(count == 0) {
                correctEmail = true;
            } else {
                correctEmail = false;
            }
        }

        if(correctEmail == true && !password.equals("")) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                final FirebaseUser user = mAuth.getCurrentUser();

                                // Check how many plans the user logged has, to know what activity should be shown
                                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        int countEqualsId = 0;

                                        for(DataSnapshot plansSnapShot : dataSnapshot.getChildren()) {
                                            Plans plan = plansSnapShot.getValue(Plans.class);

                                            if(user.getUid().equals(plan.userId)) {
                                                countEqualsId++;
                                            }
                                        }

                                        Intent intent = new Intent();

                                        if(countEqualsId == 0) {
                                            intent = new Intent(getApplicationContext(), PlanoEmpty.class);
                                        } else {
                                            intent = new Intent(getApplicationContext(), list_planos.class);
                                        }

                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        // Failed to read value
                                        Log.w("Log", "Failed to read value.", error.toException());
                                    }
                                });

                            } else {
                                // If sign in fails, display a message to the user.
                                Snackbar.make(view, "O e-mail ou a palavra-passe estão errados!",Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            }
                        }
                    });
        } else {
            Snackbar.make(view, "Não preencheu todos os campos ou há espaços no campo e-mail!",Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    public void goRegister(View view)
    {
        Intent intent = new Intent(this, register.class);
        startActivity(intent);
    }
}
