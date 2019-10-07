package com.example.kittydetector;

import android.app.Activity;


public class ClassifierFloatMobileNet extends Classifier {

    public ClassifierFloatMobileNet(Activity activity) {
        super(activity);
    }

    @Override
    public int getSize() {
        return 224;
    }

    @Override
    protected String getModelPath() {
        // you can download this file from
        // see build.gradle for where to obtain this file. It should be auto
        // downloaded into assets.
        return "mobilenet_v1_1.0_224.tflite";
    }


    @Override
    protected boolean runInference() {
        float[][] output = new float[1][1001];
        float THRESHOLD = 0.1f;
        tflite.run(imgData, output);
        for (int i = 282; i < 288; ++i)
        {
          if (output[0][i] > THRESHOLD)
                return true;
        }
        return false;
    }
}
