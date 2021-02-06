package com.google.cloud.android.speech;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.client.NoonAnimationController;
import com.client.NoonFragment;
import com.client.controller_activities.InputActivity;
import com.client.model_utils.EC2Service;
import com.client.starting_fragments.LoginFragment;
import com.client.starting_fragments.WelcomeFragment;
import com.client.user_verification_utils.SpeakerRecognizer;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements WelcomeFragment.OnFragmentInteractionListener,
        LoginFragment.OnFragmentInteractionListener,
        NoonFragment.OnFragmentInteractionListener {

    private static final int REQUEST_READ_PHONE_STATE = 10;
    private static String filePath;
    private UUID profileId = null;
    NoonAnimationController noonAnimationController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("LOGS", "MainActivity: onCreate()");
        setContentView(R.layout.activity_main);

        filePath = Environment.getExternalStorageDirectory().getPath()
                + "/" + "user_record.wav";

        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        } else {
            //test();
        }

        //WaveformView waveformView = (WaveformView) findViewById(R.id.waveformView);
        //waveformView.updateVisualizer(fileToBytes(file));
/*
        Intent intent = new Intent(this, AnalysisActivity.class);
        startActivity(intent);
*/
        checkPermission();

        ec2TestConnection();

/*
        //startMainFragment();
        startWelcomeFragment();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startNoonFragment();
                startLoginFragment();
            }
        }, 4000);
*/

    }

    private void ec2TestConnection() {
        String ami = "ami-0dad20bd1b9c8c004";
        String instanceType = "t2.micro";
        String keyPair = "noon_ec2";

        //EC2Service.requestInstance(this, ami, instanceType, keyPair);
        EC2Service.requestInstances(getApplicationContext());

    }

    private void test() {
        SpeakerRecognizer speakerRecognizer = new SpeakerRecognizer(MainActivity.this);

        speakerRecognizer.initializeRecognitionClient();

        if (speakerRecognizer.isClientInitialized()) {
            speakerRecognizer.recognizerSpeaker(filePath);
        }
    }

    /*
    public void buildWaveformView() {
        File file = new File(filePath);
        byte[] bytes = fileToBytes(file);

        GraphView graphView = (GraphView) findViewById(R.id.graph);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
        double x=0, y, sum;
        int i, j, stride;

        stride = 1000;
        Log.d("length", String.valueOf(bytes.length));
        for (i = 0; i < bytes.length; i+=stride) {
            x += 0.000001;
            sum = 0;
            for (j = 0; j < stride; j++) {
                if (i+j < bytes.length) {
                    sum += bytes[i + j];
                }
            }
            y = Double.valueOf(sum/stride);
            series.appendData(new DataPoint(x, y), true, bytes.length);
        }

        graphView.addSeries(series);
    }
    */

    public static byte[] fileToBytes(File file) {
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    // method to create replace contents of shared View with LoginFragment
    public void startLoginFragment() {
        LoginFragment newFragment = LoginFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, newFragment);
        fragmentTransaction.commit();
    }

    // method to create replace contents of shared View with WelcomeFragment
    public void startWelcomeFragment() {
        WelcomeFragment newFragment = WelcomeFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_right_to_left);
        fragmentTransaction.replace(R.id.noonContainer, newFragment);
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

    public void startInputActivity() {
        Intent intent = new Intent(getApplicationContext(), InputActivity.class);
        startActivity(intent);
    }

    @Override
    public void loginBtnOnClick() {
        startInputActivity();
    }

    @Override
    public void newUserBtnOnClick() {
        //Intent intent = new Intent(getApplicationContext(), NewUserActivity.class);
        //startActivity(intent);
    }

    public void initializeAnimationController(ImageView circle, ImageView bars, TextView message) {
        noonAnimationController = new NoonAnimationController(circle, bars, message);
        //animateTalking();
        noonAnimationController.displayMessage("Hello! My name is Noon.\nI'm your pronunciation\nassistant.");
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

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //test();
                }
                break;

            default:
                break;
        }
    }

}
