package com.client.search_activities;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.client.NoonAnimationController;
import com.client.NoonFragment;
import com.client.speech_utils.TextToSpeechSynthesizer;
import com.client.text_search_fragments.PronunciationFragment;
import com.client.text_search_fragments.TextInputFragment;
import com.google.cloud.android.speech.R;

import java.util.Locale;

public class TextSearchActivity extends AppCompatActivity
        implements SpellCheckerSession.SpellCheckerSessionListener,
        TextInputFragment.OnFragmentInteractionListener,
        NoonFragment.OnFragmentInteractionListener,
        PronunciationFragment.OnFragmentInteractionListener {

    TextToSpeechSynthesizer textToSpeechSynthesizer;
    SpellCheckerSession spellCheckerSession;
    NoonAnimationController noonAnimationController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up);
        setContentView(R.layout.activity_text_search);

        // prepare spelling checker API
        TextServicesManager textServicesManager = (TextServicesManager) getSystemService(
                TEXT_SERVICES_MANAGER_SERVICE);
        spellCheckerSession = textServicesManager.newSpellCheckerSession(
                null, Locale.ENGLISH, this, false);

        // prepare speech synthesizer
        textToSpeechSynthesizer = new TextToSpeechSynthesizer(
                getApplicationContext());
        textToSpeechSynthesizer.setupNewMediaPlayer();

        startNoonFragment();
        startTextInputFragment();
    }

    @Override
    public void onGetSuggestions(SuggestionsInfo[] suggestionsInfos) {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < suggestionsInfos.length; ++i) {
            // Returned suggestions are contained in SuggestionsInfo
            final int len = suggestionsInfos[i].getSuggestionsCount();
            Log.d("suggest", String.valueOf(len));
            sb.append('\n');

            for (int j = 0; j < len; ++j) {
                sb.append("," + suggestionsInfos[i].getSuggestionAt(j));
            }

            sb.append(" (" + len + ")");
        }
        runOnUiThread(new Runnable() {
            public void run() {
                //tv1.append(sb.toString());
            }
        });
    }

    @Override
    public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] sentenceSuggestionsInfos) {
        Log.d("suggest", sentenceSuggestionsInfos[0].toString());
    }

    public void startNoonFragment() {
        NoonFragment newFragment = NoonFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.noonContainer, newFragment);
        fragmentTransaction.commit();
    }

    private void startTextInputFragment() {
        Log.d("DEBUG", "TextSearchActivity: startTextInputFragment()");
        TextInputFragment newFragment = TextInputFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, newFragment);
        fragmentTransaction.commit();
    }

    public void startPronunciationFragment() {
        PronunciationFragment newFragment = PronunciationFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, newFragment);
        fragmentTransaction.commit();

        displayMessage("Here's my pronunciation.\nWhen you're ready to try, hit\n" +
                "the button 'Let me try'.");
    }

    public void startVoiceSearchActivity() {
        Intent intent = new Intent(this, VoiceSearchActivity.class);
        startActivity(intent);
    }

    public void initializeAnimationController(ImageView circle, ImageView bars, TextView message) {
        noonAnimationController = new NoonAnimationController(circle, bars, message);
        //animateTalking();
        noonAnimationController.displayMessage("What would you like me\nto pronounce?");
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
        //displayMessage("this is another message");
    }
    public void synthesizeSpeech(String searchTerm) {
        textToSpeechSynthesizer.synthesize(searchTerm, "Salli");
        startPronunciationFragment();
    }
}
