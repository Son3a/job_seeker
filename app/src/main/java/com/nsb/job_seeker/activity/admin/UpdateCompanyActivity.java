package com.nsb.job_seeker.activity.admin;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nsb.job_seeker.activity.Activity_Profile;
import com.nsb.job_seeker.activity.seeker.SeekerMainActivity;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.CustomToast;
import com.nsb.job_seeker.common.LoadingDialog;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.ActivityUpdateCompanyBinding;
import com.nsb.job_seeker.model.Company;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class UpdateCompanyActivity extends AppCompatActivity {
    private ActivityUpdateCompanyBinding binding;
    private String encodeImage = "";
    private PreferenceManager preferenceManager;
    private LoadingDialog loadingDialog;
    private Company company;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityUpdateCompanyBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        init();
        setEvent();
    }

    private void init() {
        company = (Company) getIntent().getExtras().getSerializable(Constant.COMPANY_MODEL);
        preferenceManager = new PreferenceManager(this);
        loadingDialog = new LoadingDialog(this);
    }

    private void setEvent() {
        loadContent();
        openStorage();
        clickUpdateCompany();
    }

    private void loadContent() {
        if (company != null) {
            if (company.getImage() != null && !company.getImage().equals("")) {
                binding.imageCompany.setImageBitmap(Constant.getBitmapFromEncodedString(company.getImage()));
            }
            binding.textNameCompany.setText(company.getName());
            binding.textIntroduceCompany.setText(company.getAbout());
            binding.textLink.setText(company.getLink());
            binding.textAddressCompany.setText(company.getAddress());
            binding.textAmountEmployer.setText(company.getTotalEmployee() + " nhân viên");
        }
    }

    private void openStorage() {
        binding.imageCompany.setOnClickListener(v -> {
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
                            binding.imageCompany.setImageBitmap(bitmap);
                            binding.textChooseImageCompany.setVisibility(View.GONE);
                            encodeImage = Constant.encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private void clickUpdateCompany() {
        binding.btnApply.setOnClickListener(v -> {
            try {
                updateCompany(
                        binding.textNameCompany.getText().toString().trim(),
                        Constant.getNumFromString(binding.textAmountEmployer.getText().toString()),
                        binding.textLink.getText().toString().trim(),
                        encodeImage,
                        binding.textIntroduceCompany.getText().toString().trim(),
                        binding.textAddressCompany.getText().toString().trim());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void updateCompany(String name, int totalEmployee, String link, String image,
                               String about, String address) throws JSONException {
        String url = Constant.url_dev + "/company/update";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JSONObject jsonReq = new JSONObject();
        jsonReq.put("_id", company.getId());
        jsonReq.put("name", name);
        jsonReq.put("totalEmployee", totalEmployee);
        jsonReq.put("link", link);
        jsonReq.put("image", image);
        jsonReq.put("about", about);
        jsonReq.put("address", address);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, url, jsonReq, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = response.getJSONObject("relsult");

                    Company company = new Company(
                            jsonObject.getString("_id"),
                            jsonObject.getString("name"),
                            jsonObject.getString("isDelete"),
                            jsonObject.getString("link"),
                            jsonObject.getString("image"),
                            jsonObject.getInt("totalEmployee"),
                            jsonObject.getString("about"),
                            jsonObject.getString("address"),
                            "",
                            ""
                    );

                    CustomToast.makeText(UpdateCompanyActivity.this, "Cập nhật thành công!", CustomToast.LENGTH_SHORT, CustomToast.SUCCESS).show();
                    Intent intent = new Intent(UpdateCompanyActivity.this, SeekerMainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constant.COMPANY_MODEL, company);
                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", preferenceManager.getString(Constant.TOKEN));
                return params;
            }
        };
        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 0;
            }

            @Override
            public int getCurrentRetryCount() {
                return 0;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
                throw new VolleyError(error);
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}