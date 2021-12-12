package com.example.clouddrive;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    Button reset;
    EditText email;
    CommonClass commonClass;
    ScrollView scrollView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_forgot_password);



        commonClass = new CommonClass();

        email = findViewById(R.id.email_textbox_reset);
        reset = findViewById(R.id.signin_button_reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reset();
            }
        });
        scrollView = findViewById(R.id.scroller_forget);

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (scrollView != null) {
                    if (getSupportActionBar() != null)
                        if (scrollView.getScrollY() > 1) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                getSupportActionBar().setElevation(10);
                            }
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                getSupportActionBar().setElevation(1);
                            }
                        }
                }
            }
        });

        email.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Reset();
                }
                return false;
            }
        });

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        hideKeyboard();
    }

    public void Reset() {
        EditText editText = new EditText(this);
        editText.setText("123456");
        if (commonClass.validateInput(email, editText)) {
            hideKeyboard();
            requestForgotPassword();
        }
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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


    public void requestForgotPassword() {
        commonClass.showProgress(ForgotPassword.this, "", commonClass.getStringRes(this, R.string.forget_loading_reset_password));
        mAuth.sendPasswordResetEmail(email.getText().toString().replace(" ", ""))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            showDialog(-1, R.string.forget_email_sent);
                            commonClass.cancelProgress();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        commonClass.cancelProgress();
                        if (e.getMessage()!= null) {
                            if (e.getMessage().contains("[ 7: ]") || e.getMessage().contains("internal error"))
                                commonClass.showDialog("Connection Error",
                                        e.getMessage(),//"Unable to reach server, please check your internet connection and try login again.",
                                        ForgotPassword.this);
                            else
                                commonClass.showDialog("Reset Error",
                                        e.getMessage(),
                                        ForgotPassword.this);

                        } else
                            commonClass.showDialog("Reset Error",
                                    "Unable to reset your password at this time, please try again later.",
                                    ForgotPassword.this);

                    }
                });
    }


    public void showDialog(int title, int body) {
        commonClass.showDialog(
                title != -1 ? getResources().getString(title) : null,
                getResources().getString(body),
                ForgotPassword.this);
    }


}

