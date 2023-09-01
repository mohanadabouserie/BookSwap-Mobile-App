package com.example.bookswap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class LibraryActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{
    private FirebaseFirestore database;
    Button UploadedBooks;
    Button RequestedBooks;
    Button RequestedBooksByYou;
    Button UploadBooks;
    TextView username_textView;
    TextView credits;
    ImageView username_PP;
    SessionManager session;
    String username;
    BottomNavigationView bottomNavigationView;
    CollectionReference usersCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        username_textView = findViewById(R.id.textView);
        credits = findViewById(R.id.textView2);
        UploadedBooks = findViewById(R.id.button3);
        RequestedBooks = findViewById(R.id.button4);
        RequestedBooksByYou = findViewById(R.id.button5);
        UploadBooks = findViewById(R.id.button2);
        username_PP = findViewById(R.id.username_PP);
        session = SessionManager.getInstance();
        username = session.getUsername();
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.library);
        username_textView.setText(username);
        displayProfilePic();
        database = FirebaseFirestore.getInstance();
        usersCollection = database.collection("user");
        database.collection("users").document(username).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    long Credits = document.getLong("credits");
                    credits.setText(Credits + " credits");
                }

            }
        });
        UploadedBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LibraryActivity.this, pending_uploaded_books.class);
                startActivity(intent);
            }
        });
        RequestedBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LibraryActivity.this, uploaded_requested_books.class);
                startActivity(intent);
            }
        });
        RequestedBooksByYou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LibraryActivity.this, MyRequestActivity.class);
                startActivity(intent);
            }
        });
        UploadBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LibraryActivity.this, UploadBookActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.library);
    }
    private void displayProfilePic() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference imageReference = storageReference.child("profile_picture_images/" + username + "/Profile_Image");

        imageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            // Load the image into the ImageView using Picasso
            Picasso.get()
                    .load(uri)
                    .placeholder(R.drawable.profile_picture_placeholder) // Placeholder image while loading
                    .error(R.drawable.profile_picture_placeholder) // Error image if loading fails
                    .into(username_PP);
        }).addOnFailureListener(e -> {
            // Handle the failure to load the image
            username_PP.setImageResource(R.drawable.profile_picture_placeholder);
        });
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.home) {
            Intent i = new Intent(LibraryActivity.this, Home.class);
            startActivity(i);
            return true;
        } else if (id == R.id.search) {
            Intent i = new Intent(LibraryActivity.this, Search.class);
            startActivity(i);
            return true;
        } else if (id == R.id.library) {
            //Intent i = new Intent(MyProfile.this, LibraryActivity.class);
            //startActivity(i);
            return true;
        } else if (id == R.id.profile) {
            Intent i = new Intent(LibraryActivity.this, MyProfile.class);
            startActivity(i);
            return true;
        }
        return false;
    }
}