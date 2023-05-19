package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private FrameLayout gameLayout;
    private ImageView ground;
    private ImageView playerImage;

    private Player player;
    private Stone stones;

    private ImageButton leftButton;
    private ImageButton rightButton;

    private Timer timer = new Timer();

    private int screenWidth = 0;
    private int screenHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the id of components
        gameLayout = findViewById(R.id.gameLayout);
        ground = findViewById(R.id.ground);
        playerImage = findViewById(R.id.player);
        leftButton = findViewById(R.id.leftButton);
        rightButton = findViewById(R.id.rightButton);

        // Initialize the player and stones
        player = new Player(playerImage.getContext());
        stones = new Stone(gameLayout);

        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        screenWidth = size.x;
        screenHeight = size.y;

        timer.schedule(new TimerTask() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void run() {
                // TODO: game mechanics, player and object collisions, spawn stones

                // Move the player
                leftButton.setOnTouchListener((v, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        playerImage.setX(player.moveLeft());
                    }
                    return true;
                });

                rightButton.setOnTouchListener((v, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        playerImage.setX(player.moveRight());
                    }
                    return true;
                });


            }
        }, 0, 20);

        initResources();
    }

    private void initResources() {
        Bitmap bitmapTexture = BitmapFactory.decodeResource(getResources(), R.drawable.ground);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmapTexture);

        bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        ground.setBackground(bitmapDrawable);
    }

}
