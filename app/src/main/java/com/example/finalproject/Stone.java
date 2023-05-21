package com.example.finalproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.res.ResourcesCompat;

import java.util.Random;

@SuppressLint("AppCompatCustomView")
public class Stone extends ImageView {
    private RectF bounds;
    private float speed = 0f;
    private final Random random = new Random();

    // for debuging
    private Paint paint = new Paint();

    public Stone(Context context) {
        super(context);
    }

    public Stone(Context context, int resourceId) {
        super(context);

        setLayoutParams(new ViewGroup.LayoutParams(150, 150));

        // Initialize the stone resource
        setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), resourceId, null));
        setBackgroundColor(Color.TRANSPARENT);
        setY(-30);
        setSpeed(3 + random.nextFloat() * (25 - 3));
        setScaleType(ScaleType.CENTER_CROP);

        bounds = new RectF(getDrawable().getBounds());
        getImageMatrix().mapRect(bounds);
        bounds.round(getDrawable().getBounds());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6);
        canvas.drawRect(bounds, paint);
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

    public RectF getBounds() {
        return bounds;
    }
}
