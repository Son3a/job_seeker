package com.nsb.job_seeker.activity.seeker;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.nsb.job_seeker.activity.BaseActivity;

import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.nsb.job_seeker.common.Constant;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.adapter.JobAdapter;
import com.nsb.job_seeker.adapter.KeywordAdapter;
import com.nsb.job_seeker.adapter.PositionAdapter;
import com.nsb.job_seeker.adapter.FilterAdapter;
import com.nsb.job_seeker.common.CustomDialogDelete;
import com.nsb.job_seeker.common.EventKeyboard;
import com.nsb.job_seeker.databinding.ActivitySeekerSearchResultBinding;
import com.nsb.job_seeker.databinding.ListViewItemJobBinding;
import com.nsb.job_seeker.listener.JobListener;
import com.nsb.job_seeker.listener.KeywordListener;
import com.nsb.job_seeker.listener.PositionListener;
import com.nsb.job_seeker.listener.FilterListener;
import com.nsb.job_seeker.model.Job;
import com.nsb.job_seeker.room.KeyWord;
import com.nsb.job_seeker.room.KeywordDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchResultActivity extends BaseActivity implements JobListener,
        PositionListener,  KeywordListener {
    private ActivitySeekerSearchResultBinding binding;
    private List<Job> jobList;
    private JobAdapter jobAdapter;
    private View layoutBottomExperience, layoutBottomSalary, layoutBottomPosition;
    private BottomSheetDialog bottomSheetExperience, bottomSheetSalary, bottomSheetPosition;
    private List<String> listExperiences, listSalary, listPosition;
    private RecyclerView recyclerViewExperience, recyclerViewSalary, recyclerViewPosition;
    private PositionAdapter positionAdapter;
    private SearchView searchView;
    boolean isKeyboardShowing = false;
    private List<KeyWord> keyWordList;
    private KeywordAdapter keywordAdapter;
    private String experience = "", salary = "", key = "", locationWorking = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        binding = ActivitySeekerSearchResultBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        init();

        try {
            setEvent();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        key = getIntent().getStringExtra("Keyword");
        jobList = new ArrayList<>();
        jobAdapter = new JobAdapter(this, jobList, this, true);
        binding.rcvResultSearch.setAdapter(jobAdapter);

        layoutBottomPosition = getLayoutInflater().inflate(R.layout.layout_position, null);
        searchView = layoutBottomPosition.findViewById(R.id.searchPosition);

        initBottomSheetSalary();
        initBottomSheetExperience();
        initBottomSheetPosition();
    }

    private void setEvent() throws JSONException {
        Log.d("Keyword", key);
        binding.textKeySearch.setText(key);

        back();
        findJob(key, experience, salary, locationWorking);
        getProvinceApi();
        openBottomExperience();
        openBottomSalary();
        openBottomPosition();
        filterPosition();
        setStateHistory();
        clickSearchJob();
        hideKeyBoard();
        eventKeyBoard();
        clickDeleteHistory();
        closeBottomPosition();
        closeBottomSalary();
        closeBottomExperience();
    }

    private void back() {
        binding.imageBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void clickDeleteHistory() {
        binding.textDeleteAll.setOnClickListener(v -> {
            CustomDialogDelete dialogDelete = new CustomDialogDelete(this, "Xóa", "Hủy", true) {
                @Override
                public void doSomeThing() {
                    deleteAllHistory();
                }
            };
            dialogDelete.openDiaLogDelete(getString(R.string.string_delete_all));
        });
    }

    private void deleteAllHistory() {
        KeywordDatabase.getInstance(this).keywordDAO().deleteAll();
        keyWordList.clear();
        showHistorySearch();
    }

    private void addKeyWordHistory(String data) {
        if (data.isEmpty()) return;
        deleteKeywordHistory(data);
        KeyWord keyWord = new KeyWord(data);
        KeywordDatabase.getInstance(this).keywordDAO().insertKeyword(keyWord);
    }

    private void deleteKeywordHistory(String data) {
        List<KeyWord> listDuplicate = KeywordDatabase.getInstance(this).keywordDAO().checkKeyWord(data);
        if (listDuplicate.size() == 0) return;
        KeywordDatabase.getInstance(this).keywordDAO().deleteKeyword(listDuplicate.get(0));
    }


    private void eventKeyBoard() {
        Constant.eventKeyBoard(binding.getRoot(), new EventKeyboard() {
            @Override
            public void hideKeyboard() {
                if (isKeyboardShowing) {
                    binding.textKeySearch.clearFocus();
                    binding.textKeySearch.setBackgroundResource(R.drawable.background_search_job);
                    isKeyboardShowing = false;
                }
            }

            @Override
            public void showKeyboard() {
                if (!isKeyboardShowing) {
                    isKeyboardShowing = true;
                    binding.textKeySearch.setBackgroundResource(R.drawable.background_search_job_border_green);
                }
            }
        });
    }

    private void hideKeyBoard() {
        binding.layoutContent.setOnClickListener(v -> {
            hideKeyboardFrom(SearchResultActivity.this, binding.textKeySearch);
        });
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void clickSearchJob() {
        binding.textKeySearch.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                try {
                    hideKeyboardFrom(SearchResultActivity.this, binding.textKeySearch);
                    if (binding.textKeySearch.getText() != null) {
                        key = binding.textKeySearch.getText().toString();
                    }
                    findJob(key, experience, salary, locationWorking);
                    addKeyWordHistory(binding.textKeySearch.getText().toString());
                    binding.layoutContentSearch.setVisibility(View.VISIBLE);
                    binding.layoutHistory.setVisibility(View.INVISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            }
            return false;
        });
    }

    private void initBottomSheetPosition() {
        listPosition = new ArrayList<>();
        listPosition.add("Tất cả");

        positionAdapter = new PositionAdapter(listPosition, this);
        recyclerViewPosition = layoutBottomPosition.findViewById(R.id.rcvPosition);
        recyclerViewPosition.setAdapter(positionAdapter);
        bottomSheetPosition = new BottomSheetDialog(this);
        bottomSheetPosition.setContentView(layoutBottomPosition);

        FrameLayout bottomSheet = bottomSheetPosition.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void filterPosition() {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                positionAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                positionAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private void initBottomSheetExperience() {
        listExperiences = new ArrayList<>();
        listExperiences.add("Tất cả");
        listExperiences.add("Không yêu cầu");
        listExperiences.add("Dưới 1 năm");
        listExperiences.add("1 năm");
        listExperiences.add("2 năm");
        listExperiences.add("3 năm");
        listExperiences.add("4 năm");
        listExperiences.add("5 năm");
        listExperiences.add("Trên 5 năm");


        layoutBottomExperience = getLayoutInflater().inflate(R.layout.layout_filter, null);
        bottomSheetExperience = new BottomSheetDialog(this);
        bottomSheetExperience.setContentView(layoutBottomExperience);

        FilterAdapter experienceAdapter = new FilterAdapter(listExperiences, new FilterListener() {
            @Override
            public void onClickItem(String data, int position) {
                experience = data;
                binding.textExperience.setText(data);
                bottomSheetExperience.dismiss();
                if (data.equals(listExperiences.get(0))) {
                    experience = "";
                    binding.textExperience.setText("Kinh nghiệm");
                    binding.textExperience.setTextColor(ContextCompat.getColor(SearchResultActivity.this, R.color.black));
                    binding.textExperience.setBackgroundResource(R.drawable.background_border);
                    TextViewCompat.setCompoundDrawableTintList(binding.textExperience, ColorStateList.valueOf(ContextCompat.getColor(SearchResultActivity.this, R.color.secondary_text)));
                } else {
                    binding.textExperience.setTextColor(ContextCompat.getColor(SearchResultActivity.this, R.color.green));
                    binding.textExperience.setBackgroundResource(R.drawable.background_border_green);
                    TextViewCompat.setCompoundDrawableTintList(binding.textExperience, ColorStateList.valueOf(ContextCompat.getColor(SearchResultActivity.this, R.color.green)));
                }
            }
        });
        recyclerViewExperience = layoutBottomExperience.findViewById(R.id.rcvFilter);
        recyclerViewExperience.setAdapter(experienceAdapter);
    }

    private void initBottomSheetSalary() {
        listSalary = new ArrayList<>();
        listSalary.add("Tất cả");
        listSalary.add("Dưới 10 triệu");
        listSalary.add("10 - 15 triệu");
        listSalary.add("15 - 20 triệu");
        listSalary.add("20 - 25 triệu");
        listSalary.add("25 - 30 triệu");
        listSalary.add("30 - 50 triệu");
        listSalary.add("Trên 50 triệu");
        listSalary.add("Thỏa thuận");

        layoutBottomSalary = getLayoutInflater().inflate(R.layout.layout_filter, null);
        FilterAdapter salaryAdapter = new FilterAdapter(listSalary, new FilterListener() {
            @Override
            public void onClickItem(String data, int position) {
                salary = data;
                binding.textSalary.setText(data);
                bottomSheetSalary.dismiss();
                if (data.contains("Dưới")) {
                    salary += "<";
                } else if (data.contains("Trên")) {
                    salary += ">";
                }
                if (data.equals(listSalary.get(0))) {
                    salary = "";
                    binding.textSalary.setText("Mức lương");
                    binding.textSalary.setTextColor(ContextCompat.getColor(SearchResultActivity.this, R.color.black));
                    binding.textSalary.setBackgroundResource(R.drawable.background_border);
                    TextViewCompat.setCompoundDrawableTintList(binding.textSalary, ColorStateList.valueOf(ContextCompat.getColor(SearchResultActivity.this, R.color.secondary_text)));
                } else {
                    binding.textSalary.setTextColor(ContextCompat.getColor(SearchResultActivity.this, R.color.green));
                    binding.textSalary.setBackgroundResource(R.drawable.background_border_green);
                    TextViewCompat.setCompoundDrawableTintList(binding.textSalary, ColorStateList.valueOf(ContextCompat.getColor(SearchResultActivity.this, R.color.green)));
                }
            }
        });
        recyclerViewSalary = layoutBottomSalary.findViewById(R.id.rcvFilter);
        recyclerViewSalary.setAdapter(salaryAdapter);

        bottomSheetSalary = new BottomSheetDialog(this);
        bottomSheetSalary.setContentView(layoutBottomSalary);
    }

    private void openBottomPosition() {
        binding.layoutPosition.setOnClickListener(v -> {
            bottomSheetPosition.show();
        });
    }

    private void openBottomExperience() {
        binding.textExperience.setOnClickListener(v -> {
            bottomSheetExperience.show();
        });
    }

    private void openBottomSalary() {
        binding.textSalary.setOnClickListener(v -> {
            bottomSheetSalary.show();
        });
    }

    private void closeBottomExperience() {
        ImageView imageClose = bottomSheetExperience.findViewById(R.id.imageClose);
        imageClose.setOnClickListener(v -> {
            bottomSheetExperience.dismiss();
        });
    }

    private void closeBottomSalary() {
        ImageView imageClose = bottomSheetSalary.findViewById(R.id.imageClose);
        imageClose.setOnClickListener(v -> {
            bottomSheetSalary.dismiss();
        });
    }

    private void closeBottomPosition() {
        ImageView imageClose = bottomSheetPosition.findViewById(R.id.imageClose);
        imageClose.setOnClickListener(v -> {
            bottomSheetPosition.dismiss();
        });
    }

    private void getProvinceApi() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://provinces.open-api.vn/api/";

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                        String data = jsonObject.getString("name");
                        String newData = data.replace("Tỉnh", "");
                        listPosition.add(newData.replace("Thành phố", ""));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                positionAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof com.android.volley.NoConnectionError) {

                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
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
        queue.add(jsonObjectRequest);
    }

    private void setStateHistory() {
        binding.textKeySearch.setOnFocusChangeListener((view, b) -> {
            if (b) {
                if (binding.textKeySearch.getText() == null || binding.textKeySearch.getText().toString().equals("")) {
                    showHistorySearch();
                }
            }
        });

        binding.textKeySearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (binding.textKeySearch.getText().toString().equals("")) {
                    showHistorySearch();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void showHistorySearch() {
        keyWordList = KeywordDatabase.getInstance(this).keywordDAO().getListKeyword();
        Collections.reverse(keyWordList);
        if (keyWordList.size() != 0) {
            keywordAdapter = new KeywordAdapter(keyWordList, this);
            binding.rcvHistorySearch.setAdapter(keywordAdapter);
            //keywordAdapter.notifyDataSetChanged();
            binding.layoutHistory.setVisibility(View.VISIBLE);
            binding.layoutContentSearch.setVisibility(View.INVISIBLE);
        }
    }

    private void findJob(String key, String experience, String salary, String locationWorking) throws JSONException {
        binding.pbLoading.setVisibility(View.VISIBLE);
        binding.layoutAmountResult.setVisibility(View.INVISIBLE);
        binding.textEmpty.setVisibility(View.GONE);
        String url = Constant.url_dev + "/job/list/search";

        jobList.clear();

        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject params = new JSONObject();
        params.put("key", key);
        params.put("salary", salary);
        params.put("experience", experience);
        params.put("locationWorking", locationWorking);

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray listJobs = response.getJSONArray("data");
                    if (listJobs.length() == 0) {
//                        tvNote.setText("Kết quả tìm kiếm không có");
//                        tvNote.setVisibility(View.VISIBLE);
//                        listView.setVisibility(View.GONE);
                        binding.textEmpty.setVisibility(View.VISIBLE);
                        binding.pbLoading.setVisibility(View.GONE);
                        binding.textAmountResult.setText("0");
                        return;
                    }
                    binding.textEmpty.setVisibility(View.INVISIBLE);
                    binding.textAmountResult.setText(String.valueOf(listJobs.length()));
                    binding.layoutAmountResult.setVisibility(View.VISIBLE);
                    for (int i = 0; i < listJobs.length(); i++) {
                        JSONObject job = listJobs.getJSONObject(i);
                        if (job.getString("status").equals("true")) {
                            jobList.add(new Job(
                                    job.getString("_id"),
                                    job.getString("name"),
                                    job.getJSONObject("idCompany").getString("name"),
                                    job.getString("locationWorking"),
                                    job.getString("salary"),
                                    Constant.setTime(job.getString("deadline")),
                                    job.getString("description"),
                                    job.getString("requirement"),
                                    job.getJSONObject("idOccupation").getString("name"),
                                    job.getJSONObject("idCompany").getString("image"),
                                    job.getString("amount"),
                                    job.getString("workingForm"),
                                    job.getString("experience"),
                                    job.getString("gender")
                            ));
                        }
                    }
                    jobAdapter.notifyDataSetChanged();
                    binding.pbLoading.setVisibility(View.GONE);

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof com.android.volley.NoConnectionError) {

                }
                System.out.println(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        sr.setRetryPolicy(new RetryPolicy() {
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
        queue.add(sr);
    }

    @Override
    public void onClick(Job job) {
        Intent i = new Intent(this, JobDetailActivity.class);
        i.putExtra("id", job.getId());
        i.putExtra("isApply", true);
        startActivity(i);
    }

    @Override
    public void onSave(Job job, ListViewItemJobBinding binding) {

    }

    @Override
    public void onClickPosition(String data) {
        locationWorking = data;
        binding.textPosition.setText(data);
        bottomSheetPosition.dismiss();
        if (data.equals(listPosition.get(0))) {
            locationWorking = "";
            binding.textPosition.setText("Khu vực");
        }
        try {
            findJob(key, experience, salary, locationWorking);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClickRemove(KeyWord keyWord, int position) {
        CustomDialogDelete dialogDelete = new CustomDialogDelete(this, "Xóa", "Hủy", true) {
            @Override
            public void doSomeThing() {
                if (keyWordList.size() != 0) {
                    keyWordList.remove(keyWord);
                    keywordAdapter.notifyDataSetChanged();
                    if (keyWordList.size() == 0) {
                        binding.layoutHistory.setVisibility(View.INVISIBLE);
                        binding.layoutContentSearch.setVisibility(View.VISIBLE);
                    }
                }
                KeywordDatabase.getInstance(SearchResultActivity.this).keywordDAO().deleteKeyword(keyWord);
            }
        };
        dialogDelete.openDiaLogDelete(getString(R.string.string_delete_one));
    }

    @Override
    public void onClickItem(KeyWord keyWord) {
        if (binding.textKeySearch.getText() != null) {
            key = binding.textKeySearch.getText().toString();
        }
        try {
            findJob(key, experience, salary, locationWorking);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
