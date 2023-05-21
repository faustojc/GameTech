package com.example.finalproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.ViewGroup;
import android.widget.ImageView;

@SuppressLint("AppCompatCustomView")
public class Player extends ImageView {
    public static final int DIRECTION_NONE = 0;
    public static final int DIRECTION_LEFT = 1;
    public static final int DIRECTION_RIGHT = 2;

    private final ViewGroup.LayoutParams gameFrameLayout;
    private ImageView source;
    private RectF bounds;
    private int currDirection = DIRECTION_NONE;
    private int prevDirection = DIRECTION_NONE;
    private final float speed = 20f;
    private int x = 0;

    // for debuging
    private Paint paint = new Paint();

    public Player(Context context) {
        super(context);
        gameFrameLayout = null;
    }

    public Player(Context context, ViewGroup.LayoutParams gameFrameLayout, ImageView source) {
        super(context);
        this.gameFrameLayout = gameFrameLayout;
        this.source = source;

        setLayoutParams(source.getLayoutParams());

        setX(source.getX());
        setY(source.getY());

        bounds = new RectF(source.getDrawable().getBounds());
        getImageMatrix().mapRect(bounds);
        bounds.round(source.getDrawable().getBounds());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6);
        canvas.drawRect(bounds, paint);
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

    public void setCurrDirection(int currDirection) {
        this.currDirection = currDirection;
    }

    public int getCurrDirection() {
        return currDirection;
    }

    public RectF getBounds() {
        return bounds;
    }

    public void setPrevDirection(int prevDirection) {
        this.prevDirection = prevDirection;
    }

    public int getPrevDirection() {
        return prevDirection;
    }
}
