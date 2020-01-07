package com.suyong.contractmanager.utils;


import android.app.ProgressDialog;
import android.content.Context;

public class ProgressDialogUtil {

    public static ProgressDialog newInstatnce(Context context, String title, String message){
        ProgressDialog progressdialog = new ProgressDialog(context);
        progressdialog.setTitle(title);
        progressdialog .setMessage(message);
        progressdialog.setCancelable(false);
        return progressdialog;
    }

}
