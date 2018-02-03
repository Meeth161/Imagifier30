package com.android.example.imagifier30;

/**
 * Created by Meeth on 03-Feb-18.
 */

public class Search {
    String imageUrl;
    String desc;

    public Search() {
    }

    public Search(String imageUrl, String desc) {
        this.imageUrl = imageUrl;
        this.desc = desc;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDesc() {
        return desc;
    }
}
