package com.example.clouddrive.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.DownloadProgressListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.clouddrive.CommonClass;
import com.example.clouddrive.Globals;
import com.example.clouddrive.R;
import com.example.clouddrive.UserPref;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<HomeModel> objects;

    private Context context;
    UserPref userPref;
    private LayoutInflater layoutInflater;
    String[] email_token;
    CommonClass commonClass = new CommonClass();

    public HomeAdapter(Context context, List<HomeModel> objects) {
        this.context = context;
        this.objects = objects;
        userPref = new UserPref(context);
        email_token = userPref.getAuthentication();
        this.layoutInflater = LayoutInflater.from(context);
    }


    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull final RecyclerView.ViewHolder holder, final int position) {
        ((ViewHolder) holder).fileTitle.setText(objects.get(position).getFileName());
        ((ViewHolder) holder).fileCreated.setText(objects.get(position).getFileCreated());
        if (objects.get(position).getType() == HomeModel.FILE_TYPE.FOLDER) {
            ((ViewHolder) holder).fileSize.setVisibility(View.GONE);
            ((ViewHolder) holder).textView7.setVisibility(View.GONE);
        } else {
            ((ViewHolder) holder).fileSize.setVisibility(View.VISIBLE);
            ((ViewHolder) holder).fileSize.setText(objects.get(position).getFileSize());
            ((ViewHolder) holder).textView7.setVisibility(View.VISIBLE);
        }
        switch (objects.get(position).getType()) {
            case TEXT:
                ((ViewHolder) holder).fileIcon.setBackgroundResource(R.drawable.ic_notepad_svgrepo_com);
                break;
            case FOLDER:
                ((ViewHolder) holder).fileIcon.setBackgroundResource(R.drawable.ic_icons8_folder);
                break;
            case VIDEO:
                ((ViewHolder) holder).fileIcon.setBackgroundResource(R.drawable.ic_iconmonstr_video_8);
                break;
            case IMAGE:
                ((ViewHolder) holder).fileIcon.setBackgroundResource(R.drawable.ic_image_svgrepo_com);
                break;
            case AUDIO:
                ((ViewHolder) holder).fileIcon.setBackgroundResource(R.drawable.ic_audio_svgrepo_com);
                break;
            case ARCH:
                ((ViewHolder) holder).fileIcon.setBackgroundResource(R.drawable.ic_zip_file);
                break;
            default:
                ((ViewHolder) holder).fileIcon.setBackgroundResource(R.drawable.ic_unknown);
        }

        if (objects.get(position).getType() != HomeModel.FILE_TYPE.FOLDER) {
            ((ViewHolder) holder).goNext.setVisibility(View.GONE);
        } else {
            ((ViewHolder) holder).goNext.setVisibility(View.VISIBLE);
        }
        // MORE MENU BUTTON
        final HomeModel objj = objects.get(position);
        ((ViewHolder) holder).moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu(view, objj);
            }
        });
        if (objects.get(position).getType() != HomeModel.FILE_TYPE.FOLDER) {
            String n = objects.get(position).getFileName() + objects.get((position)).type_ext.toLowerCase();
            String l = Globals.EXPLORER_API + objects.get(position).explorer;
            ((ViewHolder) holder).customFileCard.setOnClickListener(view ->
                    downloadFile(n, l)
            );
        } else {
            Bundle arguments = new Bundle();
            arguments.putString("link", objects.get(position).explorer);
            arguments.putString("title", objects.get(position).title);
            ((ViewHolder) holder).customFileCard.setOnClickListener(view -> Navigation.findNavController(view).navigate(
                    R.id.nav_home, arguments));
        }
    }

    private void menu(final View view, final HomeModel obj) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater()
                .inflate(R.menu.popup_menu, popup.getMenu());
        popup.getMenu().getItem(0).setVisible(obj.getType() != HomeModel.FILE_TYPE.FOLDER);

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_delete) {
                    deleteFile(obj);
                } else if (item.getItemId() == R.id.action_download) {
                    downloadFile(obj.getFileName() + obj.type_ext.toLowerCase(),
                            Globals.EXPLORER_API + obj.explorer);
                }
                return true;
            }
        });
        popup.show(); //showing popup menu
    }

    private void deleteFile(final HomeModel obj) {
        commonClass.showProgress(context, "", "deleting directory...");
        Log.d("HomeAdapter", "delete file: " + Globals.DELETE_API + obj.explorer);
        AndroidNetworking.get(Globals.DELETE_API + obj.explorer)
                .addHeaders("email", email_token[0])
                .addHeaders("authorization", email_token[1])
                .addHeaders(email_token[2].equals("1") ? "test" : "isadminfiles", "1")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        commonClass.cancelProgress();
                        Log.d("HomeAdapter", "delete file res: " + response.toString());
                        if (processResponse(response)) {
                            int pos = objects.indexOf(obj);
                            objects.remove(obj);
                            notifyItemRemoved(pos);
                            Toast.makeText(context, obj.getFileName() + " deleted successfully", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        commonClass.cancelProgress();
                        Log.d("HomeAdapter", "ANERROR: " + anError.getErrorCode() + ", " + anError.getErrorBody());
                        commonClass.showDialog("Delete error", commonClass.onError(anError),
                                context);
                    }
                });
    }

    private boolean processResponse(JSONObject response) {
        try {
            if (response.getString("message").equalsIgnoreCase("success")) {
                return true;
            }
            commonClass.showDialog("File not deleted", response.getString("message"), context);
        } catch (JSONException e) {
            Log.d("HomeAdapter", "JSONEXCEP: " + e.getMessage());
            commonClass.showDialog("File not deleted", "Unknown error occur while parsing data", context);
            return false;
        }
        return false;
    }

    private void downloadFile(String title, String url) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                commonClass.showDialog("Permission denied", "Please allow the permission in settings to work app properly.", context);
                return;
            }
        }


        commonClass.showProgress(context, title, "Downloading file...");


        String external = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        Log.d("HomeAdapter", "Download task: " + external
                + "; title: " + title + "; url: " + url);
        // new changes
        File f;
        int i = 1;
        String title_ = title;
        String ext = "";
        if (title.lastIndexOf(".") != -1)
            ext = title.substring(title.lastIndexOf(".") + 1);
        do {
            f = new File(external + "/" + title_);
            if (f.exists()) {
                title_ = new HomeModel().removeExtension(title) + " (" + i + ")";
                title_ += "." + ext;
                Log.d("HomeAdapter", "new file: " + title_ + " exsts" + f.exists());
            }
            i++;
        } while (f.exists());
        Log.d("HomeAdapter", "Download task: " + external
                + "; title: " + title + "; url: " + url);
