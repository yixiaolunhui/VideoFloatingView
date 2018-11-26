package com.dl.videofloating.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.Toast;

import com.dl.videofloating.R;
import com.dl.videofloating.VideoCallActivity;
import com.dl.videofloating.floatingview.FloatingViewListener;
import com.dl.videofloating.floatingview.FloatingViewManager;
import com.google.android.cameraview.CameraView;

/**
 * @author zwl
 * @describe 悬浮窗Service
 * @date on 2018/11/26
 */
public class FloatingViewService extends Service implements FloatingViewListener {

    private static final String TAG = "FloatingViewService";

    private FloatingViewManager mFloatingViewManager;

    private Chronometer ch;

    private CameraView cameraView;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
        init();
    }

    private int init() {
        if (this.mFloatingViewManager != null) {
            return START_STICKY;
        }

        Log.e(TAG, "悬浮窗Service已启动");
        View floatView = LayoutInflater.from(this).inflate(R.layout.call_float_view, null, false);
        ch = (Chronometer) floatView.findViewById(R.id.call_time_ch);
        cameraView = (CameraView) floatView.findViewById(R.id.camera);
        cameraView.start();
        ch.start();
        ch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if (callback != null) {
                    callback.onDataChanged(chronometer.getText().toString());
                }
            }
        });
        floatView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplication(), "点击了悬浮窗", Toast.LENGTH_SHORT).show();
                //这样启动activity是解决启动延迟的问题
                Intent intent = new Intent(FloatingViewService.this, VideoCallActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                try {
                    pendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        });

        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);

        this.mFloatingViewManager = new FloatingViewManager(this, this);

        FloatingViewManager.Configs configs = new FloatingViewManager.Configs();
        configs.floatingViewX = dm.widthPixels / 2;
        configs.floatingViewY = dm.heightPixels / 4;
        configs.overMargin = -(int) (8 * dm.density);
        configs.animateInitialMove = false;
        this.mFloatingViewManager.addFloatingView(floatView, configs);

        return START_REDELIVER_INTENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        try{
            if (cameraView != null) {
                cameraView.stop();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        destroyFloatingView();

        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }


    public class MyBinder extends Binder {
        public FloatingViewService getService() {
            return FloatingViewService.this;
        }


        public void setData(long data) {//写一个公共方法，用来对data数据赋值。
            ch.setBase(data);
            Log.e("123123", "setData=" + data);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onFinishFloatingView() {
        stopSelf();
    }

    /**
     * 销毁悬浮窗
     */
    private void destroyFloatingView() {
        if (this.mFloatingViewManager != null) {
            this.mFloatingViewManager.removeAllFloatingView();
            this.mFloatingViewManager = null;
        }
        Log.d(TAG, "悬浮窗已销毁");
    }

    private CallBack callback;

    public void setCallback(CallBack callback) {
        this.callback = callback;
    }

    public static interface CallBack {
        void onDataChanged(String data);
    }
}
