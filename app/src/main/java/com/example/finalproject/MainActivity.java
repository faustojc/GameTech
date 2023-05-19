package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private ImageView ground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ground = findViewById(R.id.ground);

        initResources();
    }

    private void initResources() {
        Bitmap bitmapTexture = BitmapFactory.decodeResource(getResources(), R.drawable.ground);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmapTexture);

        bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        ground.setBackground(bitmapDrawable);
    }
}
