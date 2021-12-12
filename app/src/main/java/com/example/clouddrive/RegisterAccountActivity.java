package com.example.clouddrive;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import org.json.JSONException;
import org.json.JSONObject;

public class RegisterAccountActivity extends AppCompatActivity {
    public static String TAG = "RegisterAccount";
    Button signIn;
    EditText name, email, password, confirm;
    ScrollView scrollView;
    CommonClass commonClass;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_register_account);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Welcome to " + getString(R.string.app_name));


        commonClass = new CommonClass();

        scrollView = findViewById(R.id.scroller_register);
        name = findViewById(R.id.name_textbox_register);
        confirm = findViewById(R.id.confirm_password_textbox_register);
        email = findViewById(R.id.email_textbox_register);
        password = findViewById(R.id.password_textbox_register);
        signIn = findViewById(R.id.signin_button_register);
        ((TextView) findViewById(R.id.caution)).setText(getString(R.string.cautions));

        signIn.setOnClickListener(v -> SignUp());


        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (scrollView != null) {
                    if (getSupportActionBar() != null)
                        if (scrollView.getScrollY() > 1) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                getSupportActionBar().setElevation(10);
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                getSupportActionBar().setElevation(1);
                        }
                }
            }
        });

        confirm.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    SignUp();
                }
                return false;
            }
        });

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        hideKeyboard();
    }

    public void SignUp() {
        if (
                commonClass.validateEditText(name, "name") &&
                        commonClass.validateInput(email, password) &&
                        isMatch()
        ) {
            create();
        }
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public boolean isMatch() {
        if (!password.getText().toString().equals(confirm.getText().toString())) {
            confirm.setError("Password not match");
            return false;
        } else
            return true;
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
        commonClass.showProgress(RegisterAccountActivity.this, null, "Creating an account for you, please wait...");

        Log.d(TAG, "SignUp: " + Globals.SIGN_UP_API + ";" +
                "email:" + email.getText().toString() + ";" +
                "name:" + name.getText().toString() + ";" +
                "pass:" + password.getText().toString());
        AndroidNetworking.post(Globals.SIGN_UP_API)
                .addUrlEncodeFormBodyParameter("email", email.getText().toString())
                .addUrlEncodeFormBodyParameter("name", name.getText().toString())
                .addUrlEncodeFormBodyParameter("password", password.getText().toString())
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
                            commonClass.showDialog("Account Registered",
                                    "Congratulations " + name.getText().toString() + "!\nYour account has been registered successfully.\n\nYou can now login to drive.",
                                    RegisterAccountActivity.this);
                        } catch (JSONException e) {

                        }


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
                                        RegisterAccountActivity.this);
                                return;
                            }
                        } catch (JSONException e) {
                            commonClass.showDialog(
                                    "",
                                    "Something bad happened on our end, please try again later.",
                                    RegisterAccountActivity.this);
                            return;
                        }
                        MLog.log("LoginError : " + error.getErrorBody() + ", " + error.getMessage() + ", " + error.getErrorCode() + ", " + error.getErrorDetail());
                        commonClass.showConnectionError(RegisterAccountActivity.this);
                    }
                });
    }


}
