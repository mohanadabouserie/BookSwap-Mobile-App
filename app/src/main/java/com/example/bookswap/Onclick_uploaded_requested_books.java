package com.example.bookswap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class Onclick_uploaded_requested_books extends AppCompatActivity {

    String Book_key, tag;
    String User_Name, requester;
    String Phone_Number, Book_ISBN;
    String Email;
    String Cover_Uri, Profile_Uri;
    ArrayList<String> Uploaded_Pictures;
    TextView title, author, isbn, genre, user, phone, email, not_received;
    ImageView user_profile_pic, book_cover, first_pic, second_pic, third_pic;
    Button Accept, Decline, Delivered;
    Boolean received;
    private ProgressDialog progressDialog;
    private FirebaseFirestore database;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onclick_uploaded_requested_books);
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
        Accept = findViewById(R.id.Accept);
        Decline = findViewById(R.id.Decline);
        Delivered = findViewById(R.id.Delivered);
        not_received = findViewById(R.id.not_received_text_view);
        database = FirebaseFirestore.getInstance();
        Intent i = getIntent();
        Book_key = i.getStringExtra("bookkey");
        tag = i.getStringExtra("tag");
        received = i.getBooleanExtra("received", false);
        if(tag.equals("not accepted"))
        {
            Accept.setVisibility(View.VISIBLE);
            Decline.setVisibility(View.VISIBLE);
            Delivered.setVisibility(View.GONE);
            not_received.setVisibility(View.GONE);
        }
        else if(tag.equals("not received"))
        {
            Decline.setVisibility(View.GONE);
            Delivered.setVisibility(View.GONE);
            Accept.setVisibility(View.GONE);
            not_received.setVisibility(View.VISIBLE);
        }
        else if(tag.equals("not delivered"))
        {
            Accept.setVisibility(View.GONE);
            Decline.setVisibility(View.GONE);
            Delivered.setVisibility(View.VISIBLE);
            not_received.setVisibility(View.GONE);
        }

        database.collection("books").document(Book_key).get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                DocumentSnapshot document = task.getResult();
                if (document.exists())
                {
                    String Book_Title = document.getString("title");
                    Book_ISBN = document.getString("isbn");
                    String Book_Author = document.getString("author");
                    String Book_Genres = document.getString("genres");
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
                    User_Name = document.getString("book_username");
                    requester = document.getString("requester");
                    title.setText(Book_Title);
                    author.setText(Book_Author);
                    isbn.setText(Book_ISBN);
//                    genre.setText(Book_Genres);
                    user.setText(requester);
                    Cover_Uri =document.getString("cover_page");
                    Uploaded_Pictures = new ArrayList<>();
                    Uploaded_Pictures = (ArrayList<String>) document.get("imageUrls");
                    Display_Pic(Cover_Uri, book_cover);
                    Display_Pic(Uploaded_Pictures.get(0), first_pic);
                    Display_Pic(Uploaded_Pictures.get(1), second_pic);
                    Display_Pic(Uploaded_Pictures.get(2), third_pic);
                    database.collection("users").document(requester).get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            DocumentSnapshot document2 = task2.getResult();
                            if (document2.exists())
                            {
                                Phone_Number = document2.getString("phoneNumber");
                                Email = document2.getString("email");
                                Profile_Uri = document2.getString("profilepic");
                                if(tag.equals("not received") || tag.equals("not delivered"))
                                {
                                    Display_Pic(Profile_Uri, user_profile_pic);
                                    phone.setText(Phone_Number);
                                    email.setText(Email);
                                }
                            }
                        }
                    });
                }
            }
        });


        Accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.collection("books")
                        .document(Book_key)
                        .update("accepted", true)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Onclick_uploaded_requested_books.this, "Book accepted!", Toast.LENGTH_SHORT).show();
                                Intent refresh = new Intent(Onclick_uploaded_requested_books.this, uploaded_requested_books.class);
                                refresh.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(refresh);
                                Intent intent = new Intent(Onclick_uploaded_requested_books.this, LibraryActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Onclick_uploaded_requested_books.this, "Failed to accept book: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        Decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                database.collection("books")
                        .document(Book_key)
                        .update("requested", false,
                                "requester", "")
                        .addOnSuccessListener(aVoid -> {
                            database.collection("users")
                                    .document(requester)
                                    .update("myrequests", FieldValue.arrayRemove(Book_key))
                                    .addOnSuccessListener(aVoid2 -> {
                                        Toast.makeText(Onclick_uploaded_requested_books.this, "Request declined successfully!", Toast.LENGTH_SHORT).show();
                                        Intent refresh = new Intent(Onclick_uploaded_requested_books.this, uploaded_requested_books.class);
                                        refresh.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                        startActivity(refresh);
                                        Intent intent = new Intent(Onclick_uploaded_requested_books.this, LibraryActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(Onclick_uploaded_requested_books.this, "Failed to decline book " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(Onclick_uploaded_requested_books.this, "Failed to decline book " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
        Delivered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (received) {
                    progressDialog = new ProgressDialog(Onclick_uploaded_requested_books.this);
                    progressDialog.setMessage("Loading...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    database.collection("users")
                            .document(User_Name)
                            .update("credits", FieldValue.increment(1))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    database.collection("users")
                                            .document(requester)
                                            .update("credits", FieldValue.increment(-1))
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid2) {
                                                    database.collection("books")
                                                            .document(Book_key)
                                                            .delete()
                                                            .addOnSuccessListener(aVoid3 -> {
                                                                database.collection("users")
                                                                        .document(User_Name)
                                                                        .update("mybooks", FieldValue.arrayRemove(Book_key))
                                                                        .addOnSuccessListener(avoid4 -> {
                                                                            // Book document and key removed from database
                                                                            // Delete the directory from Firebase Storage
                                                                            String directoryPath = "books_images/" + User_Name + "/" + Book_ISBN;
                                                                            Log.e("Path", directoryPath);
                                                                            FirebaseStorage storage = FirebaseStorage.getInstance();
                                                                            StorageReference directoryRef = storage.getReference().child(directoryPath);

                                                                            // Delete Image_1
                                                                            StorageReference image1Ref = directoryRef.child("Image_1");
                                                                            image1Ref.delete()
                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid5) {
                                                                                            // Continue with deleting Image_2
                                                                                            StorageReference image2Ref = directoryRef.child("Image_2");
                                                                                            image2Ref.delete()
                                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onSuccess(Void aVoid6) {
                                                                                                            // Continue with deleting Image_3
                                                                                                            StorageReference image3Ref = directoryRef.child("Image_3");
                                                                                                            image3Ref.delete()
                                                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                        @Override
                                                                                                                        public void onSuccess(Void aVoid7) {
                                                                                                                            // Create a batched write
                                                                                                                            WriteBatch batch = database.batch();

                                                                                                                            // Remove the Book_key from the mybooks array of the current user
                                                                                                                            DocumentReference currentUserRef = database.collection("users").document(User_Name);
                                                                                                                            batch.update(currentUserRef, "mybooks", FieldValue.arrayRemove(Book_key));

                                                                                                                            // Remove the Book_key from the myrequests array of the requester
                                                                                                                            DocumentReference requesterRef = database.collection("users").document(requester);
                                                                                                                            batch.update(requesterRef, "myrequests", FieldValue.arrayRemove(Book_key));

                                                                                                                            // Commit the batched write
                                                                                                                            batch.commit()
                                                                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                                        @Override
                                                                                                                                        public void onSuccess(Void aVoid8) {
                                                                                                                                            progressDialog.dismiss();
                                                                                                                                            Toast.makeText(Onclick_uploaded_requested_books.this, "Book marked as delivered!", Toast.LENGTH_SHORT).show();
                                                                                                                                            Toast.makeText(Onclick_uploaded_requested_books.this, "Your credits updated successfully!", Toast.LENGTH_SHORT).show();
                                                                                                                                            Intent refresh = new Intent(Onclick_uploaded_requested_books.this, uploaded_requested_books.class);
                                                                                                                                            refresh.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                                                                                                                            startActivity(refresh);
                                                                                                                                            Intent refresh2 = new Intent(Onclick_uploaded_requested_books.this, LibraryActivity.class);
                                                                                                                                            refresh2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                                                                                                                            startActivity(refresh2);
                                                                                                                                            Intent intent = new Intent(Onclick_uploaded_requested_books.this, LibraryActivity.class);
                                                                                                                                            startActivity(intent);
                                                                                                                                            finish();
                                                                                                                                        }
                                                                                                                                    })
                                                                                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                                                                                        @Override
                                                                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                                                                            Toast.makeText(Onclick_uploaded_requested_books.this, "Failed to remove the book from my requests of the requester: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                                                        }
                                                                                                                                    });
                                                                                                                        }

                                                                                                                    })
                                                                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                                                                        @Override
                                                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                                                            Toast.makeText(Onclick_uploaded_requested_books.this, "Failed to delete Image_3: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                                        }
                                                                                                                    });
                                                                                                        }
                                                                                                    })
                                                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                                                        @Override
                                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                                            Toast.makeText(Onclick_uploaded_requested_books.this, "Failed to delete Image_2: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    });
                                                                                        }
                                                                                    })
                                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                                        @Override
                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                            Toast.makeText(Onclick_uploaded_requested_books.this, "Failed to delete Image_1: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    });
                                                                        })
                                                                        .addOnFailureListener(e -> {
                                                                            // Failed to remove book key from user's mybooks array
                                                                            Toast.makeText(Onclick_uploaded_requested_books.this, "Failed to remove book key from mybooks array: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                        });
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                // Failed to delete the book from the database
                                                                Toast.makeText(Onclick_uploaded_requested_books.this, "Failed to delete book: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            });
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(Onclick_uploaded_requested_books.this, "Failed to update credits for requester: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Onclick_uploaded_requested_books.this, "Failed to update credits for book owner: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else
                {
                    database.collection("books")
                            .document(Book_key)
                            .update("delivered", true)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(Onclick_uploaded_requested_books.this, "Book marked as delivered successfully!", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(Onclick_uploaded_requested_books.this, "Waiting for the receipt confirmation!", Toast.LENGTH_LONG).show();
                                    Intent refresh = new Intent(Onclick_uploaded_requested_books.this, uploaded_requested_books.class);
                                    refresh.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    startActivity(refresh);
                                    Intent intent = new Intent(Onclick_uploaded_requested_books.this, LibraryActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Onclick_uploaded_requested_books.this, "Failed to mark book as delivered: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
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