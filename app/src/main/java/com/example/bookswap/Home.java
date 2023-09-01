package com.example.bookswap;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
import android.widget.LinearLayout;
import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import android.util.Log;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.List;
import retrofit2.http.GET;



public class Home extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private RecyclerView bookRecyclerView;
    private BookAdapter bookAdapter;
    private List<TheBook> suggestedBooks;
    private TextView welcomeTextView;
    private TextView genre;
    private String username;
    private FirebaseFirestore db;
    BottomNavigationView bottomNavigationView;
    private LinearLayout genreLayoutContainer;
    //private static final int MAX_GENRES_TO_DISPLAY = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        welcomeTextView = findViewById(R.id.welcome_text);
        genre = findViewById(R.id.explore);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.home);

        SessionManager sessionManager = SessionManager.getInstance();
        username = sessionManager.getUsername();
        db = FirebaseFirestore.getInstance();
        loadUserDataFromDatabase();

        bookRecyclerView = findViewById(R.id.book_recycler_view);
        bookRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        suggestedBooks = new ArrayList<>();
        bookAdapter = new BookAdapter(suggestedBooks, this);
        bookRecyclerView.setAdapter(bookAdapter);
        genreLayoutContainer = findViewById(R.id.genre_layout_container);



        db = FirebaseFirestore.getInstance();
        loadBooksForGenre(genre.getText().toString().replace("Explore ", ""), suggestedBooks, bookAdapter, R.id.book_recycler_view);

        loadRandomQuote();

        Button viewAllBooksButton = findViewById(R.id.view_all_books_button);
        viewAllBooksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, AllBooksActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.home);
    }

    private void loadUserDataFromDatabase() {
        db.collection("users").document(username).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String firstName = document.getString("firstName");
                    welcomeTextView.setText("Happy Reading, " + firstName + "!");

                    // Retrieve genres array from the "users" collection
                    List<String> genres = (List<String>) document.get("genres");
                    if (genres != null && !genres.isEmpty()) {
                        genreLayoutContainer.removeAllViews(); // Remove any existing genre layouts

                        // Shuffle the genres if you want, but it's not necessary
                        // Collections.shuffle(genres);

                        for (String genre : genres) {
                            addGenreLayout(genre);
                        }
                    }
                }
            }
        });
    }

    private List<Integer> genreLayoutIds = new ArrayList<>();

    private void addGenreLayout(String genre) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View genreLayout = inflater.inflate(R.layout.item_genre, genreLayoutContainer, false);
        TextView exploreTextView = genreLayout.findViewById(R.id.explore);
        exploreTextView.setText("Explore " + genre);

        RecyclerView bookRecyclerView = genreLayout.findViewById(R.id.book_recycler_view);
        bookRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<TheBook> suggestedBooks = new ArrayList<>();
        BookAdapter bookAdapter = new BookAdapter(suggestedBooks, this);
        bookRecyclerView.setAdapter(bookAdapter);

        int genreLayoutId = View.generateViewId(); // Generate a unique ID for the genre layout
        genreLayout.setId(genreLayoutId); // Set the generated ID to the genre layout
        genreLayoutIds.add(genreLayoutId); // Store the genre layout ID

        loadBooksForGenre(genre, suggestedBooks, bookAdapter, genreLayoutId);

        // Add the genre layout to the container
        genreLayoutContainer.addView(genreLayout);
    }

    private void loadBooksForGenre(String genre, List<TheBook> suggestedBooks, BookAdapter bookAdapter, int genreLayoutId) {
        db.collection("books")
                .orderBy("title", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            suggestedBooks.clear(); // Clear the list before adding new books

                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                TheBook book = document.toObject(TheBook.class);
                                if (book != null) {
                                    // Check if the document contains the necessary fields
                                    if (document.contains("bookKey")
                                            && document.contains("title")
                                            && document.contains("author")
                                            && document.contains("cover_page")
                                            && document.contains("book_username")
                                            && document.contains("genres")) {

                                        // Filter books for the selected genre
                                        String genresString = book.getGenres();
                                        String[] genres = genresString.split(", ");
                                        for (String bookGenre : genres) {
                                            if (bookGenre.equals(genre)) {
                                                // Retrieve bookkey and other details
                                                String bookkey = document.getString("bookKey");
                                                String title = document.getString("title");
                                                String cover = document.getString("cover_page");
                                                String author = document.getString("author");
                                                String user = document.getString("book_username");
                                                boolean requested = document.getBoolean("requested");

                                                // Skip the book if it belongs to the current user or if it is requested
                                                if (user != null && user.equals(username) || requested) {
                                                    continue;
                                                }

                                                // Create the TheBook object and set its values
                                                TheBook theBook = new TheBook(bookkey, title, author, cover, user, genresString);
                                                suggestedBooks.add(theBook);
                                                break;
                                            }
                                        }
                                    } else {
                                        Log.d("Home", "Invalid document format for book: " + document.getId());
                                    }
                                }
                            }

                            bookAdapter.notifyDataSetChanged();

                            // Check if no books are found for the genre and hide the genre layout accordingly
                            if (suggestedBooks.isEmpty()) {
                                hideGenreLayout(genreLayoutId);
                            } else {
                                showGenreLayout(genreLayoutId);
                            }
                        } else {
                            // An error occurred while fetching the books
                            hideGenreLayout(genreLayoutId);
                        }
                    } else {
                        // An error occurred while fetching the books
                        hideGenreLayout(genreLayoutId);
                    }
                });
    }


    private void showGenreLayout(int genreLayoutId) {
        View genreLayout = findViewById(genreLayoutId);
        RecyclerView recyclerView = genreLayout.findViewById(R.id.book_recycler_view);
        TextView exploreTextView = genreLayout.findViewById(R.id.explore);
        View lineView = genreLayout.findViewById(R.id.line);

        if (recyclerView != null && exploreTextView != null) {
            recyclerView.setVisibility(View.VISIBLE);
            exploreTextView.setVisibility(View.VISIBLE);
            lineView.setVisibility(View.VISIBLE);
        }
    }

    private void hideGenreLayout(int genreLayoutId) {
        View genreLayout = findViewById(genreLayoutId);
        RecyclerView recyclerView = genreLayout.findViewById(R.id.book_recycler_view);
        TextView exploreTextView = genreLayout.findViewById(R.id.explore);
        View lineView = genreLayout.findViewById(R.id.line);

        if (recyclerView != null && exploreTextView != null) {
            recyclerView.setVisibility(View.GONE);
            exploreTextView.setVisibility(View.GONE);
            lineView.setVisibility(View.GONE);
        }
    }

    private void loadRandomQuote() {
        List<String> quotes = new ArrayList<>();
        quotes.add("Love is or it ain't. Thin love ain't love at all.");
        quotes.add("I am not afraid of storms, for I am learning how to sail my ship.");
        quotes.add("There is always something left to love.");
        quotes.add("They say nothing lasts forever but they're just scared it will last longer than they can love it.");

        List<String> authors = new ArrayList<>();
        authors.add("Toni Morrison");
        authors.add("Louisa May Alcott");
        authors.add("Gabriel García Márquez");
        authors.add("Ocean Vuong");

        Random random = new Random();
        int index = random.nextInt(quotes.size());
        String randomQuote = quotes.get(index);
        String randomAuthor = authors.get(index);

        TextView quoteTextView = findViewById(R.id.quote);
        TextView quoteAuthorTextView = findViewById(R.id.quote_author);
        quoteTextView.setText(randomQuote);
        quoteAuthorTextView.setText("- " + randomAuthor);
    }

