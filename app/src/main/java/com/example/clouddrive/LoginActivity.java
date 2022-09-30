package com.example.clouddrive;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class LoginActivity extends AppCompatActivity {

    Button signIn, create;
    EditText email, password;
    Toast toast;
    CommonClass commonClass;
    ScrollView scrollView;
    UserPref userPref;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_login_);

        userPref = new UserPref(LoginActivity.this);

        AndroidNetworking.initialize(getApplicationContext());
        // Adding an Network Interceptor for Debugging purpose :
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .build();
        AndroidNetworking.initialize(getApplicationContext(), okHttpClient);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Login " + getString(R.string.app_name));

        commonClass = new CommonClass();

        toast = Toast.makeText(LoginActivity.this, "", Toast.LENGTH_SHORT);

        scrollView = findViewById(R.id.scroller_login);
        email = findViewById(R.id.email_textbox);
        password = findViewById(R.id.password_textbox);
        create = signIn = findViewById(R.id.create);
        signIn = findViewById(R.id.signin_button);


        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleEmailPassword();
            }
        });


        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Create();
            }
        });

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (scrollView != null) {
                    if (getSupportActionBar() != null)
                        if (scrollView.getScrollY() > 1) {
                            getSupportActionBar().setElevation(10);
                        } else {
                            getSupportActionBar().setElevation(1);
                        }
                }
            }
        });

        findViewById(R.id.forgot_button).setOnClickListener(v -> resetPassword());

        password.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                handleEmailPassword();
            }
            return false;
        });

        hideKeyboard();
    }

    public void resetPassword() {
        startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
        overridePendingTransition(R.anim.right_to_left, R.anim.blank_anim);
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void Create() {
        startActivity(new Intent(LoginActivity.this, RegisterAccountActivity.class));
        overridePendingTransition(R.anim.right_to_left, R.anim.blank_anim);
    }

    public void showToast(String t) {
        toast.setText(t);
        toast.show();
    }

    private void authError() {
        commonClass.cancelProgress();
        commonClass.showDialog(null,
                "We can't authenticate you at this time, try again later." +
                        "\nIf problem persists please contact app administrator.",
                LoginActivity.this);
    }

    private void handleEmailPassword() {
        if (commonClass.validateInput(email, password)) {
            sendLogin(email.getText().toString(), password.getText().toString());
        }
    }


    public void sendLogin(final String email_, final String password_) {
        commonClass.showProgress(LoginActivity.this, "", "Signing in...");
        MLog.log("Auth Token:: " + email_);
        AndroidNetworking.post(Globals.SIGN_IN_API)
                .addUrlEncodeFormBodyParameter("email", email_)
                .addUrlEncodeFormBodyParameter("password", password_)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        MLog.log(response.toString());
                        // do anything with response
                        commonClass.cancelProgress();
                        applyLogin(email_, response);
                    }

                    @Override
                    public void onError(ANError error) {
                        commonClass.cancelProgress();
                        try {
                            // handle error
                            if (error.getErrorCode() != 0) {
                                JSONObject jsonObject = new JSONObject(error.getErrorBody());
                                commonClass.showDialog(
                                        CommonClass.find(error.getErrorCode() + ""),
                                        jsonObject.getString("message"),
                                        LoginActivity.this);
                                return;
                            }
                        } catch (JSONException e) {
                            authError();
                            return;
                        }
                        MLog.log("LoginError : " + error.getErrorBody() + ", " + error.getMessage() + ", " + error.getErrorCode() + ", " + error.getErrorDetail());
                        commonClass.showConnectionError(LoginActivity.this);
                    }
                });
    }

    public void applyLogin(String email, JSONObject response) {
        try {
            MLog.log("response Total: " + response.toString());
            userPref.addAuthentication(
                    email,
                    response.getString("token"),
                    response.getString("regdate"),
                    response.getString("name"),
                    response.getBoolean("is_admin")
            );
            doneLogin(response);
        } catch (JSONException e) {
            e.printStackTrace();
            authError();
        }
    }


    public void doneLogin(JSONObject response) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("json", response.toString());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}