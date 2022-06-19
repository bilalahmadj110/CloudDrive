package com.example.clouddrive.ui.change_password;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.clouddrive.CommonClass;
import com.example.clouddrive.Globals;
import com.example.clouddrive.MLog;
import com.example.clouddrive.R;
import com.example.clouddrive.UserPref;

import org.json.JSONException;
import org.json.JSONObject;

public class ChangeFragment extends Fragment {

    EditText oldPassword, newPassword, confirmPassword;
    Toast toast;
    CommonClass commonClass = new CommonClass();
    Button change;
    String email = null, token = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_change_password, container, false);

        oldPassword = root.findViewById(R.id.oldPassword);
        newPassword = root.findViewById(R.id.newPassword);
        confirmPassword = root.findViewById(R.id.confirmPassword);
        change = root.findViewById(R.id.change);

        UserPref userPref = new UserPref(getContext());
        email = userPref.getAuthentication()[0];
        token = userPref.getAuthentication()[1];

        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (oldPassword.getText().toString().isEmpty()) {
                    oldPassword.setError("Enter old password");
                    return;
                } else if (newPassword.getText().toString().isEmpty()) {
                    newPassword.setError("Enter new password");
                    return;
                } else if (confirmPassword.getText().toString().isEmpty()) {
                    confirmPassword.setError("Enter confirm password");
                    return;
                } else if (oldPassword.getText().length() < 6) {
                    oldPassword.setError("Password must be at least 6 characters");
                    return;
                } else if (newPassword.getText().length() < 6) {
                    newPassword.setError("Password must be at least 6 characters");
                    return;
                } else if (!newPassword.getText().toString().equals(confirmPassword.getText().toString())) {
                    confirmPassword.setError("Password does not match");
                    return;
                }

                changePasswordRequest(oldPassword.getText().toString(), newPassword.getText().toString());


            }
        });
        toast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    public void changePasswordRequest(final String oldPassword, final String newPassword) {
        commonClass.showProgress(getContext(), "", "Requesting to change password...");
        MLog.log("Email:: " + email + "; Token:: " + token);
        AndroidNetworking.get(Globals.CHANGE_PASSWORD_API)
                .addHeaders("email", email)
                .addHeaders("new", newPassword)
                .addHeaders("password", oldPassword)
                .addHeaders("Authorization", token)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        MLog.log(response.toString());
                        try {
                            commonClass.cancelProgress();
                            String msg = response.getString("message");
                            if (msg.equalsIgnoreCase("success"))
                                showToast("Password changed successfully");
                            else
                                showToast("Password could not be changed.\nError: " + msg);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            error("Something bad happened on our side. Please try again later.\nError: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        MLog.log(error.getErrorBody());
                        commonClass.cancelProgress();
                        try {
                            // handle error
                            if (error.getErrorCode() != 0) {
                                JSONObject jsonObject = new JSONObject(error.getErrorBody());
                                commonClass.showDialog(
                                        CommonClass.find(error.getErrorCode() + ""),
                                        jsonObject.getString("message"),
                                        getContext());
                                return;
                            }
                        } catch (JSONException e) {
                            error("Something bad happened on our side. Please try again later.\nError: " + e.getMessage());
                            return;
                        }
                        MLog.log("LoginError : " + error.getErrorBody() + ", " + error.getMessage() + ", " + error.getErrorCode() + ", " + error.getErrorDetail());
                        commonClass.showConnectionError(getContext());
                    }
                });
    }

    private void error(String text) {
        commonClass.cancelProgress();
        commonClass.showDialog(null,
                text,
                getContext());
    }

    public void showToast(String t) {
        toast.setText(t);
        toast.show();
    }
}