package com.example.babelwalk;

import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MLModel {

    private Interpreter interpreter;

    public MLModel(AssetManager assetManager) {
        try {
            interpreter = new Interpreter(loadModelFile(assetManager, "walking_model.tflite"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String filename) throws Exception {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(filename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();

        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public float predict(float[] input) {
        float[][] output = new float[1][1];
        interpreter.run(input, output);
        return output[0][0];
    }
}
