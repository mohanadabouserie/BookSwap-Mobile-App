package com.example.bookswap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MyRequestActivity extends AppCompatActivity {
    ListView listview;
    ArrayList<Book_item> BooksDetails;
    String username;
    ArrayList<String> myRequests;

    CustomAdapter adapter;
    private String clickedBookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_request);
        SessionManager sessionManager = SessionManager.getInstance();
        username = sessionManager.getUsername();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(username).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        myRequests = (ArrayList<String>) document.get("myrequests");
                        if (myRequests!= null && !myRequests.isEmpty()) {
                            retrieveBookDetails(db, myRequests);
                        }
                    }
                }
            }
        });

        listview = findViewById(R.id.RequestedBooks);
        BooksDetails = new ArrayList<>();
        adapter = new CustomAdapter(this, BooksDetails);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Book_item clickedBook = (Book_item) adapterView.getItemAtPosition(i);
                clickedBookId = clickedBook.getBookId();
                db.collection("books").document(clickedBookId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot bookDocument = task.getResult();
                            if (bookDocument.exists()) {
                                Boolean accepted = bookDocument.getBoolean("accepted");
                                Boolean delivered = bookDocument.getBoolean("delivered");
                                Boolean received = bookDocument.getBoolean("received");
                                Intent intent = new Intent(MyRequestActivity.this, Onclick_my_requests.class);
                                if(accepted != null && !accepted)
                                {
                                    intent.putExtra("tag", "not accepted");
                                }
                                else if(accepted != null && delivered != null && accepted && !received)
                                {
                                    intent.putExtra("tag", "not received");
                                }
                                else if(accepted != null && delivered != null && received != null && accepted && !delivered && received)
                                {
                                    intent.putExtra("tag", "not delivered");
                                }
                                else
                                {
                                    intent.putExtra("tag", "error");
                                }
                                intent.putExtra("delivered", delivered);
                                intent.putExtra("bookkey", clickedBookId);
                                startActivity(intent);
                            }
                        }
                    }
                });
            }
        });
    }
    private void retrieveBookDetails(FirebaseFirestore db, ArrayList<String> myRequests) {
        BooksDetails.clear(); // Clear the existing list

        for (String bookKey : myRequests) {
            db.collection("books").document(bookKey).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot bookDocument = task.getResult();
                        if (bookDocument.exists()) {
                            Boolean requested = bookDocument.getBoolean("requested");
                            // Retrieve additional book details
                            String isbn = bookDocument.getString("isbn");
                            String title = bookDocument.getString("title");
                            String author = bookDocument.getString("author");
                            String coverPage = bookDocument.getString("cover_page");

                            // Create a new Book_item object and set its details
                            Book_item bookItem = new Book_item(bookKey);
                            bookItem.setIsbn(isbn);
                            bookItem.setTitle(title);
                            bookItem.setAuthor(author);
                            bookItem.setCoverPage(coverPage);

                            BooksDetails.add(bookItem);

                            adapter.notifyDataSetChanged();

                        }

                    }
                }
            });
        }
    }

    class Book_item {
        private String bookId;
        private String isbn;
        private String title;
        private String author;
        private String coverPage;

        public Book_item(String bookId) {
            this.bookId = bookId;
        }

        // Getters and setters for the properties

        public String getBookId() {
            return bookId;
        }

        public void setBookId(String bookId) {
            this.bookId = bookId;
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

        public String getCoverPage() {
            return coverPage;
        }

        public void setCoverPage(String coverPage) {
            this.coverPage = coverPage;
        }
    }

    class CustomAdapter extends BaseAdapter {
        Context context;
        LayoutInflater inflater;
        ArrayList<Book_item> books_items;

        public CustomAdapter(Context context, ArrayList<Book_item> book_items) {
            this.context = context;
            this.books_items = book_items;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return books_items.size();
        }

        @Override
        public Object getItem(int i) {
            return books_items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = inflater.inflate(R.layout.row_my_requests, null);
            TextView isbn_text_view = (TextView) view.findViewById(R.id.bookIsbn);
            TextView title_text_view = (TextView) view.findViewById(R.id.bookTitle);
            TextView author_text_view = (TextView) view.findViewById(R.id.bookAuthor);

            ImageView bookImage = (ImageView) view.findViewById(R.id.bookImage);
            isbn_text_view.setText(books_items.get(i).getIsbn());
            title_text_view.setText(books_items.get(i).getTitle());
            author_text_view.setText(books_items.get(i).getAuthor());
            Picasso.get()
                    .load(books_items.get(i).getCoverPage())
                    .placeholder(R.drawable.book_cover) // Placeholder image while loading
                    .error(R.drawable.book_cover) // Error image if loading fails
                    .into(bookImage);
            return view;
        }
    }


}