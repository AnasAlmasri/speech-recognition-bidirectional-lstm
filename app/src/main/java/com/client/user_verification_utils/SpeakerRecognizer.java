package com.client.user_verification_utils;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.cloud.android.speech.R;
import com.microsoft.cognitive.speakerrecognition.SpeakerIdentificationClient;
import com.microsoft.cognitive.speakerrecognition.SpeakerIdentificationRestClient;
import com.microsoft.cognitive.speakerrecognition.contract.identification.CreateProfileResponse;
import com.microsoft.cognitive.speakerrecognition.contract.identification.EnrollmentOperation;
import com.microsoft.cognitive.speakerrecognition.contract.identification.IdentificationOperation;
import com.microsoft.cognitive.speakerrecognition.contract.identification.OperationLocation;
import com.microsoft.cognitive.speakerrecognition.contract.identification.Status;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.UUID;

public class SpeakerRecognizer {

    private static String TAG = "DEBUG_SpeakerRecognizer";
    private Context CONTEXT;
    private UUID profileId = null;

    private SpeakerIdentificationClient client;
    private String filePath;

    public SpeakerRecognizer(Context context) {
        CONTEXT = context;
    }

    public void initializeRecognitionClient() {
        client = new SpeakerIdentificationRestClient("8c6a806a2e5c47858e2c5cccb1a66b26");
        Log.d(TAG, "client initialized");
    }

    public boolean isClientInitialized() {
        if (client == null) { return false; }
        return true;
    }

    public void recognizerSpeaker(String filePath) {
        Log.d(TAG, "recognize speaker");
        this.filePath = filePath;

        identifyFromAudio();

        if (profileId == null) {
            enrollSpeaker();
        } else {
            identifyFromAudio();
        }
    }

    private void identifyFromAudio() {
        Log.d(TAG, "identify speaker");

        new SpeakerIdentificationTask().execute();
    }

    private void enrollSpeaker() {
        Log.d(TAG, "enroll speaker");

        new SpeakerEnrolmentTask().execute();
    }

    class SpeakerIdentificationTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // Identify using recorded audio
                OperationLocation loc = client.identify(new FileInputStream(filePath),
                        Arrays.asList(profileId));

                Log.d(TAG, "waiting for result");
                // Wait for result
                IdentificationOperation op = client.checkIdentificationStatus(loc);

                Log.d(TAG, "op.status " + op.status.toString());
                while (op.status ==
                        com.microsoft.cognitive.speakerrecognition.contract.identification
                                .Status.RUNNING
                        || op.status ==
                        com.microsoft.cognitive.speakerrecognition.contract.identification
                                .Status.NOTSTARTED) {
                    op = client.checkIdentificationStatus(loc);
                }

                Log.d(TAG, "op.status " + op.status.toString());
                // Show result to user
                if (op.status ==
                        com.microsoft.cognitive.speakerrecognition.contract.identification.Status.SUCCEEDED) {
                    Log.d(TAG, "identification success");
                    new AlertDialog.Builder(CONTEXT)
                            .setTitle("Identified User")
                            .setMessage("User was identified as: " + op.processingResult.identifiedProfileId)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    Log.d(TAG, "identification failed");
                    new AlertDialog.Builder(CONTEXT)
                            .setTitle("Unable to identify user")
                            .setMessage("Status: " + op.message)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            } catch (Exception e) {
                Log.d(TAG, "error identifying speaker");
                e.printStackTrace();
            }

            return null;
        }
    }

    class SpeakerEnrolmentTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                CreateProfileResponse response = client.createProfile("en-US");
                profileId = response.identificationProfileId;

                Log.d(TAG, "returned profileId: " + profileId.toString());

                OperationLocation loc = client.enroll(new FileInputStream(filePath), profileId, false);

                Log.d(TAG, "waiting for status");
                // Wait for enrollment status
                EnrollmentOperation op = client.checkEnrollmentStatus(loc);

                Log.d(TAG, "op.status " + op.status.toString());
                while (op.status == com.microsoft.cognitive.speakerrecognition.contract.identification.Status.RUNNING || op.status == com.microsoft.cognitive.speakerrecognition.contract.identification.Status.NOTSTARTED) {
                    op = client.checkEnrollmentStatus(loc);
                }
                Log.d(TAG, "op.status " + op.status.toString());

                // Bail out if it failed...
                if (op.status != com.microsoft.cognitive.speakerrecognition.contract.identification.Status.SUCCEEDED)
                    Log.d(TAG, "enrolment failed");
                    return null;

            } catch (Exception e) {
                Log.d(TAG, "error enrolling speaker");
                e.printStackTrace();
            }

            return null;
        }
    }

}
