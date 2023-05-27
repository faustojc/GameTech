package com.example.finalproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

@SuppressLint("AppCompatCustomView")
public class Player extends ImageView {
    public static final int DIRECTION_NONE = 0;
    public static final int DIRECTION_LEFT = 1;
    public static final int DIRECTION_RIGHT = 2;

    private RectF gameLayoutBounds = new RectF();
    private final RectF bounds = new RectF();
    private final float speed = 20f;

    private int currDirection = DIRECTION_NONE;
    private int prevDirection = DIRECTION_NONE;
    private float originalPosX = 0f;

    public Player(Context context) {
        super(context);
    }

    public Player(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Player(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void moveLeft() {
        setX(getX() - speed);

        if (getX() < 0) {
            setX(0);
        }

        float centerX = getX() + getWidth() / 2f;
        float centerY = getY() + getHeight() / 2f;
        float radiusX = getWidth() / 2f;
        float radiusY = getHeight() / 2f;
        bounds.set(centerX - radiusX, centerY - radiusY, centerX + radiusX, centerY + radiusY);
    }

    public void moveRight() {
        setX(getX() + speed);

        if (getX() > gameLayoutBounds.width() - bounds.width()) {
            setX(gameLayoutBounds.width() - bounds.width());
        }

        float centerX = getX() + getWidth() / 2f;
        float centerY = getY() + getHeight() / 2f;
        float radiusX = getWidth() / 2f;
        float radiusY = getHeight() / 2f;
        bounds.set(centerX - radiusX, centerY - radiusY, centerX + radiusX, centerY + radiusY);
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

    public void setParentBounds(RectF gameLayoutBounds) {
        this.gameLayoutBounds = gameLayoutBounds;
    }

    public void setOriginalPosX(float originalPosX) {
        this.originalPosX = originalPosX;
    }

    public void resetPosition() {
        setX(originalPosX);
        bounds.set(getX(), getY(), getX() + getWidth(), getY() + getHeight());
    }
}
