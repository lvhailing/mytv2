package com.jbtm.parentschool.update.version;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;

import com.jbtm.parentschool.activity.PersonalInformationActivity;
import com.jbtm.parentschool.dialog.ExitLoginDialog;
import com.jbtm.parentschool.dialog.UpdateDialog;
import com.jbtm.parentschool.models.VersionModel;

public class UpdateManager {
    private Context context;

    public void checkVersion(Context context, VersionModel version) {
        if (!(context instanceof Activity)) {
            return;
        }
        if (((Activity) context).isFinishing()) {
            return;
        }
        if (version == null || TextUtils.isEmpty(version.download_url)) {
            return;
        }

        this.context = context;

        showUpdateDialog(version);
    }

    private void showUpdateDialog(final VersionModel version) {
        //update_type：升级类型（1非强制 2强制）
        final UpdateDialog dialog = new UpdateDialog(context, version.download_msg, version.update_type != 1);
        dialog.show();
        dialog.setOnMyClickListener(new UpdateDialog.MyClickListener() {
            @Override
            public void sure() {
                dialog.dismiss();
                startDownload(version.download_url);
//                startDownload("https://download.dgstaticresources.net/fusion/android/app-c6-release.apk");
            }

            @Override
            public void cancel() {
                dialog.dismiss();
            }
        });
    }

    private void showDialog(final VersionModel version, boolean isForce) {
        String versionDesc = TextUtils.isEmpty(version.download_msg) ? "有新版本，请马上更新" : version.download_msg;
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("版本更新")
                .setMessage(versionDesc)
                .setCancelable(!isForce)
                .setPositiveButton("更新", new Dialog.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startDownload(version.download_url);
//                        startDownload("https://download.dgstaticresources.net/fusion/android/app-c6-release.apk");
                    }

                });
        if (!isForce) {
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        builder.create().show();
    }

    private void startDownload(String url) {
        new DownloadFileDialog(context, url).show();
    }
}
