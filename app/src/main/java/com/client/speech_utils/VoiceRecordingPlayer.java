package com.client.speech_utils;

import android.media.AudioManager;
import android.media.AudioTrack;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class VoiceRecordingPlayer {

    private String FILE_PATH;
    private int SAMPLE_RATE;
    private int CHANNEL;
    private int ENCODING;
    private AudioTrack audio;

    public VoiceRecordingPlayer(String path, int sampleRate, int channel, int encoding) {
        FILE_PATH = path;
        SAMPLE_RATE = sampleRate;
        CHANNEL = channel;
        ENCODING = encoding;

        initializePlayer();
    }

    private void initializePlayer() {
        int bufSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING);

        audio = new AudioTrack(AudioManager.STREAM_MUSIC,
                SAMPLE_RATE, //sample rate
                CHANNEL, //2 channel
                ENCODING, // 16-bit
                bufSize,
                AudioTrack.MODE_STREAM );
        audio.play();
    }

    public void playRawAudio() {

        BufferedInputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(FILE_PATH));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        try {
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
        } catch (Exception e) { e.printStackTrace(); }

        audio.write(buffer.toByteArray(),0, buffer.toByteArray().length);
    }

    public void playRawAudio(byte[] data) {
        audio.write(data, 0, data.length);
    }
}
