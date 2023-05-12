package com.nsb.job_seeker.seeder;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.employer.EditRecruitmentActivity;
import com.nsb.job_seeker.model.Job;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchFragment extends Fragment {

    private ImageView imgFilter; //test
    private Spinner spinnerLocation, spinnerTypeJob;
    private Button btnSubmit, btnReset;
    private View searchView, bottomSheetDialogView;
    private BottomSheetDialog bottomSheetDialog;
    private AutoCompleteTextView edtSearch;
    private ListView listView;
    private List<Job> jobResultList;
    private ProgressBar pbLoading;
    private TextView tvNote;

    private String url = "https://job-seeker-smy5.onrender.com/job/list/search";

    private ArrayList<String> locationList;
    private ArrayList<String> typeJobList;
    private ArrayList<String> idtypeJobList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        searchView = inflater.inflate(R.layout.fragment_seeker_search, container, false);
        bottomSheetDialogView = inflater.inflate(R.layout.bottom_sheet_filter, container, false);

        setControl();
        setEvent();

        return searchView;
    }

    private void setControl() {
        spinnerLocation = bottomSheetDialogView.findViewById(R.id.spinner_location);
        spinnerTypeJob = bottomSheetDialogView.findViewById(R.id.spinner_type_job);
        btnSubmit = bottomSheetDialogView.findViewById(R.id.btn_submit);
        btnReset = bottomSheetDialogView.findViewById(R.id.btn_reset);
        edtSearch = searchView.findViewById(R.id.edt_search);
        imgFilter = (ImageView) searchView.findViewById(R.id.img_filter);
        tvNote = searchView.findViewById(R.id.tv_note);

        listView = searchView.findViewById(R.id.lv_job_search);
        pbLoading = searchView.findViewById(R.id.idLoadingPB);

        typeJobList = new ArrayList<>();
        locationList = new ArrayList<>(Arrays.asList("Tất cả", "Ho Chi Minh", "Da Nang", "Ha Noi"));
        idtypeJobList = new ArrayList<>();
        typeJobList.add("Tất cả");
    }

    private void setEvent() {
        setDataFilter();
        imgFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.show();
            }
        });

        getTypeJob(); // set value for spinner

        resetDataSpinner(); // reset spinner

        setAdapterEditText(); // set suggested value for form edittext search

        searchJob(); // search job by call api
    }

    private void searchJob() {
        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                try {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        if (!edtSearch.getText().toString().trim().equals("")) {
                            Program.hideKeyboardFrom(getActivity(), getActivity().getCurrentFocus());
                            findJob(edtSearch.getText().toString(), spinnerTypeJob.getSelectedItemPosition(),spinnerLocation.getSelectedItemPosition());
                        } else {
                            tvNote.setText("Kết quả tìm kiếm không có");
                            tvNote.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.GONE);
                        }
                        handled = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return handled;
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getActivity(), String.valueOf(positionSpnLocation), Toast.LENGTH_SHORT).show();
                try {
                    bottomSheetDialog.dismiss();
                    findJob(edtSearch.getText().toString(), spinnerTypeJob.getSelectedItemPosition(),spinnerLocation.getSelectedItemPosition());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setAdapterEditText() {
        String[] data = new String[]{"Nodejs", "Python", "Java", "Reactjs", "C++", "SQL","C#","PHP","C","Javascript","Ruby"};
        ArrayAdapter arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, data);
        edtSearch.setAdapter(arrayAdapter);
    }

    private void resetDataSpinner() {
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerLocation.setSelection(0);
                spinnerTypeJob.setSelection(0);
            }
        });
    }

    private void findJob(String key, int positionSpnIdOccupation, int positionSpnLocation) throws JSONException {
        pbLoading.setVisibility(View.VISIBLE);

        String idOccupation[] = new String[0];
        String locationWorking[] = new String[0];

        if (positionSpnLocation != 0) {
            locationWorking = new String[]{spinnerLocation.getSelectedItem().toString()};
        }

        if ((positionSpnIdOccupation - 1) >= 0) {
            idOccupation = new String[]{idtypeJobList.get(positionSpnIdOccupation - 1)};
        }

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        jobResultList = new ArrayList<Job>();

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
                        tvNote.setText("Kết quả tìm kiếm không có");
                        tvNote.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        pbLoading.setVisibility(View.GONE);
                        return;
                    }
                    for (int i = 0; i < listJobs.length(); i++) {
                        JSONObject job = listJobs.getJSONObject(i);
                        if (!job.isNull("idCompany")) {
                            idCompany = job.getJSONObject("idCompany").getString("name");
                        } else {
                            idCompany = "";
                        }

                        String time = Program.setTime(job.getString("postingDate"));
                        if (time.equals(null))
                            time = "Vừa mới cập nhật";
                        else
                            time = "Cập nhật " + time + " trước";
                        jobResultList.add(new Job(
                                job.getString("_id"),
                                job.getString("name"),
                                idCompany,
                                job.getString("locationWorking"),
                                job.getString("salary"),
                                time
                        ));

                        System.out.println(jobResultList.get(i).toString());
                    }
                    setListView();
                    tvNote.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    pbLoading.setVisibility(View.GONE);
                    System.out.println("Create Successful!!!");

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

    private void setListView() {
        ListViewApdapter listViewApdapter = new ListViewApdapter(getActivity(), R.layout.list_view_item_job, jobResultList, true);
        listView.setAdapter(listViewApdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent i = new Intent(getActivity(), JobDetailActivity.class);
                i.putExtra("id", jobResultList.get(position).getId());
                i.putExtra("isApplied", true);
                startActivity(i);


            }
        });
    }

    private void getTypeJob() {

        String urlTypeJob = "https://job-seeker-smy5.onrender.com/occupation/list";
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.GET, urlTypeJob, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray listTypeJob = response.getJSONObject("data").getJSONArray("data");
                    for (int i = 0; i < listTypeJob.length(); i++) {
                        JSONObject typeJob = listTypeJob.getJSONObject(i);
                        if (typeJob.getString("isDelete").equals("false")) {
                            typeJobList.add(typeJob.getString("name"));
                            idtypeJobList.add(typeJob.getString("_id"));
                        }
                    }
                    bindingDataToSpinner();
                } catch (JSONException e) {
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

    private void bindingDataToSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_style, locationList);
        spinnerLocation.setAdapter(adapter);

        adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_style, typeJobList);
        spinnerTypeJob.setAdapter(adapter);
    }

    private void setDataFilter() {
        bindingDataToSpinner();
        bottomSheetDialog = new BottomSheetDialog(getActivity());
        bottomSheetDialog.setContentView(bottomSheetDialogView);
    }
}
