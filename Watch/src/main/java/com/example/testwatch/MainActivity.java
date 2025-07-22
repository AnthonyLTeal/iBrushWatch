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
import org.tensorflow.lite.Interpreter;

public class MainActivity extends Activity implements SensorEventListener {

    private TextView mTextView;
    private TextView currentX, currentY, currentZ, currentGx,currentGy,currentGz;
    private SensorManager sensorManager;
    private Sensor sensor_acce;
    private Sensor sensor_gyro;
    private float X, Y, Z, Gx,Gy,Gz, lastX, lastY, lastZ, lastGx,lastGy,lastGz;
    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;
    private float deltaGx = 0;
    private float deltaGy = 0;
    private float deltaGz = 0;
    private String file_name;
    private FileOutputStream fileOutputStream;
    private OutputStreamWriter outputWriter;
    private String acc_file_name;
    private String gy_file_name;
    private FileOutputStream accFileOutputStream;
    private FileOutputStream gyFileOutputStream;
    private OutputStreamWriter accFileWriter;
    private OutputStreamWriter gyFileWriter;

    private LocalDateTime localDateTime;

    private int seconds = 0;
    private boolean running;
    private boolean wasRunning;
    private Interpreter tflite;

    private static final int FEATURECOUNT = 103;
    private static final int DATAPOINTS = 6;
    private static final int TECHNIQUECOUNT = 7;
    private static final int EPOCHS = 50;

