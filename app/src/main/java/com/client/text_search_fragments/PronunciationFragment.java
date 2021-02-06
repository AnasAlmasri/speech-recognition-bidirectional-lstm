package com.client.text_search_fragments;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;

import com.client.speech_utils.VoiceRecordingPlayer;
import com.google.cloud.android.speech.R;


public class PronunciationFragment extends Fragment {

    ImageView playBtn;
    Button proceedBtn;

    private OnFragmentInteractionListener mListener;

    public PronunciationFragment() {
        // Required empty public constructor
    }

    public static PronunciationFragment newInstance() {
        PronunciationFragment fragment = new PronunciationFragment();
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
        View view = inflater.inflate(R.layout.fragment_pronunciation, container, false);

        playBtn = (ImageView) view.findViewById(R.id.playBtn);
        proceedBtn = (Button) view.findViewById(R.id.proceedBtn);

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playAudio();
            }
        });

        proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.startVoiceSearchActivity();
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
        void startVoiceSearchActivity();
    }

    private void playAudio() {
        String filePath = Environment.getExternalStorageDirectory().getPath()
                + "/" + "app_record.pcm";

        VoiceRecordingPlayer voiceRecordingPlayer = new VoiceRecordingPlayer(
                filePath, 16000,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        voiceRecordingPlayer.playRawAudio();
    }

}
