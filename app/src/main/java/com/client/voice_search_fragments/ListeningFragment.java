package com.client.voice_search_fragments;

import android.content.Context;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.cloud.android.speech.R;
import com.skyfishjy.library.RippleBackground;

public class ListeningFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public ListeningFragment() {
        // Required empty public constructor
    }

    public static ListeningFragment newInstance() {
        ListeningFragment fragment = new ListeningFragment();
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
        View view = inflater.inflate(R.layout.fragment_listening, container, false);
        final RippleBackground rippleBackground = (RippleBackground) view.findViewById(R.id.rippleEffect);
        final Button stopRecBtn = (Button) view.findViewById(R.id.stopRecBtn);

        rippleBackground.startRippleAnimation();

        stopRecBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rippleBackground.stopRippleAnimation();
                mListener.stopListening();
            }
        });

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
        //void startVoiceRecorder();
        void stopListening();
    }
}
