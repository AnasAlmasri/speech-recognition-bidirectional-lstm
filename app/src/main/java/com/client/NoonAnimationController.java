package com.client;

import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.cloud.android.speech.R;

public class NoonAnimationController {

    AnimationDrawable barAnimation;
    ImageView noonCircle;
    ImageView noonBars;
    TextView noonMessage;

    public NoonAnimationController(ImageView circle, ImageView bars, TextView message) {
        noonCircle = circle;
        noonBars = bars;
        noonMessage = message;

        noonMessage.setVisibility(View.INVISIBLE);
    }

    public void animateRotation() {
        RotateAnimation rotate =
                new RotateAnimation(
                        0,
                        360,
                        Animation.RELATIVE_TO_SELF,
                        0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f);

        rotate.setDuration(1000);
        rotate.setInterpolator(new LinearInterpolator());

        noonCircle.startAnimation(rotate);
    }

    public void animateListening() {
        noonBars.setBackgroundResource(R.drawable.noon_listening_animator);
        barAnimation = (AnimationDrawable) noonBars.getBackground();
        animateRotation();
        barAnimation.start();
    }

    public void animateTalking() {
        noonBars.setBackgroundResource(R.drawable.noon_talking_animator);
        barAnimation = (AnimationDrawable) noonBars.getBackground();
        animateRotation();
        barAnimation.start();
    }

    public void stopTalking() {
        barAnimation.stop();
    }

    public void stopListening() {
        barAnimation.stop();
    }

    public void displayMessage(String message) {
        ScaleAnimation scale = new ScaleAnimation(0, 1, 0, 1, ScaleAnimation.RELATIVE_TO_SELF, .5f, ScaleAnimation.RELATIVE_TO_SELF, .5f);
        scale.setDuration(1000);
        scale.setInterpolator(new OvershootInterpolator());

        animateRotation();
        noonMessage.setText(message);
        noonMessage.setVisibility(View.VISIBLE);
        noonMessage.startAnimation(scale);
    }
}
