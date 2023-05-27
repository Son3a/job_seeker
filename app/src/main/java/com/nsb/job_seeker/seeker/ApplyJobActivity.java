package com.nsb.job_seeker.seeker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.auth.MainActivity;
import com.nsb.job_seeker.common.AsyncTasks;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.common.RealPathUtil;
import com.nsb.job_seeker.common.VolleyMultipartRequest;
import com.nsb.job_seeker.model.DataPart;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;


public class ApplyJobActivity extends AppCompatActivity {
    private static final int MY_REQUEST_CODE = 10;
    private static final String TAG = ApplyJobActivity.class.getName();
    private ImageView imgBack;
    private Button btnSendCv;
    private TextView tvUploadCV;
    private ProgressBar pbLoading;
    private Uri mUri;
    private String fileName;
    private String url = "https://job-seeker-smy5.onrender.com/application/create";
    private PreferenceManager preferenceManager;

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
                        String path = RealPathUtil.getPath(ApplyJobActivity.this, uri);
                        fileName = path.substring(path.lastIndexOf("/") + 1);
                        tvUploadCV.setText(fileName);
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
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
        pbLoading = findViewById(R.id.idLoadingPB);

        preferenceManager = new PreferenceManager(this);
    }

    private void setEvent() throws URISyntaxException {
        back();

        clickOpenFile();

        clickCreateApplication();
    }

    private void back() {
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void clickCreateApplication() {
        btnSendCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(ApplyJobActivity.this, fileName + "\n" + mUri, Toast.LENGTH_SHORT).show();
                uploadPDF(fileName, mUri);
            }
        });
    }

    private void uploadPDF(final String pdfname, Uri pdffile) {
        String access_token = preferenceManager.getString(Program.TOKEN);
        String idJob = getIntent().getExtras().getString("idJob");
        pbLoading.setVisibility(View.VISIBLE);
        RequestQueue rQueue = Volley.newRequestQueue(ApplyJobActivity.this);

        InputStream iStream = null;
        try {

            iStream = getContentResolver().openInputStream(pdffile);
            final byte[] inputData = getBytes(iStream);

            VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                    new Response.Listener<NetworkResponse>() {
                        @Override
                        public void onResponse(NetworkResponse response) {
                            pbLoading.setVisibility(View.GONE);
                            Toast.makeText(ApplyJobActivity.this, "Apply thành công!", Toast.LENGTH_SHORT).show();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse.statusCode == 401 && error.networkResponse != null) {
                                Toast.makeText(getApplicationContext(), "Hết phiên đăng nhập", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                preferenceManager.clear();
                                startActivity(i);
                            }
                            pbLoading.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }) {

                /*
                 * If you want to add more parameters with the image
                 * you can do it here
                 * here we have only one parameter with the image
                 * which is tags
                 * */
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", access_token);
                    return headers;
                }

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("idJob", idJob);
                    return params;
                }

                /*
                 *pass files using below method
                 * */
                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    params.put("cv", new DataPart(pdfname, inputData));
                    return params;
                }
            };


            volleyMultipartRequest.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 50000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 50000;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {
                    if(error.networkResponse != null) {
                        if (error.networkResponse.statusCode == 401) {

                            new AsyncTasks() {
                                @Override
                                public void onPreExecute() {
                                }

                                @Override
                                public void doInBackground() {
                                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    preferenceManager.clear();
                                    startActivity(i);
                                }

                                @Override
                                public void onPostExecute() {
                                    Toast.makeText(getApplicationContext(), "Hết phiên đăng nhập", Toast.LENGTH_SHORT).show();
                                }
                            }.execute();
                        }
                    }
                }
            });

            rQueue.add(volleyMultipartRequest);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void clickOpenFile() {
        tvUploadCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickRequestPermision();
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
        i.setType("application/pdf");
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
