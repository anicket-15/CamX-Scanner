package com.anicket.camxscanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.mlkit.vision.barcode.Barcode;

public class MainActivity extends AppCompatActivity {

    public static final String [] PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            "android.permission.CAMERA",
    };

    public static final int REQUEST_CODE = 101;
    public static final int ACTIVITY_REQUEST_CODE = 1;

    // On Click Listener
    public void startScannerButtonOnClickListener(View view) {
        Intent intent = new Intent(this,Scanner.class);
        startActivityForResult(intent,ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==ACTIVITY_REQUEST_CODE){
            if(resultCode==RESULT_OK){
                String[] detectedBarCode = data.getStringArrayExtra("BarCodes");
                setContentView(R.layout.activity_main_show_data);
                TextView detectedCodeDisplay = findViewById(R.id.detectedCodeDisplay);
                detectedCodeDisplay.setText(detectedBarCode[0]);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
            // TODO: 16-09-2020 Start Scanner Activity
            Toast.makeText(this, "Permission Successfully Granted", Toast.LENGTH_SHORT).show();
        }
        else{
            ActivityCompat.requestPermissions(this,PERMISSIONS,REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int REQUEST_CODE, @NonNull String[] PERMISSIONS, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(REQUEST_CODE, PERMISSIONS, grantResults);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
            // TODO: 16-09-2020 Start Scanner Activity
            Toast.makeText(this, "Permission Successfully Granted", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Please Grant the Required Permissions", Toast.LENGTH_LONG).show();
        }
    }

}
