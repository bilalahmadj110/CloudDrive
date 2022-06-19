package com.example.clouddrive.ui.home;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static android.os.Build.VERSION.SDK_INT;
import static androidx.core.content.ContextCompat.checkSelfPermission;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.transition.TransitionInflater;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.example.clouddrive.CommonClass;
import com.example.clouddrive.Globals;
import com.example.clouddrive.PathUtil;
import com.example.clouddrive.R;
import com.example.clouddrive.UserPref;
import com.github.clans.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final int FILE_SELECT_CODE = 1000;
    private static final int REQUEST_CODE_FOR_READING_EXTERNAL_STORAGE = 2000;
    private static final String PERMISSION_READING_STORAGE = READ_EXTERNAL_STORAGE;
    private static final String PERMISSION_WRITING_STORAGE = WRITE_EXTERNAL_STORAGE;

    private SwipeRefreshLayout refreshlayout;
    private LinearLayout info, load;
    HomeAdapter homeAdapter;
    CommonClass commonClass;

    FloatingActionButton upload, create;

    String explorer, title;


    UserPref userPref;
    private final List<HomeModel> objects = new ArrayList<>();
    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.slide_right));
        setExitTransition(inflater.inflateTransition(R.transition.fade));
        if (getArguments() != null) {
            Bundle bundle = this.getArguments();
            explorer = bundle.getString("link", null);
            if (explorer == null) {
                explorer = "";
            }
            title = bundle.getString("title", "Home");
            Log.d("HomeFragment", "link " + explorer);
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d("HomeFragment", "onCreatedView: " + view + ", " + explorer + "," + title);
        if (view != null) {
            return view;
        }
        view = inflater.inflate(R.layout.fragment_home, container, false);
        userPref = new UserPref(getContext());
        commonClass = new CommonClass(getContext());

        Log.d("HomeFragment", "onCreatedView " + view);

        refreshlayout = view.findViewById(R.id.refreshlayout);
        RecyclerView recyclerview = view.findViewById(R.id.recyclerview);
        load = view.findViewById(R.id.load_load_layout);
        info = view.findViewById(R.id.load_info_layout);
        upload = view.findViewById(R.id.fab_upload);
        create = view.findViewById(R.id.fab_create);

        homeAdapter = new HomeAdapter(getActivity(), objects);
        recyclerview.setAdapter(homeAdapter);


        create.setOnClickListener(view -> showFileChooser());
        upload.setOnClickListener(view -> showInput());
        refreshlayout.setOnRefreshListener(() -> {

            if (refreshlayout.isRefreshing())
                refreshlayout.setRefreshing(false);
            objects.clear();
            start();
        });

        info.getChildAt(1).setOnClickListener(view1 -> {
            objects.clear();
            start();
        });
        start();

        return view;
    }


    private synchronized void download() {
        Log.d("HomeFragment", "dowload");
        download(Globals.EXPLORER_API + explorer);
    }

    private synchronized void download(String link) {
        Log.d("HomeFragment", "dowload-" + link);
        downloadNetworking(link);
    }

    private void showInput() {

        LayoutInflater factory = LayoutInflater.from(getContext());
        final View customView = factory.inflate(R.layout.input_folder, null);
        Log.d("HomeFragment", "showing input");
        final EditText txtUrl = customView.findViewById(R.id.editTextTextPersonName);
        // Set the default text to a link of the Queen

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle("Create a directory")
                .setView(customView)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d("HomeFragment", "cancel");
                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = txtUrl.getText().toString().trim();
                if (CommonClass.validateFolderName(url)) {
                    Log.d("HomeFragment", "sending mkdir");
                    mkdir(url);
                    dialog.dismiss();
                    return;
                }
                txtUrl.setError("Invalid directory name");
            }
        });
    }

    private boolean processResponse(JSONObject response, boolean a) {
        try {
            if (response.getString("message").equalsIgnoreCase("success")) {
                return true;
            }
            commonClass.showDialog("File not deleted", response.getString("message"), getContext());
        } catch (JSONException e) {
            Log.d("HomeAdapter", "JSONEXCEP: " + e.getMessage());
            commonClass.showDialog("File not deleted", "Unknown error occur while parsing data", getContext());
            return false;
        }
        return false;
    }

    private void downloadNetworking(String link) {
        showLoading();
        String[] email_token = userPref.getAuthentication();
        Log.d("HomeFragment", "email,token: " + email_token[0] + "," + email_token[1]);
        AndroidNetworking.get(link)
                .addHeaders("email", email_token[0])
                .addHeaders("authorization", email_token[1])
                .addHeaders("isadminfiles", "1")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        processResponse(response);
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d("HomeFragment", "ANERROR: " + anError.getErrorCode() + ", " + anError.getErrorBody());
                        showInfo(commonClass.onError(anError));
                    }
                });
    }

    private void processResponse(JSONObject response) {
        try {
            if (!response.getString("message").equalsIgnoreCase("success")) {
                throw new JSONException("");
            }
            JSONArray array = response.getJSONArray("node");
            Log.d("HomeFragment", "resp: " + response.toString());
            if (array.length() == 0) {
                showInfo("This folder is empty");
                return;
            }
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                objects.add(new HomeModel(obj));
            }
            initView();
            homeAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            Log.d("HomeFragment", "JSONEXCEP: " + e.getMessage());
            showInfo("Unknown error occur while parsing data");
        }
    }

    private void initView() {
        Log.d("HomeFragment", "init view");
        if (refreshlayout.getVisibility() != View.VISIBLE)
            refreshlayout.setVisibility(View.VISIBLE);
        if (load.getVisibility() == View.VISIBLE)
            load.setVisibility(View.GONE);
        if (info.getVisibility() == View.VISIBLE)
            info.setVisibility(View.GONE);
    }

    private void showLoading() {
        Log.d("HomeFragment", "show loading");
        if (refreshlayout.getVisibility() == View.VISIBLE)
            refreshlayout.setVisibility(View.GONE);
        if (load.getVisibility() != View.VISIBLE)
            load.setVisibility(View.VISIBLE);
        if (info.getVisibility() == View.VISIBLE)
            info.setVisibility(View.GONE);
    }

    private void showInfo(String inf) {

        Log.d("HomeFragment", "info text:" + inf);
        if (refreshlayout.getVisibility() == View.VISIBLE)
            refreshlayout.setVisibility(View.GONE);
        if (load.getVisibility() == View.VISIBLE)
            load.setVisibility(View.GONE);
        if (info.getVisibility() != View.VISIBLE)
            info.setVisibility(View.VISIBLE);
        ((TextView) info.getChildAt(0)).setText(inf);
    }


    public void start() {
        if (isPermissionGivenFor(WRITE_EXTERNAL_STORAGE)) {
            if (isPermissionGivenFor(READ_EXTERNAL_STORAGE)) {
                download();
            } else {
                showInfo("Permission required");
                requestPermissionFor(new String[]{PERMISSION_READING_STORAGE}, REQUEST_CODE_FOR_READING_EXTERNAL_STORAGE);
            }
        } else {
            showInfo("Permission required");
            requestPermissionFor(new String[]{PERMISSION_WRITING_STORAGE, PERMISSION_READING_STORAGE}, REQUEST_CODE_FOR_READING_EXTERNAL_STORAGE);
        }
    }

    public boolean isPermissionGivenFor(String permission) {
        return checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermissionFor(String[] permission, int requestCode) {
        if (SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(permission, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("PERMISSION...", requestCode + " " + grantResults.length + " " + grantResults);
        if (requestCode == REQUEST_CODE_FOR_READING_EXTERNAL_STORAGE) {
            Log.d("PERMISSION...", "Reach here");
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                download();
                Log.d("PERMISSION...", "Reach here!!!");
            } else if (SDK_INT >= Build.VERSION_CODES.M)
                if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
                    retryAlert(REQUEST_CODE_FOR_READING_EXTERNAL_STORAGE);
                    Log.d("PERMISSION...", "Reach here........");
                } else
                    settingAlert();
            Log.d("PERMISSION...", "Reach here~~~");
        }
    }


    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(intent, FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(), "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("HomeFragment", "CODE ;;;;" + requestCode + ";" + data);
        if (requestCode == FILE_SELECT_CODE) {
            if (resultCode == RESULT_OK && null != data) {
                Uri content_describer = data.getData();
                try {
                    PathUtil pathUtil = new PathUtil();
                    String path = pathUtil.getPath(getContext(), content_describer);
                    Log.d("HomeFragment", "path to picker: " + path);
                    uploadFile(path, getFileSize(content_describer));

                } catch (Exception e) {
                    e.printStackTrace();
                    commonClass.showDialog("", "Unable to retrieve path, please try again.", getContext());
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private long getFileSize(Uri fileUri) {
        Cursor returnCursor = getContext().getContentResolver().
                query(fileUri, null, null, null, null);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();

        return returnCursor.getLong(sizeIndex);
    }

    private void mkdir(String name) {
        Log.d("HomeFragment", "create file: " + (Globals.CREATE_API + (explorer == null ? "" : explorer)) + ":" + name);
        String[] email_token = userPref.getAuthentication();
        commonClass.showProgress(getContext(), "", "creating directory");
        AndroidNetworking.get(Globals.CREATE_API + (explorer == null ? "" : explorer))
                .addHeaders("email", email_token[0])
                .addHeaders("authorization", email_token[1])
                .addHeaders("name", name)
                .addHeaders("isadminfiles", "1")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        commonClass.cancelProgress();
                        Log.d("HomeFragment", "create file res: " + response.toString());
                        if (processResponse(response, false)) {
                            try {
                                // new changes - now HomeModel would handle whole JSON response, since I've made some changes on mkdir section backend
                                HomeModel dirModel = new HomeModel(response);
//                                dirModel.type_ext = "DIR";
//                                dirModel.explorer = "/" + name.replace(" ", "%20");
//                                Log.d("HomeFragment", "Encode r" + dirModel.explorer);
//                                dirModel.title = "/" + name;
                                objects.add(0, dirModel);
                                initView();
                                homeAdapter.notifyItemInserted(0);

                                Toast.makeText(getContext(), "\"" + dirModel.getFileName() + "\" created successfully", Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                                commonClass.showDialog("Create error", e.getMessage(),
                                        getContext());
                            }
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d("HomeAdapter", "ANERROR: " + anError.getErrorCode() + ", " + anError.getErrorBody());
                        commonClass.showDialog("Create error", commonClass.onError(anError),
                                getContext());
                        commonClass.cancelProgress();
                    }
                });
    }


    private void uploadFile(String path, long size) {
        File f = new File(path);
        String name = f.getName();
        final String ext;
        int i = name.lastIndexOf('.');
        if (i > 0) {
            ext = name.substring(i + 1);
        } else {
            ext = "";
        }
        commonClass.showProgress(getContext(), "", "Uploading " + path);
        String[] email_token = userPref.getAuthentication();
        Log.d("HomeFragment", "Upload fle to: " + Globals.UPLOAD_API + (explorer == null ? "" : explorer) + " >> " +
                path);
        AndroidNetworking.upload(Globals.UPLOAD_API + (explorer == null ? "" : explorer))
                .addMultipartFile("file", new File(path))
                .addHeaders("email", email_token[0])
                .addHeaders("authorization", email_token[1])
                .addHeaders("isadminfiles", "1")
                .setPriority(Priority.MEDIUM)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        float progress = (bytesUploaded * 100f / totalBytes);
                        Log.d("HomeFragment", (bytesUploaded * 100 / totalBytes) + "");
                        commonClass.setTextProgress("Uploading file (" + (int) progress + "%)...");
                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("HomeFragment", "upload complete " + response);
                        commonClass.cancelProgress();
                        if (processResponse(response, false)) {
                            try {
                                // new changes - since the backend have changed
                                HomeModel dirModel = new HomeModel(response);
                                Log.d("HomeFragment", dirModel.toString());
                                objects.add(0, dirModel);
                                initView();
                                homeAdapter.notifyItemInserted(0);
//                                objects.clear();
//                                start();

                                Toast.makeText(getContext(), "\"" + name + "\" created successfully", Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                commonClass.showDialog("Upload error", e.getMessage(),
                                        getContext());
                            }
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        commonClass.cancelProgress();
                        // handle error
                        commonClass.showDialog("Upload error", commonClass.onError(error),
                                getContext());
                        Log.d("HomeFragment", error.getErrorBody() + "," + error.getErrorCode() + "," + error.getErrorDetail());
                        Log.d("HomeFragment", "errorrrr");
                    }
                });
    }

    public void settingAlert() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle("Permission Disabled")
                .setMessage("You've disabled the permission to access the device storage. We need this permission for download and upload purposes.")
                .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create().show();
    }

    public void retryAlert(final int requestCode) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle("Allow Permission")
                .setMessage("Please allow the storage permission to work this app properly.")
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (!isPermissionGivenFor(requestCode == REQUEST_CODE_FOR_READING_EXTERNAL_STORAGE ?
                                READ_EXTERNAL_STORAGE : Manifest.permission.SEND_SMS))
                            requestPermissionFor(requestCode == REQUEST_CODE_FOR_READING_EXTERNAL_STORAGE ?
                                    new String[]{READ_EXTERNAL_STORAGE} :
                                    new String[]{Manifest.permission.SEND_SMS}, requestCode);
                    }
                })
                .setNegativeButton("Deny", null)
                .create().show();
    }


}