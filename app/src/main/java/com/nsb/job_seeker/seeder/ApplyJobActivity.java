package com.nsb.job_seeker.seeder;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.nsb.job_seeker.R;

import java.io.File;
import java.net.URISyntaxException;


public class ApplyJobActivity extends AppCompatActivity {
    private static final int MY_REQUEST_CODE = 10;
    private static final String TAG = ApplyJobActivity.class.getName();
    private ImageView imgBack;
    private Button btnSendCv;
    private TextView tvUploadCV, imgUpload;
    private Uri mUri;
    private ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.e(TAG, "onActivityResult");
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null) {
                            return;
                        }
                        Uri uri = data.getData();
                        mUri = uri;
                        tvUploadCV.setText(getFileName(uri));
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_seeker_apply_job);

        setControl();
        try {
            setEvent();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private void setControl() {
        tvUploadCV = findViewById(R.id.tv_upload_cv);
        imgBack = findViewById(R.id.ic_back);
        btnSendCv = findViewById(R.id.btn_send_cv);
        imgUpload = findViewById(R.id.img_upload);
    }

    private void setEvent() throws URISyntaxException {
        back();

        clickOpenFile();

        createApplication();
    }

    private void back() {
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void createApplication() throws URISyntaxException {
//        String strRealPath = RealPathUtil.getPath(ApplyJobActivity.this, Uri.parse("content://com.android.providers.downloads.documents/document/document/msf%3A1000000034"));
//        String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
//        File file = new File(baseDir +  File.separator + "Download" + File.separator + tvUploadCV.getText().toString());

        btnSendCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = null;
                cursor = ApplyJobActivity.this.getContentResolver().query(mUri, new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
//                    String fileName = cursor.getString(0);
////                    String path = Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName;
//                    final int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.);

//                    if (!TextUtils.isEmpty(cursor.getString(index))) {
//                        imgUpload.setText(cursor.getString(index));
//                    }
//
                }

            }
        });
    }

    private void clickOpenFile() {
        tvUploadCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                onClickRequestPermision();
                openStorge();
            }
        });
    }

    private String getFileName(Uri uri) throws IllegalArgumentException {
        // Obtain a cursor with information regarding this uri
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor.getCount() <= 0) {
            cursor.close();
            throw new IllegalArgumentException("Can't obtain file name, cursor is empty");
        }

        cursor.moveToFirst();

        String fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));

        cursor.close();

        return fileName;
    }

    private void openStorge() {
        Intent i = new Intent();
        i.setType("*/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(i, "Select file"));
    }

    private void onClickRequestPermision() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openStorge();
            return;
        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openStorge();
        } else {
            String[] permision = {Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(permision, MY_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openStorge();
            }
        }
    }


}
