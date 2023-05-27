package com.example.finalproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.res.ResourcesCompat;

import java.util.Random;

@SuppressLint("AppCompatCustomView")
public class Stone extends ImageView {
    private RectF bounds;
    private float speed = 0f;

    public Stone(Context context) {
        super(context);
    }

    public Stone(Context context, int resourceId) {
        super(context);
        Random random = new Random();

        setLayoutParams(new ViewGroup.LayoutParams(150, 150));

        // Initialize the stone resource
        setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), resourceId, null));
        setBackgroundColor(Color.TRANSPARENT);
        setY(-30);
        setSpeed(3 + random.nextFloat() * (25 - 3));
        setScaleType(ScaleType.CENTER_CROP);

        bounds = new RectF(getDrawable().getBounds());
        getImageMatrix().mapRect(bounds);
        bounds.round(new Rect((int) bounds.left, (int) bounds.top, (int) bounds.right, (int) bounds.bottom));

        // Set the bounds of the Stone to circle
        float centerX = getLayoutParams().width / 2f;
        float centerY = getLayoutParams().height / 2f;
        float radius = Math.min(centerX, centerY);
        bounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }

    public void moveStone() {
        setY(getY() + speed);

        float centerX = getX() + getLayoutParams().width / 2f;
        float centerY = getY() + getLayoutParams().height / 2f;
        float radius = Math.min(getWidth(), getHeight()) / 2f;

        // Update the bounds with the new coordinates
        bounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
    }

    public RectF getBounds() {
        return bounds;
    }
}
