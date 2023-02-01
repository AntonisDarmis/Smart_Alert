package com.unipi.adarmis.smartalert;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class Register extends AppCompatActivity  {
    Button registerButton;
    EditText editEmail,editPassword;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private String token = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //init firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        registerButton = findViewById(R.id.registerButton);
        editEmail = findViewById(R.id.email);
        editPassword = findViewById(R.id.regPassword);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = String.valueOf(editEmail.getText());
                String password = String.valueOf(editPassword.getText());
                if(TextUtils.isEmpty(email))
                {
                    Toast.makeText(Register.this,"Email is required!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password) || email.length() <= 6 )
                {
                    Toast.makeText(Register.this,"Password is required and must be at least 6 characters long!",Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                    return;
                                }

                                // Get new FCM registration token
                                token = task.getResult();

                                // Log and toast
                                //String msg = getString(R.string.msg_token_fmt, token);
                                //Log.d(TAG, msg);
                                //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();

                            }
                        });

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    Map<String,Object> user = new HashMap<>();
                                    user.put("email",email);
                                    user.put("role","USER");
                                    user.put("token",token);
                                    db.collection("users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                            .set(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "DocumentSnapshot successfully written!");
                                                    Toast.makeText(Register.this, "Account created successfully",
                                                            Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(Register.this,MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error writing document", e);
                                                    Toast.makeText(Register.this, "An error occurred, please retry.",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(Register.this, "Account creation failed.",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }
        });
    }




}