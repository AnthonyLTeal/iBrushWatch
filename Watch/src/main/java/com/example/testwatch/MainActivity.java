package com.example.testwatch;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class MainActivity extends Activity implements SensorEventListener {
    private SensorManager sensorManager;
    private FileOutputStream fileOutputStream;
    private OutputStreamWriter outputWriter;
    private int seconds = 0;
    private boolean running;
    private boolean wasRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor_acce = sensorManager.getDefaultSensor((Sensor.TYPE_ACCELEROMETER));
        Sensor sensor_gyro = sensorManager.getDefaultSensor((Sensor.TYPE_GYROSCOPE));
        Sensor sensor_mag = sensorManager.getDefaultSensor((Sensor.TYPE_MAGNETIC_FIELD));
        sensorManager.registerListener(this, sensor_acce, 16670);
        sensorManager.registerListener(this, sensor_gyro, 16670);
        sensorManager.registerListener(this, sensor_mag, 16670);

        if (savedInstanceState != null)
        {
            seconds = savedInstanceState.getInt("seconds");
            running = savedInstanceState.getBoolean("running");
            wasRunning = savedInstanceState.getBoolean("wasRunning");
        }

        runTimer();
    }

    public void StartNewFile(){
        String file_name = "motion-data-"+LocalDateTime.now().toString()+".csv";
        try
        {
            this.fileOutputStream = openFileOutput(file_name, Context.MODE_APPEND);
            this.outputWriter = new OutputStreamWriter(fileOutputStream);
            Log.d("raw_data", "Creating new File: " + file_name);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public void CloseFile(){
        try {
            if (this.outputWriter != null) {
                this.outputWriter.flush();
                this.outputWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.outputWriter = null;
            this.fileOutputStream = null;
            Log.d("raw_data", "Closing File");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (running) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                float magX = sensorEvent.values[0];
                float magY = sensorEvent.values[1];
                float magZ = sensorEvent.values[2];

                String mag_x_str = Float.toString(magX);
                String mag_y_str = Float.toString(magY);
                String mag_z_str = Float.toString(magZ);

                String mag_str = LocalDateTime.now().toString() + "\tmag\t" + mag_x_str + "\t" + mag_y_str + "\t" + mag_z_str + "\n";
                try {
                    Log.d("raw_data", mag_str);
                    this.outputWriter.write(mag_str);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float accX = sensorEvent.values[0];
                float accY = sensorEvent.values[1];
                float accZ = sensorEvent.values[2];

                String acc_x_str = Float.toString(accX);
                String acc_y_str = Float.toString(accY);
                String acc_z_str = Float.toString(accZ);

                String acc_str = LocalDateTime.now().toString() + "\tacc\t" + acc_x_str + "\t" + acc_y_str + "\t" + acc_z_str + "\n";
                try {
                    //Log.d("raw_data", acc_str);
                    this.outputWriter.write(acc_str);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                float gyrX = sensorEvent.values[0];
                float gyrY = sensorEvent.values[1];
                float gyrZ = sensorEvent.values[2];

                String gy_x_str = Float.toString(gyrX);
                String gy_y_str = Float.toString(gyrY);
                String gy_z_str = Float.toString(gyrZ);

                String gy_str = LocalDateTime.now().toString() + "\tgyro\t" + gy_x_str + "\t" + gy_y_str + "\t" + gy_z_str + "\n";
                try {
                    //Log.d("raw_data", gy_str);
                    this.outputWriter.write(gy_str);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i){

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState
                .putInt("seconds", seconds);
        savedInstanceState
                .putBoolean("running", running);
        savedInstanceState
                .putBoolean("wasRunning", wasRunning);
    }

    public void onClickButton(View view)
    {
        if (running) {
            running = false;
            CloseFile();
            seconds = 0;
        }
        else {
            running = true;
            StartNewFile();
        }
    }

    private void runTimer() {

        final TextView timeView = (TextView) findViewById(R.id.time_view);

        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
                timeView.setText(time);

                if (running)
                {
                    seconds++;
                }

                handler.postDelayed(this, 1000);
            }
        });
    }
}