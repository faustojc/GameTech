package com.example.finalproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.RectF;
import android.view.ViewGroup;
import android.widget.ImageView;

@SuppressLint("AppCompatCustomView")
public class Player extends ImageView {
    public static final int DIRECTION_NONE = 0;
    public static final int DIRECTION_LEFT = 1;
    public static final int DIRECTION_RIGHT = 2;

    private final ViewGroup.LayoutParams gameFrameLayout;
    private final ViewGroup.LayoutParams groundLayoutParams;
    private final RectF bounds;
    private int currDirection = DIRECTION_NONE;
    private int prevDirection = DIRECTION_NONE;
    private final float speed = 20f;

    public Player(Context context) {
        super(context);
        gameFrameLayout = null;
        groundLayoutParams = null;
        bounds = null;
    }

    public Player(Context context, ViewGroup.LayoutParams gameFrameLayout, ViewGroup.LayoutParams groundLayoutParams,ImageView source) {
        super(context);
        this.gameFrameLayout = gameFrameLayout;
        this.groundLayoutParams = groundLayoutParams;

        setLayoutParams(source.getLayoutParams());

        setX(groundLayoutParams.width / 2f);
        setY((float) gameFrameLayout.height - (float) groundLayoutParams.height - source.getLayoutParams().height);

        bounds = new RectF(source.getDrawable().getBounds());
        getImageMatrix().mapRect(bounds);
        bounds.round(source.getDrawable().getBounds());

        bounds.set(getX(), getY(), getX() + getLayoutParams().width, getY() + getLayoutParams().height);
    }

    public float moveLeft() {
        setX(getX() - speed);

        if (getX() < 0) {
            setX(0);
        }

        bounds.set(getX(), getY(), getX() + getLayoutParams().width, getY() + getLayoutParams().height);

        return getX();
    }

    public float moveRight() {
        setX(getX() + speed);

        if (getX() > gameFrameLayout.width - getLayoutParams().width) {
            setX((float) gameFrameLayout.width - getLayoutParams().width);
        }

        bounds.set(getX(), getY(), getX() + getLayoutParams().width, getY() + getLayoutParams().height);

        return getX();
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

    public void resetPosition() {
        setX(groundLayoutParams.width / 2f);

        bounds.set(getX(), getY(), getX() + getLayoutParams().width, getY() + getLayoutParams().height);
    }
}