//        if (f.exists()) {
//            Log.d("HomeAdapter", "exists");
//            f.delete();
//        }
        final String newTitle = title_;

        AndroidNetworking.download(url,
                external,
                newTitle) // new changes
                .setTag("downloadTest")
                .addHeaders("email", email_token[0])
                .addHeaders("authorization", email_token[1])
                .addHeaders(email_token[2].equals("1") ? "test" : "isadminfiles", "1")
                .setPriority(Priority.HIGH)
                .build()
                .setDownloadProgressListener(new DownloadProgressListener() {
                    @Override
                    public void onProgress(long bytesDownloaded, long totalBytes) {
                        float progress = (bytesDownloaded * 100f) / totalBytes;
                        Log.d("HomeAdapter", "prgoress: " + progress);
                        commonClass.setTextProgress("Downloading file (" + (int) progress + "%)...");
                    }
                })
                .startDownload(new DownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        commonClass.cancelProgress();
                        // changed title
                        commonClass.showDialog(newTitle, "File has been successfuly saved to path \"" + external + "\"", context);
                    }

                    @Override
                    public void onError(ANError error) {
                        commonClass.cancelProgress();
                        Log.d("HomeAdapter", "ANERROR: " + error.getErrorCode() + ", " + error.getErrorBody() + "," +
                                error.getMessage() + ";" + error.getErrorDetail() + "," + error.getResponse());
                        commonClass.showDialog("", commonClass.onError(error), context);
                    }
                });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    private void initializeViews(HomeModel object, ViewHolder holder) {
        //TODO implement
    }


    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout customFileCard;
        private ImageView fileIcon, goNext;
        private TextView fileTitle;
        private ImageButton moreBtn;
        private TextView fileCreated;
        private TextView textView7;
        private TextView fileSize;

        public ViewHolder(@NonNull View view) {
            super(view);
            customFileCard = (ConstraintLayout) view.findViewById(R.id.custom_file_card);
            fileIcon = (ImageView) view.findViewById(R.id.file_icon);
            goNext = (ImageView) view.findViewById(R.id.imageView2);
            fileTitle = (TextView) view.findViewById(R.id.file_title);
            moreBtn = (ImageButton) view.findViewById(R.id.more_btn);
            fileCreated = (TextView) view.findViewById(R.id.file_created);
            textView7 = (TextView) view.findViewById(R.id.textView7);
            fileSize = (TextView) view.findViewById(R.id.file_size);
        }
    }
}
