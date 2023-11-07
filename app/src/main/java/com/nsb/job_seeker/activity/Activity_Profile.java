package com.nsb.job_seeker.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

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
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.activity.seeker.SeekerMainActivity;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.CustomToast;
import com.nsb.job_seeker.common.EventKeyboard;
import com.nsb.job_seeker.common.LoadingDialog;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ActivityProfileBinding;

import org.json.JSONException;
import org.json.JSONObject;

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
    private boolean isKeyboardShowing = false;
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
        back();

        loadInfo();

        clickSave();

        openStorage();

        eventKeyBoard();
    }

    private void eventKeyBoard() {
        Constant.eventKeyBoard(binding.getRoot(), new EventKeyboard() {
            @Override
            public void hideKeyboard() {
                if (isKeyboardShowing) {
                    binding.textName.clearFocus();
                    binding.textPhone.clearFocus();
                    isKeyboardShowing = false;
                }
            }

            @Override
            public void showKeyboard() {
                if (!isKeyboardShowing) {
                    isKeyboardShowing = true;
                }
            }
        });
    }

    private void back() {
        binding.btnBack.setOnClickListener(v -> {
            finish();
        });

        binding.icBack.setOnClickListener(v -> {
            finish();
        });
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

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageAvatar.setImageBitmap(bitmap);
                            encodeImage = Constant.encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private void clickSave() {
        binding.btnSave.setOnClickListener(v -> {
            if (checkChange()) {
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
        binding.layoutContent.setVisibility(View.INVISIBLE);
        loadingDialog.showDialog();
        RequestQueue mRequestQueue = Volley.newRequestQueue(this);

        Log.d("Info", "Load info");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    loadingDialog.hideDialog();
                    binding.layoutContent.setVisibility(View.VISIBLE);
                    Log.d("Info", "Load info success!");

                    if (response.getString("phone") != null) {
                        phone = response.getString("phone");
                        binding.textPhone.setText(phone);
                    }

                    name = response.getString("name");
                    binding.textName.setText(name);
                    binding.textEmail.setText(response.getString("email"));

                    Log.d("Info", "Email + Name: " + name + " " + response.getString("email"));

                    if (response.getString("avatar") != null) {
                        image = response.getString("avatar");
                        encodeImage = response.getString("avatar");
                        if (image != null && !image.equals("")) {
                            binding.imageAvatar.setImageBitmap(Constant.getBitmapFromEncodedString(image));
                        }
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
                binding.layoutContent.setVisibility(View.VISIBLE);
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
                throw new VolleyError(error.getMessage());
            }
        });
        mRequestQueue.add(jsonObjectRequest);
    }

    private void changProfileService(String name, String email, String phone) throws JSONException {
        RequestQueue mRequestQueue = Volley.newRequestQueue(Activity_Profile.this);
        loadingDialog.showDialog();
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
                    loadingDialog.hideDialog();
                    String message = response.getString("message");
                    Log.d("ABC", message);
                    preferenceManager.putString(Constant.NAME, name);
                    preferenceManager.putString(Constant.PHONE, phone);
                    preferenceManager.putString(Constant.MAIL, email);
                    preferenceManager.putString(Constant.AVATAR, encodeImage);
                    CustomToast.makeText(Activity_Profile.this, "Cập nhật thông tin thành công!", CustomToast.LENGTH_SHORT, CustomToast.SUCCESS).show();

                    sendBroadcast(encodeImage);

                    Intent intent = new Intent(Activity_Profile.this, SeekerMainActivity.class);
                    intent.putExtra(Constant.AVATAR, encodeImage);
                    intent.putExtra(Constant.NAME, name);
                    setResult(RESULT_OK, intent);
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

    private void sendBroadcast(String avatar) {
        Intent intent = new Intent(Constant.BROADCAST_AVATAR);
        intent.putExtra(Constant.AVATAR, avatar);
        sendBroadcast(intent);
    }
}
