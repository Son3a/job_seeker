package com.nsb.job_seeker.common;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.nsb.job_seeker.R;

public class LoadingDialog {
    Context context;
    Dialog dialog;

    public LoadingDialog(Context context) {
        this.context = context;
    }

    public void showDialog() {
        if (context != null) {
            dialog = new Dialog(context);
            dialog.setContentView(R.layout.layout_loading);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setCancelable(false);
            dialog.create();
            dialog.show();
        }
    }

    public void hideDialog() {
        if (dialog != null && context != null) {
            dialog.dismiss();
        }
    }
}
