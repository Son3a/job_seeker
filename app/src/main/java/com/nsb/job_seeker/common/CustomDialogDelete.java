package com.nsb.job_seeker.common;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.nsb.job_seeker.R;

public abstract class CustomDialogDelete {
    private Context context;
    private String accept, cancel, msg;
    private Integer resourceImage;

    public CustomDialogDelete(Context context, String msg, String accept, String cancel) {
        this.context = context;
        this.accept = accept;
        this.cancel = cancel;
        this.msg = msg;
    }
    public CustomDialogDelete(Context context, String msg, String accept, String cancel, Integer resourceImage) {
        this.context = context;
        this.accept = accept;
        this.cancel = cancel;
        this.resourceImage = resourceImage;
        this.msg = msg;
    }

    public abstract void doAccept();

    public abstract void doCancel();

    public void openDiaLogDelete() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_layout_answer);

        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams winLayoutParams = window.getAttributes();
        winLayoutParams.gravity = Gravity.CENTER;
        window.setAttributes(winLayoutParams);

        dialog.setCancelable(false);

        MaterialButton btnDelete = dialog.findViewById(R.id.btnDelete);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);
        TextView textContent = dialog.findViewById(R.id.textContent);
        ImageView imageView = dialog.findViewById(R.id.image);
        if (resourceImage == null) {
            imageView.setVisibility(View.GONE);
        } else {
            imageView.setImageResource(resourceImage);
        }
        textContent.setText(msg);
        btnDelete.setText(accept);
        btnCancel.setText(cancel);

        btnDelete.setOnClickListener(v -> {
            doAccept();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> {
            doCancel();
            dialog.dismiss();
        });

        dialog.show();
    }
}