package com.nsb.job_seeker.seeder;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.gson.Gson;
import com.nsb.job_seeker.Program;
import com.nsb.job_seeker.R;

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
    private Spinner spinnerLocation, spinnerSalary, spinnerTimeUpdated;
    private Button btnSubmit, btnReset;
    private View searchView, bottomSheetDialogView;
    private BottomSheetDialog bottomSheetDialog;
    private AutoCompleteTextView edtSearch;
    private ListView listView;
    private List<Job> jobResultList;
    private ProgressBar pbLoading;
    private TextView tvNoteHistory;

    private String url = "https://job-seeker-smy5.onrender.com/job/list/search";

    private ArrayList<String> locationList = new ArrayList<>(Arrays.asList("Tất cả", "Hồ Chí Minh", "Hà Nội", "Đà Nẵng"));
    private ArrayList<String> salaryList = new ArrayList<>(Arrays.asList("Tất cả", "3.000.000 - 5.000.000", "5.000.000 - 7.0000.000", "7.000.000 - 12.000.000", "12.000.000 - 15.000.000", "15.000.000 - 20.000.000", "Trên 20.000.000"));
    private ArrayList<String> timeUpdated = new ArrayList<>(Arrays.asList("Mới nhất", "Cũ nhất"));

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
        spinnerSalary = bottomSheetDialogView.findViewById(R.id.spinner_salary);
        spinnerTimeUpdated = bottomSheetDialogView.findViewById(R.id.spinner_time_updated);
        btnSubmit = bottomSheetDialogView.findViewById(R.id.btn_submit);
        btnReset = bottomSheetDialogView.findViewById(R.id.btn_reset);
        edtSearch = searchView.findViewById(R.id.edt_search);
        imgFilter = (ImageView) searchView.findViewById(R.id.img_filter);
        tvNoteHistory = searchView.findViewById(R.id.tv_note_search);

        listView = searchView.findViewById(R.id.lv_job_search);
        pbLoading = searchView.findViewById(R.id.idLoadingPB);
    }

    private void setEvent() {
        setDataFilter();
        imgFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.show();
            }
        });

        resetDataSpinner();

        applyDataFilter();

        setAdapterEditText();

        searchJob();

    }

    private void searchJob() {
        String idOccupation[] = {};
        String idCompany[] = {};
        String locationWorking[] = {};
        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!edtSearch.getText().toString().trim().equals("")) {
                        Toast.makeText(getActivity(), edtSearch.getText().toString(), Toast.LENGTH_SHORT).show();
                    }
                    handled = true;
                }
                return handled;
            }
        });
//        edtSearch.setOnKeyListener(new View.OnKeyListener() {
//
//
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
//                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
//                    findJob(edtSearch.getText().toString(), idOccupation, idCompany, locationWorking);
////                    if(edtSearch.getText().toString().trim() != ""){
////                        Toast.makeText(getActivity(), edtSearch.getText().toString(), Toast.LENGTH_SHORT).show();
////                    }
//
//                    return true;
//                }
//                return false;
//            }
//        });
    }

    private void setAdapterEditText() {
//        ArrayList<String> dataList = new ArrayList<>();
//        dataList.add("Nodejs");
//        dataList.add("Python");
//        dataList.add("Java");
//        dataList.add("Reactjs");
//        dataList.add("C++");
//        dataList.add("SQL");
        String[] data = new String[]{"Nodejs", "Python", "Java", "Reactjs", "C++", "SQL"};
        ArrayAdapter arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, data);
        edtSearch.setAdapter(arrayAdapter);
//        edtSearch.setInputType(0);
    }

    private void applyDataFilter() {
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String[] salarys = spinnerSalary.getSelectedItem().toString().split("-");
//                for (int i = 0; i < salarys.length; i++) {
//                    int temp = Integer.parseInt(salarys[i].trim().replace(".", ""));
//                    System.out.println(temp);
//                }
            }
        });
    }

    private void resetDataSpinner() {
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerLocation.setSelection(0);
                spinnerTimeUpdated.setSelection(0);
                spinnerSalary.setSelection(0);
            }
        });
    }

    private void filter(String text) {

    }


    private void findJob(String key, String[] idOccupation, String[] idCompany, String[] locationWorking) {
        pbLoading.setVisibility(View.VISIBLE);
        jobResultList = new ArrayList<Job>();
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        // String jsonBody = "{" +
//                        "\"key\":\"" + key + "\"," +
//                        "\"idOccupation\":\"" + idOccupation + "\"," +
//                        "\"idCompany\":\"" + idCompany + "\"," +
//                        "\"locationWorking\":\"" + locationWorking + "\"}";
        //String[] idOccupation = {""};
//        Gson gson = new Gson();
//        String inputString = "{\"id\":2550,\"cityName\":\"Langkawi\",\"hotelName\":\"favehotel Cenang Beach - Langkawi\",\"hotelId\":\"H1266070\"}";
//        List<String> myList = gson.fromJson(inputString, ArrayList.class);
////        String gt4g = "gt4g";
//        String json = gson.toJson({idOccupation:);
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put(,);

        StringRequest sr = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray listJobs = jsonObject.getJSONArray("data");
                    String idCompany = "";
                    for (int i = 0; i < listJobs.length(); i++) {
                        JSONObject job = listJobs.getJSONObject(i);
                        if (!job.isNull("idCompany")) {
                            idCompany = job.getJSONObject("idCompany").getString("name");
                        } else {
                            idCompany = "";
                        }
                        jobResultList.add(new Job(
                                job.getString("_id"),
                                job.getString("name"),
                                idCompany,
                                job.getString("locationWorking"),
                                job.getString("salary"),
                                "Cập nhật 37 phút trước"
                        ));

                        System.out.println(jobResultList.get(i).toString());
                    }
                    setListView();
                    tvNoteHistory.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    pbLoading.setVisibility(View.GONE);
                    System.out.println("Create Successful!!!");

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
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
//                String jsonBody = "{" +
//                        "\"key\":\"" + key + "\"," +
//                        "\"idOccupation\":\"" + idOccupation + "\"," +
//                        "\"idCompany\":\"" + idCompany + "\"," +
//                        "\"locationWorking\":\"" + locationWorking + "\"}";

                String jsonBody = "{" +
                        "\"key\":\"" + key + "\"}";

                if ((getMethod() == Method.POST) && (jsonBody != null)) {
                    return jsonBody.getBytes();
                } else {
                    return super.getBody();
                }
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
                startActivity(i);


            }
        });
    }

    private void bindingDataToSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_style, locationList);
        spinnerLocation.setAdapter(adapter);

        adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_style, salaryList);
        spinnerSalary.setAdapter(adapter);

        adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_style, timeUpdated);
        spinnerTimeUpdated.setAdapter(adapter);
    }

    private void setDataFilter() {
        bindingDataToSpinner();
        bottomSheetDialog = new BottomSheetDialog(getActivity());
        bottomSheetDialog.setContentView(bottomSheetDialogView);
    }
}
