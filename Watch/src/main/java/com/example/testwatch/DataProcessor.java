package com.example.testwatch;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

public class DataProcessor {

    public static GravityData GravityFilter(Vector3D[] data, float alpha){
        Vector3D currentGravity = new Vector3D(0, 0, 0);
        Vector3D[] linearAccelerationVectors = new Vector3D[data.length];
        Vector3D[] gravityVectors = new Vector3D[data.length];
        for (int i = 0; i < data.length; i++){
            double gravityX = alpha * currentGravity.getX() + (1 - alpha) * data[i].getX();
            double gravityY = alpha * currentGravity.getY() + (1 - alpha) * data[i].getY();
            double gravityZ = alpha * currentGravity.getZ() + (1 - alpha) * data[i].getZ();
            currentGravity = new Vector3D(gravityX, gravityY, gravityZ);

            double linearX = data[i].getX() - gravityX;
            double linearY = data[i].getY() - gravityY;
            double linearZ = data[i].getZ() - gravityZ;
            Vector3D linearVector = new Vector3D(linearX, linearY, linearZ);

            linearAccelerationVectors[i] = linearVector;
            gravityVectors[i] = currentGravity;
        }
        return new GravityData(gravityVectors, linearAccelerationVectors);
    }

    public static GravityData GravityFilter(Vector3D[] data) {
        return GravityFilter(data, 0.8f);
    }

    public static Vector2D[] GetPartialEulerAngles(Vector3D[] gravityVectors){
        Vector2D[] partialEulerAngles = new Vector2D[gravityVectors.length];
        for (int i = 0; i < gravityVectors.length; i++){
            float pitch = (float)Math.atan2(-gravityVectors[i].getX(), Math.sqrt(Math.pow(gravityVectors[i].getY(), 2) + Math.pow(gravityVectors[i].getZ(), 2)));
            float roll = (float)Math.atan2(gravityVectors[i].getY(), gravityVectors[i].getZ());
            partialEulerAngles[i] = new Vector2D(pitch, roll);
        }
        return partialEulerAngles;
    }

    public static Vector3D[] ComplementaryFilter(Vector3D[] gyro, Vector2D[] gravityAngles, float dt, float alpha){
        Vector3D[] filteredValues = new Vector3D[gravityAngles.length - 1];

        double pitch = gravityAngles[0].getX();
        double roll = gravityAngles[0].getY();
        double yaw = 0;

        //skip the first index here since we set it to the base pitch and roll
        for (int i = 1; i < gravityAngles.length; i++){
            Vector3D g = gyro[i];
            Vector2D gAngle = gravityAngles[i];
            pitch = alpha * (pitch + g.getY() * dt) + (1 - alpha) * gAngle.getY();
            roll = alpha * (roll + g.getX() * dt) + (1 - alpha) * gAngle.getX();
            yaw += g.getZ() * dt;
            filteredValues[i - 1] = new Vector3D(yaw, pitch, roll);
        }

        return filteredValues;
    }

    public static Vector3D[] ComplementaryFilter(Vector3D[] gyro, Vector2D[] gravityAngles){
        return ComplementaryFilter(gyro, gravityAngles, 0.15f, 0.98f);
    }

    public static double[][][] CreateSlidingWindow(Vector3D[] filteredAngles, Vector3D[] linearAcceleration, int windowSize, float slidingOffsetPercent){
        int windowCount = filteredAngles.length / windowSize;
        windowCount = (int)(((windowCount-1)/slidingOffsetPercent) + 1);
        //Log.d("INFO", "slidingOffsetPercent: " + slidingOffsetPercent);
        //Log.d("INFO", "WindowCount: " + windowCount);
        double[][][] windowedData = new double[windowCount][windowSize][6];
        for (int i = 0; i < windowCount; i++){
            //Log.d("Info", "i: " + i);
            for (int j = 0; j < windowSize; j++){
                //Log.d("Info", "j: " + j);
                int initialIndex = (int)((float)i * slidingOffsetPercent * windowSize) + j;
                windowedData[i][j][0] = linearAcceleration[initialIndex].getX();
                windowedData[i][j][1] = linearAcceleration[initialIndex].getY();
                windowedData[i][j][2] = linearAcceleration[initialIndex].getZ();
                windowedData[i][j][3] = filteredAngles[initialIndex].getX();
                windowedData[i][j][4] = filteredAngles[initialIndex].getY();
                windowedData[i][j][5] = filteredAngles[initialIndex].getZ();

//                Log.d("INFO", "Index: " + (i * 100 + j) + ": " +
//                        windowedData[i][j][0] + " " +
//                        windowedData[i][j][1] + " " +
//                        windowedData[i][j][2] + " " +
//                        windowedData[i][j][3] + " " +
//                        windowedData[i][j][4] + " " +
//                        windowedData[i][j][5] );
            }
        }
        return windowedData;
    }

    public static float[] StandardScaler(float[] data) {
        return new float[0];
    }
}
