package com.example.finalproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.widget.ImageView;

@SuppressLint("AppCompatCustomView")
public class Player extends ImageView {
    private Rect bounds = new Rect();

    public Player(Context context) {
        super(context);

        getHitRect(bounds);
    }

    public float moveRight() {
        this.setX(this.getX() + 10);
        return getX();
    }

    public float moveLeft() {
        this.setX(this.getX() - 10);
        return getX();
    }
}
