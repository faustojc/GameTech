package com.example.finalproject;

import android.content.Context;
import android.graphics.Rect;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.Random;

public class Stone{
    private FrameLayout gameLayout;
    private ImageView stoneImage;

    private Rect bounds = new Rect();

    private Random random;

    public Stone(FrameLayout gameLayout) {
        this.gameLayout = gameLayout;

    }

    private float x = 0f;
    private float y = 0f;

    public float moveDown() {
        this.y -= 10;
        return y;
    }

    public void spawnStones() {
        stoneImage = new ImageView(gameLayout.getContext());
        random = new Random();

        x = random.nextInt(gameLayout.getWidth() - gameLayout.getWidth());

        stoneImage.setX(x);
        stoneImage.setY(y);
        gameLayout.addView(stoneImage);
    }
}
