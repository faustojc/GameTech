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
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private ConstraintLayout gameLayout = null;
    private LinearLayout powerupDisplayLayout = null;
    private FrameLayout startFrameLayout = null;
    private Player player = null;
    private ImageView platform = null;
    private TextView scoreText = null;
    private TextView startText = null;
    private ImageView gameoverImageView = null;
    private TextView levelDisplay = null;
    private TextView levelText = null;

    private TextView shieldText = null;
    private TextView speedText = null;
    private TextView bombText = null;

    private final RectF gameLayoutBounds = new RectF();
    private final RectF platformBounds = new RectF();

    private Animation fadeIn = null;
    private Animation fadeOut = null;
    private Animation slideUp = null;

    private final ArrayList<Stone> spawnedStones = new ArrayList<>();
    private final ArrayList<Powerup> spawnedPowerups = new ArrayList<>();

    private Drawable playerMoveLeft = null;
    private Drawable playerMoveRight = null;
    private Drawable playerIdleLeft = null;
    private Drawable playerIdleRight = null;

    private ImageView shieldImageView = null;
    private ImageView speedImageView = null;

    private ImageView shieldEffectImageView = null;

    private final Random random = new Random();
    private int level = 0;
    private int score = 0;

    private ImageButton leftButton = null;
    private ImageButton rightButton = null;
    private ImageButton startButton = null;
    private Button restartButton = null;
    private boolean isGameOver = false;

    private boolean leftButtonPressed = false;
    private boolean rightButtonPressed = false;

    private Timer timer = new Timer();

    private final ArrayList<Integer> batoIDs = new ArrayList<>();

    private final HandlerThread handlerThread = new HandlerThread("Update UI Thread");
    private Handler spawnStoneHandler = null;
    private Handler spawnPowerupHandler = null;
    private Handler scoreHandler = null;
    private Handler powerupHandler = null;

    private final Runnable spawnStoneRunnable = new Runnable() {
        @Override
        public void run() {
            long delay = random.nextInt((3000 - (level * 20)) - 1000) + 1000L - (level * 12L);

            spawnStoneHandler.postDelayed(this, delay);
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

    private final Runnable spawnPowerupRunnable = new Runnable() {
        @Override
        public void run() {
            // Spawn the Powerup
            spawnPowerupHandler.postDelayed(this, random.nextInt(3000) + 2000L); // delay between 2sec and 7sec
            Log.d(getClass().getName(), "----- spawn powerup called -----");

            runOnUiThread(() -> {
                Powerup randomPowerup = new Powerup(MainActivity.this, random.nextInt(3));

                float x = (gameLayoutBounds.width() - randomPowerup.getBounds().width() <= 0) ?
                        randomPowerup.getBounds().width() : gameLayoutBounds.width() - randomPowerup.getLayoutParams().width;

                randomPowerup.setX(random.nextInt((int) x));

                gameLayout.addView(randomPowerup);
                spawnedPowerups.add(randomPowerup);
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

                if (score == 50) {
                    levelDisplay.setAnimation(null); // stop the animation in displaying because it's the final level
                }
                else {
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

                float playerCenterX = player.getX() + player.getWidth() / 2f;
                float playerCenterY = player.getY() + player.getHeight() / 2f;
                float radiusX = player.getWidth() / 2f;
                float radiusY = player.getHeight() / 2f;

                player.getBounds().set(playerCenterX - radiusX, playerCenterY - radiusY, playerCenterX + radiusX, playerCenterY + radiusY);
                player.setOriginalPosX(player.getX());

                // Calculate the top-left coordinates of the powerup texts
                float shieldTextX = playerCenterX - shieldText.getWidth() / 2f;
                float shieldTextY = playerCenterY - shieldText.getHeight() / 2f;

                float speedTextX = playerCenterX - speedText.getWidth() / 2f;
                float speedTextY = playerCenterY - speedText.getHeight() / 2f;

                float bombTextX = playerCenterX - bombText.getWidth() / 2f;
                float bombTextY = playerCenterY - bombText.getHeight() / 2f;

                shieldText.setX(shieldTextX);
                shieldText.setY(shieldTextY);

                speedText.setX(speedTextX);
                speedText.setY(speedTextY);

                bombText.setX(bombTextX);
                bombText.setY(bombTextY);

                player.setShieldText(shieldText);
                player.setSpeedText(speedText);
                player.setBombText(bombText);
            }
        };

        gameLayout.getViewTreeObserver().addOnGlobalLayoutListener(gameLayoutListener);
        platform.getViewTreeObserver().addOnGlobalLayoutListener(platformLayoutListener);
        player.getViewTreeObserver().addOnGlobalLayoutListener(playerLayoutListener);

        player.setLayoutParams(player.getLayoutParams());
        player.setImageDrawable(playerIdleRight);

        player.setShieldEffectImageView(shieldEffectImageView);

        setButtonsListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        spawnStoneHandler.removeCallbacks(spawnStoneRunnable);
        spawnPowerupHandler.removeCallbacks(spawnPowerupRunnable);
        scoreHandler.removeCallbacks(scoreRunnable);
        handlerThread.quitSafely();
        timer.cancel();
    }

    private void runGame() {
        if (!isGameOver) {
            // Drop the stones
            dropSpawnedStones();

            // Drop the powerups
            dropSpawnedPowerups();

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
    }

    private void dropSpawnedStones() {
        Iterator<Stone> stoneIterator = spawnedStones.iterator();

        while (stoneIterator.hasNext()) {
            Stone stone = stoneIterator.next();
            stone.moveStone();
            stone.setRotation(stone.getRotation() + stone.getSpeed());

            if (stone.getBounds().intersect(platformBounds)) {
                gameLayout.removeView(stone);
                stoneIterator.remove();
            }

            // Check if the stones hit the player
            if (stone.getBounds().intersect(player.getBounds()) && !player.isShielded()) {
                Log.d(getClass().getName(), "----- player hit -----");

                // Stop the game
                gameoverImageView.setVisibility(View.VISIBLE);
                restartButton.setVisibility(View.VISIBLE);

                spawnStoneHandler.removeCallbacks(spawnStoneRunnable);
                spawnPowerupHandler.removeCallbacks(spawnPowerupRunnable);
                scoreHandler.removeCallbacks(scoreRunnable);
                timer.cancel();

                levelDisplay.setAnimation(null);
                leftButton.setEnabled(false);
                rightButton.setEnabled(false);

                levelDisplay.setAnimation(null);

                leftButtonPressed = false;
                rightButtonPressed = false;

                isGameOver = true;
                break;
            }
        }
    }

    private void dropSpawnedPowerups() {
        for (Powerup powerup : spawnedPowerups) {
            powerup.movePowerup();

            if (powerup.getBounds().intersect(platformBounds) || powerup.getBounds().intersect(player.getBounds())) {
                gameLayout.removeView(powerup);
                spawnedPowerups.remove(powerup);
            }

            // Check if the powerups hit the player
            if (powerup.getBounds().intersect(player.getBounds())) {
                if (powerup.getType() == Powerup.TYPE_SHIELD) {
                    player.setShielded(true);
                    player.getShieldEffectImageView().setVisibility(View.VISIBLE);
                    player.getShieldEffectImageView().setRotation(player.getShieldEffectImageView().getRotation() + 6);

                    if (shieldImageView.getParent() != null) {
                        powerupDisplayLayout.removeView(shieldImageView);
                    }
                    powerupDisplayLayout.addView(shieldImageView);

                    shieldText.startAnimation(slideUp);
                    startPowerupAnimListener(shieldText);

                    powerupHandler.postDelayed(() -> runOnUiThread(() -> {
                        player.setShielded(false);
                        player.getShieldEffectImageView().setVisibility(View.INVISIBLE);
                        powerupDisplayLayout.removeView(shieldImageView);
                    }), 3000);
                }
                else if (powerup.getType() == Powerup.TYPE_SPEED) {
                    player.setSpeed(player.getSpeed() + 15);

                    if (speedImageView.getParent() != null) {
                        powerupDisplayLayout.removeView(speedImageView);
                    }
                    powerupDisplayLayout.addView(speedImageView);

                    speedText.startAnimation(slideUp);
                    startPowerupAnimListener(speedText);

                    powerupHandler.postDelayed(() -> runOnUiThread(() -> {
                        player.setSpeed(player.getSpeed() - 15);
                        powerupDisplayLayout.removeView(speedImageView);
                    }), 3000);
                }
                else if (powerup.getType() == Powerup.TYPE_BOMB) {
                    // Remove all stones
                    Iterator<Stone> stoneIterator = spawnedStones.iterator();

                    while (stoneIterator.hasNext()) {
                        Stone stone = stoneIterator.next();
                        gameLayout.removeView(stone);
                        stoneIterator.remove();
                    }

                    bombText.startAnimation(slideUp);
                    startPowerupAnimListener(bombText);
                }
            }
        }
    }

    private void startPowerupAnimListener(TextView powerupTextView) {
        slideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                powerupTextView.setAlpha(1);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                powerupTextView.setAlpha(0);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { /* TODO empty */ }
        });
    }

    private void initResources() {
        // Get the id of components FIRST
        gameLayout = findViewById(R.id.gameLayout);
        startFrameLayout = findViewById(R.id.startFrameLayout);
        powerupDisplayLayout = findViewById(R.id.powerupDisplayLayout);
        gameoverImageView = findViewById(R.id.gameover);
        levelDisplay = findViewById(R.id.levelDisplay);
        levelText = findViewById(R.id.levelText);
        startText = findViewById(R.id.startText);
        shieldText = findViewById(R.id.shieldText);
        speedText = findViewById(R.id.speedText);
        bombText = findViewById(R.id.bombText);
        scoreText = findViewById(R.id.scoreText);
        platform = findViewById(R.id.platform);
        player = findViewById(R.id.player);
        leftButton = findViewById(R.id.leftButton);
        rightButton = findViewById(R.id.rightButton);
        restartButton = findViewById(R.id.restartButton);
        startButton = findViewById(R.id.startButton);

        // Get the animation resources
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        // Get player drawable movement states
        playerMoveLeft = ResourcesCompat.getDrawable(getResources(), R.drawable.jarl_move_left, null);
        playerMoveRight = ResourcesCompat.getDrawable(getResources(), R.drawable.jarl_move_right, null);
        playerIdleRight = ResourcesCompat.getDrawable(getResources(), R.drawable.jarl_idle_right, null);
        playerIdleLeft = ResourcesCompat.getDrawable(getResources(), R.drawable.jarl_idle_left, null);

        // Get the drawable resources for powerups
        shieldImageView = new ImageView(this);
        speedImageView = new ImageView(this);

        shieldImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.shield, null));
        speedImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.speed, null));

        shieldImageView.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
        speedImageView.setLayoutParams(new ViewGroup.LayoutParams(100, 100));

        shieldEffectImageView = new ImageView(this);
        shieldEffectImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.shieldeffect, null));
        shieldEffectImageView.setVisibility(View.INVISIBLE);
        shieldEffectImageView.setLayoutParams(new ViewGroup.LayoutParams(250, 250));

        gameLayout.addView(shieldEffectImageView);

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
        startButton.setOnClickListener(view -> {
            startFrameLayout.setVisibility(View.INVISIBLE);
            startFrameLayout.setEnabled(false);

            // Set the handlers thread
            handlerThread.start();
            spawnStoneHandler = new Handler(handlerThread.getLooper());
            spawnPowerupHandler = new Handler(handlerThread.getLooper());
            scoreHandler = new Handler(handlerThread.getLooper());
            powerupHandler = new Handler(handlerThread.getLooper());

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // Run the game in the main UI thread
                    runOnUiThread(() -> runGame());
                }
            }, 0, 20);

            spawnStoneHandler.postDelayed(spawnStoneRunnable, 1000);
            spawnPowerupHandler.postDelayed(spawnPowerupRunnable, 2000);
            scoreHandler.post(scoreRunnable);
        });

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
            isGameOver = false;

            levelDisplay.setAlpha(0);

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

            for (Powerup powerup : spawnedPowerups) {
                gameLayout.removeView(powerup);
            }

            // Remove all powerups
            spawnedPowerups.clear();

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

            spawnStoneHandler.postDelayed(spawnStoneRunnable, 1000);
            spawnPowerupHandler.postDelayed(spawnPowerupRunnable, 2000);
            scoreHandler.post(scoreRunnable);
        });
    }
}
