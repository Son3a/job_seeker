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
    private String accept, cancel;
    private boolean isShowImage;

    public CustomDialogDelete(Context context, String accept, String cancel, boolean isShowImage){
        this.context = context;
        this.accept = accept;
        this.cancel = cancel;
        this.isShowImage = isShowImage;
    }

    public abstract void doSomeThing();

    public void openDiaLogDelete(String contentDelete) {
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
        if(isShowImage == false){
            imageView.setVisibility(View.GONE);
        }
        textContent.setText(contentDelete);
        btnDelete.setText(accept);
        btnCancel.setText(cancel);

        btnDelete.setOnClickListener(v -> {
            doSomeThing();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }
}