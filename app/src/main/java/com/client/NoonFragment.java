package com.client;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.cloud.android.speech.R;

public class NoonFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    ImageView noonCircle;
    ImageView noonBars;
    TextView noonMessage;

    public NoonFragment() {
        // Required empty public constructor
    }

    public static NoonFragment newInstance() {
        NoonFragment fragment = new NoonFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_noon, container, false);

        noonCircle = (ImageView) view.findViewById(R.id.noonCircle);
        noonBars = (ImageView) view.findViewById(R.id.noonLines);
        noonMessage = (TextView) view.findViewById(R.id.noonMessage);

        noonCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.noonOnClick();
            }
        });

        noonMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {// Load the animation like this
                Animation animSlide = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
                        R.anim.exit_left_to_right);

                // Start the animation like this
                noonMessage.startAnimation(animSlide);
                noonMessage.setVisibility(View.INVISIBLE);
            }
        });

        mListener.initializeAnimationController(noonCircle, noonBars, noonMessage);
        //animateRotation();
        //animateTalking();
        //animateListening();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;

        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void initializeAnimationController(ImageView circle, ImageView bars, TextView message);
        void animateRotation();
        void animateListening();
        void animateTalking();
        void displayMessage(String message);
        void noonOnClick();
    }
}

