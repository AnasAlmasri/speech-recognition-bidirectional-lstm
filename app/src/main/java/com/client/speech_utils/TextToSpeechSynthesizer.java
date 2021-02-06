
package com.client.speech_utils;

import android.content.Context;
import android.media.AudioFormat;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.services.polly.AmazonPollyPresigningClient;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechPresignRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.polly.model.Voice;
import com.client.model_utils.RawToWavAudio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class TextToSpeechSynthesizer {

    private static Context CONTEXT;
    private static final String TAG = "DEBUG_SpeechSynthesizer";
    // Backend resources
    static AmazonPollyPresigningClient client;
    static URL presignedSynthesizeSpeechUrl;
    private List<Voice> voices;
    // Media player
    private static MediaPlayer mediaPlayer;
    SynthesizeSpeechRequest synthesizeSpeechRequest;
    private static boolean isInitialized = false;

    String filePath = Environment.getExternalStorageDirectory().getPath()
            + "/" + "app_record.pcm";

    public TextToSpeechSynthesizer(Context context) {
        Log.d(TAG, "TextToSpeechSynthesizer created");
        CONTEXT = context;
        initPollyClient();
    }

    public void initPollyClient() {
        Log.d(TAG, "initPollyClient called");
        new initializeClient().execute();
    }

    public void setupNewMediaPlayer() {
        Log.d(TAG, "setupNewMediaPlayer called");
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                setupNewMediaPlayer();
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
    }

    public void synthesize(String textToRead, String selectedVoice) {
        Log.d(TAG, "synthesize called");
        // Create speech synthesis request.
        synthesizeSpeechRequest =
                new SynthesizeSpeechRequest()
                        // Set text to synthesize.
                        .withText(textToRead)
                        // Set voice selected by the user.
                        .withVoiceId(selectedVoice)
                        // Set format to PCM.
                        .withOutputFormat(OutputFormat.Pcm);

        Log.d("test", synthesizeSpeechRequest.toString());

        // wait for client initialization thread to be done executing
        while (client == null) {}

        // start saving file on a different thread
        new saveAudioStreamTask().execute();

        /*
        // Get the presigned URL for synthesized speech audio stream.
        presignedSynthesizeSpeechUrl =
                client.getPresignedSynthesizeSpeechUrl(synthesizeSpeechPresignRequest);

        uploadFileToCloud();
        */
        // playAudioFile();

        /*
        Log.i(TAG, "Playing speech from presigned URL: " + presignedSynthesizeSpeechUrl);

        // Create a media player to play the synthesized audio stream.
        if (mediaPlayer.isPlaying()) {
            setupNewMediaPlayer();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            // Set media player's data source to previously obtained URL.
            mediaPlayer.setDataSource(presignedSynthesizeSpeechUrl.toString());
        } catch (IOException e) {
            Log.e(TAG, "Unable to set data source for the media player! " + e.getMessage());
        }

        // Start the playback asynchronously (since the data source is a network stream).
        mediaPlayer.prepareAsync();
        */
    }

    public void playAudioFile() {

        VoiceRecordingPlayer voiceRecordingPlayer = new VoiceRecordingPlayer(
                filePath, 16000,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        voiceRecordingPlayer.playRawAudio();
    }

    public void clientInitialized(boolean state) {
        isInitialized = state;
    }

    class initializeClient extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            AWSMobileClient.getInstance().initialize(CONTEXT, new Callback<UserStateDetails>() {
                @Override
                public void onResult(UserStateDetails result) {
                    // Create a client that supports generation of presigned URLs.
                    client = new AmazonPollyPresigningClient(AWSMobileClient.getInstance());
                    Log.d(TAG, "onResult: Created polly pre-signing client");

                    clientInitialized(true);

                    if (voices == null) {
                        // Create describe voices request.
                        DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();

                        try {
                            // Synchronously ask the Polly Service to describe available TTS voices.
                            DescribeVoicesResult describeVoicesResult = client.describeVoices(describeVoicesRequest);

                            // Get list of voices from the result.
                            voices = describeVoicesResult.getVoices();

                            // Log a message with a list of available TTS voices.
                            Log.i(TAG, "Available Polly voices: " + voices);
                        } catch (RuntimeException e) {
                            Log.e(TAG, "Unable to get available voices.", e);
                            return;
                        }
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "onError: Initialization error", e);
                }
            });
            return null;
        }
    }

    class saveAudioStreamTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "doInBackground called");
            if (client == null) {
                Log.d(TAG, "Client is null");
            }
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                SynthesizeSpeechResult synthesizeSpeechResult =
                        client.synthesizeSpeech(synthesizeSpeechRequest);
                Log.d("test", synthesizeSpeechResult.getContentType());

                byte[] buffer = new byte[2 * 1024];
                int readBytes;

                try (InputStream in = synthesizeSpeechResult.getAudioStream()){
                    while ((readBytes = in.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, readBytes);
                    }
                }

                // convert to wav and save file
                File raw = new File(filePath);
                File wav = new File(
                        Environment.getExternalStorageDirectory().getPath()
                                + "/" + "app_record.wav");
                new RawToWavAudio(raw, wav);
            } catch (Exception e) {
                System.err.println("Exception caught: " + e);
            }
            return null;
        }
    }

}
