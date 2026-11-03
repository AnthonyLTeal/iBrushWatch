package com.example.testwatch;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.List;

public class GravityData {
    private final Vector3D[] _gravity;
    private final Vector3D[] _linearAcceleration;
    public GravityData (Vector3D[] gravity, Vector3D[] linearAcceleration)
    {
        _gravity = gravity;
        _linearAcceleration = linearAcceleration;
    }

    public Vector3D[] getGravityData() {
        return _gravity;
    }

    public Vector3D[] getLinearAcceleration() {
        return _linearAcceleration;
    }
}
