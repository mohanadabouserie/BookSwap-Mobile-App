package com.example.bookswap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.messaging.FirebaseMessaging;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, usernameEditText, firstNameEditText, lastNameEditText, phoneNumberEditText;
    private EditText birthdateEditText; // Calendar textbox
    private Button signupButton, backButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.clearSession();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        usernameEditText = findViewById(R.id.username);
        firstNameEditText = findViewById(R.id.firstname);
        lastNameEditText = findViewById(R.id.lastname);
        phoneNumberEditText = findViewById(R.id.phonenumber);
        birthdateEditText = findViewById(R.id.birthdate);

        signupButton = findViewById(R.id.signupbutton);
        backButton = findViewById(R.id.backbutton);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Set OnClickListener to open DatePickerDialog
        birthdateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private boolean isValidEmail(String email) {
        // Regular expression pattern for email validation
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        return email.matches(emailPattern);
    }

    private void signUp() {
        final String email = emailEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();
        final String username = usernameEditText.getText().toString().trim();
        final String firstName = firstNameEditText.getText().toString().trim();
        final String lastName = lastNameEditText.getText().toString().trim();
        final String phoneNumber = phoneNumberEditText.getText().toString().trim();
        final String birthdate = birthdateEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || username.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty() || birthdate.isEmpty()) {
            Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            return;
        } else if (!isValidEmail(email)) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (username.contains("_")) {
            Toast.makeText(this, "Username cannot contain '_'", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (!phoneNumber.matches("\\d{11}")) {
            Toast.makeText(this, "Please enter a valid 11-digit phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> passwordErrors = validatePassword(password);
        if (!passwordErrors.isEmpty()) {
            for (String error : passwordErrors) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Check if username, email, or phone number already exists in the database
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Username already exists, ask the user to enter a different username
                            Toast.makeText(SignUp.this, "Username already exists. Please choose a different username.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Check if email already exists in the database
                            db.collection("users")
                                    .whereEqualTo("email", email)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            if (!task1.getResult().isEmpty()) {
                                                // Email already exists
                                                Toast.makeText(SignUp.this, "Email already exists.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Check if phone number already exists in the database
                                                db.collection("users")
                                                        .whereEqualTo("phoneNumber", phoneNumber)
                                                        .get()
                                                        .addOnCompleteListener(task2 -> {
                                                            if (task2.isSuccessful()) {
                                                                if (!task2.getResult().isEmpty()) {
                                                                    // Phone number already exists
                                                                    Toast.makeText(SignUp.this, "Phone number already exists.", Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    // Set the document ID as the username
                                                                    Map<String, Object> user = new HashMap<>();
                                                                    user.put("email", email);
                                                                    user.put("password", password);
                                                                    user.put("username", username);
                                                                    user.put("firstName", firstName);
                                                                    user.put("lastName", lastName);
                                                                    user.put("phoneNumber", phoneNumber);
                                                                    user.put("birthdate", birthdate);
                                                                    List<String> myBooks = new ArrayList<>();
                                                                    user.put("mybooks", myBooks);
                                                                    List<String> selectedGenres = new ArrayList<>();
                                                                    user.put("genres", selectedGenres);
                                                                    List<String> myrequests = new ArrayList<>();
                                                                    user.put("myrequests", myrequests);
                                                                    user.put("credits", 1);
                                                                    FirebaseMessaging.getInstance().getToken()
                                                                            .addOnCompleteListener(new OnCompleteListener<String>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<String> task) {
                                                                                    if (!task.isSuccessful()) {
                                                                                        return;
                                                                                    }

                                                                                    // Get new FCM registration token
                                                                                    String token = task.getResult();
                                                                                    List<String> tokens = new ArrayList<>();
                                                                                    tokens.add(token);
                                                                                    user.put("tokens", tokens);
                                                                                    db.collection("users")
                                                                                            .document(username)
                                                                                            .set(user)
                                                                                            .addOnSuccessListener(aVoid -> {
                                                                                                Toast.makeText(SignUp.this, "Signup successful", Toast.LENGTH_SHORT).show();
                                                                                                Intent intent = new Intent(SignUp.this, preferences.class);
                                                                                                intent.putExtra("username", username);
                                                                                                startActivity(intent);
                                                                                                finish();
                                                                                            })
                                                                                            .addOnFailureListener(e -> {
                                                                                                Toast.makeText(SignUp.this, "Failed to save data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                            });

                                                                                    // Log and toast
                                                                                }
                                                                            });
                                                                    // Add the user data to Firestore with the document ID as the username

                                                                }
                                                            } else {
                                                                // Error occurred while querying Firestore for phone number
                                                                Toast.makeText(SignUp.this, "Failed to check phone number: " + task2.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        } else {
                                            // Error occurred while querying Firestore for email
                                            Toast.makeText(SignUp.this, "Failed to check email: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        // Error occurred while querying Firestore for username
                        Toast.makeText(SignUp.this, "Failed to check username: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private List<String> validatePassword(String password) {
        List<String> errors = new ArrayList<>();

        if (password.length() < 8) {
            errors.add("Password must be at least 8 characters long");
        }

        if (!password.matches(".*[a-z].*")) {
            errors.add("Password must contain at least one lowercase letter");
        }

        if (!password.matches(".*[A-Z].*")) {
            errors.add("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*\\d.*")) {
            errors.add("Password must contain at least one digit");
        }

        if (!password.matches(".*[@#$%^&+=/!?.\\-_~].*")) {
            errors.add("Password must contain at least one special character");
        }

        return errors;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.clearSession();
    }
    @Override
    protected void onStop() {

        super.onStop();
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        usernameEditText = findViewById(R.id.username);
        firstNameEditText = findViewById(R.id.firstname);
        lastNameEditText = findViewById(R.id.lastname);
        phoneNumberEditText = findViewById(R.id.phonenumber);
        birthdateEditText = findViewById(R.id.birthdate);

        emailEditText.setText("");
        passwordEditText.setText("");
        usernameEditText.setText("");
        firstNameEditText.setText("");
        lastNameEditText.setText("");
        phoneNumberEditText.setText("");
        birthdateEditText.setText("");
    }
    private void showDatePickerDialog() {
        // Get current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(SignUp.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Update EditText with selected date
                        birthdateEditText.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, dayOfMonth);

        // Show the dialog
        datePickerDialog.show();
    }
}

