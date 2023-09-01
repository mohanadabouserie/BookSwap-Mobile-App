package com.example.bookswap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        SessionManager sessionManager;
        db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = getSharedPreferences("moh_mal_mar", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("is_in_@@%", false);

        if (isLoggedIn)
        {
            // User is logged in, proceed to main screen
            String username = EncryptionUtil.decrypt(sharedPreferences.getString("re_65_@#", ""));
            sessionManager = SessionManager.getInstance();
            sessionManager.setUsername(username);
            Intent intent = new Intent(MainActivity.this, Home.class);
            startActivity(intent);
            finish();
        }

        Button login = findViewById(R.id.loginbtn);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        Button signUpButton = findViewById(R.id.signupbtn);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUp.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        TextView username = findViewById(R.id.username);
        TextView password = findViewById(R.id.password);
        username.setText("");
        password.setText("");
    }
    private void login() {
        TextView username = findViewById(R.id.username);
        TextView password = findViewById(R.id.password);
        String enteredUsername = username.getText().toString();
        String enteredPassword = password.getText().toString();

        if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
        } else {
            // Query Firestone for the entered username
            db.collection("users").document(enteredUsername).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // User found, check password
                        String storedPassword = document.getString("password");
                        if (enteredPassword.equals(storedPassword)) {
                            // Correct credentials
                            Toast.makeText(MainActivity.this, "LOGIN SUCCESSFUL", Toast.LENGTH_SHORT).show();
                            // After successful sign-in or sign-up
                            SessionManager sessionManager = SessionManager.getInstance();
                            sessionManager.setUsername(username.getText().toString()); // Set the username

                            ArrayList<String> mygenres = (ArrayList<String>) document.get("genres");
                            if(mygenres.size() < 3)
                            {
                                Intent pref = new Intent(MainActivity.this, preferences.class);
                                pref.putExtra("username",username.getText().toString());
                                startActivity(pref);
                                finish();
                            }
                            else {
                                FirebaseMessaging.getInstance().getToken()
                                        .addOnCompleteListener(new OnCompleteListener<String>() {
                                            @Override
                                            public void onComplete(@NonNull Task<String> task) {
                                                if (!task.isSuccessful()) {
                                                    return;
                                                }
                                                String fcmToken = task.getResult();
                                                List<String> tokens = (List<String>) document.get("tokens");
                                                if (tokens == null || !tokens.contains(fcmToken)) {
                                                    // FCM token doesn't exist, add it to the tokens array
                                                    if (tokens == null) {
                                                        tokens = new ArrayList<>();
                                                    }
                                                    tokens.add(fcmToken);
                                                    document.getReference().update("tokens", tokens)
                                                            .addOnSuccessListener(aVoid -> Log.e("HIII", "FCM token added to user document"))
                                                            .addOnFailureListener(e -> Log.e("HIII", "Failed to add FCM token to user document: " + e.getMessage()));
                                                }
                                                SharedPreferences sharedPreferences = getSharedPreferences("moh_mal_mar", MODE_PRIVATE);
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putString("re_65_@#", EncryptionUtil.encrypt(username.getText().toString()));
                                                editor.putString("sal_88_*&", EncryptionUtil.encrypt(enteredPassword));
                                                editor.putBoolean("is_in_@@%", true);
                                                editor.apply();
                                                Intent hom = new Intent(MainActivity.this, Home.class);
                                                startActivity(hom);
                                                finish();
                                            }
                                        });
                            }
                        } else {
                            // Incorrect password
                            Toast.makeText(MainActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // User not found
                        Toast.makeText(MainActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Error occurred while querying Firestone
                    Toast.makeText(MainActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}



