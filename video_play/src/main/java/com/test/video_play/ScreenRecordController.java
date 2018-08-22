package com.test.video_play;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.test.video_play.control.VideoPlayController;
import com.test.video_play.server.tcp.TcpServer;
import com.test.video_play.server.tcp.interf.OnAcceptBuffListener;
import com.test.video_play.server.tcp.interf.OnServerStateChangeListener;

/**
 * Created by xu.wang
 * Date on  2018/08/22 14:47:47.
 *
 * @Desc 录屏控制类
 */

public class ScreenRecordController {
    public static Handler mHandler;
    public static Context mContext;
    public static TcpServer tcpServer;
    public static int port = 11111;
    public static OnServerStateChangeListener mServerStateChangeListener;
    private OnAcceptBuffListener acceptBuffListener;
    private VideoPlayController mVideoPlayController;


    private static ScreenRecordController mController;

    private ScreenRecordController() {
    }

    public static ScreenRecordController getInstance() {
        synchronized (ScreenRecordController.class) {
            if (mController == null) {
                mController = new ScreenRecordController();
            }
        }
        return mController;
    }


    public ScreenRecordController init(Application application) {
        mHandler = new Handler(application.getMainLooper());
        mContext = application;
        return mController;
    }

    //开启server
    public ScreenRecordController startServer() {
        if (tcpServer == null) {
            tcpServer = new TcpServer();
        }
        tcpServer.startServer();
        if (acceptBuffListener != null) tcpServer.setOnAccepttBuffListener(acceptBuffListener);
        return mController;
    }

    public ScreenRecordController setPort(int port) {
        this.port = port;
        return mController;
    }

    public ScreenRecordController stopServer() {
        mVideoPlayController = null;
        acceptBuffListener = null;
        if (tcpServer != null) tcpServer.stopServer();
        tcpServer = null;
        return mController;
    }

    public void setOnAcceptTcpStateChangeListener(OnServerStateChangeListener listener) {
        this.mServerStateChangeListener = listener;
    }

    public ScreenRecordController setVideoPlayController(VideoPlayController videoPlayController) {
        mVideoPlayController = videoPlayController;
        acceptBuffListener = mVideoPlayController.getAcceptBuffListener();
        if (tcpServer != null) tcpServer.setOnAccepttBuffListener(acceptBuffListener);
        return mController;
    }
}
