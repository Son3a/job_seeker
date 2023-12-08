package com.nsb.job_seeker.fragment.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.activity.admin.UpdateCompanyActivity;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.common.LoadingDialog;
import com.nsb.job_seeker.common.PreferenceManager;
import com.nsb.job_seeker.databinding.FragmentEmployerCompanyBinding;
import com.nsb.job_seeker.model.Company;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class CompanyFragment extends Fragment {
    private FragmentEmployerCompanyBinding binding;
    private PreferenceManager preferenceManager;
    private LoadingDialog loadingDialog;
    private Company company;
    private final int RESULT_OK = -1;

    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent intent = result.getData();
                        Company company = (Company) intent.getExtras().getSerializable(Constant.COMPANY_MODEL);

                        if (company.getImage() != null && !company.getImage().equals("")) {
                            binding.imageCompany.setImageBitmap(Constant.getBitmapFromEncodedString(company.getImage()));
                        }
                        binding.textNameCompany.setText(company.getName());
                        binding.textIntroduceCompany.setText(company.getAbout());
                        binding.textLink.setText(company.getLink());
                        binding.textAddress.setText(company.getAddress());
                        binding.textAmountEmployer.setText(company.getTotalEmployee() + " nhân viên");
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEmployerCompanyBinding.inflate(getLayoutInflater());

        init();
        setEvent();

        return binding.getRoot();
    }

    private void init() {
        if (getContext() != null) {
            loadingDialog = new LoadingDialog(getContext());
            preferenceManager = new PreferenceManager(getContext());
        }
    }

    private void setEvent() {
        if (getContext() != null) {
            setExpandText();
            getInfoCompany();
            openBottomFunc();
        }
    }

    private void openBottomFunc() {
        binding.cvExpand.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
            View layoutFunc = getLayoutInflater().inflate(R.layout.layout_function_company, null);
            bottomSheetDialog.setContentView(layoutFunc);

            LinearLayout layoutEditCompany = layoutFunc.findViewById(R.id.layoutEditCompany);
            layoutEditCompany.setOnClickListener(view -> {
                if (company != null) {
                    Intent intent = new Intent(getContext(), UpdateCompanyActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constant.COMPANY_MODEL, company);
                    intent.putExtras(bundle);
                    activityResultLauncher.launch(intent);
                    bottomSheetDialog.dismiss();
                }
            });

            ImageView imageClose = layoutFunc.findViewById(R.id.imageClose);
            imageClose.setOnClickListener(view -> {
                bottomSheetDialog.dismiss();
            });

            bottomSheetDialog.show();
        });
    }

    private void setExpandText() {
        binding.textCollapse.setOnClickListener(v -> {
            binding.textCollapse.setText(binding.textIntroduceCompany.isExpanded() ? "Xem thêm" : "Rút gọn");
            binding.textIntroduceCompany.toggle();

        });
    }

    private void getInfoCompany() {
        String url = Constant.url_dev + "/company/detail?id=" + preferenceManager.getString(Constant.COMPANY_ID);
        if(loadingDialog!=null) {
            loadingDialog.showDialog();
        }
        binding.layoutHeader.setVisibility(View.GONE);
        binding.layoutContent.setVisibility(View.GONE);
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = response.getJSONObject("data");

                    company = new Company(
                            jsonObject.getString("_id"),
                            jsonObject.getString("name"),
                            jsonObject.getString("isDelete"),
                            jsonObject.getString("link"),
                            jsonObject.getString("image"),
                            jsonObject.getInt("totalEmployee"),
                            jsonObject.getString("about"),
                            jsonObject.getString("address"),
                            jsonObject.getString("location"),
                            jsonObject.getString("phone")
                    );
                    if (company.getImage() != null && !company.getImage().equals("")) {
                        if (!URLUtil.isValidUrl(company.getImage())) {
                            binding.imageCompany.setImageBitmap(Constant.getBitmapFromEncodedString(company.getImage()));
                        } else {
                            Picasso.get().load(company.getImage()).into(binding.imageCompany);
                        }
                    }
                    binding.textNameCompany.setText(company.getName());
                    binding.textIntroduceCompany.setText(company.getAbout());
                    binding.textLink.setText(company.getLink());
                    binding.textAddress.setText(company.getAddress());
                    binding.textAmountEmployer.setText(company.getTotalEmployee() + " nhân viên");
                    if(loadingDialog!=null) {
                        loadingDialog.hideDialog();
                    }
                    binding.layoutHeader.setVisibility(View.VISIBLE);
                    binding.layoutContent.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
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
