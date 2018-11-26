package com.dl.videofloating;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

/**
 * @author zwl
 * @date on 2018/11/26
 */
public class VideoCallTimeUtils {
    private Runnable updateTimeRunnable;
    private Handler handler;
    private long time = 0;

    private static VideoCallTimeUtils instance;

    public static VideoCallTimeUtils getInstance() {
        if (null == instance) {
            synchronized (VideoCallTimeUtils.class) {
                if (null == instance) {
                    instance = new VideoCallTimeUtils();
                }
            }
        }
        return instance;
    }

    public VideoCallTimeUtils() {
        handler = new Handler();
    }

    /**
     * 开始启动时间
     */
    public void startTime() {
        startTime(null);
    }

    public void startTime(TextView textView) {
        try {
            if (updateTimeRunnable != null) {
                handler.removeCallbacks(updateTimeRunnable);
            }
            if (textView != null) {
                textView.setVisibility(View.VISIBLE);
            }
            updateTimeRunnable = new UpdateTimeRunnable(textView);
            handler.post(updateTimeRunnable);
            if (listener != null) {
                listener.onVideoCallStart();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止计时时间
     */
    public void stopTime() {
        if (updateTimeRunnable != null) {
            handler.removeCallbacks(updateTimeRunnable);
        }
        if (listener != null) {
            listener.onVideoCallEnd();
        }
    }


    private class UpdateTimeRunnable implements Runnable {
        private TextView timeView;

        public UpdateTimeRunnable(TextView timeView) {
            this.timeView = timeView;
        }

        @Override
        public void run() {
            time++;
            if (time >= 3600) {
                if (timeView != null)
                    timeView.setText(String.format("%d:%02d:%02d", time / 3600, (time % 3600) / 60, (time % 60)));
            } else {
                if (timeView != null)
                    timeView.setText(String.format("%02d:%02d", (time % 3600) / 60, (time % 60)));
            }
            if (listener != null) {
                listener.onVideoCalling(time * 1000);
            }
            handler.postDelayed(this, 1000);
        }
    }

    OnVideoCallTimeListener listener;

    public void setOnVideoCallTimeListener(OnVideoCallTimeListener listener) {
        this.listener = listener;
    }

    public interface OnVideoCallTimeListener {
        void onVideoCallStart();

        void onVideoCalling(long time);

        void onVideoCallEnd();
    }
}
