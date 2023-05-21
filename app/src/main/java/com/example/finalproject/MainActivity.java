package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private FrameLayout gameLayout;
    private ViewGroup.LayoutParams gameLayoutParams;
    private ViewGroup.LayoutParams groundLayoutParams;
    private ImageView ground;
    private ImageView playerImage;

    private ImageView gameoverImageView;

    private Player player;
    private final ArrayList<Stone> spawnedStones = new ArrayList<>();

    private final Random random = new Random();

    private ImageButton leftButton;
    private ImageButton rightButton;
    private Button restartButton;

    private boolean leftButtonPressed = false;
    private boolean rightButtonPressed = false;

    private Timer timer = new Timer();

    private final ArrayList<Integer> batoIDs = new ArrayList<>();

    private final Handler handler = new Handler();
    private final Runnable spawnRunnable = new Runnable() {
        @Override
        public void run() {
            long delayMillis = random.nextInt(1000 - 300) + 300L;

            handler.postDelayed(this, delayMillis);
            Log.d(getClass().getName(), "----- spawn stones called -----");
            spawnStones();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the id of components
        gameLayout = findViewById(R.id.gameFrameLayout);
        gameoverImageView = findViewById(R.id.gameover);
        ground = findViewById(R.id.ground);
        playerImage = findViewById(R.id.player);
        leftButton = findViewById(R.id.leftButton);
        rightButton = findViewById(R.id.rightButton);
        restartButton = findViewById(R.id.restartButton);
        initResources();

        gameLayoutParams = gameLayout.getLayoutParams();
        groundLayoutParams = ground.getLayoutParams();

        // Initialize the player
        player = new Player(playerImage.getContext(), gameLayoutParams, groundLayoutParams, playerImage);

        player.setX(groundLayoutParams.width / 2f);
        player.setY((float) gameLayoutParams.height - (float) groundLayoutParams.height - playerImage.getLayoutParams().height);

        playerImage.setX(groundLayoutParams.width / 2f);
        playerImage.setY((float) gameLayoutParams.height - (float) groundLayoutParams.height - playerImage.getLayoutParams().height);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // TODO: optimize

                // Run the game in the main UI thread
                runOnUiThread(() -> runGame());
            }
        }, 0, 20);

        buttonMovePlayer();
        handler.post(spawnRunnable);

        // Reset game state
        restartButton.setOnClickListener(view -> {
            gameoverImageView.setVisibility(View.INVISIBLE);
            leftButton.setEnabled(true);
            rightButton.setEnabled(true);
            restartButton.setVisibility(View.INVISIBLE);

            for (Stone stone : spawnedStones) {
                gameLayout.removeView(stone);
            }

            // Remove all stones
            spawnedStones.clear();

            // Reset player position X and its source ImageView
            player.resetPosition();
            playerImage.setX(player.getX());

            // Start game again
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // Run the game in the main UI thread
                    runOnUiThread(() -> runGame());
                }
            }, 0, 20);

            handler.post(spawnRunnable);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(spawnRunnable);
        timer.cancel();
    }

    private void runGame() {
        // Drop the stones
        for (int i = spawnedStones.size() - 1; i >= 0; i--) {
            Stone s = spawnedStones.get(i);
            s.moveStone();
            s.setRotation(s.getRotation() + s.getSpeed());

            if ((s.getY() + s.getHeight()) >= (gameLayoutParams.height - groundLayoutParams.height)) {
                gameLayout.removeView(s);
                spawnedStones.remove(i);
            }

            // Check if the stones hit the player
            if (s.getBounds().intersect(player.getBounds())) {
                Log.d(getClass().getName(), "----- player hit -----");

                // Stop the game
                gameoverImageView.setVisibility(View.VISIBLE);
                restartButton.setVisibility(View.VISIBLE);
                timer.cancel();
                handler.removeCallbacks(spawnRunnable);
                leftButton.setEnabled(false);
                rightButton.setEnabled(false);

                leftButtonPressed = false;
                rightButtonPressed = false;

                break;
            }
        }

        // Move the player
        if (leftButtonPressed || rightButtonPressed) {
            if (player.getCurrDirection() == Player.DIRECTION_LEFT) {
                playerImage.setX(player.moveLeft());
                playerImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.jarl_move_right, null));
            }
            else if (player.getCurrDirection() == Player.DIRECTION_RIGHT) {
                playerImage.setX(player.moveRight());
                playerImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.jarl_move_left, null));
            }
        }
        else {
            if (player.getPrevDirection() == Player.DIRECTION_LEFT) {
                playerImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.jarl_left_idle, null));
            }
            else if (player.getPrevDirection() == Player.DIRECTION_RIGHT) {
                playerImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.jarl_right_idle, null));
            }
        }
    }

    private void initResources() {
        Bitmap bitmapTexture = BitmapFactory.decodeResource(getResources(), R.drawable.ground);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmapTexture);

        bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        ground.setBackground(bitmapDrawable);

        try {
            Field[] drawableFiles = R.drawable.class.getFields();

            for (Field field: drawableFiles) {
                String name = field.getName();

                if (name.startsWith("bato")) {
                    batoIDs.add(field.getInt(null));
                }
            }
        } catch (IllegalAccessException e) {
            Log.d(this.getClass().getName(), e.getMessage());
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void buttonMovePlayer() {
        leftButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                leftButtonPressed = true;
                player.setCurrDirection(Player.DIRECTION_LEFT);
            }
            else if (event.getAction() == MotionEvent.ACTION_UP) {
                leftButtonPressed = false;
                player.setCurrDirection(Player.DIRECTION_NONE);
                player.setPrevDirection(Player.DIRECTION_LEFT);
            }
            return true;
        });

        rightButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                rightButtonPressed = true;
                player.setCurrDirection(Player.DIRECTION_RIGHT);
            }
            else if (event.getAction() == MotionEvent.ACTION_UP) {
                rightButtonPressed = false;
                player.setCurrDirection(Player.DIRECTION_NONE);
                player.setPrevDirection(Player.DIRECTION_RIGHT);
            }
            return true;
        });
    }

    private void spawnStones() {
        runOnUiThread(() -> {
            Stone randomStone = new Stone(this, batoIDs.get(random.nextInt(batoIDs.size())));

            int x = (gameLayoutParams.width - randomStone.getLayoutParams().width <= 0) ?
                    randomStone.getLayoutParams().width : gameLayoutParams.width - randomStone.getLayoutParams().width;

            randomStone.setX(random.nextInt(x));

            gameLayout.addView(randomStone);
            spawnedStones.add(randomStone);
        });
    }
}
