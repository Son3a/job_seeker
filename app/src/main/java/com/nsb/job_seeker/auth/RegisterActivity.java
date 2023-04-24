package com.nsb.job_seeker.auth;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nsb.job_seeker.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class RegisterActivity extends AppCompatActivity {
    private EditText edtName, edtEmail, edtUsername, edtPassword, edtPasswordConfirm,edtPhone;
    private Button btnRegister;
    private TextView txtError;
    private RequestQueue mRequestQueue;
    private String base_url = "http://192.168.1.10:8000/auth";
    private RegisterTask registerTask;
    private LoadingDialog loadingDialog;
    private DialogNotification dialogNotification = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_register);
        this.loadingDialog = new LoadingDialog(RegisterActivity.this);

        setControl();
        setEvent();
    }

    private void setControl() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtPasswordConfirm = findViewById(R.id.edtPasswordConfirm);
        edtUsername = findViewById(R.id.edtUsername);
        btnRegister = findViewById(R.id.btnRegister);
        txtError = findViewById(R.id.txtError);
    }

    private void setEvent() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edtEmail.getText().toString().equals("") || edtName.getText().toString().equals("") || edtUsername.getText().toString().equals("") || edtPassword.getText().toString().equals("") || edtPasswordConfirm.getText().toString().equals("")|| edtPhone.getText().toString().equals("")) {
                    dialogNotification.openDialogNotification("Không được để trống bất kỳ ô nào !", RegisterActivity.this);

                } else if (!edtPassword.getText().toString().equals(edtPasswordConfirm.getText().toString())) {
                    dialogNotification.openDialogNotification("Mật khẩu xác thực không khớp", RegisterActivity.this);
                } else {
                    txtError.setVisibility(View.GONE);
                    registerTask = new RegisterTask();
                    registerTask.execute();
                }

            }
        });
    }

    private class RegisterTask extends AsyncTask<Void, Void, Void> {
        private String name;
        private String email;
        private String username;
        private String password;
        private String phone;

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            loadingDialog.dismissDialog();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.name = edtName.getText().toString();
            this.email = edtEmail.getText().toString();
            this.username = edtUsername.getText().toString();
            this.name = edtName.getText().toString();
            this.password = edtPassword.getText().toString();
            this.phone = edtPhone.getText().toString();

            loadingDialog.startLoadingDialog();
        }

        private void registerApi() throws JSONException {
            //RequestQueue initialized
////            1.Tạo request
            mRequestQueue = Volley.newRequestQueue(RegisterActivity.this);
            //post data
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", this.username);
            jsonObject.put("password", this.password);
            jsonObject.put("name", this.name);
            jsonObject.put("phone", this.phone);
            jsonObject.put("email", this.email);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, base_url + "/register", jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JsonObject convertedObject = new Gson().fromJson(response.getString("data"), JsonObject.class);
                        Log.d("ABC", convertedObject.toString());
                        String message = response.getString("message");
                        dialogNotification.openDialogNotification(message.substring( 0, message.length() - 1 ), RegisterActivity.this);

                    } catch (JSONException e) {
                        Toast.makeText(RegisterActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    String body;
                    //get status code here
                    String statusCode = String.valueOf(error.networkResponse.statusCode);
                    if(error.networkResponse.data!=null) {
                        try {
                            body = new String(error.networkResponse.data,"UTF-8");
                            JsonObject convertedObject = new Gson().fromJson(body, JsonObject.class);
                            String message = convertedObject.get("message").toString();

                            dialogNotification.openDialogNotification(message.substring( 1, message.length() - 1 ), RegisterActivity.this);
                            Log.d("ABC", message);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            mRequestQueue.add(jsonObjectRequest);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                registerApi();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }


    }
}