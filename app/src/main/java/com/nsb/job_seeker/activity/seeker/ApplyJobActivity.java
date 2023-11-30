package com.nsb.job_seeker.activity.seeker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.activity.BaseActivity;
import com.nsb.job_seeker.activity.LoginActivity;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.CustomToast;
import com.nsb.job_seeker.common.LoadingDialog;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.common.RealPathUtil;
import com.nsb.job_seeker.common.VolleyMultipartRequest;
import com.nsb.job_seeker.databinding.LayoutApplyJobBinding;
import com.nsb.job_seeker.model.DataPart;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;


public class ApplyJobActivity extends BaseActivity {
    private LayoutApplyJobBinding binding;
    private static final int MY_REQUEST_CODE = 10;
    private static final String TAG = ApplyJobActivity.class.getName();
    private Uri mUri;
    private String fileName;
    private String url = Constant.url_dev + "/application/create";
    private PreferenceManager preferenceManager;
    private LoadingDialog loadingDialog;
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
                        binding.textFileName.setText(fileName);
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        binding = LayoutApplyJobBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        setControl();
        try {
            setEvent();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private void setControl() {
        preferenceManager = new PreferenceManager(this);
        loadingDialog = new LoadingDialog(this);
    }

    private void setEvent() throws URISyntaxException {
        back();
        loadInfo();
        clickOpenFile();
        removeFile();
        clickApply();
    }

    private void loadInfo() {
        binding.textEmailSeeker.setText(preferenceManager.getString(Constant.MAIL));
        binding.textNameSeeker.setText(preferenceManager.getString(Constant.NAME));
        if (preferenceManager.getString(Constant.PHONE) != null) {
            binding.textPhoneSeeker.setText(preferenceManager.getString(Constant.PHONE));
        }
    }

    private void clickApply() {
//        binding.btnApply.setOnClickListener(v -> {
//            //validate email
//            if (isEmpty(binding.textEmailSeeker)) {
//                binding.layoutErrorEmail.setVisibility(View.VISIBLE);
//                binding.textEmailSeeker.setBackgroundResource(R.drawable.background_edittext_error);
//            } else {
//                binding.layoutErrorEmail.setVisibility(View.GONE);
//                binding.textEmailSeeker.setBackgroundResource(R.drawable.background_edittext_register);
//            }
//
//            //validate password
//            if (isEmpty(binding.textPhoneSeeker)) {
//                binding.layoutErrorPhone.setVisibility(View.VISIBLE);
//                binding.textPhoneSeeker.setBackgroundResource(R.drawable.background_edittext_error);
//            } else {
//                binding.layoutErrorPhone.setVisibility(View.GONE);
//                binding.textPhoneSeeker.setBackgroundResource(R.drawable.background_edittext_register);
//            }
//
//            //validate name
//            if (isEmpty(binding.textNameSeeker)) {
//                binding.layoutErrorName.setVisibility(View.VISIBLE);
//                binding.textNameSeeker.setBackgroundResource(R.drawable.background_edittext_error);
//            } else {
//                binding.layoutErrorName.setVisibility(View.GONE);
//                binding.textNameSeeker.setBackgroundResource(R.drawable.background_edittext_register);
//            }
//
//            if (!isEmpty(binding.textNameSeeker) && !isEmpty(binding.textPhoneSeeker) && !isEmpty(binding.textEmailSeeker)) {
//                binding.layoutErrorName.setVisibility(View.GONE);
//                binding.textNameSeeker.setBackgroundResource(R.drawable.background_edittext_register);
//                binding.layoutErrorPhone.setVisibility(View.GONE);
//                binding.textPhoneSeeker.setBackgroundResource(R.drawable.background_edittext_register);
//                binding.layoutErrorEmail.setVisibility(View.GONE);
//                binding.textEmailSeeker.setBackgroundResource(R.drawable.background_edittext_register);
//
//                uploadPDF(fileName, mUri);
//            }
//        });

    }


    private boolean isEmpty(EditText editText) {
        if (editText.getText().toString().isEmpty() || editText.getText().toString().trim().equals("")) {
            return true;
        }
        return false;
    }

    private void removeFile() {
        binding.imageRemoveFile.setOnClickListener(v -> {
            binding.textFileName.setText("");
            mUri = null;
        });
    }

    private void back() {

    }

    private void uploadPDF(final String pdfname, Uri pdffile) {
        String access_token = preferenceManager.getString(Constant.TOKEN);
        String idJob = getIntent().getExtras().getString("idJob");
        Log.d("idJob", idJob);
        loadingDialog.showDialog();
        RequestQueue rQueue = Volley.newRequestQueue(ApplyJobActivity.this);

        InputStream iStream = null;
        try {

            iStream = getContentResolver().openInputStream(pdffile);
            final byte[] inputData = getBytes(iStream);

            VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                    new Response.Listener<NetworkResponse>() {
                        @Override
                        public void onResponse(NetworkResponse response) {
                            loadingDialog.hideDialog();
                            Constant.idAppliedJob.add(idJob);
                            CustomToast.makeText(ApplyJobActivity.this, "Ứng tuyển thành công!",
                                    CustomToast.LENGTH_SHORT, CustomToast.SUCCESS).show();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            loadingDialog.hideDialog();
                            if (error instanceof com.android.volley.NoConnectionError) {

                            } else if (error.networkResponse.statusCode == 401) {
                                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                preferenceManager.clear();
                                startActivity(i);
                            }
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
                    params.put("nameSeeker", binding.textNameSeeker.getText().toString().trim());
                    params.put("phoneSeeker", binding.textPhoneSeeker.getText().toString().trim());
                    params.put("emailSeeker", binding.textEmailSeeker.getText().toString().trim());
                    if (binding.textLetterOfRecommendation.getText() != null &&
                            !binding.textLetterOfRecommendation.getText().toString().trim().equals("")) {
                        params.put("letterRecommendation", binding.textLetterOfRecommendation.getText().toString().trim());
                    }
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


            volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

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
        binding.btnUploadCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickRequestPermision();
            }
        });
    }

    private void openStorage() {
        Intent i = new Intent();
        i.setType("*/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(i, "Select file"));
    }

    private void onClickRequestPermision() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openStorage();
            return;
        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openStorage();
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
                openStorage();
            }
        }
    }
}
