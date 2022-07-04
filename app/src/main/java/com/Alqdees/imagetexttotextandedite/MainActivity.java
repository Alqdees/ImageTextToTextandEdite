package com.Alqdees.imagetexttotextandedite;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.utils.widget.MotionButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private ImageView mimage;
    private EditText tv_data;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;
    String cameraPermission[];
    String storagePermission[];
    private FloatingActionButton send;
    private MotionButton zain;
    private Bitmap bitmap;
    Uri image_uri;
    private ActivityResultLauncher<String> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle("");
        send= findViewById(R.id.open);
        tv_data = findViewById(R.id.tv_data);
        zain = findViewById(R.id.zain);
        zain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launcher.launch(Manifest.permission.CALL_PHONE);
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            showImageImportDialog();
            }
        });
          launcher  =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(),granted ->{
                    if (granted){
                        runUSSD();
                    }
                });

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.addimage) {
//            showImageImportDialog();
//        }
//        if (id == R.id.settings) {
//
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private void showImageImportDialog() {
        String[] item = {"الكاميرا", "الستوديو"};
        AlertDialog.Builder dilog = new AlertDialog.Builder(this);
        dilog.setTitle("أختر صورة");
        dilog.setItems(item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickCamera();
                    }
                }
                if (i == 1) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                                false == Environment.isExternalStorageManager()) {
                            Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
                            startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri));
                        }
                    } else {
                        pickGallery();
                    }
                }
            }

        });
        dilog.create().show();
    }

                    private void pickGallery() {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"),IMAGE_PICK_GALLERY_CODE);
                    }

                    private void pickCamera() {
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.TITLE, "NewPic");
                        values.put(MediaStore.Images.Media.DESCRIPTION, "image to text");
                        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        camera.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
                        startActivityForResult(camera, IMAGE_PICK_CAMERA_CODE);
                    }

                    private void requestStoragePermission() {
                        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
                    }

                    private boolean checkStoragePermission() {
                        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                (PackageManager.PERMISSION_GRANTED);
                        return result;
                    }

                    private void requestCameraPermission() {
                        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
                    }

                    private boolean checkCameraPermission() {
                        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
                        boolean result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                (PackageManager.PERMISSION_GRANTED);
                        return result1 && result2;
                    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted ){
                        Toast.makeText(this, "انطي صلاحيات", Toast.LENGTH_SHORT).show();
                    }else {
                        pickCamera();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted){
                        Toast.makeText(this, "انطي صلاحيات", Toast.LENGTH_SHORT).show();

                    }else {
//                        pickGallery();

                    }
                }
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                Uri resulturi = result.getUri();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),resulturi);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                Bitmap bit = bitmap.getBitmap();
                TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                if (!recognizer.isOperational()){
                    Toast.makeText(this, "خطأ", Toast.LENGTH_SHORT).show();
                }
                else {
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> item = recognizer.detect(frame);
                    StringBuilder sp = new StringBuilder();
                    for (int i  = 0 ;i< item.size(); i++){
                        TextBlock myItem = item.valueAt(i);
                        sp.append(myItem.getValue());
                        sp.append("\n");
                    }
                    String result_data = sp.toString();
                    result_data = result_data.replaceAll("\\D+","");
                    tv_data.setText("*101#" +result_data +"#");
                }
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                Exception error = result.getError();
                Toast.makeText(this, "" + error, Toast.LENGTH_SHORT).show();
            }
        }
    }
    @SuppressLint("MissingPermission")
    private void runUSSD() {

        String USSd = tv_data.getText().toString();

//        if (!USSd.startsWith("*") && !USSd.endsWith("#")) {
//            Toast.makeText(this, "أدخال خاطئ", Toast.LENGTH_LONG).show();
//            return;
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) !=
                    PackageManager.PERMISSION_GRANTED) {
                return;
            }
            @SuppressLint("ServiceCast")
            TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//            View view = LayoutInflater.from(this).inflate(R.layout.)
            manager.sendUssdRequest("*101#"+USSd+"#", new TelephonyManager.UssdResponseCallback() {
                @Override
                public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                    super.onReceiveUssdResponse(telephonyManager, request, response);
                    tv_data.setText(response.toString());
                }

                @Override
                public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {
                    super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode);
                }
            }, new Handler());
        } else {
//            USSd = USSd.substring(0, USSd.length() - 1);

//            USSd = USSd + Uri.encode("#");
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + USSd));
            startActivity(intent);
        }
    }
}