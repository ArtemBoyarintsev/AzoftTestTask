package com.example.kittydetector;

import android.app.Activity;


class CustomClassifier extends Classifier {

    private static final float THRESHOLD = 0.1f;

    CustomClassifier(Activity activity) {
        super(activity);
    }

    @Override
    public int getSize() {
        return 64;
    }

    @Override
    protected String getModelPath() {
        return "custom_model.tflite";
    }


    @Override
    protected boolean runInference() {
        float[][] output = new float[1][1];
        tflite.run(imgData, output);
        return output[0][0] > THRESHOLD;
    }
}