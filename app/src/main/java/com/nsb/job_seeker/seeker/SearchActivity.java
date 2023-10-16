package com.nsb.job_seeker.seeker;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.android.material.button.MaterialButton;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.adapter.KeywordAdapter;
import com.nsb.job_seeker.databinding.ActivitySeekerSearchBinding;
import com.nsb.job_seeker.databinding.ListViewItemJobBinding;
import com.nsb.job_seeker.listener.JobListener;
import com.nsb.job_seeker.model.Job;
import com.nsb.job_seeker.room.KeyWord;
import com.nsb.job_seeker.room.KeywordDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements JobListener {
    private ActivitySeekerSearchBinding binding;
    private String url = "https://job-seeker-smy5.onrender.com/job/list/search";
    boolean isKeyboardShowing = false;
    private List<KeyWord> keyWordList;
    private KeywordAdapter keywordAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        binding = ActivitySeekerSearchBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        init();
        setEvent();
    }

    private void init() {
        keyWordList = new ArrayList<>();
    }

    private void setEvent() {
        back();
        loadingHistory();
        eventKeyBoard();
        hideKeyBoard();
        clickSearchJob();
        clickDeleteHistory();
    }

    private void back() {
        binding.imageBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void deleteKeywordHistory(String data) {
        List<KeyWord> listDuplicate = KeywordDatabase.getInstance(this).keywordDAO().checkKeyWord(data);
        if (listDuplicate.size() == 0) return;
        KeywordDatabase.getInstance(this).keywordDAO().deleteKeyword(listDuplicate.get(0));
    }

    private void getDataFromHistory() {
        keyWordList = KeywordDatabase.getInstance(this).keywordDAO().getListKeyword();
        Collections.reverse(keyWordList);
        keywordAdapter.notifyDataSetChanged();
    }

    private void openDiaLogDelete() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_layout_answer);

        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams winLayoutParams = window.getAttributes();
        winLayoutParams.gravity = Gravity.CENTER;
        window.setAttributes(winLayoutParams);

        dialog.setCancelable(false);

        MaterialButton btnDelete = dialog.findViewById(R.id.btnDelete);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);
        TextView textContent = dialog.findViewById(R.id.textContent);

        btnDelete.setOnClickListener(v -> {
            deleteAllHistory();
        });

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    private void loadingHistory() {
        keyWordList = KeywordDatabase.getInstance(this).keywordDAO().getListKeyword();
        Collections.reverse(keyWordList);
        if (keyWordList.size() != 0) {
            binding.layoutHistory.setVisibility(View.VISIBLE);
            binding.layoutImage.setVisibility(View.GONE);
            keywordAdapter = new KeywordAdapter(keyWordList);
            binding.rcvHistorySearch.setAdapter(keywordAdapter);
        } else {
            binding.layoutHistory.setVisibility(View.GONE);
            binding.layoutImage.setVisibility(View.VISIBLE);
        }
    }

    private void clickSearchJob() {
        binding.textKeySearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                addKeyWordHistory(binding.textKeySearch.getText().toString());
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
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
            hideKeyboardFrom(SearchActivity.this, binding.textKeySearch);
        });
    }

    private void clickDeleteHistory() {
        binding.textDeleteAll.setOnClickListener(v -> {
            openDiaLogDelete();
        });
    }

    private void deleteAllHistory() {
        KeywordDatabase.getInstance(this).keywordDAO().deleteAll();
        keyWordList.clear();
        getDataFromHistory();
        binding.layoutImage.setVisibility(View.VISIBLE);
        binding.layoutHistory.setVisibility(View.GONE);
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void addKeyWordHistory(String data) {
        if (data.isEmpty()) return;
        deleteKeywordHistory(data);
        KeyWord keyWord = new KeyWord(data);
        KeywordDatabase.getInstance(this).keywordDAO().insertKeyword(keyWord);
    }

//    private void searchJob() {
//        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                boolean handled = false;
//                try {
//                    if (actionId == EditorInfo.IME_ACTION_DONE) {
//                        if (!edtSearch.getText().toString().trim().equals("")) {
//                            Program.hideKeyboardFrom(getActivity(), getActivity().getCurrentFocus());
//                            findJob(edtSearch.getText().toString(), spinnerTypeJob.getSelectedItemPosition(), spinnerLocation.getSelectedItemPosition());
//                        } else {
//                            tvNote.setText("Kết quả tìm kiếm không có");
//                            tvNote.setVisibility(View.VISIBLE);
//                            listView.setVisibility(View.GONE);
//                        }
//                        handled = true;
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                return handled;
//            }
//        });
//
//        btnSubmit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Toast.makeText(getActivity(), String.valueOf(positionSpnLocation), Toast.LENGTH_SHORT).show();
//                try {
//                    bottomSheetDialog.dismiss();
//                    findJob(edtSearch.getText().toString(), spinnerTypeJob.getSelectedItemPosition(), spinnerLocation.getSelectedItemPosition());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }
//
//    private void setAdapterEditText() {
//        String[] data = new String[]{"Nodejs", "Python", "Java", "Reactjs", "C++", "SQL", "C#", "PHP", "C", "Javascript", "Ruby"};
//        ArrayAdapter arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, data);
//        edtSearch.setAdapter(arrayAdapter);
//    }
//
//    private void resetDataSpinner() {
//        btnReset.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                spinnerLocation.setSelection(0);
//                spinnerTypeJob.setSelection(0);
//            }
//        });
//    }
//
//    private void findJob(String key, int positionSpnIdOccupation, int positionSpnLocation) throws JSONException {
//        pbLoading.setVisibility(View.VISIBLE);
//
//        String idOccupation[] = new String[0];
//        String locationWorking[] = new String[0];
//
//        if (positionSpnLocation != 0) {
//            locationWorking = new String[]{spinnerLocation.getSelectedItem().toString()};
//        }
//
//        if ((positionSpnIdOccupation - 1) >= 0) {
//            idOccupation = new String[]{idTypeJobList.get(positionSpnIdOccupation - 1)};
//        }
//
//        RequestQueue queue = Volley.newRequestQueue(getActivity());
//        jobResultList.clear();
//
//        JSONArray idOccupationParam = new JSONArray(idOccupation);
//        JSONArray locationWorkingParam = new JSONArray(locationWorking);
//
//        JSONObject params = new JSONObject();
//        params.put("key", key);
//        params.put("locationWorking", locationWorkingParam);
//        params.put("idOccupation", idOccupationParam);
//
//        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                try {
//                    JSONArray listJobs = response.getJSONArray("data");
//                    String idCompany = "";
//                    if (listJobs.length() == 0) {
//                        tvNote.setText("Kết quả tìm kiếm không có");
//                        tvNote.setVisibility(View.VISIBLE);
//                        listView.setVisibility(View.GONE);
//                        pbLoading.setVisibility(View.GONE);
//                        return;
//                    }
//                    for (int i = 0; i < listJobs.length(); i++) {
//                        JSONObject job = listJobs.getJSONObject(i);
//                        if (!job.isNull("idCompany")) {
//                            idCompany = job.getJSONObject("idCompany").getString("name");
//                        } else {
//                            idCompany = "";
//                        }
//
//                        String time = Program.setTime(job.getString("postingDate"));
//                        if (time.equals(null))
//                            time = "Vừa mới cập nhật";
//                        else
//                            time = "Cập nhật " + time + " trước";
//                        jobResultList.add(new Job(
//                                job.getString("_id"),
//                                job.getString("name"),
//                                idCompany,
//                                job.getString("locationWorking"),
//                                job.getString("salary"),
//                                time
//                        ));
//
//                        System.out.println(jobResultList.get(i).toString());
//                    }
//                    jobAdapter.notifyDataSetChanged();
//                    tvNote.setVisibility(View.GONE);
//                    listView.setVisibility(View.VISIBLE);
//                    pbLoading.setVisibility(View.GONE);
//                    System.out.println("Create Successful!!!");
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                System.out.println(error);
//            }
//        }) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                HashMap<String, String> headers = new HashMap<String, String>();
//                headers.put("Content-Type", "application/json");
//                return headers;
//            }
//        };
//        sr.setRetryPolicy(new RetryPolicy() {
//            @Override
//            public int getCurrentTimeout() {
//                return 50000;
//            }
//
//            @Override
//            public int getCurrentRetryCount() {
//                return 50000;
//            }
//
//            @Override
//            public void retry(VolleyError error) throws VolleyError {
//                System.out.println(error);
//            }
//        });
//        queue.add(sr);
//    }
//
//    private void getTypeJob() {
//
//        String urlTypeJob = "https://job-seeker-smy5.onrender.com/occupation/list";
//        RequestQueue queue = Volley.newRequestQueue(getActivity());
//
//        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.GET, urlTypeJob, null, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                try {
//                    JSONArray listTypeJob = response.getJSONObject("data").getJSONArray("data");
//                    for (int i = 0; i < listTypeJob.length(); i++) {
//                        JSONObject typeJob = listTypeJob.getJSONObject(i);
//                        if (typeJob.getString("isDelete").equals("false")) {
//                            typeJobList.add(typeJob.getString("name"));
//                            idTypeJobList.add(typeJob.getString("_id"));
//                        }
//                    }
//                    bindingDataToSpinner();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                System.out.println(error);
//            }
//        }) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                HashMap<String, String> headers = new HashMap<String, String>();
//                headers.put("Content-Type", "application/json");
//                return headers;
//            }
//        };
//        sr.setRetryPolicy(new RetryPolicy() {
//            @Override
//            public int getCurrentTimeout() {
//                return 50000;
//            }
//
//            @Override
//            public int getCurrentRetryCount() {
//                return 50000;
//            }
//
//            @Override
//            public void retry(VolleyError error) throws VolleyError {
//                System.out.println(error);
//            }
//        });
//        queue.add(sr);
//    }
//
//    private void bindingDataToSpinner() {
//        if (getActivity() != null) {
//            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_style, locationList);
//            spinnerLocation.setAdapter(adapter);
//
//            adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_style, typeJobList);
//            spinnerTypeJob.setAdapter(adapter);
//        }
//    }
//
//    private void setDataFilter() {
//        bindingDataToSpinner();
//        bottomSheetDialog = new BottomSheetDialog(getActivity());
//        bottomSheetDialog.setContentView(bottomSheetDialogView);
//    }

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
}
