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

//    public static GravityData GravityFilter(Vector3D[] data, float alpha){
//        Vector3D currentGravity = new Vector3D(0, 0, 0);
//        Vector3D[] linearAccelerationVectors = new Vector3D[data.length];
//        Vector3D[] gravityVectors = new Vector3D[data.length];
//        for (int i = 0; i < data.length; i++){
//            double gravityX = alpha * currentGravity.getX() + (1 - alpha) * data[i].getX();
//            double gravityY = alpha * currentGravity.getY() + (1 - alpha) * data[i].getY();
//            double gravityZ = alpha * currentGravity.getZ() + (1 - alpha) * data[i].getZ();
//            currentGravity = new Vector3D(gravityX, gravityY, gravityZ);
//
//            double linearX = data[i].getX() - gravityX;
//            double linearY = data[i].getY() - gravityY;
//            double linearZ = data[i].getZ() - gravityZ;
//            Vector3D linearVector = new Vector3D(linearX, linearY, linearZ);
//
//            linearAccelerationVectors[i] = linearVector;
//            gravityVectors[i] = currentGravity;
//        }
//        return new GravityData(gravityVectors, linearAccelerationVectors);
//    }
//
//    public static GravityData GravityFilter(Vector3D[] data) {
//        return GravityFilter(data, 0.8f);
//    }

    public static float[] GetAccelAngles(float[] data){
        //Vector2D[] partialEulerAngles = new Vector2D(0,0);
        //for (int i = 0; i < gravityVectors.length; i++){
        float pitch = (float)Math.atan2(-data[0], Math.sqrt(Math.pow(data[1], 2) + Math.pow(data[2], 2)));
        float roll = (float)Math.atan2(data[0], data[1]);
            //partialEulerAngles[i] = new Vector2D(pitch, roll);
        //}
        return new float[] {pitch, roll};
    }

    public static float[][] ComplementaryFilter(float[][] data, float dt, float alpha){
        float[][]filteredData = new float[data.length - 1][6];

        //Initialize with the first sample
        float[] first = data[0];
        float[] accelAngles = GetAccelAngles(first);
        float pitch = accelAngles[0];
        float roll = accelAngles[1];
        float yaw = 0;

        for (int i = 0; i < data.length; i ++){
            float[] line = data[i];
            float ax = line[0];
            float ay = line[1];
            float az = line[2];
            float gx = line[3];
            float gy = line[4];
            float gz = line[5];

            accelAngles = GetAccelAngles(data[i]);
            float accPitch = accelAngles[0];
            float accRoll = accelAngles[1];

            pitch = alpha * (pitch + gy * dt) + (1 - alpha) * accPitch;
            roll = alpha * (roll + gx * dt) + (1 - alpha) * accRoll;
            yaw += gz * dt; //this one is going to drift but should probably be okay

            float gravX = -9.8f * (float)Math.sin(pitch);
            float gravY = 9.8f * (float)Math.cos(pitch) * (float)Math.sin(roll);
            float gravZ = 9.8f * (float)Math.cos(pitch) * (float)Math.cos(roll);

            float linearAx = ax - gravX;
            float linearAy = ay - gravY;
            float linearAz = az - gravZ;

            filteredData[i] = new float[] {linearAx, linearAy, linearAz, yaw, pitch, roll};
        }

        return filteredData;
    }

    public static float[][] ComplementaryFilter(float[][] data){
        return ComplementaryFilter(data, 0.01167f, 0.98f);
    }

    public static float[][][] CreateSlidingWindow(float[][] data, int windowSize, float slidingOffsetPercent) {

        int stride = Math.max(1, (int)(windowSize * (1 - slidingOffsetPercent)));
        int windowCount = (data.length - windowSize) / stride + 1;

        if (windowCount <= 0) return new float[0][0][0];

        float[][][] windowedData = new float[windowCount][windowSize][6];

        for (int i = 0; i < windowCount; i++) {
            int startIdx = i * stride;
            for (int j = 0; j < windowSize; j++) {
                // No need for complex math here, just start + offset
                windowedData[i][j] = data[startIdx + j];
            }
        }
        return windowedData;
    }
//
//    public static float[] StandardScaler(float[] data) {
//        return new float[0];
//    }
}
