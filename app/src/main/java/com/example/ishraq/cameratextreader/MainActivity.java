package com.example.ishraq.cameratextreader;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
//import android.support.v4.app.ActivityCompatApi23;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.lang.Object;

//Used a google library for this mini-project
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

public class MainActivity extends AppCompatActivity {

    //Instance Variables for the text reader
    SurfaceView cameraView;
    TextView textView;
    Button button;
    boolean isClicked;

    CameraSource cameraSource;
    final int requestCameraPermission = 1001;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Make sure app grants permission to start camera
        
        if (requestCode == requestCameraPermission) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

        @Override
        protected void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            cameraView = (SurfaceView) findViewById(R.id.surface_view);
            textView = (TextView) findViewById(R.id.text_view);
            button = (Button) findViewById(R.id.button);
            button.setText("OFF");
            isClicked = false;
            final StringBuilder stringBuilder = new StringBuilder();
            final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
            if (!textRecognizer.isOperational()) {
                Log.w("MainActivity", "Detector dependencies are not yet available");
            } else {
                // The camera that is going to read the text, I tested some of the features to get the size and resolution that I felt fit best
                cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer).setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setRequestedPreviewSize(1280, 1024)
                        .setRequestedFps(2.0f)
                        .setAutoFocusEnabled(true)
                        .build();
                            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                                @Override
                                public void surfaceCreated(SurfaceHolder holder) {
                                    try {
                                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},
                                                    requestCameraPermission);
                                            return;
                                        }
                                        cameraSource.start(cameraView.getHolder());

                                        //If button is on, then camera will read text, otherwise no
                                        button.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if(!isClicked){
                                                    isClicked = true;
                                                    button.setText("ON");
                                                    textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                                                        @Override
                                                        public void release() {

                                                        }

                                                        @Override
                                                        public void receiveDetections(Detector.Detections<TextBlock> detections) {
                                                            if (isClicked) {
                                                                final SparseArray<TextBlock> items = detections.getDetectedItems();
                                                                if (items.size() != 0) {
                                                                    textView.post(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            //StringBuilder stringBuilder = new StringBuilder();
                                                                            for (int i = 0; i < items.size(); i++) {
                                                                                TextBlock item = items.valueAt(i);
                                                                                stringBuilder.append(item.getValue());
                                                                                stringBuilder.append("\n");

                                                                            }
                                                                            textView.setText(stringBuilder.toString());
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        }
                                                    });
                                                }
                                                else{
                                                    isClicked = false;
                                                    button.setText("OFF");
                                                    stringBuilder.setLength(0);
                                                    textView.setText(stringBuilder.toString());
                                                }
                                            }
                                        });


                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                                }

                                @Override
                                public void surfaceDestroyed(SurfaceHolder holder) {
                                    cameraSource.stop();
                                }
                            });


            }
        }
}
