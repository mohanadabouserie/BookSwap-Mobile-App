package com.example.bookswap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class preferences extends AppCompatActivity {

    private List<String> selectedGenres;
    private FirebaseFirestore db;
    private CollectionReference usersCollection;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        db = FirebaseFirestore.getInstance();
        usersCollection = db.collection("users");
        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        selectedGenres = new ArrayList<>();

        // Get references to all genre buttons
        Button fantasy = findViewById(R.id.fantasy);
        Button sci_fi = findViewById(R.id.sci_fi);
        Button dystopian = findViewById(R.id.dystopian);
        Button action_adventure = findViewById(R.id.action_adventure);
        Button mystery = findViewById(R.id.mystery);
        Button horror = findViewById(R.id.horror);
        Button thriller_suspense = findViewById(R.id.thriller_suspense);
        Button historical = findViewById(R.id.historical);
        Button graphic_novel = findViewById(R.id.graphic_novel);
        Button short_story = findViewById(R.id.short_story);
        Button young_adult = findViewById(R.id.young_adult);
        Button new_adult = findViewById(R.id.new_adult);
        Button childrens = findViewById(R.id.childrens);
        Button non_fiction = findViewById(R.id.non_fiction);
        Button auto_biography = findViewById(R.id.auto_biography);
        Button self_help = findViewById(R.id.self_help);
        Button historical_fiction = findViewById(R.id.historical_fiction);
        Button travel = findViewById(R.id.travel);
        Button true_crime = findViewById(R.id.true_crime);
        Button humor = findViewById(R.id.humor);
        Button academic = findViewById(R.id.academic);
        Button romance = findViewById(R.id.romance);
        Button contemporary_fiction = findViewById(R.id.contemporary_fiction);
        Button done_button = findViewById(R.id.done_button);

        // Set click listeners for all genre buttons
        fantasy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGenreButton(fantasy, "Fantasy");
            }
        });

        sci_fi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGenreButton(sci_fi, "Sci-fi");
            }
        });

        dystopian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGenreButton(dystopian, "Dystopian");
            }
        });

        action_adventure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGenreButton(action_adventure, "Action & Adventure");
            }
        });

        mystery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGenreButton(mystery, "Mystery");
            }
        });

        horror.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGenreButton(horror, "Horror");
            }
        });

        thriller_suspense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGenreButton(thriller_suspense, "Thriller & Suspense");
            }
        });

        historical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGenreButton(historical, "Historical");
            }
        });

        graphic_novel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGenreButton(graphic_novel, "Graphic Novel");
            }
        });

        short_story.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGenreButton(short_story, "Short Story");
            }
        });

        young_adult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGenreButton(young_adult, "Young Adult");
            }
        });

        new_adult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGenreButton(new_adult, "New Adult");
            }
        });

        childrens.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGenreButton(childrens, "Children's");
            }
        });

        non_fiction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGenreButton(non_fiction, "Non-Fiction");
            }
        });

        auto_biography.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGenreButton(auto_biography, "Auto-Biography");
            }
        });

        self_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGenreButton(self_help, "Self-Help");
            }
        });

        historical_fiction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGenreButton(historical_fiction, "Historical Fiction");
            }
        });

        travel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGenreButton(travel, "Travel");
            }
        });

        true_crime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGenreButton(true_crime, "True Crime");
            }
        });

        humor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGenreButton(humor, "Humor");
            }
        });

        academic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGenreButton(academic, "Academic");
            }
        });

        romance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleGenreButton(romance, "Romance");

            }
        });

        contemporary_fiction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleGenreButton(contemporary_fiction, "Contemporary Fiction");

            }
        });

        done_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedGenres.size() < 3)
                {
                    Toast.makeText(preferences.this, "Choose at least 3 genres", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    saveSelectedGenres();
                    Intent i = new Intent(preferences.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                }
            }
        });


    }

    private void toggleGenreButton(Button button, String genre) {
        if (selectedGenres.contains(genre)) {
            selectedGenres.remove(genre);
            button.setSelected(false);
        } else {
            selectedGenres.add(genre);
            button.setSelected(true);
        }
    }

    private void saveSelectedGenres() {
        DocumentReference userDocument = usersCollection.document(username);

        userDocument.update("genres", selectedGenres)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Genres saved successfully
                        Toast.makeText(preferences.this, "Selected genres saved", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to save genres
                        Toast.makeText(preferences.this, "Failed to save selected genres: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
