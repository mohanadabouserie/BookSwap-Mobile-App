package com.example.bookswap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Search extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener
{
    // creating variables for our request queue,
    // array list, progressbar, edittext,
    // image button and our recycler view.

    private FirebaseFirestore db;
    private CollectionReference usersCollection;
    private RequestQueue mRequestQueue;

    BottomNavigationView bottomNavigationView;
    private ArrayList<BookInfo> bookInfoArrayList;
    //private ArrayList<BookInfo> bookList;
    private ProgressBar progressBar;
    private EditText searchEdt;
    private ImageButton searchBtn;
    private String user_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        SessionManager sessionManager = SessionManager.getInstance();
        user_name = sessionManager.getUsername();

        db = FirebaseFirestore.getInstance();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.search);
        // initializing our views.
        progressBar = findViewById(R.id.idLoadingPB);
        searchEdt = findViewById(R.id.idEdtSearchBooks);
        searchBtn = findViewById(R.id.idBtnSearch);

        // initializing the bookList variable.
        //bookList = new ArrayList<>();

        // initializing on click listener for our button.
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                // checking if our edittext field is empty or not.
                if (searchEdt.getText().toString().isEmpty()) {
                    searchEdt.setError("Please enter search query");
                    return;
                }
                // if the search query is not empty then we are
                // calling get book info method to load all
                // the books from the API.
                searchBooks(searchEdt.getText().toString());
                //getBooksInfo(searchEdt.getText().toString());
            }
        });
    }

    private void searchBooks(String searchTerm) {
        ArrayList<BookInfo> bookList = new ArrayList<>();
        db.collection("books")
                .orderBy("title_lowercase")
                .whereGreaterThanOrEqualTo("title_lowercase", searchTerm.toLowerCase())
                .whereLessThanOrEqualTo("title_lowercase", searchTerm.toLowerCase() + "\uf8ff")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("BookData", "Book Found for the search term:" +searchTerm);
                            //ArrayList<BookInfo> bookList = new ArrayList<>(); // Create a new list to store the books.
                            //bookList.clear();
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d("BookData", "Test" +searchTerm);
                                String isbn = document.getString("isbn");
                                String title = document.getString("title");
                                String cover = document.getString("cover_page");
                                String user = document.getString("book_username");
                                String bookkey = document.getString("bookKey");
                                String genres = document.getString("genres");
                                String author = document.getString("author");
                                boolean requested = document.getBoolean("requested");
                                if(!requested && !user_name.equals(user)) {
                                    BookInfo bookInfo = new BookInfo(isbn, title, cover, user, bookkey, genres, author);
                                    bookList.add(bookInfo);
                                    Log.d("BookData", "ISBN: " + isbn + ", Title: " + title + ", Cover: " + cover + ", User: " + user);
                                }
                            }
                            // Check if bookList is empty or not.
                            if (bookList.isEmpty()) {
                                Log.d("BookData", "No books found for the search term: " + searchTerm);
                            }

                            // Set up RecyclerView and its adapter only once.
                            BookAdapter adapter = new BookAdapter(bookList, Search.this);
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(Search.this, RecyclerView.VERTICAL, false);
                            RecyclerView mRecyclerView = findViewById(R.id.idRVBooks);
                            mRecyclerView.setLayoutManager(linearLayoutManager);
                            mRecyclerView.setAdapter(adapter);
                            progressBar.setVisibility(View.GONE);

                        } else {
                            Toast.makeText(Search.this, "Error searching books", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Search.this, "Error searching books", Toast.LENGTH_SHORT).show();
                    }
                });
        db.collection("books")
                .orderBy("author_lowercase")
                .whereGreaterThanOrEqualTo("author_lowercase", searchTerm.toLowerCase())
                .whereLessThanOrEqualTo("author_lowercase", searchTerm.toLowerCase() + "\uf8ff")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("BookData", "Book Found for the search term:" +searchTerm);
                            //ArrayList<BookInfo> bookList = new ArrayList<>(); // Create a new list to store the books.
                            //bookList.clear();
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d("BookData", "Test" +searchTerm);
                                String isbn = document.getString("isbn");
                                String title = document.getString("title");
                                String cover = document.getString("cover_page");
                                String user = document.getString("book_username");
                                String bookkey = document.getString("bookKey");
                                String genres = document.getString("genres");
                                String author = document.getString("author");
                                boolean requested = document.getBoolean("requested");
                                if(!requested  && !user_name.equals(user)) {
                                    BookInfo bookInfo = new BookInfo(isbn, title, cover, user, bookkey, genres, author);

                                    bookList.add(bookInfo);
                                    Log.d("BookData", "ISBN: " + isbn + ", Title: " + title + ", Cover: " + cover + ", User: " + user);
                                }
                            }
                            // Check if bookList is empty or not.
                            if (bookList.isEmpty()) {
                                Log.d("BookData", "No books found for the search term: " + searchTerm);
                            }

                            // Set up RecyclerView and its adapter only once.
                            BookAdapter adapter = new BookAdapter(bookList, Search.this);
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(Search.this, RecyclerView.VERTICAL, false);
                            RecyclerView mRecyclerView = findViewById(R.id.idRVBooks);
                            mRecyclerView.setLayoutManager(linearLayoutManager);
                            mRecyclerView.setAdapter(adapter);
                            progressBar.setVisibility(View.GONE);

                        } else {
                            Toast.makeText(Search.this, "Error searching books", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Search.this, "Error searching books", Toast.LENGTH_SHORT).show();
                    }
                });
        db.collection("books")
                .whereEqualTo("isbn", searchTerm)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("BookData", "Book Found for the search term:" +searchTerm);
                            //ArrayList<BookInfo> bookList = new ArrayList<>(); // Create a new list to store the books.
                            //bookList.clear();
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d("BookData", "Test" +searchTerm);
                                String isbn = document.getString("isbn");
                                String title = document.getString("title");
                                String cover = document.getString("cover_page");
                                String user = document.getString("book_username");
                                String bookkey = document.getString("bookKey");
                                String genres = document.getString("genres");
                                String author = document.getString("author");
                                boolean requested = document.getBoolean("requested");
                                if(!requested  && !user_name.equals(user)) {
                                    BookInfo bookInfo = new BookInfo(isbn, title, cover, user, bookkey, genres, author);

                                    bookList.add(bookInfo);
                                    Log.d("BookData", "ISBN: " + isbn + ", Title: " + title + ", Cover: " + cover + ", User: " + user);
                                }
                            }
                            // Check if bookList is empty or not.
                            if (bookList.isEmpty()) {
                                Log.d("BookData", "No books found for the search term: " + searchTerm);
                            }

                            // Set up RecyclerView and its adapter only once.
                            BookAdapter adapter = new BookAdapter(bookList, Search.this);
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(Search.this, RecyclerView.VERTICAL, false);
                            RecyclerView mRecyclerView = findViewById(R.id.idRVBooks);
                            mRecyclerView.setLayoutManager(linearLayoutManager);
                            mRecyclerView.setAdapter(adapter);
                            progressBar.setVisibility(View.GONE);

                        } else {
                            Toast.makeText(Search.this, "Error searching books", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Search.this, "Error searching books", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.search);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.home) {
            Intent i = new Intent(Search.this, Home.class);
            startActivity(i);
            return true;
        } else if (id == R.id.search) {
            //Intent i = new Intent(Search.this, Search.class);
            //startActivity(i);
            return true;
        } else if (id == R.id.library) {
            Intent i = new Intent(Search.this, LibraryActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.profile) {
            Intent i = new Intent(Search.this, MyProfile.class);
            startActivity(i);
            return true;
        }
        return false;
    }
    class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder>{
        // creating variables for arraylist and context.
        private ArrayList<BookInfo> bookInfoArrayList;
        private Context mcontext;

        // creating constructor for array list and context.
        public BookAdapter(ArrayList<BookInfo> bookInfoArrayList, Context mcontext) {
            this.bookInfoArrayList = bookInfoArrayList;
            this.mcontext = mcontext;
        }

        @NonNull
        @Override
        public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // inflating our layout for item of recycler view item.
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_rv_item, parent, false);
            return new BookViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {

            // inside on bind view holder method we are
            // setting our data to each UI component.
            BookInfo bookInfo = bookInfoArrayList.get(position);
            holder.nameTV.setText(bookInfo.getTitle());
            holder.authorTV.setText(bookInfo.getAuthor());
            holder.isbnTV.setText(bookInfo.getISBN());


            // below line is use to set image from URL in our image view.
            Picasso.get().load(bookInfo.getCover()).into(holder.bookIV);

            // below line is use to add on click listener for our item of recycler view.
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // inside on click listener method we are calling a new activity
                    // and passing all the data of that item in next intent.
                    Intent i = new Intent(mcontext, RequestBook.class);
                    i.putExtra("bookkey", bookInfo.getBookkey());
                    i.putExtra("username", bookInfo.getUser());


                    // after passing that data we are
                    // starting our new  intent.
                    mcontext.startActivity(i);
                }
            });
        }

        @Override
        public int getItemCount() {
            // inside get item count method we
            // are returning the size of our array list.
            return bookInfoArrayList.size();
        }

        public class BookViewHolder extends RecyclerView.ViewHolder {

            // below line is use to initialize
            // our text view and image views.
            TextView nameTV, isbnTV, authorTV;
            ImageView bookIV;

            public BookViewHolder(View itemView) {
                super(itemView);
                nameTV = itemView.findViewById(R.id.idTVBookTitle);
                //userTV = itemView.findViewById(R.id.idTVUser);
                authorTV = itemView.findViewById(R.id.idTVAuthor);
                isbnTV = itemView.findViewById(R.id.idTVISBN);
                bookIV = itemView.findViewById(R.id.idIVbook);
            }
        }

    }

    class BookInfo {
        // creating string, int and array list
        // variables for our book details
        private String isbn;
        private String title;
        private String cover;
        private String user;
        private String bookkey;
        private String genres;
        private String author;


        // creating getter and setter methods

        public String getISBN() {
            return isbn;
        }

        public void setISBN(String isbn) {
            this.isbn = isbn;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCover() {
            return cover;
        }

        public void setCover(String cover) {
            this.cover = cover;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getBookkey() {
            return bookkey;
        }

        public void setBookkey(String bookkey) {
            this.bookkey = bookkey;
        }

        public String getGenres() {
            return genres;
        }

        public void setGenres(String genres) {
            this.genres = genres;
        }

        public String getAuthor() {
            return author;
        }
        public void setAuthor(String author) {
            this.author = author;
        }


        // creating a constructor class for our BookInfo
        public BookInfo(String isbn, String title, String cover, String user, String bookkey, String genres, String author) {
            this.isbn = isbn;
            this.title = title;
            this.cover = cover;
            this.user = user;
            this.bookkey = bookkey;
            this.genres= genres;
            this.author = author;

        }
    }
}