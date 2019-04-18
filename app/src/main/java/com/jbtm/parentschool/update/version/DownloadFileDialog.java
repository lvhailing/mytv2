package com.jbtm.parentschool.update.version;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jbtm.parentschool.R;
import com.jbtm.parentschool.utils.ToastUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadFileDialog extends Dialog {
    private String mDownloadUrl;
    private TextView tvProgress;
    private ProgressBar progressDownload;
    private Context context;
    private int fileLength;
    private DownloadTask mTask;
    private int currentLength = 0;
    private int currentProgress;

    public DownloadFileDialog(Context context, String downloadUrl) {
        super(context);
        this.context = context;
        this.mDownloadUrl = downloadUrl;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("文件下载");

        setContentView(R.layout.dialog_download_apk);

        setCancelable(false);

        initView();
    }

    private void initView() {
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        int[] deviceWH = DownloadUtil.getDeviceWH(context);
        lp.width = (int) (deviceWH[0] * 0.65);  // 宽度
        dialogWindow.setAttributes(lp);

        tvProgress = (TextView) findViewById(R.id.tvProgress);
        progressDownload = (ProgressBar) findViewById(R.id.progressDownload);

        mTask = new DownloadTask();
        mTask.execute();
    }

    private class DownloadTask extends AsyncTask<Void, Integer, File> {
        @Override
        protected File doInBackground(Void... arg0) {
            if (TextUtils.isEmpty(mDownloadUrl)) {
                return null;
            }
            File file = null;
            String fileName = mDownloadUrl.substring(mDownloadUrl.lastIndexOf("/") + 1);
            String pathName = DownloadUtil.getSecondDir(getContext(), "apk") + File.separator + fileName;
            try {
                URL url = new URL(mDownloadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10 * 1000);
                connection.connect();
                if (connection.getResponseCode() == 200) {
                    fileLength = connection.getContentLength();
                    if (fileLength <= 0) {
                        return null;
                    }
                    progressDownload.setMax(fileLength);
                    file = new File(pathName);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    InputStream inputStream = connection.getInputStream();
                    ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[2048];
                    while (true) {
                        int len = inputStream.read(buffer);
                        currentLength += len;
                        currentProgress = (currentLength / fileLength) * 100;   // 当前长度/文件长度
                        publishProgress(len);
                        if (len == -1) {
                            break;
                        }
                        arrayOutputStream.write(buffer, 0, len);
                    }
                    arrayOutputStream.close();
                    inputStream.close();
                    byte[] data = arrayOutputStream.toByteArray();
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(data);
                    fileOutputStream.close();
                }
            } catch (Exception e) {
                Log.i("tag", "download error: " + e.getMessage());
                ToastUtil.showCustom(e.getMessage());
                e.printStackTrace();
            }
            return file;
        }

        @Override
        protected void onPostExecute(File file) {
            if (file == null || !file.exists()) {
                Toast.makeText(getContext(), "文件下载失败！", Toast.LENGTH_LONG).show();
                dismiss();
                return;
            }
            installApk(file);
            dismiss();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            tvProgress.setText(getContext().getString(R.string.download_progress,
                    (double) currentLength / 1024, (double) fileLength / 1024, currentProgress));

            progressDownload.setProgress(currentLength);
        }
    }

    protected void installApk(File file) {
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        getContext().startActivity(intent);
    }
}
