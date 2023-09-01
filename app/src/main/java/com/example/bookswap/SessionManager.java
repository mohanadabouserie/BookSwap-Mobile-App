package com.example.bookswap;

public class SessionManager {
    private static SessionManager instance;
    private String username;

    private SessionManager() { }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
    public void clearSession() {
        username = null;
    }
}
