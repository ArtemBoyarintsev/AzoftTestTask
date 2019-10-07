/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.example.kittydetector;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import org.tensorflow.lite.Interpreter;



public abstract class Classifier {

    /** Dimensions of inputs. */
    private static final int DIM_BATCH_SIZE = 1;

    private static final int DIM_PIXEL_SIZE = 3;

    /** Preallocated buffers for storing image data in. */
    private final int[] intValues = new int[getSize() * getSize()];

    /** Options for configuring the Interpreter. */
    private final Interpreter.Options tfliteOptions = new Interpreter.Options();

    /** The loaded TensorFlow Lite model. */
    private MappedByteBuffer tfliteModel;

    /** An instance of the driver class to run model inference with Tensorflow Lite. */
    protected Interpreter tflite;

    /** A ByteBuffer to hold image data, to be feed into Tensorflow Lite as inputs. */
    protected ByteBuffer imgData = null;

    public Classifier(Activity activity) {
        try {
            tfliteModel = loadModelFile(activity);
            // tfliteOptions.setUseNNAPI(true);

            tfliteOptions.setNumThreads(1);
            tflite = new Interpreter(tfliteModel, tfliteOptions);
            imgData =
                    ByteBuffer.allocateDirect(
                                    DIM_BATCH_SIZE
                                    * getSize()
                                    * getSize()
                                    * DIM_PIXEL_SIZE
                                    * 4);
            imgData.order(ByteOrder.nativeOrder());
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    /** Memory-map the model file in Assets. */
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        Resources res = activity.getResources();
        AssetManager am = res.getAssets();

        AssetFileDescriptor fileDescriptor = am.openFd(getModelPath());
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /** Writes Image data into a {@code ByteBuffer}. */
    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (imgData == null) {
            return;
        }
        imgData.rewind();

        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, getSize(), getSize(), true);
        scaled.getPixels(intValues, 0, scaled.getWidth(), 0, 0, scaled.getWidth(), scaled.getHeight());
        // Convert the image to floating point.
        int pixel = 0;
        for (int i = 0; i < getSize(); ++i) {
            for (int j = 0; j < getSize(); ++j) {
                final int val = intValues[pixel++];
                addPixelValue(val);
            }
        }
    }

    /** Runs inference and returns the classification results. */
    public boolean recognizeImage(final Bitmap bitmap) {
        convertBitmapToByteBuffer(bitmap);
        return runInference();
    }

    public abstract int getSize();

    protected abstract String getModelPath();

    private void addPixelValue(int pixelValue)
    {
        imgData.putFloat((((pixelValue >> 16) & 0xFF) - 0.0f) / 255.0f);
        imgData.putFloat((((pixelValue >> 8) & 0xFF) - 0.0f) / 255.0f);
        imgData.putFloat(((pixelValue & 0xFF) - 0.0f) / 255.0f);
    }
    protected abstract boolean runInference();

}
