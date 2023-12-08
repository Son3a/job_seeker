package com.nsb.job_seeker.activity.seeker;

import static java.lang.Math.abs;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.activity.BaseActivity;
import com.nsb.job_seeker.activity.LoginActivity;
import com.nsb.job_seeker.activity.admin.EditRecruitmentActivity;
import com.nsb.job_seeker.activity.admin.ListCVActivity;
import com.nsb.job_seeker.activity.messenger.ChatActivity;
import com.nsb.job_seeker.adapter.JobDetailAdapter;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.CustomToast;
import com.nsb.job_seeker.common.LoadingDialog;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.common.RealPathUtil;
import com.nsb.job_seeker.common.VolleyMultipartRequest;
import com.nsb.job_seeker.databinding.ActivitySeekerJobDetailBinding;
import com.nsb.job_seeker.databinding.LayoutApplyJobBinding;
import com.nsb.job_seeker.model.Company;
import com.nsb.job_seeker.model.DataPart;
import com.nsb.job_seeker.model.Job;
import com.nsb.job_seeker.model.User;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobDetailActivity extends BaseActivity {
    private ActivitySeekerJobDetailBinding binding;
    private LayoutApplyJobBinding bindingApplyJob;
    private String IDCompany = "";
    private String IDJob = "";
    private JobDetailAdapter jobDetailAdapter;
    private List<Job> listRelatedJob;
    private PreferenceManager preferenceManager;
    private Job job;
    private String idUserCompany, companyName;

    private BottomSheetDialog bottomSheetApplyJob;
    private static final int MY_REQUEST_CODE = 10;
    private static final String TAG = ApplyJobActivity.class.getName();
    private Uri mUri;
    private String fileName;
    private String url = Constant.url_dev + "/application/create";
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
                        mUri = null;
                        Uri uri = data.getData();
                        String path = RealPathUtil.getPath(JobDetailActivity.this, uri);
                        int sizeFile = (int) new File(path).length();
                        Log.d("SizeFile", sizeFile + "");
                        if (sizeFile > 2097152) {
                            bindingApplyJob.textErrorFile.setText("Kích thước file phải <= 2MB");
                            bindingApplyJob.layoutErrorFile.setVisibility(View.VISIBLE);
                        } else {
                            mUri = uri;
                            bindingApplyJob.layoutErrorFile.setVisibility(View.GONE);
                            bindingApplyJob.layoutFile.setBackgroundResource(R.drawable.background_apply);
                        }
                        fileName = path.substring(path.lastIndexOf("/") + 1);
                        bindingApplyJob.textFileName.setText(fileName);
                    }
                }
            });

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.JOB_MODEL.equals(intent.getAction())) {
                job = (Job) intent.getSerializableExtra(Constant.JOB_MODEL);
                binding.textNameJob.setText(job.getNameJob());
                binding.textNameCompany.setText(job.getCompanyName());
                binding.textPosition.setText(job.getPlace());
                binding.textSalary.setText(job.getSalary());
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        binding = ActivitySeekerJobDetailBinding.inflate(getLayoutInflater());
        bindingApplyJob = LayoutApplyJobBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        bottomSheetApplyJob = new BottomSheetDialog(this);
        bottomSheetApplyJob.setContentView(bindingApplyJob.getRoot());

        setControl();
        setEvent();
    }

    private void setControl() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("isLinkCompany")) {
            //binding..setEnabled(false);
        }
        IDJob = bundle.getString(Constant.JOB_ID);
        listRelatedJob = new ArrayList<>();
        preferenceManager = new PreferenceManager(this);
        if (preferenceManager.getBoolean(Constant.KEY_IS_SIGNED_IN)) {
            if (preferenceManager.getString(Constant.ROLE).equals(Constant.ADMIN_ROLE)) {
                binding.tabLayout.setVisibility(View.GONE);
                binding.tabLayout.getLayoutParams().height = 0;
                binding.layoutBottomSheetApply.getRoot().getLayoutParams().height = 0;
            } else {
                binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Thông tin"));
                binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Công việc liên quan"));
                binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Công ty"));
            }

            if (preferenceManager.getString(Constant.ROLE).equals(Constant.USER_ROLE)) {
                if (Constant.idAppliedJob.contains(IDJob)) {
                    binding.layoutBottomSheetApply.getRoot().setVisibility(View.INVISIBLE);
                    binding.layoutBottomSheetApplyAgain.getRoot().setVisibility(View.VISIBLE);
                } else {
                    binding.layoutBottomSheetApplyAgain.getRoot().setVisibility(View.INVISIBLE);
                    binding.layoutBottomSheetApply.getRoot().setVisibility(View.VISIBLE);
                }
            }
        }
        loadingDialog = new LoadingDialog(this);
        FrameLayout bottomSheet = bottomSheetApplyJob.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }


    private void setEvent() {
        binding.viewPager.setUserInputEnabled(false);
        if (preferenceManager.getBoolean(Constant.KEY_IS_SIGNED_IN)) {
            getToken();
            clickSaveJob();
            setIconSave();
            gotoAppJob();
            openBottomEditRecruitment();
            gotoChat();

            //bottom apply job
            loadInfoApplyJob();
            clickOpenFile();
            removeFile();
            clickApply();
            closeBottomApplyJob();
        } else {
            gotoLogin();
        }
        getJobDetail();
        setStateAppBar();
        back();
        setTabLayout();
    }

    private void closeBottomApplyJob() {
//        bindingApplyJob.btnBack.setOnClickListener(v -> {
//            if (bottomSheetApplyJob != null) {
//                bottomSheetApplyJob.dismiss();
//            }
//        });
    }

    private boolean isEmpty(EditText editText) {
        if (editText.getText().toString().isEmpty() || editText.getText().toString().trim().equals("")) {
            return true;
        }
        return false;
    }

    private void clickApply() {
        bindingApplyJob.btnApply.setOnClickListener(v -> {
            //validate email
            if (isEmpty(bindingApplyJob.textEmailSeeker)) {
                bindingApplyJob.layoutErrorEmail.setVisibility(View.VISIBLE);
                bindingApplyJob.textEmailSeeker.setBackgroundResource(R.drawable.background_apply_error);
            } else {
                bindingApplyJob.layoutErrorEmail.setVisibility(View.GONE);
                bindingApplyJob.textEmailSeeker.setBackgroundResource(R.drawable.background_apply);
            }

            //validate password
            if (isEmpty(bindingApplyJob.textPhoneSeeker)) {
                bindingApplyJob.layoutErrorPhone.setVisibility(View.VISIBLE);
                bindingApplyJob.textPhoneSeeker.setBackgroundResource(R.drawable.background_apply_error);
            } else {
                bindingApplyJob.layoutErrorPhone.setVisibility(View.GONE);
                bindingApplyJob.textPhoneSeeker.setBackgroundResource(R.drawable.background_apply);
            }

            //validate name
            if (isEmpty(bindingApplyJob.textNameSeeker)) {
                bindingApplyJob.layoutErrorName.setVisibility(View.VISIBLE);
                bindingApplyJob.textNameSeeker.setBackgroundResource(R.drawable.background_apply_error);
            } else {
                bindingApplyJob.layoutErrorName.setVisibility(View.GONE);
                bindingApplyJob.textNameSeeker.setBackgroundResource(R.drawable.background_apply);
            }

            if (mUri == null || mUri.equals("")) {
                bindingApplyJob.layoutErrorFile.setVisibility(View.VISIBLE);
                bindingApplyJob.layoutFile.setBackgroundResource(R.drawable.background_apply_error);
            } else {
                bindingApplyJob.layoutErrorFile.setVisibility(View.GONE);
                bindingApplyJob.layoutFile.setBackgroundResource(R.drawable.background_apply);
            }

            if (!isEmpty(bindingApplyJob.textNameSeeker) && !isEmpty(bindingApplyJob.textPhoneSeeker) && !isEmpty(bindingApplyJob.textEmailSeeker) &&
                    mUri != null) {
                bindingApplyJob.layoutErrorName.setVisibility(View.GONE);
                bindingApplyJob.textNameSeeker.setBackgroundResource(R.drawable.background_apply);
                bindingApplyJob.layoutErrorPhone.setVisibility(View.GONE);
                bindingApplyJob.textPhoneSeeker.setBackgroundResource(R.drawable.background_apply);
                bindingApplyJob.layoutErrorEmail.setVisibility(View.GONE);
                bindingApplyJob.textEmailSeeker.setBackgroundResource(R.drawable.background_apply);
                bindingApplyJob.layoutErrorFile.setVisibility(View.GONE);
                bindingApplyJob.layoutFile.setBackgroundResource(R.drawable.background_apply);
                uploadPDF(fileName, mUri);
            }
        });
    }

    private void removeFile() {
        bindingApplyJob.imageRemoveFile.setOnClickListener(v -> {
            bindingApplyJob.textFileName.setText("");
            mUri = null;
        });
    }

    private void uploadPDF(final String pdfname, Uri pdffile) {
        String access_token = preferenceManager.getString(Constant.TOKEN);
        loadingDialog.showDialog();
        RequestQueue rQueue = Volley.newRequestQueue(JobDetailActivity.this);

        InputStream iStream = null;
        try {

            iStream = getContentResolver().openInputStream(pdffile);
            final byte[] inputData = getBytes(iStream);

            VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                    new Response.Listener<NetworkResponse>() {
                        @Override
                        public void onResponse(NetworkResponse response) {
                            loadingDialog.hideDialog();
                            Constant.idAppliedJob.add(IDJob);
                            CustomToast.makeText(JobDetailActivity.this, "Ứng tuyển thành công!",
                                    CustomToast.LENGTH_SHORT, CustomToast.SUCCESS).show();
                            binding.layoutBottomSheetApplyAgain.getRoot().setVisibility(View.VISIBLE);
                            binding.layoutBottomSheetApply.getRoot().setVisibility(View.INVISIBLE);
                            bottomSheetApplyJob.dismiss();
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
                    params.put("idJob", IDJob);
                    params.put("nameSeeker", bindingApplyJob.textNameSeeker.getText().toString().trim());
                    params.put("phoneSeeker", bindingApplyJob.textPhoneSeeker.getText().toString().trim());
                    params.put("emailSeeker", bindingApplyJob.textEmailSeeker.getText().toString().trim());
                    if (bindingApplyJob.textLetterOfRecommendation.getText() != null &&
                            !bindingApplyJob.textLetterOfRecommendation.getText().toString().trim().equals("")) {
                        params.put("letterRecommendation", bindingApplyJob.textLetterOfRecommendation.getText().toString().trim());
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
        bindingApplyJob.btnUploadCV.setOnClickListener(new View.OnClickListener() {
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

    private void loadInfoApplyJob() {
        bindingApplyJob.textEmailSeeker.setText(preferenceManager.getString(Constant.MAIL));
        bindingApplyJob.textNameSeeker.setText(preferenceManager.getString(Constant.NAME));
        if (preferenceManager.getString(Constant.PHONE) != null) {
            bindingApplyJob.textPhoneSeeker.setText(preferenceManager.getString(Constant.PHONE));
        }
    }

    private void sendBroadcastSaveJob(int position) {
        Log.d("PositionSave", position + "");
        Intent intent = new Intent(Constant.BROADCAST_SAVE_JOB);
        intent.putExtra(Constant.POSITION, position);
        sendBroadcast(intent);
    }

    private void gotoLogin() {
        binding.layoutBottomSheetApply.btnApplyJob.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        binding.layoutBottomSheetApply.cvSaveJob.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        preferenceManager.putString(Constant.KEY_FCM_TOKEN, token);

        FirebaseFirestore database = FirebaseFirestore.getInstance();

        DocumentReference documentReference =
                database.collection(Constant.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constant.KEY_USER_ID)
                );
        documentReference.update(Constant.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> {
                    Log.d("Error", "Error update token: " + e.toString());
                    CustomToast.makeText(JobDetailActivity.this, "Unable to update token", CustomToast.LENGTH_SHORT, CustomToast.SUCCESS).show();
                });

        Log.d("TokenSender", preferenceManager.getString(Constant.KEY_USER_ID));
    }

    private void gotoChat() {
        binding.layoutBottomSheetApplyAgain.btnMessage.setOnClickListener(v -> {
            if (idUserCompany != null) {
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                database.collection(Constant.KEY_COLLECTION_USERS)
                        .whereEqualTo(Constant.KEY_USER_ID, idUserCompany)
                        .get()
                        .addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && task.getResult() != null &&
                                            task.getResult().getDocuments().size() > 0) {
                                        Log.d("ReceiverId", "getting info receiver");
                                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                        User receiverUser = new User(
                                                documentSnapshot.getId(),
                                                documentSnapshot.getString(Constant.KEY_NAME),
                                                documentSnapshot.getString(Constant.KEY_EMAIL),
                                                documentSnapshot.getString(Constant.KEY_IMAGE)
                                        );

                                        receiverUser.setToken(documentSnapshot.getString(Constant.KEY_FCM_TOKEN));
                                        Log.d("ReceiverId", receiverUser.toString());
                                        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                                        intent.putExtra(Constant.KEY_COLLECTION_USERS, receiverUser);
                                        intent.putExtra(Constant.KEY_COMPANY, companyName);
                                        startActivity(intent);
                                    }
                                }
                        ).addOnFailureListener(e -> {
                            Log.d("Error", e.getMessage());
                        });
            }
        });
    }

    private void openBottomEditRecruitment() {
        binding.cvExpand.setOnClickListener(v -> {
            if (preferenceManager.getString(Constant.ROLE).equals(Constant.ADMIN_ROLE)) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(JobDetailActivity.this);
                View layoutFunc = getLayoutInflater().inflate(R.layout.layout_function_company, null);
                bottomSheetDialog.setContentView(layoutFunc);

                TextView textTitle = layoutFunc.findViewById(R.id.textOption);
                textTitle.setText("Cập nhật tin tuyển dụng");

                LinearLayout layoutListCv = layoutFunc.findViewById(R.id.layoutListCV);
                layoutListCv.setVisibility(View.VISIBLE);

                layoutListCv.setOnClickListener(view -> {
                    if (!IDJob.equals("")) {
                        Intent intent = new Intent(JobDetailActivity.this, ListCVActivity.class);
                        intent.putExtra(Constant.JOB_ID, IDJob);
                        startActivity(intent);
                        bottomSheetDialog.dismiss();
                    }
                });

                LinearLayout layoutEditCompany = layoutFunc.findViewById(R.id.layoutEditCompany);
                layoutEditCompany.setOnClickListener(view -> {
                    if (job != null) {
                        Intent intent = new Intent(JobDetailActivity.this, EditRecruitmentActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Constant.JOB_MODEL, job);
                        bundle.putInt(Constant.POSITION, getIntent().getIntExtra(Constant.POSITION, -1));
                        intent.putExtras(bundle);
                        startActivity(intent);
                        bottomSheetDialog.dismiss();
                    }
                });
                ImageView imgClose = layoutFunc.findViewById(R.id.imageClose);
                imgClose.setOnClickListener(view -> {
                    bottomSheetDialog.dismiss();
                });

                bottomSheetDialog.show();
            }
        });
    }

    private void setIconSave() {
        if (preferenceManager.getString(Constant.ROLE).equals(Constant.USER_ROLE)) {
            if (IDJob != null && !IDJob.equals("")) {
                if (Constant.idSavedJobs.contains(IDJob)) {
                    binding.layoutBottomSheetApply.imageSave.setImageResource(R.drawable.ic_saved);
                    binding.layoutBottomSheetApply.imageSave.setColorFilter(ContextCompat.getColor(JobDetailActivity.this, R.color.green));
                } else {
                    binding.layoutBottomSheetApply.imageSave.setImageResource(R.drawable.ic_not_save);
                    binding.layoutBottomSheetApply.imageSave.setColorFilter(ContextCompat.getColor(JobDetailActivity.this, R.color.secondary_text));
                }
            }
        }
    }

    private void hideLayout(int visible) {
        binding.layoutHeader1.setVisibility(visible);
        binding.tabLayout.setVisibility(visible);
        binding.layoutBottomSheetApply.getRoot().setVisibility(visible);
        binding.viewPager.setVisibility(visible);
    }

    private void gotoAppJob() {
        binding.layoutBottomSheetApply.btnApplyJob.setOnClickListener(v -> {
            bottomSheetApplyJob.show();
        });

        binding.layoutBottomSheetApplyAgain.btnApplyJob.setOnClickListener(v -> {
            bottomSheetApplyJob.show();
        });
    }

    private void setStateAppBar() {
        binding.appBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (abs(verticalOffset) - appBarLayout.getTotalScrollRange() == 0) {
                binding.toolbar.setVisibility(View.VISIBLE);
            } else if (verticalOffset == 0) {

            } else {
                binding.toolbar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void setViewAdapter(List<Job> listRelatedJob, Job job, Company company) {
        jobDetailAdapter = new JobDetailAdapter(JobDetailActivity.this, listRelatedJob, job, company);
        binding.viewPager.setAdapter(jobDetailAdapter);
    }

    private void setTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void back() {
        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void clickSaveJob() {
        binding.layoutBottomSheetApply.cvSaveJob.setOnClickListener(v -> {
            try {
                saveJob(IDJob);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void saveJob(String jobId) throws JSONException {
        String base_url = Constant.url_dev + "/job";

        RequestQueue mRequestQueue = Volley.newRequestQueue(this);
        //post data
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("jobId", jobId);
//        pbLoading.setVisibility(View.VISIBLE);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, base_url + "/list-job-favourite", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status = response.getString("status");
//                    int position
                    int position = getIntent().getIntExtra(Constant.POSITION, -1);
                    sendBroadcastSaveJob(position);
                    if (status.equals("1")) {
                        binding.layoutBottomSheetApply.imageSave.setImageResource(R.drawable.ic_saved);
                        binding.layoutBottomSheetApply.imageSave.setColorFilter(ContextCompat.getColor(JobDetailActivity.this, R.color.green));
                        Constant.idSavedJobs.add(jobId);
                    } else {
                        binding.layoutBottomSheetApply.imageSave.setImageResource(R.drawable.ic_not_save);
                        Constant.idSavedJobs.remove(jobId);
                        binding.layoutBottomSheetApply.imageSave.setColorFilter(ContextCompat.getColor(JobDetailActivity.this, R.color.secondary_text));
                        CustomToast.makeText(JobDetailActivity.this, "Bạn đã bỏ lưu công việc!", CustomToast.LENGTH_SHORT, CustomToast.SUCCESS).show();
                    }

                } catch (JSONException e) {
                    Toast.makeText(JobDetailActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
//                    pbLoading.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String body;
                //get status code here
                String statusCode = String.valueOf(error.networkResponse.statusCode);
                if (error instanceof com.android.volley.NoConnectionError) {

                } else if (error.networkResponse.data != null) {
                    try {
                        if (error.networkResponse.statusCode == 401) {
                            Intent i = new Intent(JobDetailActivity.this, LoginActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            preferenceManager.clear();
                            startActivity(i);
                        }
                        body = new String(error.networkResponse.data, "UTF-8");
                        JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);
                        String message = convertedObject.get("message").toString();

                        Toast.makeText(JobDetailActivity.this, message.substring(1, message.length() - 1), Toast.LENGTH_SHORT).show();
                        //pbLoading.setVisibility(View.GONE);
                        Log.d("ABC", body);
                    } catch (UnsupportedEncodingException e) {
                        //pbLoading.setVisibility(View.GONE);
                        e.printStackTrace();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", preferenceManager.getString(Constant.TOKEN));
                return params;
            }

            ;
        };
        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
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
                throw new VolleyError(error.getMessage());
            }
        });
        mRequestQueue.add(jsonObjectRequest);
    }

    private void getJobDetail() {
        RequestQueue requestQueue = Volley.newRequestQueue(JobDetailActivity.this);

        String url = Constant.url_dev + "/job/detail?id=" + getIntent().getStringExtra(Constant.JOB_ID);
//        binding.idLoadingPB.setVisibility(View.VISIBLE);
        hideLayout(View.INVISIBLE);

        JsonObjectRequest data = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("data");
                    JSONObject jsonUserCompany = jsonObject.getJSONObject("userCompany");
                    idUserCompany = jsonUserCompany.getString("_id");
                    Log.d("idUserCompany", idUserCompany);
                    String nameCompany = "", place = "";
                    if (!jsonObject.isNull("idCompany")) {
                        nameCompany = jsonObject.getJSONObject("idCompany").getString("name");
                        place = jsonObject.getJSONObject("idCompany").getString("location");
                        IDCompany = jsonObject.getJSONObject("idCompany").getString("_id");
                    }
                    companyName = nameCompany;
                    binding.textNameJob.setText(jsonObject.getString("name"));
                    binding.textNameCompany.setText(nameCompany);
                    binding.textPosition.setText(place);
                    binding.textSalary.setText(jsonObject.getString("salary"));
                    if (!jsonObject.getJSONObject("idCompany").getString("image").isEmpty()) {
                        if(!URLUtil.isValidUrl(jsonObject.getJSONObject("idCompany").getString("image"))) {
                            binding.imageCompany.setImageBitmap(Constant.getBitmapFromEncodedString(jsonObject.getJSONObject("idCompany").getString("image")));
                        } else {
                            Picasso.get().load(jsonObject.getJSONObject("idCompany").getString("image")).into(binding.imageCompany);
                        }
                    }

                    for (int i = 0; i < jsonObject.getJSONArray("relatedJob").length(); i++) {
                        JSONObject jobRelated = jsonObject.getJSONArray("relatedJob").getJSONObject(i);

                        if (jobRelated.getString("status").equals("true")) {
                            listRelatedJob.add(new Job(
                                    jsonObject.getString("_id"),
                                    jsonObject.getString("name"),
                                    jsonObject.getJSONObject("idCompany").getString("_id"),
                                    jsonObject.getJSONObject("idCompany").getString("name"),
                                    jsonObject.getString("locationWorking"),
                                    jsonObject.getString("salary"),
                                    jsonObject.getString("deadline"),
                                    jsonObject.getString("description"),
                                    jsonObject.getString("requirement"),
                                    jsonObject.getJSONObject("idOccupation").getString("_id"),
                                    jsonObject.getJSONObject("idOccupation").getString("name"),
                                    jsonObject.getJSONObject("idCompany").getString("image"),
                                    jsonObject.getString("amount"),
                                    jsonObject.getString("workingForm"),
                                    jsonObject.getString("experience"),
                                    jsonObject.getString("gender")
                            ));
                        }
                    }

                    Company company = new Company(
                            jsonObject.getJSONObject("idCompany").getString("_id"),
                            jsonObject.getJSONObject("idCompany").getString("name"),
                            jsonObject.getJSONObject("idCompany").getString("isDelete"),
                            jsonObject.getJSONObject("idCompany").getString("link"),
                            jsonObject.getJSONObject("idCompany").getString("image"),
                            jsonObject.getJSONObject("idCompany").getInt("totalEmployee"),
                            jsonObject.getJSONObject("idCompany").getString("about"),
                            jsonObject.getJSONObject("idCompany").getString("address"),
                            jsonObject.getJSONObject("idCompany").getString("location"),
                            jsonObject.getJSONObject("idCompany").getString("phone")
                    );

                    job = new Job(
                            jsonObject.getString("_id"),
                            jsonObject.getString("name"),
                            jsonObject.getJSONObject("idCompany").getString("_id"),
                            jsonObject.getJSONObject("idCompany").getString("name"),
                            jsonObject.getString("locationWorking"),
                            jsonObject.getString("salary"),
                            jsonObject.getString("deadline"),
                            jsonObject.getString("description"),
                            jsonObject.getString("requirement"),
                            jsonObject.getJSONObject("idOccupation").getString("_id"),
                            jsonObject.getJSONObject("idOccupation").getString("name"),
                            jsonObject.getJSONObject("idCompany").getString("image"),
                            jsonObject.getString("amount"),
                            jsonObject.getString("workingForm"),
                            jsonObject.getString("experience"),
                            jsonObject.getString("gender")
                    );

                    setViewAdapter(
                            listRelatedJob,
                            job,
                            company
                    );
                    hideLayout(View.VISIBLE);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("Error", e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof com.android.volley.NoConnectionError) {

                }
                Log.d("Error", error.getMessage());
            }
        });
        data.setRetryPolicy(new RetryPolicy() {
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
                throw new VolleyError(error.getMessage());
            }
        });
        requestQueue.add(data);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(Constant.JOB_MODEL);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
