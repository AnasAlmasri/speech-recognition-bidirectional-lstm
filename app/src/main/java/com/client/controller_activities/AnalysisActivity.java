package com.client.controller_activities;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.client.search_activities.VoiceSearchActivity;
import com.google.cloud.android.speech.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AnalysisActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        String appPath = Environment.getExternalStorageDirectory().getPath()
                + "/" + "app_record.wav";
        String userPath = Environment.getExternalStorageDirectory().getPath()
                + "/" + "user_record.wav";

        GraphView graph = (GraphView) findViewById(R.id.comparisonGraph);
        buildWaveformView(new File(appPath), graph);
        buildWaveformView(new File(userPath), graph);
    }

    private void loadModel() {

    }

    public void tryAgainBtnOnClick(View view) {
        Intent intent = new Intent(this, VoiceSearchActivity.class);
        startActivity(intent);
    }

    public void startOverBtnOnClick(View view) {
        Intent intent = new Intent(this, InputActivity.class);
        startActivity(intent);
    }


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

    public void buildWaveformView(File file, GraphView graphView) {
        byte[] bytes = fileToBytes(file);

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

}
