package com.nsb.job_seeker.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.widget.Toast;

import com.nsb.job_seeker.common.CustomToast;

public class InternetService extends BroadcastReceiver {
    private boolean isConnected;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            if (isNetWorkAvailable(context)) {
                if(isConnected == true) {
                    CustomToast.makeText(context, "Hệ thống đã kết nối!", CustomToast.LENGTH_SHORT, CustomToast.SUCCESS).show();
                }
                isConnected = false;
//                ((Activity) context).startActivity(((Activity) context).getIntent());
            } else {
                CustomToast.makeText(context, "Hệ thống mất kết nối!", CustomToast.LENGTH_SHORT, CustomToast.WARNING).show();
                isConnected = true;
            }
        }
    }

    private boolean isNetWorkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return false;
            }

            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            return networkCapabilities != null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }
}
