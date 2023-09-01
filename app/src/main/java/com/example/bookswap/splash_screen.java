package com.example.bookswap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class splash_screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start the main activity
                Intent intent = new Intent(splash_screen.this, WelcomePage.class);
                startActivity(intent);

                finish();
            }
        }, 3000); // 2000 milliseconds (2 seconds) delay

    }
}