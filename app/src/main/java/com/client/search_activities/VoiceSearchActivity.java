/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.client.search_activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.client.NoonAnimationController;
import com.client.NoonFragment;
import com.client.controller_activities.ComparisonActivity;
import com.client.model_utils.RawToWavAudio;
import com.client.voice_search_fragments.ConfirmationFragment;
import com.client.voice_search_fragments.ListeningFragment;
import com.google.cloud.android.speech.R;
import com.google.cloud.android.speech.MessageDialogFragment;
import com.google.cloud.android.speech.SpeechService;
import com.google.cloud.android.speech.VoiceRecorder;
import java.io.File;
import java.util.ArrayList;
import android.support.v7.widget.RecyclerView;


public class VoiceSearchActivity extends AppCompatActivity
        implements MessageDialogFragment.Listener,
        ListeningFragment.OnFragmentInteractionListener,
        ConfirmationFragment.OnFragmentInteractionListener,
        NoonFragment.OnFragmentInteractionListener {

    private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";

    private static final String STATE_RESULTS = "results";

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    private SpeechService mSpeechService;

    private VoiceRecorder mVoiceRecorder;

    NoonAnimationController noonAnimationController;

    private ResultAdapter mResultAdapter;

    private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback() {

        @Override
        public void onVoiceStart() {
            Log.d("DEBUG", "VoiceSearchActivity: onVoiceStart()");
            if (mSpeechService != null) {
                mSpeechService.startRecognizing(mVoiceRecorder.getSampleRate());
            }
        }

        @Override
        public void onVoice(byte[] data, int size) {
            //Log.d("DEBUG", "VoiceSearchActivity: onVoice()");
            if (mSpeechService != null) {
                mSpeechService.recognize(data, size);
            }
        }

        @Override
        public void onVoiceEnd() {
            Log.d("DEBUG", "VoiceSearchActivity: onVoiceEnd()");
            if (mSpeechService != null) {
                mSpeechService.finishRecognizing();
            }
        }

    };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            Log.d("DEBUG", "VoiceSearchActivity: onServiceConnected()");
            mSpeechService = SpeechService.from(binder);
            mSpeechService.addListener(mSpeechServiceListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("DEBUG", "VoiceSearchActivity: onServiceDisconnected()");
            mSpeechService = null;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("DEBUG", "VoiceSearchActivity: onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ArrayList<String> results = savedInstanceState == null ? null :
                savedInstanceState.getStringArrayList(STATE_RESULTS);
        mResultAdapter = new ResultAdapter(results);

        startNoonFragment();
    }

    @Override
    protected void onStart() {
        Log.d("DEBUG", "VoiceSearchActivity: onStart()");
        super.onStart();

        // Prepare Cloud Speech API
        bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);

        // Start listening to voices
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startListeningFragment();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            showPermissionMessageDialog();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    @Override
    protected void onStop() {
        Log.d("DEBUG", "VoiceSearchActivity: onStop()");
        // Stop listening to voice
        stopVoiceRecorder();

        // Stop Cloud Speech API
        mSpeechService.removeListener(mSpeechServiceListener);
        unbindService(mServiceConnection);
        mSpeechService = null;

        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mResultAdapter != null) {
            outState.putStringArrayList(STATE_RESULTS, mResultAdapter.getResults());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (permissions.length == 1 && grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListeningFragment();
            } else {
                showPermissionMessageDialog();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

// recognizing from file
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_file:
                //mSpeechService.recognizeInputStream(getResources().openRawResource(R.raw.audio));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startVoiceRecorder() {
        Log.d("DEBUG", "VoiceSearchActivity: startVoiceRecorder()");
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
        }
        String filePath = Environment.getExternalStorageDirectory().getPath()
                + "/" + "user_record.pcm";
        mVoiceRecorder = new VoiceRecorder(mVoiceCallback, this, filePath);
        mVoiceRecorder.start();
    }

    public void stopVoiceRecorder() {
        Log.d("DEBUG", "VoiceSearchActivity: stopVoiceRecorder()");
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
            mVoiceRecorder = null;
        }
    }

    private void showPermissionMessageDialog() {
        MessageDialogFragment
                .newInstance(getString(R.string.permission_message))
                .show(getSupportFragmentManager(), FRAGMENT_MESSAGE_DIALOG);
    }

    @Override
    public void onMessageDialogDismissed() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    private final SpeechService.Listener mSpeechServiceListener =
            new SpeechService.Listener() {
                @Override
                public void onSpeechRecognized(final String text, final boolean isFinal) {
                    Log.d("DEBUG", "VoiceSearchActivity: onSpeechRecognized()");
                    if (isFinal) {
                        mVoiceRecorder.dismiss();
                    }
                    if (!TextUtils.isEmpty(text)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isFinal) {
                                    mResultAdapter.addResult(text);
                                }
                            }
                        });
                    }
                }
            };

    @Override
    public ArrayList<String> getTranscriptionResults() {
        Log.d("DEBUG", "VoiceSearchActivity: getTranscriptionResults()");
        return mResultAdapter.getResults();
    }

    @Override
    public void startListeningFragment() {
        Log.d("DEBUG", "VoiceSearchActivity: startListeningFragment()");
        ListeningFragment newFragment = ListeningFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, newFragment);
        fragmentTransaction.commit();

        startVoiceRecorder();
        displayMessage("You can start speaking now!");
    }

    private void startConfirmationFragment() {
        Log.d("DEBUG", "VoiceSearchActivity: startConfirmationFragment()");
        ConfirmationFragment newFragment = ConfirmationFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, newFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void startComparisonActivity(String text) {
        Log.d("DEBUG", "VoiceSearchActivity: startComparisonActivity()");
        Log.d("LOGS", "start comparison: " + text);
        Intent intent = new Intent(getApplicationContext(), ComparisonActivity.class);
        intent.putExtra("text", text);
        startActivity(intent);
    }

    @Override
    public void stopListening() {
        Log.d("DEBUG", "VoiceSearchActivity: stopListening()");
        // finish up with recording process
        stopVoiceRecorder();

        // convert raw audio to wav an save it
        File raw = new File(
                Environment.getExternalStorageDirectory().getPath()
                        + "/" + "user_record.pcm");
        File wav = new File(
                Environment.getExternalStorageDirectory().getPath()
                        + "/" + "user_record.wav");
        new RawToWavAudio(raw, wav);

        // update UI elements
        displayMessage("Is this what you had in mind?\nSwipe right if yes, and left\nif no.");

        // redirect user to next component
        startConfirmationFragment();
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        TextView text;

        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_result, parent, false));
            text = (TextView) itemView.findViewById(R.id.text);
        }

    }

    private static class ResultAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final ArrayList<String> mResults = new ArrayList<>();

        ResultAdapter(ArrayList<String> results) {
            if (results != null) {
                mResults.addAll(results);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.text.setText(mResults.get(position));
        }

        @Override
        public int getItemCount() {
            return mResults.size();
        }

        void addResult(String result) {
            Log.d("DEBUG", "VoiceSearchActivity: addResult()");
            mResults.add(0, result);
            notifyItemInserted(0);
        }

        public ArrayList<String> getResults() {
            return mResults;
        }

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
        //animateTalking();
        noonAnimationController.displayMessage("You can start speaking now!");
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