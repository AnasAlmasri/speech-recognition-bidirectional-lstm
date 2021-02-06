package com.client.controller_activities;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.client.NoonAnimationController;
import com.client.NoonFragment;
import com.client.input_type_fragments.SaySomethingFragment;
import com.client.input_type_fragments.TypeSomethingFragment;
import com.client.search_activities.TextSearchActivity;
import com.client.search_activities.VoiceSearchActivity;

import com.google.cloud.android.speech.R;

public class InputActivity extends AppCompatActivity
        implements TypeSomethingFragment.OnFragmentInteractionListener,
        SaySomethingFragment.OnFragmentInteractionListener,
        NoonFragment.OnFragmentInteractionListener {

    private FrameLayout upperContainer;
    private FrameLayout lowerContainer;

    NoonAnimationController noonAnimationController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        upperContainer = (FrameLayout) findViewById(R.id.upperFragmentContainer);
        lowerContainer = (FrameLayout) findViewById(R.id.lowerFragmentContainer);

        upperContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TextSearchActivity.class);
                startActivity(intent);
            }
        });

        lowerContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), VoiceSearchActivity.class);
                startActivity(intent);
            }
        });

        startNoonFragment();
        startTypeSomethingFragment();
        startSaySomethingFragment();
    }

    public void startTypeSomethingFragment() {
        TypeSomethingFragment typeFragment = TypeSomethingFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_down, R.anim.slide_out_down);
        fragmentTransaction.replace(R.id.upperFragmentContainer, typeFragment);
        // Start the animated transition.
        fragmentTransaction.commit();
    }

    public void startSaySomethingFragment() {
        SaySomethingFragment sayFragment = SaySomethingFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
        fragmentTransaction.replace(R.id.lowerFragmentContainer, sayFragment);
        // Start the animated transition.
        fragmentTransaction.commit();
    }

    public void startNoonFragment() {
        NoonFragment newFragment = NoonFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.noonContainer, newFragment);
        fragmentTransaction.commit();
    }

    public void initializeAnimationController(ImageView circle, ImageView bars, TextView message) {
        noonAnimationController = new NoonAnimationController(circle, bars, message);

        noonAnimationController.displayMessage("Let's choose the way you\nwould like to input a phrase.");
        //animateTalking();
    }

    public void animateRotation() {
        noonAnimationController.animateRotation();
    }

    public void animateListening() {
        noonAnimationController.animateListening();
    }

    public void animateTalking() {
        noonAnimationController.animateTalking();
    }

    public void displayMessage(String message) {
        noonAnimationController.displayMessage(message);
    }

    public void noonOnClick() {
        displayMessage("this is another message");
    }
}
