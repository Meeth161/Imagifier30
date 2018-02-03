package com.android.example.imagifier30;

/**
 * Created by Meeth on 03-Feb-18.
 */

public class User {
    String name;
    String email;
    String uid;

    public User() {
    }

    public User(String name, String email, String uid) {
        this.name = name;
        this.email = email;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getUid() {
        return uid;
    }
}
