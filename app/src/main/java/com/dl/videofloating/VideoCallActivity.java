package com.dl.videofloating;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dl.videofloating.service.FloatingViewService;
import com.dl.videofloating.utils.VideoCallTimeUtils;
import com.google.android.cameraview.CameraView;

import java.util.List;

/**
 * @author zwl
 * @date on 2018/11/26
 */
public class VideoCallActivity extends AppCompatActivity {

    private String TAG = "VideoCallActivity";
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 100;
    private Button mSmallBtn;
    private CameraView mCameraView;
    private long startTime;
    private TextView mVideoCallTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        initView();
    }

    private void initView() {
        mSmallBtn = findViewById(R.id.video_small_btn);
        mVideoCallTime = findViewById(R.id.video_time);
        mCameraView = findViewById(R.id.camera);
        mSmallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFloatingView(VideoCallActivity.this);
            }
        });
        VideoCallTimeUtils.getInstance().startTime(mVideoCallTime);
        VideoCallTimeUtils.getInstance().setOnVideoCallTimeListener(new VideoCallTimeUtils.OnVideoCallTimeListener() {
            @Override
            public void onVideoCallStart() {

            }

            @Override
            public void onVideoCalling(long time) {
                startTime = time;
            }

            @Override
            public void onVideoCallEnd() {

            }
        });
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");

        try {
            if (mCameraView != null) {
                mCameraView.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        try {
            if (mCameraView != null) {
                mCameraView.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");
        showFloatingView(VideoCallActivity.this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "onRestart");
        stopVideoService();//关闭悬浮窗服务
    }

    @Override
    protected void onDestroy() {
        stopVideoService();
        super.onDestroy();
        VideoCallTimeUtils.getInstance().stopTime();
    }

    /**
     * 显示悬浮窗
     *
     * @param context
     */
    private void showFloatingView(Context context) {
        Log.e(TAG, "手机系统版本：" + Build.VERSION.SDK_INT);
        // API22以下直接启动
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            startVideoService();
        } else {
            if (!Settings.canDrawOverlays(VideoCallActivity.this)) {
                showDialog();
            } else {
                startVideoService();
            }
        }
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("请开启悬浮窗权限");
        builder.setPositiveButton("开启", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + VideoCallActivity.this.getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE && resultCode == RESULT_OK) {
            startVideoService();
        }
    }

    /**
     * 打开悬浮窗服务
     */
    public void startVideoService() {
        moveTaskToBack(true);//最小化Activity
        boolean isWork = isServiceWork(this, FloatingViewService.class.getCanonicalName());
        Log.e("12341234", "startVideoService-isWork=" + isWork);
        if (!isWork) {
            Intent intent = new Intent(this, FloatingViewService.class);//开启服务显示悬浮框
            bindService(intent, mVideoServiceConnection, Context.BIND_AUTO_CREATE);
        }

    }

    /**
     * 关闭悬浮窗服务
     */
    public void stopVideoService() {
        boolean isWork = isServiceWork(this, FloatingViewService.class.getCanonicalName());
        Log.e("12341234", "stopVideoService-isWork=" + isWork);
        if (isWork) {
            unbindService(mVideoServiceConnection);//不显示悬浮框
        }
    }


    ServiceConnection mVideoServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 获取服务的操作对象
            FloatingViewService.MyBinder binder = (FloatingViewService.MyBinder) service;
            binder.getService();
            //这里测试 设置通话从10秒开始
            binder.setData(SystemClock.elapsedRealtime() - startTime);
            binder.getService().setCallback(new FloatingViewService.CallBack() {
                @Override
                public void onDataChanged(String data) {
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param mContext
     * @param className 是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public boolean isServiceWork(Context mContext, String className) {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            ActivityManager.RunningServiceInfo serviceInfo = serviceList.get(i);
            ComponentName serviceName = serviceInfo.service;
            if (serviceName.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }
}
