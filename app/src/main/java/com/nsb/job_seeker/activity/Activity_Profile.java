package com.nsb.job_seeker.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.nsb.job_seeker.activity.BaseActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.CustomToast;
import com.nsb.job_seeker.common.LoadingDialog;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ActivityProfileBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class Activity_Profile extends BaseActivity {
    private ActivityProfileBinding binding;
    private String base_url = Constant.url_dev + "/auth";
    private PreferenceManager preferenceManager;
    private LoadingDialog loadingDialog;

    String name, phone, image;
    private String encodeImage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        init();
        setEvent();
    }

    private void init() {
        loadingDialog = new LoadingDialog(this);
        preferenceManager = new PreferenceManager(getApplicationContext());
    }

    private void setEvent() {
        binding.icBack.setOnClickListener(v -> {
            onBackPressed();
        });

        loadInfo();

        clickSave();

        openStorage();
    }

    private boolean checkChange() {
        if (!binding.textName.getText().toString().trim().equals(name)) {
            return true;
        }
        if (!binding.textPhone.getText().toString().trim().equals(phone)) {
            return true;
        }
        if (!image.equals(encodeImage)) {
            return true;
        }
        return false;
    }

    private void openStorage() {
        binding.imageAvatar.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(i);
        });
    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageAvatar.setImageBitmap(bitmap);
                            encodeImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private void clickSave() {
        binding.btnSave.setOnClickListener(v -> {
            if (!checkChange()) {
                if (isEmpty(binding.textName)) {
                    binding.layoutErrorName.setVisibility(View.VISIBLE);
                } else {
                    binding.layoutErrorName.setVisibility(View.GONE);
                }
                if (isEmpty(binding.textPhone)) {
                    binding.layoutErrorPhone.setVisibility(View.VISIBLE);
                } else {
                    binding.layoutErrorPhone.setVisibility(View.GONE);

                }

                if (!isEmpty(binding.textName) && !isEmpty(binding.textPhone)) {
                    binding.layoutErrorPhone.setVisibility(View.GONE);
                    binding.layoutErrorName.setVisibility(View.GONE);
                    try {
                        changProfileService(binding.textName.getText().toString(), binding.textEmail.getText().toString(), binding.textPhone.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private boolean isEmpty(EditText editText) {
        if (editText.getText().toString().isEmpty() || editText.getText().toString().trim().equals("")) {
            return true;
        }
        return false;
    }

    // show file choose to upload

    private void loadInfo() {
        String url = Constant.url_dev + "/auth/info-user";
        loadingDialog.showDialog();
        RequestQueue mRequestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    loadingDialog.hideDialog();
                    name = response.getString("name");
                    phone = response.getString("phone");

                    binding.textName.setText(name);
                    binding.textPhone.setText(phone);
                    binding.textEmail.setText(response.getString("email"));

                    image = response.getString("avatar");
                    if (image != null && !image.equals("")) {
                        binding.imageAvatar.setImageBitmap(getBitmapFromEncodedString(image));
                    }

                } catch (JSONException e) {
                    Log.d("Error", e.getMessage());
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingDialog.hideDialog();
                if (error instanceof com.android.volley.NoConnectionError) {

                } else if (error.networkResponse.data != null & error.networkResponse.statusCode == 401) {
                    Intent i = new Intent(Activity_Profile.this, LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    preferenceManager.clear();
                    startActivity(i);
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
                loadingDialog.hideDialog();
                if (error.networkResponse.data != null & error.networkResponse.statusCode == 401) {
                    Intent i = new Intent(Activity_Profile.this, LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    preferenceManager.clear();
                    startActivity(i);
                }
            }
        });
        mRequestQueue.add(jsonObjectRequest);
    }

    private void changProfileService(String name, String email, String phone) throws JSONException {
        RequestQueue mRequestQueue = Volley.newRequestQueue(Activity_Profile.this);
        //post data
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        jsonObject.put("email", email);
        jsonObject.put("phone", phone);
        jsonObject.put("avatar", encodeImage);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, base_url + "/edit-profile", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String message = response.getString("message");
                    Log.d("ABC", message);
                    CustomToast.makeText(Activity_Profile.this, "Đổi mật khẩu thành công!", CustomToast.LENGTH_SHORT, CustomToast.SUCCESS).show();
                    finish();
                } catch (JSONException e) {
                    Log.d("ABC", e.toString());
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String body;
                //get status code here
                try {
                    if (error instanceof com.android.volley.NoConnectionError) {

                    } else if (error.networkResponse.data != null & error.networkResponse.statusCode == 401) {
                        Intent i = new Intent(Activity_Profile.this, LoginActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        preferenceManager.clear();
                        startActivity(i);
                    } else {
                        body = new String(error.networkResponse.data, "UTF-8");
                        Log.d("ABC", body);
                        JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);
                        String message = convertedObject.get("message").toString();
                    }

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences sharedPreferences = getSharedPreferences(Constant.sharedPreferencesName, MODE_PRIVATE);
                String ACCESSTOKEN = sharedPreferences.getString("accessToken", "");
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", preferenceManager.getString(Constant.TOKEN));
                return params;
            }
        };
        ;
        mRequestQueue.add(jsonObjectRequest);
    }
}