package com.example.finalproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.res.ResourcesCompat;

@SuppressLint("AppCompatCustomView")
public class Powerup extends ImageView {
    private RectF bounds;
    private int type = 0;

    private static final float SPEED = 15f;

    public static final int TYPE_SHIELD = 0;
    public static final int TYPE_SPEED = 1;
    public static final int TYPE_BOMB = 2;

    public Powerup(Context context) {
        super(context);
    }

    public Powerup(Context context, int type) {
        super(context);
        this.type = type;

        setLayoutParams(new ViewGroup.LayoutParams(150, 150));

        // Initialize the powerup resource
        if (type == TYPE_SHIELD) {
            setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.shield, null));
        }
        else if (type == TYPE_SPEED) {
            setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.speed, null));
        }
        else if (type == TYPE_BOMB) {
            setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.bomb, null));
        }

        setBackgroundColor(Color.TRANSPARENT);
        setY(-30);
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

    public void movePowerup() {
        setY(getY() + SPEED);

        float centerX = getX() + getLayoutParams().width / 2f;
        float centerY = getY() + getLayoutParams().height / 2f;
        float radius = Math.min(getWidth(), getHeight()) / 2f;

        // Update the bounds with the new coordinates
        bounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
    }

    public RectF getBounds() {
        return bounds;
    }

    public int getType() {
        return type;
    }
}
