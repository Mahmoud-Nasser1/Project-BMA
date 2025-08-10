package com.banking;

public class UserSession {
    private static UserSession instance;
    private String username;
    private String requestSource;
    private boolean passwordReset = false;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void clear() {
        this.username = null;
        this.requestSource = null;
        this.passwordReset = false;
    }

    public void setRequestSource(String source) {
        this.requestSource = source;
    }

    public String getRequestSource() {
        return requestSource;
    }

    public void setPasswordReset(boolean passwordReset) {
        this.passwordReset = passwordReset;
    }

    public boolean isPasswordReset() {
        return passwordReset;
    }
}