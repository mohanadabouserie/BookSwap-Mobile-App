package com.example.bookswap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MyProfile extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener
{

    private FirebaseFirestore database;
    BottomNavigationView bottomNavigationView;
    String username;
    TextView user_name, Credits;
    EditText lastname;
    EditText firstname;
    EditText Email;
    EditText phone;
    EditText Birth_Date;
    Button first_edit;
    Button last_edit;
    Button user_email_edit;
    Button Phone_Number_edit;
    Button Birth_Date_Edit;
    Button Profile_Pic_Edit;
    Button Save, Logout;
    Uri Profile_Image;
    ImageView profile_pic_imageView;
    private static final int PICK_IMAGE_REQUEST = 1;
    CollectionReference usersCollection;

    private void selectImage() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri Image_Uri = data.getData();

            Profile_Image = Image_Uri;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.profile);
        Intent i = getIntent();

        SessionManager sessionManager = SessionManager.getInstance();
        username = sessionManager.getUsername();
        user_name = findViewById(R.id.UserName);
        lastname = findViewById(R.id.LastName);
        firstname = findViewById(R.id.FirstName);
        Email = findViewById(R.id.Email);
        phone = findViewById(R.id.PhoneNumber);
        Birth_Date = findViewById(R.id.BirthDate);
        first_edit = findViewById(R.id.FirstNameEdit);
        last_edit = findViewById(R.id.LastNameEdit);
        user_email_edit = findViewById(R.id.EmailEdit);
        Phone_Number_edit = findViewById(R.id.PhoneNumberEdit);
        Birth_Date_Edit = findViewById(R.id.BirthDateEdit);
        Profile_Pic_Edit= findViewById(R.id.ProfileEdit);
        Save = findViewById(R.id.Save);
        Logout = findViewById(R.id.Logout);
        profile_pic_imageView = findViewById(R.id.MyProfilePicture);
        Credits = findViewById(R.id.Credits);
        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (!task.isSuccessful()) {
                                    return;
                                }
                                String fcmToken = task.getResult();
                                database.collection("users").document(username).get().addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        DocumentSnapshot document = task2.getResult();
                                        if (document.exists()) {
                                            // Get the tokens array from the document
                                            List<String> tokens = (ArrayList<String>) document.get("tokens");
                                            if (tokens != null) {
                                                // Remove the FCM token from the tokens array
                                                tokens.remove(fcmToken);
                                                // Update the tokens array in the document
                                                document.getReference().update("tokens", tokens)
                                                        .addOnSuccessListener(aVoid -> {
                                                            // FCM token removed successfully
                                                            SharedPreferences sharedPreferences = getSharedPreferences("moh_mal_mar", MODE_PRIVATE);
                                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                                            editor.remove("re_65_@#");
                                                            editor.remove("sal_88_*&");
                                                            editor.putBoolean("is_in_@@%", false);
                                                            editor.apply();
                                                            SessionManager sessionManager = SessionManager.getInstance();
                                                            sessionManager.clearSession();
                                                            Intent intent = new Intent(MyProfile.this, WelcomePage.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(intent);
                                                            finish();
                                                            Toast.makeText(MyProfile.this, "LOGOUT SUCCESSFUL!", Toast.LENGTH_SHORT).show();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            // Failed to remove FCM token from the document
                                                        });
                                            } else {
                                                // Tokens array is null, continue with logout process
                                            }
                                        }
                                    }
                                });
                            }
                        });
            }
        });

        displayProfilePic();
        database = FirebaseFirestore.getInstance();
        usersCollection = database.collection("user");
        database.collection("users").document(username).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String first_name = document.getString("firstName");
                    String last_name = document.getString("lastName");
                    String email = document.getString("email");
                    String phone_num = document.getString("phoneNumber");
                    String birth_date = document.getString("birthdate");
                    long credits = document.getLong("credits");
                    firstname.setText(first_name);
                    lastname.setText(last_name);
                    Email.setText(email);
                    phone.setText(phone_num);
                    Birth_Date.setText(birth_date);
                    Credits.setText(credits + " credits");
                }

            }
        });
        user_name.setText(username);
        first_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstname.setEnabled(true);
            }
        });

        last_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastname.setEnabled(true);
            }
        });

        user_email_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Email.setEnabled(true);
            }
        });

        Phone_Number_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phone.setEnabled(true);
            }
        });

        Birth_Date_Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get current date
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                // Create DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(MyProfile.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                // Update EditText with selected date
                                Birth_Date.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                            }
                        }, year, month, dayOfMonth);

                // Show the dialog
                datePickerDialog.show();
            }
        });

        Profile_Pic_Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstname.setEnabled(false);
                lastname.setEnabled(false);
                Email.setEnabled(false);
                phone.setEnabled(false);
                save_profilePic();
            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.profile);
    }
    private void save_profilePic() {
        ProgressDialog progressDialog = new ProgressDialog(MyProfile.this);
        progressDialog.setMessage("Uploading profile picture...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference imageReference = storageReference.child("profile_picture_images/" + username + "/Profile_Image");

        if (Profile_Image != null) {
            UploadTask uploadTask = imageReference.putFile(Profile_Image);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imageReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                progressDialog.dismiss(); // Dismiss the progress dialog

                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    String profilePicUrl = downloadUri.toString();

                    // Update the profile picture URL in Firestore
                    FirebaseFirestore.getInstance().collection("users")
                            .document(username)
                            .update("profilepic", profilePicUrl)
                            .addOnSuccessListener(aVoid -> {
                                displayProfilePic();
                                Toast.makeText(MyProfile.this, "Profile picture uploaded successfully", Toast.LENGTH_SHORT).show();
                                Profile_Image = null;
                                saveData(); // Call the method to save the data
                            })
                            .addOnFailureListener(e -> {
                            });
                } else {
                }
            });
        } else {
            progressDialog.dismiss(); // Dismiss the progress dialog
            saveData(); // Call the method to save the data
        }
    }


    private void displayProfilePic() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference imageReference = storageReference.child("profile_picture_images/" + username + "/Profile_Image");

        imageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            // Load the image into the ImageView using Picasso
            Picasso.get()
                    .load(uri)
                    .placeholder(R.drawable.baseline_person_outline_24) // Placeholder image while loading
                    .error(R.drawable.baseline_person_outline_24) // Error image if loading fails
                    .into(profile_pic_imageView);
        }).addOnFailureListener(e -> {
            // Handle the failure to load the image
            Toast.makeText(MyProfile.this, "Failed to load profile picture", Toast.LENGTH_SHORT).show();
        });
    }
    private void saveData() {
        database.collection("users")
                .document(username)
                .update(
                        "email", Email.getText().toString(),
                        "firstName", firstname.getText().toString(),
                        "lastName", lastname.getText().toString(),
                        "phoneNumber", phone.getText().toString(),
                        "birthdate", Birth_Date.getText().toString()
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MyProfile.this, "Changes Saved", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MyProfile.this, "Failed to save data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.home) {
            Intent i = new Intent(MyProfile.this, Home.class);
            startActivity(i);
            return true;
        } else if (id == R.id.search) {
            Intent i = new Intent(MyProfile.this, Search.class);
            startActivity(i);
            return true;
        } else if (id == R.id.library) {
            Intent i = new Intent(MyProfile.this, LibraryActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.profile) {
            //Intent i = new Intent(MyProfile.this, MyProfile.class);
            //startActivity(i);
            return true;
        }
        return false;
    }
}

