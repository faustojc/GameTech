package com.example.finalproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import kotlin.RequiresOptIn;

@SuppressLint("AppCompatCustomView")
public class Stone extends ImageView {
    private Rect bounds;
    private float speed = 0f;

    public Stone(Context context) {
        super(context);

        setLayoutParams(new ViewGroup.LayoutParams(150, 150));

        bounds = new Rect(
                (int) getX(),
                (int) getY(),
                (int) getX() + getLayoutParams().width,
                (int) getY() + getLayoutParams().height
        );
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }

    public void moveStone() {
        this.setY(this.getY() + speed);
    }

    public Rect getBounds() {
        return bounds;
    }
}