//    public interface QuoteApiService {
//        @GET("api/quotes")
//        Call<List<Quote>> getQuotes();
//    }
//
//    private void loadRandomQuote() {
//        // Create a Retrofit instance for making HTTP requests
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://zenquotes.io/") // Base URL of the API
//                .addConverterFactory(GsonConverterFactory.create()) // Use Gson for JSON parsing
//                .build();
//
//        // Create the service that defines the API endpoints
//        QuoteApiService quoteApiService = retrofit.create(QuoteApiService.class);
//
//        // Make the HTTP request to fetch the quotes
//        Call<List<Quote>> call = quoteApiService.getQuotes();
//        call.enqueue(new Callback<List<Quote>>() {
//            @Override
//            public void onResponse(Call<List<Quote>> call, Response<List<Quote>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    List<Quote> quotes = response.body();
//
//                    // Get a random quote from the list of quotes
//                    Random random = new Random();
//                    int index = random.nextInt(quotes.size());
//                    Quote randomQuote = quotes.get(index);
//
//                    // Update the TextViews with the random quote
//                    TextView quoteTextView = findViewById(R.id.quote);
//                    TextView quoteAuthorTextView = findViewById(R.id.quote_author);
//                    quoteTextView.setText(randomQuote.getQuote());
//                    quoteAuthorTextView.setText("- " + randomQuote.getAuthor());
//
//                    Log.d("Quote API", "Response: " + response.body());
//
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<Quote>> call, Throwable t) {
//                Log.e("Quote API", "Failed to fetch quotes: " + t.getMessage());
//            }
//        });
//    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.home) {
            //Intent i = new Intent(MyProfile.this, MyProfile.class);
            //startActivity(i);
            return true;
        } else if (id == R.id.search) {
            Intent i = new Intent(Home.this, Search.class);
            startActivity(i);
            return true;
        } else if (id == R.id.library) {
            Intent i = new Intent(Home.this, LibraryActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.profile) {
            Intent i = new Intent(Home.this, MyProfile.class);
            startActivity(i);
            return true;
        }
        return false;
    }
}

