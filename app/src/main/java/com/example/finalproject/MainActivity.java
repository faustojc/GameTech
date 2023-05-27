package com.example.finalproject;

import android.annotation.SuppressLint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private ConstraintLayout gameLayout = null;
    private Player player = null;
    private ImageView platform = null;
    private TextView scoreText = null;
    private ImageView gameoverImageView = null;
    private TextView levelDisplay = null;
    private TextView levelText = null;

    private final RectF gameLayoutBounds = new RectF();
    private final RectF platformBounds = new RectF();

    private Animation fadeIn = null;
    private Animation fadeOut = null;
    private boolean isAnimating = false;

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

                float x = (gameLayoutBounds.width() - randomStone.getBounds().width() <= 0) ?
                        randomStone.getBounds().width() : gameLayoutBounds.width() - randomStone.getLayoutParams().width;

                randomStone.setX(random.nextInt((int) x));

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

                if (levelDisplay.getAnimation() == fadeIn && levelDisplay.getAnimation().hasEnded()) {
                    levelDisplay.startAnimation(fadeOut);
                }
                else if (levelDisplay.getAnimation() == fadeOut && levelDisplay.getAnimation().hasEnded()) {
                    levelDisplay.setAnimation(null);
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
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        scoreText.setText(getString(R.string.score, score));
        levelDisplay.setText(getString(R.string.level, level));
        levelText.setText(getString(R.string.level, level));

        // Using the ViewTreeObserver to get the View's measurement that has already been measured and laid out
        ViewTreeObserver.OnGlobalLayoutListener gameLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                gameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                gameLayoutBounds.set(
                        gameLayout.getLeft(),
                        gameLayout.getTop(),
                        gameLayout.getRight(),
                        gameLayout.getBottom()
                );

                player.setParentBounds(gameLayoutBounds);
            }
        };

        ViewTreeObserver.OnGlobalLayoutListener platformLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                platform.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                platformBounds.set(
                        platform.getLeft(),
                        platform.getTop(),
                        platform.getRight(),
                        platform.getBottom()
                );
            }
        };

        ViewTreeObserver.OnGlobalLayoutListener playerLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                player.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                float centerX = player.getX() + player.getWidth() / 2f;
                float centerY = player.getY() + player.getHeight() / 2f;
                float radiusX = player.getWidth() / 2f;
                float radiusY = player.getHeight() / 2f;

                player.getBounds().set(centerX - radiusX, centerY - radiusY, centerX + radiusX, centerY + radiusY);
                player.setOriginalPosX(player.getX());
            }
        };

        gameLayout.getViewTreeObserver().addOnGlobalLayoutListener(gameLayoutListener);
        platform.getViewTreeObserver().addOnGlobalLayoutListener(platformLayoutListener);
        player.getViewTreeObserver().addOnGlobalLayoutListener(playerLayoutListener);

        player.setLayoutParams(player.getLayoutParams());
        player.setImageDrawable(playerIdleRight);

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

        setButtonsListener();
        spawnHandler.post(spawnRunnable);
        scoreHandler.post(scoreRunnable);
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

            if (s.getBounds().intersect(platformBounds)) {
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

                levelDisplay.setAnimation(null);
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
                player.moveLeft();
                player.setImageDrawable(playerMoveLeft);
            }
            else if (player.getCurrDirection() == Player.DIRECTION_RIGHT) {
                player.moveRight();
                player.setImageDrawable(playerMoveRight);
            }
        }
        else {
            if (player.getPrevDirection() == Player.DIRECTION_LEFT) {
                player.setImageDrawable(playerIdleLeft);
            }
            else if (player.getPrevDirection() == Player.DIRECTION_RIGHT) {
                player.setImageDrawable(playerIdleRight);
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
        platform = findViewById(R.id.platform);
        player = findViewById(R.id.player);
        leftButton = findViewById(R.id.leftButton);
        rightButton = findViewById(R.id.rightButton);
        restartButton = findViewById(R.id.restartButton);

        // Get the animation resources
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        // Get player drawable movement states
        playerMoveLeft = ResourcesCompat.getDrawable(getResources(), R.drawable.jarl_move_left, null);
        playerMoveRight = ResourcesCompat.getDrawable(getResources(), R.drawable.jarl_move_right, null);
        playerIdleRight = ResourcesCompat.getDrawable(getResources(), R.drawable.jarl_idle_right, null);
        playerIdleLeft = ResourcesCompat.getDrawable(getResources(), R.drawable.jarl_idle_left, null);

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
    private void setButtonsListener() {
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
