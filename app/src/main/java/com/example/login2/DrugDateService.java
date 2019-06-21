package com.example.login2;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

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
import java.util.concurrent.TimeUnit;

public class DrugDateService extends IntentService {

    static String CHANNEL_ID = "111";

    //DataBase
    private FirebaseAuth mAuth;
    FirebaseUser user;

    DatabaseReference myDrugsRef;
    public static Date drugsDate;

    public DrugDateService() {
        super("DrugDateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d("DADOKING", "ENTROU NO SERVIÇO");

        // Convert string to date
        final SimpleDateFormat dateFormater = new SimpleDateFormat("dd/MM/yyyy");

        // Get current Date
        final Date nowDate = Calendar.getInstance().getTime();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        myDrugsRef = FirebaseDatabase.getInstance().getReference("drugs");

        myDrugsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot drugSnapShot : dataSnapshot.getChildren()) {
                    Drugs drug = drugSnapShot.getValue(Drugs.class);

                    Log.d("DADOKING", "conteudo - " + drug.drugDate);

                    if(user.getUid().equals(drug.userId)) {
                        try {
                            drugsDate = dateFormater.parse(drug.drugDate);
                            long interval = drugsDate.getTime() - nowDate.getTime();

                            Log.d("DADOKING", "DRugs - " + drug.drugName);

                            if(TimeUnit.DAYS.convert(interval, TimeUnit.MILLISECONDS) <= 5 && TimeUnit.DAYS.convert(interval, TimeUnit.MILLISECONDS) > 0) {
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setContentTitle("Prazo de validade prestes a expirar!")
                                        .setContentText("A validade de " + drug.drugName + " expira em " +
                                                TimeUnit.DAYS.convert(interval, TimeUnit.MILLISECONDS) + " days!");

                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                                int notificationId = 0;
                                notificationManager.notify(notificationId, builder.build());
                            } else if(TimeUnit.DAYS.convert(interval, TimeUnit.MILLISECONDS) == 5){
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setContentTitle("Prazo de validade expirou!")
                                        .setContentText("A validade de " + drug.drugName + " expirou!");

                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                                int notificationId = 1;
                                notificationManager.notify(notificationId, builder.build());
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
