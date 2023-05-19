package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {
    private FrameLayout gameLayout;
    private ViewGroup.LayoutParams gameLayoutParams;
    private ViewGroup.LayoutParams groundLayoutParams;
    private ImageView ground;
    private ImageView playerImage;

    private ImageView gameover;

    private Player player;
    private final ArrayList<Stone> spawnedStones = new ArrayList<>();

    private final Random random = new Random();

    private int playerCurrDirection = Player.DIRECTION_NONE;

    private ImageButton leftButton;
    private ImageButton rightButton;

    private boolean leftButtonPressed = false;
    private boolean rightButtonPressed = false;

    private final Timer timer = new Timer();

    private final ArrayList<Integer> batoIDs = new ArrayList<>();

    private final Handler handler = new Handler();
    private final Runnable spawnRunnable = new Runnable() {
        @Override
        public void run() {
            long delayMillis = random.nextInt(500) + 1000L;

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
        gameover = findViewById(R.id.gameover);
        ground = findViewById(R.id.ground);
        playerImage = findViewById(R.id.player);
        leftButton = findViewById(R.id.leftButton);
        rightButton = findViewById(R.id.rightButton);
        initResources();

        gameLayoutParams = gameLayout.getLayoutParams();
        groundLayoutParams = ground.getLayoutParams();

        // Initialize the player
        player = new Player(playerImage.getContext(), gameLayoutParams, playerImage);

        player.setX(groundLayoutParams.width / 2f);
        player.setY((float) gameLayoutParams.height - (float) groundLayoutParams.height - playerImage.getLayoutParams().height);

        playerImage.setX(groundLayoutParams.width / 2f);
        playerImage.setY((float) gameLayoutParams.height - (float) groundLayoutParams.height - playerImage.getLayoutParams().height);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // TODO: optimize

                // Move the player
                runOnUiThread(() -> {
                    // Drop the stones
                    for (int i = spawnedStones.size() - 1; i >= 0; i--) {
                        Stone s = spawnedStones.get(i);
                        s.moveStone();
                        s.setRotation(s.getRotation() + s.getSpeed());

                        if ((s.getY() + s.getHeight()) >= (gameLayoutParams.height - groundLayoutParams.height)) {
                            gameLayout.removeView(s);
                            spawnedStones.remove(i);
                        }

                        if (Rect.intersects(s.getBounds(), player.getBounds())) {
                            Log.d(getClass().getName(), "----- player hit -----");

                            //gameover.setVisibility(View.VISIBLE);
                            //timer.cancel();
                            //handler.removeCallbacks(spawnRunnable);
                            //break;
                        }
                    }

                    if (playerCurrDirection == Player.DIRECTION_LEFT) {
                        playerImage.setX(player.moveLeft());
                        playerImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.jarl_move_right, null));
                    } else if (playerCurrDirection == Player.DIRECTION_RIGHT) {
                        playerImage.setX(player.moveRight());
                        playerImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.jarl_move_left, null));
                    } else {
                        if (player.getDirection() == Player.DIRECTION_LEFT) {
                            playerImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.jarl_left_idle, null));
                        } else if (player.getDirection() == Player.DIRECTION_RIGHT) {
                            playerImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.jarl_right_idle, null));
                        }
                    }
                });
            }
        }, 0, 20);

        buttonMovePlayer();
        handler.post(spawnRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(spawnRunnable);
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
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage());
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void buttonMovePlayer() {
        leftButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                leftButtonPressed = true;
                playerCurrDirection = Player.DIRECTION_LEFT;
                player.setDirection(Player.DIRECTION_LEFT);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                leftButtonPressed = false;
                playerCurrDirection = Player.DIRECTION_NONE;
            }
            return true;
        });

        rightButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                rightButtonPressed = true;
                playerCurrDirection = Player.DIRECTION_RIGHT;
                player.setDirection(Player.DIRECTION_RIGHT);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                rightButtonPressed = false;
                playerCurrDirection = Player.DIRECTION_NONE;
            }
            return true;
        });
    }

    private void spawnStones() {
        runOnUiThread(() -> {
            Stone randomStone = new Stone(this);

            int x = (gameLayoutParams.width - randomStone.getLayoutParams().width <= 0) ?
                    randomStone.getLayoutParams().width : gameLayoutParams.width - randomStone.getLayoutParams().width;

            randomStone.setImageResource(batoIDs.get(random.nextInt(batoIDs.size())));
            randomStone.setX(random.nextInt(x));
            randomStone.setY(-30);
            randomStone.setSpeed(random.nextFloat() * 3 + 25);

            gameLayout.addView(randomStone);
            spawnedStones.add(randomStone);
        });
    }
}
