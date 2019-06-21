package com.example.login2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.zip.Inflater;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class FragmentPerfil extends Fragment {
    View view;

    TextView txtName, txtEmail, txtBirthDate, txtSex;
    ImageView imgProfile;
    Button btnEdit, btnLogout;

    // Database
    private FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference myUsersRef;
    private StorageReference mStorage;
    private StorageReference updateFile;

    //User info
    String userName, userDateBirth, userSex, userImageUrl;

    //FirebaseUser currentUser;
    String currentUserId, currentUserEmail;

    // request code
    private int REQUEST_CODE = 1;

    public FragmentPerfil() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.perfil_fragment, container,false);

        //DataBase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myUsersRef = database.getReference("users");
        user = mAuth.getCurrentUser();

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

//        mStorage = FirebaseStorage.getInstance().getReference("uploads");
//
//        updateFile = mStorage.child("1560654923931.jpg");
//
//        updateFile.putFile()
//
//        mStorage.child("1560654923931.jpg").getDownloadUrl()
//                .addOnSuccessListener(new OnSuccessListener<Uri>() {
//                    @Override
//                    public void onSuccess(Uri uri) {
//                        Uri down = uri;
//                        Log.d("Picasso", "uri - " + down);
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d("Picasso", "falhou");
//                    }
//                });

        fillPageWithInfo();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtName = getView().findViewById(R.id.txtName);
        txtEmail = getView().findViewById(R.id.txtEmail);
        txtBirthDate = getView().findViewById(R.id.txtBirthDate);
        txtSex = getView().findViewById(R.id.txtSex);
        imgProfile = getView().findViewById(R.id.imgProfile);
        btnEdit = getView().findViewById(R.id.btnEdit);
        btnLogout = getView().findViewById(R.id.btnLogout);

        // Edit profile
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), EditProfile.class);
                intent.putExtra("name", userName);
                intent.putExtra("email", currentUserEmail);
                intent.putExtra("dateBirth", userDateBirth);
                intent.putExtra("sex", userSex);
                intent.putExtra("imageUrl", userImageUrl);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        // Logout
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Login.class);
                FirebaseAuth.getInstance().signOut();
                startActivity(intent);
            }
        });
    }

    private void fillPageWithInfo() {
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        myUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapShot : dataSnapshot.getChildren()) {
                    Users user = userSnapShot.getValue(Users.class);

                    if (currentUserId.equals(user.userId)) {
                        txtName.setText(user.userName);
                        txtEmail.setText(currentUserEmail);
                        txtBirthDate.setText(user.userBirthDate);
                        txtSex.setText(user.userSex);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            updateUserInfo(currentUserId, data.getExtras().getString("name"), data.getExtras().getString("birthDate"), data.getExtras().getString("sex"), userImageUrl);
            user.updateEmail(data.getExtras().getString("email"))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            fillPageWithInfo();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(view, "Ocorreu um erro na gravação da nova palavra-passe. Tente novamente!",Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        }
                    });
        }
    }

    private boolean updateUserInfo(String userId, String userName, String userBirthDate, String userSex, String userImageUrl) {

        DatabaseReference userToChange = myUsersRef.child(userId);

        Users user = new Users(userId, userName, userBirthDate, userSex, userImageUrl);

        userToChange.setValue(user);

        return true;
    }
}
