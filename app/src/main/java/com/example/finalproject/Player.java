package com.example.finalproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.view.ViewGroup;
import android.widget.ImageView;

@SuppressLint("AppCompatCustomView")
public class Player extends ImageView {
    public static final int DIRECTION_NONE = 0;
    public static final int DIRECTION_LEFT = 1;
    public static final int DIRECTION_RIGHT = 2;

    public Rect bounds;

    private int direction = DIRECTION_RIGHT;
    private final float speed = 20f;

    private int x = 0;

    private final ViewGroup.LayoutParams gameFrameLayout;

    public Player(Context context, ViewGroup.LayoutParams gameFrameLayout, ImageView source) {
        super(context);
        this.gameFrameLayout = gameFrameLayout;

        setLayoutParams(source.getLayoutParams());

        setX(source.getX());
        setY(source.getY());

        bounds = new Rect(
                (int) getX(),
                (int) getY(),
                (int) getX() + getLayoutParams().width,
                (int) getY() + getLayoutParams().height
        );
    }

    public float moveLeft() {
        x += speed;
        if (x > gameFrameLayout.width - getLayoutParams().width) {
            x = gameFrameLayout.width - getLayoutParams().width;
        }
        return x;
    }

    public float moveRight() {
        x -= speed;

        if (x < 0) {
            x = 0;
        }
        return x;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getDirection() {
        return direction;
    }

    public Rect getBounds() {
        return bounds;
    }
}