class TheBook {
    private String bookkey;
    private String title;
    private String author;
    private String cover_page;
    private String user;

    private String genres;

    public TheBook() {
        // Default no-argument constructor required for Firestore serialization
    }
    public TheBook(String bookkey, String title, String author, String cover_page, String user, String genres) {
        this.bookkey = bookkey;
        this.title = title;
        this.author = author;
        this.cover_page = cover_page;
        this.user = user;
        this.genres = genres;
    }

    // Getters and setters for the properties

    public String getBookKey() {
        return bookkey;
    }

    public void setBookKey(String bookkey) {
        this.bookkey = bookkey;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
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

    public String getCoverPage() {
        return cover_page;
    }

    public void setCoverPage(String cover_page) {
        this.cover_page = cover_page;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }
    public TheBook(String title, String author, String cover_page) {
        this.title = title;
        this.cover_page = cover_page;
        this.author = author;
    }
}

class Quote {
    private String q;
    private String a;

    public String getQuote() {
        return q;
    }

    public String getAuthor() {
        return a;
    }
}

class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {
    private List<TheBook> bookList;
    private Context context;

    public BookAdapter(List<TheBook> bookList, Context context) {
        this.bookList = bookList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TheBook book = bookList.get(position);

        holder.titleTextView.setText(book.getTitle());
        holder.authorTextView.setText(book.getAuthor());

        String coverPageUrl = book.getCoverPage();
        if (coverPageUrl != null && !coverPageUrl.isEmpty()) {
            // Load the cover page image using Picasso if the URL is not null or empty
            Picasso.get().load(coverPageUrl).into(holder.coverImageView);
        } else {
            // If the cover page URL is null or empty, set a placeholder image
            holder.coverImageView.setImageResource(R.drawable.book_cover_2); // Replace 'placeholder_image' with the ID of your placeholder image resource
        }

        // Set OnClickListener for the book item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the click event here, open RequestBook activity
                Intent intent = new Intent(context, RequestBook.class);
                // write log to see if the bookkey is correct
                Log.d("BookAdapter", "Book key: " + book.getBookKey());
                intent.putExtra("bookkey", book.getBookKey());
                intent.putExtra("username", book.getUser());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView authorTextView;
        public ImageView coverImageView;



        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            authorTextView = itemView.findViewById(R.id.author_text_view);
            coverImageView = itemView.findViewById(R.id.book_cover2);
        }
    }
}