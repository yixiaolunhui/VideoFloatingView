# VideoFloatingView
视频最小化浮窗窗口，仿微信，QQ视频通话浮窗

# 图片展示
<img src="https://github.com/dalong982242260/VideoFloatingView/blob/master/jpg/1.jpg?raw=true" width="375" />
<img src="https://github.com/dalong982242260/VideoFloatingView/blob/master/jpg/2.jpg?raw=true" width="375" />
<img src="https://github.com/dalong982242260/VideoFloatingView/blob/master/jpg/3.jpg?raw=true" width="375" />

# 说明

demo中浮窗基于网络上的开源的修改。
项目中注意几个问题。

# 1、浮窗的显示

demo是根据视频通话界面（这里只是放了一个本地相机展示）与浮窗service进行绑定。让浮窗与视频通话界面周期一致。
当视频通话点击最小化或者按home键来打开浮窗，当打开视频通话界面时关闭浮窗。

# 2、service中打开Activity异常问题。
需要给intent添加FLAG-FLAG_ACTIVITY_NEW_TASK 至于为什么不知道的可以百度下就明白。

# 3、serview中打开Activity慢的问题。
 解决方法：
     Intent intent = new Intent(FloatingViewService.this, VideoCallActivity.class);
     intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
     try {
          pendingIntent.send();
     } catch (PendingIntent.CanceledException e) {
          e.printStackTrace();
     } 
                    

