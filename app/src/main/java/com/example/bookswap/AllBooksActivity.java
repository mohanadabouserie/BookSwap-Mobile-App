package com.example.bookswap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.BaseAdapter;
import com.squareup.picasso.Picasso;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AllBooksActivity extends AppCompatActivity {
    private GridView gridView;
    private TheBookAdapter bookAdapter;
    private List<TheBook> bookList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_books);

        gridView = findViewById(R.id.gridView);
        bookList = new ArrayList<>();
        bookAdapter = new TheBookAdapter(bookList, this);
        gridView.setAdapter(bookAdapter);

        db = FirebaseFirestore.getInstance();
        loadBooksFromDatabase();
    }

    private void loadBooksFromDatabase() {
        db.collection("books").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    TheBook theBook = document.toObject(TheBook.class);
                    String bookkey = document.getString("bookKey");
                    String title = document.getString("title");
                    String cover = document.getString("cover_page");
                    String author = document.getString("author");
                    String user = document.getString("book_username");
                    Boolean requested = document.getBoolean("requested");
                    SessionManager sessionManager = SessionManager.getInstance();
                    String current_user = sessionManager.getUsername();
                    if (requested != null && requested || current_user.equals(user))
                    {
                        continue;
                    }
                    String genresString = theBook.getGenres();
                    TheBook book = new TheBook(bookkey, title, author, cover, user, genresString);
                    book.setBookKey(bookkey); // Set the bookkey for the book
                    bookList.add(book);
                }
                bookAdapter.notifyDataSetChanged();
            } else {
                // Handle the error
            }
        });
    }
}


class TheBookAdapter extends BaseAdapter {
    private List<TheBook> bookList;
    private Context context;

    public TheBookAdapter(List<TheBook> bookList, Context context) {
        this.bookList = bookList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return bookList.size();
    }

    @Override
    public Object getItem(int position) {
        return bookList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.titleTextView = convertView.findViewById(R.id.title_text_view);
            viewHolder.authorTextView = convertView.findViewById(R.id.author_text_view);
            viewHolder.coverImageView = convertView.findViewById(R.id.book_cover2);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        TheBook book = bookList.get(position);
        viewHolder.titleTextView.setText(book.getTitle());
        viewHolder.authorTextView.setText(book.getAuthor());

        Picasso.get()
                .load(book.getCoverPage())
                .placeholder(R.drawable.book_cover_2) // Placeholder image while loading
                .error(R.drawable.book_cover_2) // Error image if loading fails
                .into(viewHolder.coverImageView);

        convertView.setOnClickListener(new View.OnClickListener() {
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

        return convertView;
    }

    private static class ViewHolder {
        TextView titleTextView;
        TextView authorTextView;
        ImageView coverImageView;
    }
}