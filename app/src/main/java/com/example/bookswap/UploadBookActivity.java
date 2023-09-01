package com.example.bookswap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class UploadBookActivity extends AppCompatActivity {

    // initialize variables
    String UserName;
    TextView genresListTextView;
    EditText isbnEditText;
    boolean[] selectedGenre;
    ArrayList<Integer> langList = new ArrayList<>();
    String[] langArray = {
            "Fantasy", "Sci-fi", "Dystopian", "Action & Adventure", "Mystery", "Horror",
            "Thriller & Suspense", "Historical", "Romance", "Contemporary Fiction",
            "Graphic Novel", "Short Story", "Young Adult", "New Adult", "Children's",
            "Non-Fiction", "Autobiography", "Self-help", "Historical Fiction", "Travel",
            "True Crime", "Humor", "Academic"
    };

    FetchBookInfoTask fetchBookInfoTask;
    FirebaseFirestore db;
    CollectionReference booksCollection;
    CollectionReference usersCollection;
    StorageReference storageReference;
    Button uploadImg1, uploadImg2, uploadImg3;
    TextView Image1, Image2, Image3;
    Uri image1_uri, image2_uri, image3_uri;
    ImageView cover_image;
    APIHandler apiHandler = new APIHandler();;
    private static final int PICK_IMAGE_REQUEST = 1;

    private int buttonClicked = 0;

    private void selectImage(int buttonNumber) {
        buttonClicked = buttonNumber;

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            if (buttonClicked == 1) {
                Image1.setText(imageUri.toString());
                image1_uri = imageUri;
            } else if (buttonClicked == 2) {
                Image2.setText(imageUri.toString());
                image2_uri = imageUri;
            } else if (buttonClicked == 3) {
                Image3.setText(imageUri.toString());
                image3_uri = imageUri;
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_book);

        db = FirebaseFirestore.getInstance();
        booksCollection = db.collection("books");
        usersCollection = db.collection("user");
        Intent i = getIntent();
        uploadImg1 = findViewById(R.id.uploadImg1);
        uploadImg2 = findViewById(R.id.uploadImg2);
        uploadImg3 = findViewById(R.id.uploadImg3);

        Image1 = findViewById(R.id.Image1);
        Image2 = findViewById(R.id.Image2);
        Image3 = findViewById(R.id.Image3);


        SessionManager sessionManager = SessionManager.getInstance();
        UserName = sessionManager.getUsername();

        // assign variables
        genresListTextView = findViewById(R.id.genre);
        isbnEditText = findViewById(R.id.isbn);

        // initialize selected genre array
        selectedGenre = new boolean[langArray.length];

        uploadImg1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(1);
            }
        });

        uploadImg2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(2);
            }
        });

        uploadImg3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(3);
            }
        });
        genresListTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Initialize alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(UploadBookActivity.this);

                // Set title
                builder.setTitle("Select Genre");

                // Set dialog non-cancelable
                builder.setCancelable(false);

                builder.setMultiChoiceItems(langArray, selectedGenre, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        // Check condition
                        if (b) {
                            // When checkbox selected
                            // Add position to lang list
                            langList.add(i);
                            // Sort array list
                            Collections.sort(langList);
                        } else {
                            // When checkbox unselected
                            // Remove position from langList
                            langList.remove(Integer.valueOf(i));
                        }
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Initialize string builder
                        StringBuilder stringBuilder = new StringBuilder();
                        // Use for loop
                        for (int j = 0; j < langList.size(); j++) {
                            // Concatenate array value
                            stringBuilder.append(langArray[langList.get(j)]);
                            // Check condition
                            if (j != langList.size() - 1) {
                                // When j value is not equal to lang list size - 1
                                // Add comma
                                stringBuilder.append(", ");
                            }
                        }
                        // Set text on textView
                        genresListTextView.setText(stringBuilder.toString());
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Dismiss dialog
                        dialogInterface.dismiss();
                    }
                });

                builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Use for loop
                        for (int j = 0; j < selectedGenre.length; j++) {
                            // Remove all selection
                            selectedGenre[j] = false;
                            // Clear language list
                            langList.clear();
                            // Clear textView value
                            genresListTextView.setText("");
                        }
                    }
                });

                // Show dialog
                builder.show();
            }
        });

        isbnEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String isbn = charSequence.toString().trim();

                if (!isbn.isEmpty()) {
                    if (fetchBookInfoTask != null && !fetchBookInfoTask.isCancelled()) {
                        fetchBookInfoTask.cancel(true);
                    }

                    fetchBookInfoTask = new FetchBookInfoTask();
                    fetchBookInfoTask.execute(isbn);
                } else {
                    resetBookInfo();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
//                String isbn = editable.toString().trim();
//
//                if (!isbn.isEmpty()) {
//                    if (fetchBookInfoTask != null && !fetchBookInfoTask.isCancelled()) {
//                        fetchBookInfoTask.cancel(true);
//                    }
//
//                    fetchBookInfoTask = new FetchBookInfoTask();
//                    fetchBookInfoTask.execute(isbn);
//                } else {
//                    resetBookInfo();
//                }
            }
        });

        Button doneButton = findViewById(R.id.DoneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    saveBookToFirebase();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void resetBookInfo() {
        TextView titleTextView = findViewById(R.id.title);
        TextView authorTextView = findViewById(R.id.author);

        titleTextView.setText("");
        authorTextView.setText("");
        cover_image.setImageResource(R.drawable.book_cover);
    }

    private void saveBookToFirebase() throws IOException {
        String isbn = isbnEditText.getText().toString().trim();
        String title = ((TextView) findViewById(R.id.title)).getText().toString().trim();
        String author = ((TextView) findViewById(R.id.author)).getText().toString().trim();
        String genres = genresListTextView.getText().toString().trim();
        String image_1 = Image1.getText().toString();
        String image_2 = Image2.getText().toString();
        String image_3 = Image3.getText().toString();

        if (isbn.isEmpty() || title.isEmpty() || author.isEmpty() || genres.isEmpty() || image_1.isEmpty() || image_2.isEmpty() || image_3.isEmpty()) {
            Toast.makeText(UploadBookActivity.this, "Please fill in all the book information", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(UploadBookActivity.this);
        progressDialog.setMessage("Uploading book...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ArrayList<Task<Uri>> uploadTasks = new ArrayList<>();
        ArrayList<String> image_URLs = new ArrayList<>();

        // Upload image 1
        StorageReference storageReference1 = FirebaseStorage.getInstance().getReference("books_images/" + UserName + "/" + isbn + "/" + "Image_1");
        uploadTasks.add(storageReference1.putFile(image1_uri).continueWithTask(task -> storageReference1.getDownloadUrl()));

        // Upload image 2
        StorageReference storageReference2 = FirebaseStorage.getInstance().getReference("books_images/" + UserName + "/" + isbn + "/" + "Image_2");
        uploadTasks.add(storageReference2.putFile(image2_uri).continueWithTask(task -> storageReference2.getDownloadUrl()));

        // Upload image 3
        StorageReference storageReference3 = FirebaseStorage.getInstance().getReference("books_images/" + UserName + "/" + isbn + "/" + "Image_3");
        uploadTasks.add(storageReference3.putFile(image3_uri).continueWithTask(task -> storageReference3.getDownloadUrl()));

        Task<List<Uri>> allTasks = Tasks.whenAllSuccess(uploadTasks);

        allTasks.addOnSuccessListener(uriList -> {
            for (Uri uri : uriList) {
                image_URLs.add(uri.toString());
            }

            // Generate the book's primary key using the username and ISBN
            String bookKey = UserName + "_" + isbn;

            // Create a new book object with the provided information
            Book book = new Book(bookKey, isbn, title, author, genres, apiHandler.Imagepath, UserName, image_URLs);
            book.setTitle_lowercase(title.toLowerCase());
            book.setAuthor_lowercase(author.toLowerCase());

            // Save the book to the "books" collection in Firebase
            DocumentReference userDocument = db.collection("users").document(UserName);

            userDocument.update("mybooks", FieldValue.arrayUnion(bookKey))
                    .addOnSuccessListener(aVoid -> {
                        // Book key added to user's mybooks array
                    })
                    .addOnFailureListener(e -> {
                        // Failed to add book key to user's mybooks array
                    });

            booksCollection.document(bookKey).set(book)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss(); // Dismiss the progress dialog
                        Toast.makeText(UploadBookActivity.this, "Book uploaded successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss(); // Dismiss the progress dialog
                        Toast.makeText(UploadBookActivity.this, "Failed to upload book", Toast.LENGTH_SHORT).show();
                    });
        });

        allTasks.addOnFailureListener(e -> {
            progressDialog.dismiss(); // Dismiss the progress dialog
            // Handle failure
        });
    }




    private class FetchBookInfoTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... isbns) {
            if (isCancelled()) {
                return null;
            }
            String isbn = isbns[0];

            if (!isbn.isEmpty()) {

                String bookData = apiHandler.makeHTTPRequest(apiHandler.BuildURL(isbn));

                if (bookData != null) {
                    return bookData;
                } else {
                    TextView titleTextView = findViewById(R.id.title);
                    TextView authorTextView = findViewById(R.id.author);

                    titleTextView.setText("");
                    authorTextView.setText("");
                    cover_image.setImageResource(R.drawable.book_cover);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String bookData) {
            if (bookData != null) {
                try {
                    apiHandler.ParseJSON(bookData);

                    TextView titleTextView = findViewById(R.id.title);
                    TextView authorTextView = findViewById(R.id.author);
                    cover_image = findViewById(R.id.cover_image);

                    Picasso.get().load(apiHandler.Imagepath).into(cover_image);
                    titleTextView.setText(apiHandler.Title);
                    authorTextView.setText(apiHandler.Authors);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

class Book {
    private String bookKey;
    private String isbn;
    private String title;
    private String author;
    private String genres;
    private ArrayList<String> imageUrls;
    private String cover_page;
    private String book_username;
    private boolean requested;
    private boolean delivered;
    private boolean accepted;
    private boolean received;
    private String requester;
    private String author_lowercase;
    private String title_lowercase;



    public Book(String bookKey, String isbn, String title, String author, String genres, String cover_page, String book_username, ArrayList<String> imageUrls) {
        this.bookKey = bookKey;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.genres = genres;
        this.cover_page = cover_page;
        this.book_username = book_username;
        this.imageUrls = imageUrls;
        this.requester = "";
        this.requested = false;
        this.delivered = false;
        this.received = false;
        this.accepted = false;
        this.author_lowercase = author.toLowerCase();
        this.title_lowercase = title.toLowerCase();
    }

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }

    public String getAuthor_lowercase() {
        return author_lowercase;
    }

    public void setAuthor_lowercase(String author_lowercase) {
        this.author_lowercase = author_lowercase;
    }

    public String getTitle_lowercase() {
        return title_lowercase;
    }

    public void setTitle_lowercase(String title_lowercase) {
        this.title_lowercase = title_lowercase;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }
    public boolean isRequested() {
        return requested;
    }

    public void setRequested(boolean requested) {
        this.requested = requested;
    }

    public String getBook_username() {
        return book_username;
    }

    public void setBook_username(String book_username) {
        this.book_username = book_username;
    }

    // Add the getter and setter methods for imageUrls
    public ArrayList<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(ArrayList<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getBookKey() {
        return bookKey;
    }

    public void setBookKey(String bookKey) {
        this.bookKey = bookKey;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public String getCover_page() {
        return cover_page;
    }

    public void setCover_page(String cover_page) {
        this.cover_page = cover_page;
    }
}