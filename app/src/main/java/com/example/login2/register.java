package com.example.login2;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class register extends AppCompatActivity {

    EditText txtName, txtEmail, txtPassword, txtConfPassword, txtBirthDate;
    RadioGroup rgSex;

    // Database
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Pick image
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri;

    // Save image
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        txtConfPassword = findViewById(R.id.txtConfPassword);
        txtBirthDate = findViewById(R.id.txtBirthDate);
        rgSex = findViewById(R.id.rgSex);

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
    }

    // Pick image
    public void addImage(View view)
    {
        openFileChooser();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            mImageUri = data.getData();
        }
    }


    public static Date dateOfBirth;

    public void register(final View view) throws ParseException {
        final String name = txtName.getText().toString();
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();
        String confPassword = txtConfPassword.getText().toString();
        final String birthDate = txtBirthDate.getText().toString();
        // Obter o radio button selecionado
        int selectedId = rgSex.getCheckedRadioButtonId();
        RadioButton rb = findViewById(selectedId);

        Log.d("RADIO", "radiobutton " + rb);

        // Convert string to date
        SimpleDateFormat dateFormater = new SimpleDateFormat("dd/MM/yyyy");
        
        // Get current Date
        Date nowDate = Calendar.getInstance().getTime();

        if(name.equals("") || email.equals("") || password.equals("") || confPassword.equals("") || birthDate.equals("") || rb.equals("null")) {
            Snackbar.make(view, "Existem campos por preencher!",Snackbar.LENGTH_LONG).setAction("Action", null).show();
        } else{
            dateOfBirth = dateFormater.parse(birthDate);
            final String radio = rb.getText().toString();

            if(dateOfBirth.after(nowDate)){
                Snackbar.make(view, "O dia de aniversário tem de ser pelo menos um dia anterior ao de hoje!",Snackbar.LENGTH_LONG).setAction("Action", null).show();
            } else if(!password.equals(confPassword)) {
                Snackbar.make(view, "As palavras-passe não são iguais!",Snackbar.LENGTH_LONG).setAction("Action", null).show();
            } else if(mImageUri == null) {
                Snackbar.make(view, "Não selecionou nenhuma foto de perfil!",Snackbar.LENGTH_LONG).setAction("Action", null).show();
            } else {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    final FirebaseUser user = mAuth.getCurrentUser();

                                    // Upload of user image
                                    final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));

                                    fileReference.putFile(mImageUri)
                                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                                                    Snackbar.make(view, "Foto guardada com sucesso!",Snackbar.LENGTH_LONG).setAction("Action", null).show();

                                                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            Uri downloadUrL = uri;

                                                            // Create user in Realtime Database with his informations
                                                            Users userInfo = new Users(user.getUid(), name, birthDate, radio, downloadUrL.toString());

                                                            mDatabase.child("users").child(user.getUid()).setValue(userInfo)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Snackbar.make(view, "Perfil criado com sucesso!",Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                                                            Intent intent = new Intent(getApplicationContext(), PlanoEmpty.class);
                                                                            startActivity(intent);
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Snackbar.make(view, "Ocorreu um erro ao criar o seu perfil!",Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                                                        }
                                                                    });
                                                        }
                                                    });
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(register.this, "Ocorreu um erro durante o upload da image!", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                } else {
                                    // If sign in fails, display a message to the user
                                    Toast.makeText(register.this, "Warning: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }

    // Get the file extension (ex: jpeg, png, etc)
    private String getFileExtension(Uri uri)
    {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}
