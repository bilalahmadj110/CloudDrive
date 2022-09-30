package com.example.clouddrive;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

public class ResetPasswordActivity extends AppCompatActivity {
    public static String TAG = "ResetPasswordActivity";
    EditText email;
    Button btn;
    CommonClass commonClass = new CommonClass();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        email = findViewById(R.id.email_textbox_register);
        btn = findViewById(R.id.reset_button);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (commonClass.validateInput(email, null))
                    create();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.blank_anim, R.anim.left_to_right);
    }

    public void create() {
        commonClass.showProgress(ResetPasswordActivity.this, null, "Creating an account for you, please wait...");

        Log.d(TAG, "SignUp: " + Globals.RESET_API + ";" +
                "email:" + email.getText().toString());
        AndroidNetworking.post(Globals.RESET_API)
                .addUrlEncodeFormBodyParameter("email", email.getText().toString())
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "resp: " + response.toString());
                        commonClass.cancelProgress();
                        // do anything with response
                        try {
                            String msg = response.getString("message");
                            commonClass.showDialog("", msg, ResetPasswordActivity.this);
                            return;
                        } catch (JSONException e) {

                        }
  
                        commonClass.showDialog(
                                "",
                                "Something bad happened on our end, please try again later.",
                                ResetPasswordActivity.this);

                    }

                    @Override
                    public void onError(ANError error) {
                        Log.d(TAG, "Error: " + error.getErrorBody() + ";" + error.getErrorDetail() + ";" + error.getErrorCode());
                        commonClass.cancelProgress();
                        try {
                            // handle error
                            if (error.getErrorCode() != 0) {
                                JSONObject jsonObject = new JSONObject(error.getErrorBody());
                                commonClass.showDialog(
                                        CommonClass.find(error.getErrorCode() + ""),
                                        jsonObject.getString("message"),
                                        ResetPasswordActivity.this);
                                return;
                            }
                        } catch (JSONException e) {
                            commonClass.showDialog(
                                    "",
                                    "Something bad happened on our end, please try again later.",
                                    ResetPasswordActivity.this);
                            return;
                        }
                        MLog.log("LoginError : " + error.getErrorBody() + ", " + error.getMessage() + ", " + error.getErrorCode() + ", " + error.getErrorDetail());
                        commonClass.showConnectionError(ResetPasswordActivity.this);
                    }
                });
    }
}