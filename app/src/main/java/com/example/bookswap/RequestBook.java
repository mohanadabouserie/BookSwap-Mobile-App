package com.example.bookswap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RequestBook extends AppCompatActivity {

    String Book_key = new String();
    String User_Name;
    String requester_user_name;
    String Phone_Number;
    String Email;
    String Cover_Uri, Profile_Uri, Book_Title;
    ArrayList<String> tokens;
    ArrayList<String> Uploaded_Pictures;
    TextView title, author, isbn, genre, user, phone, email;
    ImageView user_profile_pic, book_cover, first_pic, second_pic, third_pic;
    Button Request;
    private FirebaseFirestore database;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_book);
        title = findViewById(R.id.Book_Title);
        author = findViewById(R.id.Book_Author);
        isbn = findViewById(R.id.Book_ISBN);
        genre = findViewById(R.id.Book_Genres);
        user = findViewById(R.id.user_name);
        phone = findViewById(R.id.user_phone);
        email = findViewById(R.id.user_email);
        user_profile_pic = findViewById(R.id.User_Pic);
        book_cover = findViewById(R.id.Book_Cover);
        first_pic = findViewById(R.id.First_Pic);
        second_pic = findViewById(R.id.Second_Pic);
        third_pic = findViewById(R.id.Third_Pic);
        Request= findViewById(R.id.Request_Book);
        database = FirebaseFirestore.getInstance();
        SessionManager sessionManager = SessionManager.getInstance();
        requester_user_name = sessionManager.getUsername();
        Intent i = getIntent();
        Book_key = i.getStringExtra("bookkey");
        database.collection("books").document(Book_key).get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                DocumentSnapshot document = task.getResult();
                if (document.exists())
                {
                    Book_Title = document.getString("title");
                    String Book_ISBN = document.getString("isbn");
                    String Book_Author = document.getString("author");
                    String Book_Genres = document.getString("genres");
                    User_Name = document.getString("book_username");
                    title.setText(Book_Title);
                    author.setText(Book_Author);
                    isbn.setText(Book_ISBN);
                    String[] genres = Book_Genres.split(", ");
                    // set genre to the first 3 genres
                    if(genres.length > 3)
                    {
                        genre.setText(genres[0] + ", " + genres[1] + ", " + genres[2]);
                    }
                    else
                    {
                        genre.setText(Book_Genres);
                    }
//                    genre.setText(Book_Genres);
                    user.setText(User_Name);
                    Cover_Uri =document.getString("cover_page");
                    Uploaded_Pictures = new ArrayList<>();
                    Uploaded_Pictures = (ArrayList<String>) document.get("imageUrls");
                    Display_Pic(Cover_Uri, book_cover);
                    Display_Pic(Uploaded_Pictures.get(0), first_pic);
                    Display_Pic(Uploaded_Pictures.get(1), second_pic);
                    Display_Pic(Uploaded_Pictures.get(2), third_pic);

                }
            }
        });


        Request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                database.collection("users").document(requester_user_name).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists())
                        {
                            long credits = document.getLong("credits");
                            ArrayList<String> myrequests = (ArrayList<String>) document.get("myrequests");
                            if(credits == 0)
                            {
                                Toast.makeText(RequestBook.this, "You don't have any credits", Toast.LENGTH_SHORT).show();
                            }
                            else if(myrequests.size() >= credits)
                            {
                                Toast.makeText(RequestBook.this, "You don't have enough credits", Toast.LENGTH_SHORT).show();
                                Toast.makeText(RequestBook.this, "You have " + credits + " credits and " + myrequests.size() + " pending requests", Toast.LENGTH_SHORT).show();
                            }
                            else if(credits > myrequests.size())
                            {
                                database.collection("users").document(User_Name).get().addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        DocumentSnapshot document2 = task2.getResult();
                                        if (document2.exists())
                                        {
//                                            Phone_Number = document2.getString("phoneNumber");
//                                            Email = document2.getString("email");
//                                            Profile_Uri = document2.getString("profilepic");
                                            tokens = (ArrayList<String>) document2.get("tokens");
//                                            Display_Pic(Profile_Uri, user_profile_pic);
//                                            phone.setText(Phone_Number);
//                                            email.setText(Email);
                                        }
                                    }
                                });
                                database.collection("books")
                                        .document(Book_key)
                                        .update("requested", true,
                                                "requester", requester_user_name)
                                        .addOnSuccessListener(aVoid -> {
                                            database.collection("users")
                                                    .document(requester_user_name)
                                                    .update("myrequests", FieldValue.arrayUnion(Book_key))
                                                    .addOnSuccessListener(aVoid2 -> {
                                                        Toast.makeText(RequestBook.this, "Request made!", Toast.LENGTH_SHORT).show();
                                                        for(int tok = 0; tok < tokens.size(); tok++)
                                                        {
                                                            FCMSend.pushNotification(
                                                                    RequestBook.this,
                                                                    tokens.get(tok),
                                                                    "New Request",
                                                                    "A request has been made on your book: " + Book_Title
                                                            );
                                                        }
                                                        Intent intent = new Intent(RequestBook.this, LibraryActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(RequestBook.this, "Failed to request book " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(RequestBook.this, "Failed to request book " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                            else
                            {
                                Toast.makeText(RequestBook.this, "Failed to request book", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });
    }


    private void Display_Pic(String url, ImageView Image_View_Name) {

        // Load the image into the ImageView using Picasso
        if(url!=""&&url!=null) {
            Picasso.get()
                    .load(url)
                    .placeholder(R.drawable.profile_picture_placeholder) // Placeholder image while loading
                    .error(R.drawable.profile_picture_placeholder) // Error image if loading fails
                    .into(Image_View_Name);
        }
        else
        {
            Image_View_Name.setImageResource(R.drawable.profile_picture_placeholder);
        }
    }
}