    private static final String[] TECHNIQUES = new String[]{"Bass", "Charter", "Fones", "Horizontal", "Rolling", "Stillman", "Vertical"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        initializeView();

        acc_file_name = "Acc-motion-data" + localDateTime.now().toString() +".csv";
        gy_file_name = "Gy-motion-data" + localDateTime.now().toString() +".csv";
        file_name = "motion-data-"+localDateTime.now().toString()+".csv";
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor_acce = sensorManager.getDefaultSensor((Sensor.TYPE_ACCELEROMETER));
        sensor_gyro = sensorManager.getDefaultSensor((Sensor.TYPE_GYROSCOPE));
        sensorManager.registerListener(this, sensor_acce, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensor_gyro, SensorManager.SENSOR_DELAY_GAME);

        if (savedInstanceState != null)
        {
            seconds = savedInstanceState.getInt("seconds");
            running = savedInstanceState.getBoolean("running");
            wasRunning = savedInstanceState.getBoolean("wasRunning");
        }
        runTimer();

        try
        {
            this.fileOutputStream = openFileOutput(file_name, Context.MODE_APPEND);
            this.outputWriter = new OutputStreamWriter(fileOutputStream);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public void initializeView(){
        currentX = (TextView) findViewById(R.id.currentX);
        currentY = (TextView) findViewById(R.id.currentY);
        currentZ = (TextView) findViewById(R.id.currentZ);
        currentGx = (TextView) findViewById(R.id.currentGx);
        currentGy = (TextView) findViewById(R.id.currentGy);
        currentGz = (TextView) findViewById(R.id.currentGz);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (running) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                String acc_x_str = Float.toString(sensorEvent.values[0]);
                String acc_y_str = Float.toString(sensorEvent.values[1]);
                String acc_z_str = Float.toString(sensorEvent.values[2]);
                String acc_str = localDateTime.now().toString() + "\tacc\t" + acc_x_str + "\t" + acc_y_str + "\t" + acc_z_str + "\n";
                try {
                    Log.d("raw_data", acc_str);
                    this.outputWriter.write(acc_str);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                deltaX = Math.abs(lastX - sensorEvent.values[0]);
                deltaY = Math.abs(lastY - sensorEvent.values[1]);
                deltaZ = Math.abs(lastZ - sensorEvent.values[2]);
                if (deltaX < 2) {
                    deltaX = 0;
                }
                if (deltaY < 2) {
                    deltaY = 0;
                }
                if (deltaZ < 2) {
                    deltaZ = 0;
                }
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                String gy_x_str = Float.toString(sensorEvent.values[0]);
                String gy_y_str = Float.toString(sensorEvent.values[1]);
                String gy_z_str = Float.toString(sensorEvent.values[2]);
                String gy_str = localDateTime.now().toString() + "\tgyro\t" + gy_x_str + "\t" + gy_y_str + "\t" + gy_z_str + "\n";
                try {
                    Log.d("raw_data", gy_str);
                    this.outputWriter.write(gy_str);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                deltaGx = Math.abs(lastGx - sensorEvent.values[0]);
                deltaGy = Math.abs(lastGy - sensorEvent.values[1]);
                deltaGz = Math.abs(lastGz - sensorEvent.values[2]);
                if (deltaGx < 2) {
                    deltaGx = 0;
                }
                if (deltaGy < 2) {
                    deltaGy = 0;
                }
                if (deltaGz < 2) {
                    deltaGz = 0;
                }
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
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
        }
        else {
            running = true;
        }
    }

    private float[] doTraining(float[][] processedData, int classifier){
        float[] losses = new float[EPOCHS];
        for (int epoch = 0; epoch < EPOCHS; epoch++){
            if (tflite == null)
                return null;
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("x", processedData);
            int[] classifiers = new int[processedData.length];
            for (int i = 0; i < processedData.length; i++){
                classifiers[i] = classifier;
            }
            inputs.put("y", classifiers);

            Map<String, Object> outputs = new HashMap<>();
            FloatBuffer loss = FloatBuffer.allocate(1);
            outputs.put("loss", loss);

            tflite.runSignature(inputs, outputs, "train");
            //losses[epoch] = loss.get(0);
            //Log.d("INFO", "Losses: " + loss.get(0));
        }

        //Log.d("INFO", "Losses: " + Arrays.toString(losses));

        return losses;
    }

    private float[][] doInference(float[][] processedData)
    {
        if (tflite == null)
            return null;
        //do inference
        float[][] inputVal= processedData; // inputVal should be set to the data of one of the users xml files loaded as an appropriate array
        float[][] output=new float[processedData.length][TECHNIQUECOUNT];
        tflite.run(inputVal,output);
        return output;
        //tflite.runSignature(inputVal, output, "");
        //return new float[0][0];
    }

    private float[][] loadTestData(String filename)
    {
        List<String> lines = new ArrayList<String>();

        try {
            BufferedReader reader;
            InputStream file = this.getAssets().open(filename);
            reader = new BufferedReader(new InputStreamReader(file));

            String line = reader.readLine();
            while(line != null){
                lines.add(line);
                line = reader.readLine();
            }
            reader.close();

        } catch(IOException ioe){
            ioe.printStackTrace();
        }

        float[][] testData = new float[lines.size()][DATAPOINTS];

        for (int i = 0; i < lines.size(); i++)
        {
            //TODO this is where my error is, the entire 103 elements should be copied
            String[] cells = lines.get(i).split(",");
            for (int j = 0; j < cells.length; j++){
                testData[i][j] = Float.parseFloat(cells[j]);
            }
        }

        return testData;
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor=this.getAssets().openFd("ibrushtf.tflite");
        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startOffset=fileDescriptor.getStartOffset();
        long declareLength=fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declareLength);
    }

    private void loadModel()
    {
        //load the model
        try {
            tflite = new Interpreter(loadModelFile());
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public float[][] prepareData(float[][] rawData) throws Exception {
        Vector3D[] accelerometerData = new Vector3D[rawData.length];
        Vector3D[] gyroData = new Vector3D[rawData.length];
        for (int i = 0; i < rawData.length; i++) {
            accelerometerData[i] = new Vector3D(rawData[i][0], rawData[i][1], rawData[i][2]);
            gyroData[i] = new Vector3D(rawData[i][3], rawData[i][4], rawData[i][5]);
        }

        GravityData gravityData = DataProcessor.GravityFilter(accelerometerData);
        Vector2D[] angles = DataProcessor.GetPartialEulerAngles(gravityData.getGravityData());
        Vector3D[] complement = DataProcessor.ComplementaryFilter(gyroData, angles);

        //need to remove 0th element from linearAcceleration since we removed it from the gravity angles in the complementaryFilter
        Vector3D[] linearAcceleration = new Vector3D[gravityData.getLinearAcceleration().length - 1];
        for (int i = 0; i < linearAcceleration.length; i++){
            linearAcceleration[i] = gravityData.getLinearAcceleration()[i + 1];
        }

        double[][][] windows = DataProcessor.CreateSlidingWindow(complement, linearAcceleration, 100, .5f );

        float[][] scaled = new float[windows.length][FEATURECOUNT];
        for (int i = 0; i < windows.length; i++){
            double[] features = FeatureExtractor.Features(windows[i]);
            scaled[i] = Scaler.Fitf(features);
            //Log.d("INFO", Arrays.toString(scaled[i]));
        }
        return scaled;
    }

    public void onClickTrainTest() {
        ArrayList<float[]> trainingData = new ArrayList<>();
        String[] testList = new String[]{"HorizontalAnthony1_processed.csv"};
        //loadModel();

        try {
            for (int i = 0; i < testList.length; i++){
                String file = testList[i];
                //Log.d("INFO", "File Loaded: " + file);
                float[][] testData = loadTestData(file);
                float[][] preparedData = prepareData(testData);
                trainingData.addAll(Arrays.asList(preparedData));
            }
            float[][] trainArr = new float[trainingData.size()][FEATURECOUNT];
            trainArr = trainingData.toArray(trainArr);
            doTraining(trainArr, 3);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int GetTechniqueFromFileName(String filename){
        for (int i = 0; i < TECHNIQUES.length; i++){
            String technique = TECHNIQUES[i].toUpperCase();
            if (filename.toUpperCase().contains(technique)){
                return i;
            }
        }
        return -1;
    }

    public void onClickPredict(View view)
    {
        try {
            loadModel();
            onClickTrainTest();
            String[] fileList = this.getAssets().list("");
            int[][] confusionMatrix = new int[TECHNIQUECOUNT][TECHNIQUECOUNT];
            for (String file : fileList)
            {
                //Log.d("INFO", "Filename: " + file);
                int techniqueIndex = GetTechniqueFromFileName(file);

                if (!file.contains(".csv")){
                    continue;
                }
                long initialTime = System.currentTimeMillis();

                float[][] testData = loadTestData(file);
                float[][] preparedData = prepareData(testData);
                float[][] result = doInference(preparedData);
                long endTime = System.currentTimeMillis();
                float totalTimeMilli = endTime - initialTime;

                float[] finalResults = new float[TECHNIQUECOUNT];

                for (int i = 0; i < result.length; i++){
                    String line = "";
                    int predictedIndex = 0;
                    for (int j = 0; j < result[i].length; j++){
                        line = line + " " + result[i][j];
                        finalResults[j] += result[i][j];
                        //Log.d("INFO", "Current: " + result[i][predictedIndex]);
                        //Log.d("INFO", "Test: " + result[i][j]);
                        if (result[i][j] > result[i][predictedIndex]){
                            predictedIndex = j;
                        }
                    }
                    confusionMatrix[techniqueIndex][predictedIndex] += 1;
                    //Log.d("INFO", Arrays.toString(result[i]) + " " + predictedIndex);

                    line += " " + GetTechniqueFromFileName(file);
                    Log.d("INFO", line);
                }

                float largestPrediction = 0;
                int predictedValue = -1;

                String finalResultsLine = "";

                for (int i = 0; i < finalResults.length; i++){
                    finalResults[i] /= preparedData.length;
                    if (finalResults[i] > largestPrediction){
                        largestPrediction = finalResults[i];
                        predictedValue = i;
                    }
                    finalResultsLine += finalResults[i] + " ";
                }

                //Log.d("INFO", "Final-Result: " + finalResultsLine);
                //Log.d("INFO", "Predicted-Value: " + predictedValue);

                //Log.d("INFO", "onClickPredict time to run: " + totalTimeMilli);
            }

            Log.d("INFO", "CONFUSION MATRIX");
            for (int i = 0; i < confusionMatrix.length; i++){
                String line = "";
                for (int j = 0; j < confusionMatrix[i].length; j++){
                    line += confusionMatrix[i][j] + " ";
                }
                Log.d("INFO", line);
            }


        }catch (Exception e){
            e.printStackTrace();
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