package com.nsb.job_seeker.seeker;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.adapter.JobAdapter;
import com.nsb.job_seeker.adapter.KeywordAdapter;
import com.nsb.job_seeker.adapter.PositionAdapter;
import com.nsb.job_seeker.adapter.SalaryAdapter;
import com.nsb.job_seeker.common.CustomDialogDelete;
import com.nsb.job_seeker.databinding.ActivitySeekerSearchResultBinding;
import com.nsb.job_seeker.databinding.ListViewItemJobBinding;
import com.nsb.job_seeker.listener.ExperienceListener;
import com.nsb.job_seeker.listener.JobListener;
import com.nsb.job_seeker.listener.KeywordListener;
import com.nsb.job_seeker.listener.PositionListener;
import com.nsb.job_seeker.listener.SalaryListener;
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

public class SearchResultActivity extends AppCompatActivity implements JobListener, SalaryListener,
        PositionListener, ExperienceListener, KeywordListener {
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
        jobList = new ArrayList<>();
        jobAdapter = new JobAdapter(jobList, this, true);
        binding.rcvResultSearch.setAdapter(jobAdapter);
        layoutBottomPosition = getLayoutInflater().inflate(R.layout.layout_position, null);
        searchView = layoutBottomPosition.findViewById(R.id.searchPosition);
        layoutBottomSalary = getLayoutInflater().inflate(R.layout.layout_salary, null);

        initBottomSheetSalary();
        initBottomSheetExperience();
        initBottomSheetPosition();
    }

    private void setEvent() throws JSONException {
        String key = getIntent().getStringExtra("Keyword");
        binding.textKeySearch.setText(key);

        findJob(key);
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
    }


    private void clickDeleteHistory() {
        binding.textDeleteAll.setOnClickListener(v -> {
            CustomDialogDelete dialogDelete = new CustomDialogDelete(this) {
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
        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                binding.getRoot().getWindowVisibleDisplayFrame(r);
                int screenHeight = binding.getRoot().getRootView().getHeight();

                // r.bottom is the position above soft keypad or device button.
                // if keypad is shown, the r.bottom is smaller than that before.
                int keypadHeight = screenHeight - r.bottom;

                Log.d("TAG", "keypadHeight = " + keypadHeight);

                if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                    // keyboard is opened
                    if (!isKeyboardShowing) {
                        isKeyboardShowing = true;
                        binding.textKeySearch.setBackgroundResource(R.drawable.background_search_job_border_green);
                    }
                } else {
                    // keyboard is closed
                    if (isKeyboardShowing) {
                        binding.textKeySearch.clearFocus();
                        binding.textKeySearch.setBackgroundResource(R.drawable.background_search_job);
                        isKeyboardShowing = false;

                    }
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
                    findJob("");
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
//        listExperiences = new ArrayList<>();
////        listExperiences.add("Tất cả");
////        listExperiences.add("Không yêu cầu");
////        listExperiences.add("Dưới 1 năm");
////        listExperiences.add("1 năm");
////        listExperiences.add("2 năm");
////        listExperiences.add("3 năm");
////        listExperiences.add("4 năm");
////        listExperiences.add("5 năm");
////        listExperiences.add("Trên 5 năm");
//        listExperiences.add("1");
//        listExperiences.add("2");
//        listExperiences.add("3");
//        listExperiences.add("4");
//        listExperiences.add("5");
//        listExperiences.add("6");
//        listExperiences.add("7");
//        listExperiences.add("8");
//        listExperiences.add("9");
//        listExperiences.add("10");
//        listExperiences.add("11");
//        listExperiences.add("12");
//        listExperiences.add("13");
//        listExperiences.add("14");
//
//        layoutBottomExperience = getLayoutInflater().inflate(R.layout.layout_experience, null);
////        bottomSheetExperience = new BottomSheetDialog(this);
////        bottomSheetExperience.setContentView(layoutBottomExperience);
////        bottomSheetExperience.setCancelable(true);
////
////        experienceAdapter = new ExperienceAdapter(listExperiences);
////        recyclerViewExperience = layoutBottomExperience.findViewById(R.id.rcvExperience);
////        recyclerViewExperience.setAdapter(experienceAdapter);
//        ScrollPickerView scrollPickerView = layoutBottomExperience.findViewById(R.id.scrollpickerview);
//        ScrollPickerAdapter scrollPickerAdapter = new ScrollPickerAdapter(listExperiences, R.layout.layout_experience,1);
//        ScrollPickerView.Builder(scrollPickerView).scr
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

        SalaryAdapter salaryAdapter = new SalaryAdapter(listSalary, this);
        recyclerViewSalary = layoutBottomSalary.findViewById(R.id.rcvSalary);
        recyclerViewSalary.setAdapter(salaryAdapter);
        bottomSheetSalary = new BottomSheetDialog(this);
        bottomSheetSalary.setContentView(layoutBottomSalary);
    }

    private void openBottomPosition() {
        binding.textPosition.setOnClickListener(v -> {
            bottomSheetPosition.show();
        });
    }

    private void openBottomExperience() {
        binding.textExperience.setOnClickListener(v -> {
            //bottomSheetExperience.show();
        });
    }

    private void openBottomSalary() {
        binding.textSalary.setOnClickListener(v -> {
            bottomSheetSalary.show();
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
                System.out.println(error);
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
                    Toast.makeText(SearchResultActivity.this, "Show history", Toast.LENGTH_SHORT).show();
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

    private void findJob(String key) throws JSONException {
        binding.pbLoading.setVisibility(View.VISIBLE);
        String url = "https://job-seeker-smy5.onrender.com/job/list/search";

        jobList.clear();
        String idOccupation[] = new String[0];
        String locationWorking[] = new String[0];

//        if (positionSpnLocation != 0) {
//            locationWorking = new String[]{spinnerLocation.getSelectedItem().toString()};
//        }
//
//        if ((positionSpnIdOccupation - 1) >= 0) {
//            idOccupation = new String[]{idTypeJobList.get(positionSpnIdOccupation - 1)};
//        }

        RequestQueue queue = Volley.newRequestQueue(this);


        JSONArray idOccupationParam = new JSONArray(idOccupation);
        JSONArray locationWorkingParam = new JSONArray(locationWorking);

        JSONObject params = new JSONObject();
        params.put("key", key);
        params.put("locationWorking", locationWorkingParam);
        params.put("idOccupation", idOccupationParam);

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray listJobs = response.getJSONArray("data");
                    String idCompany = "";
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
                    for (int i = 0; i < listJobs.length(); i++) {
                        JSONObject job = listJobs.getJSONObject(i);
                        if (job.getString("status").equals("true")) {

                            if (!job.isNull("idCompany")) {
                                idCompany = job.getJSONObject("idCompany").getString("name");
                            } else {
                                idCompany = "";
                            }

                            String time = Program.setTime(job.getString("deadline"));

                            jobList.add(new Job(
                                    job.getString("_id"),
                                    job.getString("name"),
                                    idCompany,
                                    job.getString("locationWorking"),
                                    job.getString("salary"),
                                    time
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
                System.out.println(error);
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
    public void onSave(Job job, int position, boolean isSaveView, ListViewItemJobBinding binding) {

    }

    @Override
    public void onClickPosition(String data) {
        binding.textPosition.setText(data);
        bottomSheetPosition.dismiss();
        if (data.equals(listPosition.get(0))) {
            binding.textPosition.setText("Khu vực");
        }
        try {
            findJob(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClickSalary(String data) {
        binding.textSalary.setText(data);
        bottomSheetSalary.dismiss();
        if (data.equals(listSalary.get(0))) {
            binding.textSalary.setText("Mức lương");
            binding.textSalary.setTextColor(ContextCompat.getColor(this, R.color.black));
            binding.textSalary.setBackgroundResource(R.drawable.background_border);
            TextViewCompat.setCompoundDrawableTintList(binding.textSalary, ColorStateList.valueOf(ContextCompat.getColor(this, R.color.secondary_text)));
        } else {
            binding.textSalary.setTextColor(ContextCompat.getColor(this, R.color.green));
            binding.textSalary.setBackgroundResource(R.drawable.background_border_green);
            TextViewCompat.setCompoundDrawableTintList(binding.textSalary, ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green)));
        }
    }

    @Override
    public void onClickExperience(String data) {
        //binding.textSalary.setText(data);
        //bottomSheetExperience.dismiss();
        //binding.textExperience.setTextColor(ContextCompat.getColor(this,R.color.green));
    }

    @Override
    public void onClickRemove(KeyWord keyWord, int position) {
        CustomDialogDelete dialogDelete = new CustomDialogDelete(this) {
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

    }
}
