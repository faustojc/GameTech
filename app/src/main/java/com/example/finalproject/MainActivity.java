package com.example.finalproject;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private FrameLayout gameLayout = null;
    private ViewGroup.LayoutParams gameLayoutParams = null;
    private ViewGroup.LayoutParams groundLayoutParams = null;
    private ImageView ground = null;
    private ImageView playerImage = null;
    private TextView scoreText = null;
    private ImageView gameoverImageView = null;
    private TextView levelDisplay = null;
    private TextView levelText = null;

    private Animation fadeIn = null;
    private Animation fadeOut = null;

    private Player player = null;
    private final ArrayList<Stone> spawnedStones = new ArrayList<>();

    private Drawable playerMoveLeft = null;
    private Drawable playerMoveRight = null;
    private Drawable playerIdleLeft = null;
    private Drawable playerIdleRight = null;

    private final Random random = new Random();
    private int level = 0;
    private int score = 0;

    private ImageButton leftButton = null;
    private ImageButton rightButton = null;
    private Button restartButton = null;

    private boolean leftButtonPressed = false;
    private boolean rightButtonPressed = false;
    private boolean isResourceInitialized = false;

    private Timer timer = new Timer();

    private final ArrayList<Integer> batoIDs = new ArrayList<>();

    private final HandlerThread handlerThread = new HandlerThread("Update UI Thread");
    private Handler spawnHandler = null;
    private Handler scoreHandler = null;

    private final Runnable spawnRunnable = new Runnable() {
        @Override
        public void run() {
            long delay = random.nextInt((3000 - (level * 10)) - 1000) + 1000L - (level * 20L);

            spawnHandler.postDelayed(this, delay);
            Log.d(getClass().getName(), "----- spawn stones called -----");

            // Spawn the stone in the main UI thread
            runOnUiThread(() -> {
                Stone randomStone = new Stone(MainActivity.this, batoIDs.get(random.nextInt(batoIDs.size())));

                int x = (gameLayoutParams.width - randomStone.getLayoutParams().width <= 0) ?
                        randomStone.getLayoutParams().width : gameLayoutParams.width - randomStone.getLayoutParams().width;

                randomStone.setX(random.nextInt(x));

                gameLayout.addView(randomStone);
                spawnedStones.add(randomStone);
            });
        }
    };

    private final Runnable scoreRunnable = new Runnable() {
        @Override
        public void run() {
            scoreHandler.postDelayed(this, 1000L);

            runOnUiThread(() -> {
                score += 1;
                scoreText.setText(getString(R.string.score, score));

                if (score % 10 == 0 && score != 0) {
                    level += 1;
                    levelDisplay.setText(getString(R.string.level, level));
                    levelText.setText(getString(R.string.level, level));

                    levelDisplay.setAlpha(1.0f);
                    levelDisplay.startAnimation(fadeIn);
                }

                if (levelDisplay.getAnimation().hasEnded()) {
                    levelDisplay.startAnimation(fadeOut);
                    levelDisplay.setAlpha(0.0f);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize resources first
        initResources();

        if (isResourceInitialized) {
            scoreText.setText(getString(R.string.score, score));
            levelDisplay.setText(getString(R.string.level, level));
            levelText.setText(getString(R.string.level, level));

            gameLayoutParams = gameLayout.getLayoutParams();
            groundLayoutParams = ground.getLayoutParams();

            // Initialize the player
            player = new Player(playerImage.getContext(), gameLayoutParams, groundLayoutParams, playerImage);

            playerImage.setX(player.getX());
            playerImage.setY(player.getY());

            // Set the handler thread
            handlerThread.start();
            spawnHandler = new Handler(handlerThread.getLooper());
            scoreHandler = new Handler(handlerThread.getLooper());

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // TODO: optimize

                    // Run the game in the main UI thread
                    runOnUiThread(() -> runGame());
                }
            }, 0, 20);

            buttonMovePlayer();
            spawnHandler.post(spawnRunnable);
            scoreHandler.post(scoreRunnable);

            // Reset game state on click
            restartButton.setOnClickListener(view -> {
                score = 0;
                level = 0;

                scoreText.setText(getString(R.string.score, score));
                levelDisplay.setText(getString(R.string.level, level));
                levelText.setText(getString(R.string.level, level));

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

                spawnHandler.post(spawnRunnable);
                scoreHandler.post(scoreRunnable);
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        spawnHandler.removeCallbacks(spawnRunnable);
        scoreHandler.removeCallbacks(scoreRunnable);
        handlerThread.quitSafely();
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

                spawnHandler.removeCallbacks(spawnRunnable);
                scoreHandler.removeCallbacks(scoreRunnable);
                timer.cancel();

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
                playerImage.setImageDrawable(playerMoveLeft);
            }
            else if (player.getCurrDirection() == Player.DIRECTION_RIGHT) {
                playerImage.setX(player.moveRight());
                playerImage.setImageDrawable(playerMoveRight);
            }
        }
        else {
            if (player.getPrevDirection() == Player.DIRECTION_LEFT) {
                playerImage.setImageDrawable(playerIdleLeft);
            }
            else if (player.getPrevDirection() == Player.DIRECTION_RIGHT) {
                playerImage.setImageDrawable(playerIdleRight);
            }
        }
    }

    private void initResources() {
        // Get the id of components FIRST
        gameLayout = findViewById(R.id.gameFrameLayout);
        gameoverImageView = findViewById(R.id.gameover);
        levelDisplay = findViewById(R.id.levelDisplay);
        levelText = findViewById(R.id.levelText);
        scoreText = findViewById(R.id.scoreText);
        ground = findViewById(R.id.ground);
        playerImage = findViewById(R.id.player);
        leftButton = findViewById(R.id.leftButton);
        rightButton = findViewById(R.id.rightButton);
        restartButton = findViewById(R.id.restartButton);

        // Get the animation resources
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        levelDisplay.setAnimation(fadeIn);

        // Get player drawable movement states
        playerMoveLeft = ResourcesCompat.getDrawable(getResources(), R.drawable.jarl_move_left, null);
        playerMoveRight = ResourcesCompat.getDrawable(getResources(), R.drawable.jarl_move_right, null);
        playerIdleRight = ResourcesCompat.getDrawable(getResources(), R.drawable.jarl_idle_right, null);
        playerIdleLeft = ResourcesCompat.getDrawable(getResources(), R.drawable.jarl_idle_left, null);

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

        isResourceInitialized = true;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void buttonMovePlayer() {
        rightButton.setOnTouchListener((view, event) -> {
            if (view.getId() == R.id.rightButton) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    rightButtonPressed = true;
                    player.setCurrDirection(Player.DIRECTION_LEFT);
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    rightButtonPressed = false;
                    player.setCurrDirection(Player.DIRECTION_NONE);
                    player.setPrevDirection(Player.DIRECTION_LEFT);
                }
            }

            return false;
        });

        leftButton.setOnTouchListener((view, event) -> {
            if (view.getId() == R.id.leftButton) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    leftButtonPressed = true;
                    player.setCurrDirection(Player.DIRECTION_RIGHT);
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    leftButtonPressed = false;
                    player.setCurrDirection(Player.DIRECTION_NONE);
                    player.setPrevDirection(Player.DIRECTION_RIGHT);
                }
            }

            return false;
        });
    }
}
