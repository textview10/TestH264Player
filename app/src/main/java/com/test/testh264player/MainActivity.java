package com.test.testh264player;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.test.video_play.ScreenRecordController;
import com.test.video_play.control.VideoPlayController;
import com.test.video_play.server.tcp.interf.OnServerStateChangeListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private SurfaceView mSurface = null;
    private SurfaceHolder mSurfaceHolder;

    private VideoPlayController mController;
    private TextView tv_speednet;
    private RelativeLayout rl_detail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mSurface = findViewById(R.id.surfaceview);
        tv_speednet = findViewById(R.id.tv_main_speednet);
        rl_detail = findViewById(R.id.rl_content_detail);
        mController = new VideoPlayController();

        ScreenRecordController.getInstance()
                .init(getApplication())
                .setPort(11111)     //设置端口号
                .startServer()  //初始化,并开启server
                .setVideoPlayController(mController)    //设置VideoController
                .setOnAcceptTcpStateChangeListener(mStateChangeListener);   //设置回调

        mSurfaceHolder = mSurface.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mController.surfaceCreate(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.e(TAG, "surface destory");
                mController.surfaceDestrory();
            }
        });
    }

    //客户端Tcp连接状态的回调...
    OnServerStateChangeListener mStateChangeListener = new OnServerStateChangeListener() {

        @Override
        public void acceptH264TcpConnect() {
            Log.e(TAG, "accept a tcp connect...");
            rl_detail.setVisibility(View.GONE);
        }

        @Override
        public void acceptH264TcpDisConnect(Exception e) {
            Log.e(TAG, "acceptTcpConnect exception = " + e.toString());
            rl_detail.setVisibility(View.VISIBLE);
        }

        @Override
        public void exception() {

        }

        @Override
        public void acceptH264TcpNetSpeed(String netSpeed) {
            super.acceptH264TcpNetSpeed(netSpeed);
            tv_speednet.setText(netSpeed);
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mController != null) mController.stop();
        ScreenRecordController.getInstance().stopServer();
    }

}
