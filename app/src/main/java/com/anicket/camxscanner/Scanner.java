package com.anicket.camxscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Scanner extends AppCompatActivity {

    Preview preview;
    ImageAnalysis imageAnalysis;
    PreviewView cameraView;
    boolean detected;

    public Scanner(){
        detected = false;
    }

    @Override
    @ExperimentalGetImage
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        cameraView = findViewById(R.id.cameraPreview);
        ListenableFuture<ProcessCameraProvider> processCameraProviderFuture = ProcessCameraProvider.getInstance(this);
        processCameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = processCameraProviderFuture.get();
                    bindToLifeCycle(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @ExperimentalGetImage
    private void bindToLifeCycle(ProcessCameraProvider cameraProvider) {
        preview = new Preview.Builder().setTargetResolution(new Size(360,480)).build();
        imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        preview.setSurfaceProvider(cameraView.createSurfaceProvider());
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                Log.d("started","Analyzer Started");
                BarcodeScannerOptions barcodeScannerOptions = new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .build();
                BarcodeScanner barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions);
                Image mediaImage = imageProxy.getImage();
                if(mediaImage!=null){
                    Log.d("notnull","Inside Not Null");
                    InputImage inputImage = InputImage.fromMediaImage(mediaImage,imageProxy.getImageInfo().getRotationDegrees());
                    Task<List<Barcode>> results = barcodeScanner.process(inputImage)
                            .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                                @Override
                                public void onSuccess(List<Barcode> barcodes) {
                                    if(barcodes.size()>0){
                                        String[] detectedBarCodes = new String[20];
                                        int i=0;
                                        for(Barcode barcode : barcodes){
                                            detectedBarCodes[i]=barcode.getRawValue();
                                            i++;
                                        }
                                        Intent replyIntent = new Intent();
                                        replyIntent.putExtra("BarCodes",detectedBarCodes);
                                        setResult(RESULT_OK,replyIntent);
                                        finish();
                                    }
                                    imageProxy.close();
                                }
                            } ).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    imageProxy.close();
                                }
                            });
                }
                else{
                    Toast.makeText(Scanner.this, "Image Obtained is null", Toast.LENGTH_SHORT).show();
                }

            }
        });
        cameraProvider.unbindAll();
        Camera camera = cameraProvider.bindToLifecycle(this,cameraSelector,preview,imageAnalysis);
    }
}
