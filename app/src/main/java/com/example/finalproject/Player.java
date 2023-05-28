package com.example.finalproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("AppCompatCustomView")
public class Player extends ImageView {
    public static final int DIRECTION_NONE = 0;
    public static final int DIRECTION_LEFT = 1;
    public static final int DIRECTION_RIGHT = 2;

    private ImageView shieldEffectImageView = null;
    private TextView shieldText = null;
    private TextView speedText = null;
    private TextView bombText = null;

    private RectF gameLayoutBounds = new RectF();
    private final RectF bounds = new RectF();
    private float speed = 20f;
    private boolean isShielded = false;

    private int currDirection = DIRECTION_NONE;
    private int prevDirection = DIRECTION_NONE;
    private float originalPosX = 0f;

    public Player(Context context) {
        super(context);
    }

    public Player(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Player(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void moveLeft() {
        setX(getX() - speed);

        if (getX() < 0) {
            setX(0);
        }

        float centerX = getX() + getWidth() / 2f;
        float centerY = getY() + getHeight() / 2f;
        float radiusX = getWidth() / 2f;
        float radiusY = getHeight() / 2f;
        bounds.set(centerX - radiusX, centerY - radiusY, centerX + radiusX, centerY + radiusY);

        updateShieldEffectViewPos();
        updatePowerupTextViewPos();
    }

    public void moveRight() {
        setX(getX() + speed);

        if (getX() > gameLayoutBounds.width() - bounds.width()) {
            setX(gameLayoutBounds.width() - bounds.width());
        }

        float centerX = getX() + getWidth() / 2f;
        float centerY = getY() + getHeight() / 2f;
        float radiusX = getWidth() / 2f;
        float radiusY = getHeight() / 2f;
        bounds.set(centerX - radiusX, centerY - radiusY, centerX + radiusX, centerY + radiusY);

        updateShieldEffectViewPos();
        updatePowerupTextViewPos();
    }

    public void setCurrDirection(int currDirection) {
        this.currDirection = currDirection;
    }

    public int getCurrDirection() {
        return currDirection;
    }

    public RectF getBounds() {
        return bounds;
    }

    public void setPrevDirection(int prevDirection) {
        this.prevDirection = prevDirection;
    }

    public int getPrevDirection() {
        return prevDirection;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }

    public void setShielded(boolean shielded) {
        isShielded = shielded;
    }

    public boolean isShielded() {
        return isShielded;
    }

    public ImageView getShieldEffectImageView() {
        return shieldEffectImageView;
    }

    public void setShieldEffectImageView(ImageView shieldEffectImageView) {
        this.shieldEffectImageView = shieldEffectImageView;
    }

    public void setShieldText(TextView shieldText) {
        this.shieldText = shieldText;
    }

    public TextView getShieldText() {
        return shieldText;
    }

    public void setSpeedText(TextView speedText) {
        this.speedText = speedText;
    }

    public TextView getSpeedText() {
        return speedText;
    }

    public void setBombText(TextView bombText) {
        this.bombText = bombText;
    }

    public TextView getBombText() {
        return bombText;
    }

    public void setParentBounds(RectF gameLayoutBounds) {
        this.gameLayoutBounds = gameLayoutBounds;
    }

    public void setOriginalPosX(float originalPosX) {
        this.originalPosX = originalPosX;
    }

    public void resetPosition() {
        setX(originalPosX);
        bounds.set(getX(), getY(), getX() + getWidth(), getY() + getHeight());
    }

    private void updateShieldEffectViewPos() {
        if (shieldEffectImageView != null) {
            // Calculate the center coordinates of the player
            float playerCenterX = getX() + getWidth() / 2f;
            float playerCenterY = getY() + getHeight() / 2f;

            // Calculate the top-left coordinates of the shield view
            float shieldViewX = playerCenterX - shieldEffectImageView.getWidth() / 2f;
            float shieldViewY = playerCenterY - shieldEffectImageView.getHeight() / 2f;

            // Position the shield view at the center of the player
            shieldEffectImageView.setX(shieldViewX);
            shieldEffectImageView.setY(shieldViewY);
        }
    }

    private void updatePowerupTextViewPos() {
        // Calculate the center coordinates of the player
        float playerCenterX = getX() + getWidth() / 2f;
        float playerCenterY = getY() + getHeight() / 2f;

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
    }
}
