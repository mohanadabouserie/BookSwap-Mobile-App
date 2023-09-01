package com.example.bookswap;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Scanner;

public class APIHandler {

    String Title, ISBN10,ISBN13, Authors, description, Imagepath;
    String[] AuthorsArray;

    public static URL BuildURL(String ISBN){
        String domain = "https://www.googleapis.com/books/v1/volumes?q=isbn:";
        String APIKey = "&key=YOUR_KEY";
        String urlstr = domain + ISBN + APIKey;
        Uri uri = Uri.parse(urlstr).buildUpon().build();

        try {
            URL url = new URL(uri.toString());
            return url;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    public static String makeHTTPRequest(URL url) {
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responsecode = connection.getResponseCode();
            if(responsecode == HttpURLConnection.HTTP_OK) {
                InputStream input = connection.getInputStream();

                Scanner scan = new Scanner(input);
                scan.useDelimiter("\\A");


                if (scan.hasNext())
                    return scan.next();
            }

        } catch (IOException e){
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
        return null;
    }

    public void ParseJSON(String data) throws JSONException {

        JSONObject root = new JSONObject(data);
        JSONObject Book = root.getJSONArray("items").getJSONObject(0);
        JSONObject VolumeInfo = Book.getJSONObject("volumeInfo");
        Title = VolumeInfo.getString("title");

        JSONArray authors = VolumeInfo.getJSONArray("authors");
        Authors = "";
        AuthorsArray = new String[authors.length()];
        for(int i = 0; i< authors.length();i++){
            AuthorsArray[i] = authors.getString(i);
            Authors = Authors + authors.getString(i);
            if(authors.length()-i != 1) Authors = Authors + ", ";
        }
        description = VolumeInfo.getString("description");
        JSONArray Identifiers = VolumeInfo.getJSONArray("industryIdentifiers");
        ISBN10 = Identifiers.getJSONObject(1).getString("identifier");
        ISBN13 = Identifiers.getJSONObject(0).getString("identifier");

        JSONObject ImageLinks = VolumeInfo.getJSONObject("imageLinks");
        Imagepath = ImageLinks.getString("thumbnail");
    }
}