package com.example.bookswap;

// MainActivity.java
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class WelcomePage extends AppCompatActivity {
    private FirebaseFirestore db;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        SessionManager sessionManager;
        if (!areNotificationsEnabled()) {
            showNotificationPermissionDialog();
        }
        db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = getSharedPreferences("moh_mal_mar", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("is_in_@@%", false);

        if (isLoggedIn)
        {
            // User is logged in, proceed to main screen
            String username = EncryptionUtil.decrypt(sharedPreferences.getString("re_65_@#", ""));
            sessionManager = SessionManager.getInstance();
            sessionManager.setUsername(username);
            Intent intent = new Intent(WelcomePage.this, Home.class);
            startActivity(intent);
            finish();
        }

        Button loginButton = findViewById(R.id.login);
        Button signUpButton = findViewById(R.id.sign_up);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch MainActivity when Login button is clicked
                Intent intent = new Intent(WelcomePage.this, MainActivity.class);
                startActivity(intent);
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch SignUpActivity when Sign Up button is clicked
                Intent intent = new Intent(WelcomePage.this, SignUp.class);
                startActivity(intent);
            }
        });
    }
    private void showNotificationPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enable Notifications");
        builder.setMessage("Allow notifications to receive important updates about your books and your requests.");
        builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestNotificationPermission();
            }
        });
        builder.setNegativeButton("Deny", null);
        builder.show();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Redirect user to App Notification settings for Android Oreo and above
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivityForResult(intent, REQUEST_NOTIFICATION_PERMISSION);
        } else {
            // Request permission for devices below Android Oreo
            NotificationManagerCompat.from(this).areNotificationsEnabled();
        }
    }

    private boolean areNotificationsEnabled() {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        return notificationManagerCompat.areNotificationsEnabled();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            // Handle the result of the notification permission request
            if (!areNotificationsEnabled()) {
                // User still denied permission
                // You may want to handle this case accordingly
            }
        }
    }
}


