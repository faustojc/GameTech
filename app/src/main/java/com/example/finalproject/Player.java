package com.example.finalproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

@SuppressLint("AppCompatCustomView")
public class Player extends ImageView {
    public static final int DIRECTION_NONE = 0;
    public static final int DIRECTION_LEFT = 1;
    public static final int DIRECTION_RIGHT = 2;

    private RectF gameLayoutBounds = new RectF();
    private final RectF bounds;
    private final float speed = 20f;

    private int currDirection = DIRECTION_NONE;
    private int prevDirection = DIRECTION_NONE;
    private float originalPosX = 0f;

    public Player(Context context) {
        super(context);
        bounds = new RectF();
    }

    public Player(Context context, AttributeSet attrs) {
        super(context, attrs);
        bounds = new RectF();
    }

    public Player(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        bounds = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        Path path = new Path();

        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(7);

        path.addRect(bounds, Path.Direction.CW);

        canvas.clipPath(path);
        canvas.drawPath(path, paint);
    }

    public void moveLeft() {
        setX(getX() - speed);

        if (getX() < 0) {
            setX(0);
        }

        bounds.set(getX(), getY(), getX() + getWidth(), getY() + getHeight());
    }

    public void moveRight() {
        setX(getX() + speed);

        if (getX() > gameLayoutBounds.width() - bounds.width()) {
            setX(gameLayoutBounds.width() - bounds.width());
        }

        bounds.set(getX(), getY(), getX() + getWidth(), getY() + getHeight());
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
