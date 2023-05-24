package com.nsb.job_seeker.common;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AsyncTasks {
    private final ExecutorService executors;

    public AsyncTasks() {
        this.executors = Executors.newSingleThreadExecutor();
    }

    private void startBackground() {
        onPreExecute();
        executors.execute(new Runnable() {
            @Override
            public void run() {
                doInBackground();
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onPostExecute();
                    }
                },500);
            }
        });
    }

    public void execute() {
        startBackground();
    }

    public void shutdown() {
        executors.shutdown();
    }


    public boolean isShutdown() {
        return executors.isShutdown();
    }

    public abstract void onPreExecute();

    public abstract void doInBackground();

    public abstract void onPostExecute();
}