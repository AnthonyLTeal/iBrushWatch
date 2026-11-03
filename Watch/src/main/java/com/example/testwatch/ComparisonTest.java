package com.example.testwatch;

import android.app.Activity;
import android.util.Log;

//import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ComparisonTest {

    private static int[] LoadClassifiersData(Activity mainActivity, String filename) throws IOException {
        BufferedReader reader;
        InputStream file = mainActivity.getAssets().open(filename);
        reader = new BufferedReader(new InputStreamReader(file));
        String line = reader.readLine();
        String[] classifiers = line.split(",");
        int[] classifiersArr = new int[classifiers.length];
        for (int i = 0; i < classifiers.length; i ++){
            classifiersArr[i] = Integer.parseInt(classifiers[i]);
        }
        return classifiersArr;
    }

//    private static float[][] LoadProcessedData(Activity mainActivity, String filename) throws IOException {
//        BufferedReader reader;
//        InputStream file = mainActivity.getAssets().open(filename);
//        reader = new BufferedReader(new InputStreamReader(file));
//        List<float[]> lines = new ArrayList<>();
//
//        String line = reader.readLine();
//        while(line != null){
//            String[] lineSplit = line.split(",");
//            float[] values = new float[lineSplit.length];
//            for (int i = 0; i < lineSplit.length; i++){
//                values[i] = Float.parseFloat(lineSplit[i]);
//            }
//            lines.add(values);
//            line = reader.readLine();
//        }
//        reader.close();
//
//        float[][] linesArray = new float[lines.size()][MainActivity.FEATURECOUNT];
//        linesArray = lines.toArray(linesArray);
//        return linesArray;
//    }
//
//    public static void RunTest(MainActivity mainActivity, String filename, String classifiersFileName){
//        float[][] data;
//        int[] classifiers;
//        try {
//            data = LoadProcessedData(mainActivity, filename);
//            classifiers = LoadClassifiersData(mainActivity, classifiersFileName);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        float[][] results=new float[data.length][MainActivity.TECHNIQUECOUNT];
//        mainActivity.getTflite().run(data,results);
//
//        int[][] confusionMatrix = BuildConfusionMatrix(results, classifiers, 7);
//
//        Log.d("INFO", "Validation Matrix: ");
//        for (int i = 0; i < confusionMatrix.length; i++){
//            String line = "";
//            for (int j = 0; j < confusionMatrix[i].length; j++){
//                line += "" + confusionMatrix[i][j] + " ";
//            }
//            Log.d("INFO", line);
//        }
//    }

    public static int[][] BuildConfusionMatrix(float[][] inferenceResults, int[] classifiers, int numClassifiers){
        int[][] matrix = new int[numClassifiers][numClassifiers];
        for (int i = 0; i < inferenceResults.length; i++){
            int actualClassifier = classifiers[i];
            int predictedClassifier = -1;
            float predictedValue = 0;
            for (int j = 0; j < inferenceResults[i].length; j++){
                float value = inferenceResults[i][j];
                if (value > predictedValue){
                    predictedValue = value;
                    predictedClassifier = j;
                }
            }
            matrix[actualClassifier][predictedClassifier] += 1;
        }
        return matrix;
    }
}
