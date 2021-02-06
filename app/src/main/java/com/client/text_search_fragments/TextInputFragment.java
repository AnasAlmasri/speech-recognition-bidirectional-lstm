package com.client.text_search_fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.textservice.TextInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.cloud.android.speech.R;;

public class TextInputFragment extends Fragment {
    private TextInputFragment.OnFragmentInteractionListener mListener;

    EditText searchEditText;
    Button proceedBtn;
    String searchTerm;

    public TextInputFragment() {
        // Required empty public constructor
    }

    public static TextInputFragment newInstance() {
        TextInputFragment fragment = new TextInputFragment();
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
        View view = inflater.inflate(R.layout.fragment_text_input, container, false);

        // retrieve initialized xml elements
        searchEditText = (EditText) view.findViewById(R.id.searchEditText);
        proceedBtn = (Button) view.findViewById(R.id.proceedBtn);

        proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchTerm = searchEditText.getText().toString();

                mListener.synthesizeSpeech(searchTerm);
                //TextToSpeechSynthesizer tts = new TextToSpeechSynthesizer(getApplicationContext());
                //tts.synthesize(searchTerm);
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void synthesizeSpeech(String searchTerm);
    }

}
