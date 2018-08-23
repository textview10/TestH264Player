package com.test.video_play.server.tcp;

import android.util.Log;

import com.test.video_play.ScreenRecordApi;
import com.test.video_play.ScreenRecordController;
import com.test.video_play.entity.ReceiveHeader;
import com.test.video_play.server.tcp.interf.OnAcceptBuffListener;
import com.test.video_play.utils.AnalyticDataUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by xu.wang
 * Date on  2018/08/22 14:47:47.
 *
 * @Desc 创建监听
 */


public class TcpServer implements AcceptStreamDataThread.OnTcpChangeListener {
    private static final String TAG = "TcpServer";
    private ServerSocket serverSocket;
    private boolean isAccept = true;
    private OnAcceptBuffListener mListener;
    //把线程给添加进来
    private AnalyticDataUtils mAnalyticUtils;
    private AcceptStreamDataThread acceptStreamDataThread;

    public TcpServer() {
        mAnalyticUtils = new AnalyticDataUtils();
    }

    public void startServer() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    serverSocket = new ServerSocket();
                    serverSocket.setReuseAddress(true);
                    InetSocketAddress socketAddress = new InetSocketAddress(ScreenRecordController.port);
                    serverSocket.bind(socketAddress);
                    Log.i(TAG, "ServerSocket start bind port " + ScreenRecordController.port);
                    while (isAccept) {
                        //服务端接收客户端的连接请求
                        try {
                            Socket socket = serverSocket.accept();
                            InputStream inputStream = socket.getInputStream();
                            byte[] temp = mAnalyticUtils.readByte(inputStream, 17);
                            ReceiveHeader receiveHeader = mAnalyticUtils.analysisHeader(temp);
                            if (receiveHeader.getMainCmd() == ScreenRecordApi.RECORD.MAIN_CMD) {//投屏请求
                                if (acceptStreamDataThread != null) {
                                    acceptStreamDataThread.shutdown();
                                    acceptStreamDataThread = null;
                                }
                                acceptStreamDataThread = new AcceptStreamDataThread(socket, mListener, TcpServer.this);
                                acceptStreamDataThread.start();
                            } else {
                                Log.e(TAG, "accept other connect and close socket");
                                socket.close();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "connect has Exception = " + e.toString());
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "exception close." + e.toString());
                } finally {
                    Log.e(TAG, "TcpServer: thread close");
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public void setOnAccepttBuffListener(OnAcceptBuffListener listener) {
        this.mListener = listener;
    }

    public void stopServer() {
        Log.e(TAG, "stopServer: stop server");
        this.mListener = null;
        isAccept = false;
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    if (acceptStreamDataThread != null) {
                        acceptStreamDataThread.shutdown();
                        acceptStreamDataThread = null;
                    }
                    if (serverSocket != null) {
                        serverSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void connect() {
        if (ScreenRecordController.mServerStateChangeListener != null) {
            if (ScreenRecordController.mServerStateChangeListener != null) {
                ScreenRecordController.mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ScreenRecordController.mServerStateChangeListener.acceptH264TcpConnect();
                    }
                });
            }
        }
    }

    @Override
    public void disconnect(final Exception e) {
        if (ScreenRecordController.mServerStateChangeListener != null) {
            ScreenRecordController.mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ScreenRecordController.mServerStateChangeListener.acceptH264TcpDisConnect(e);
                }
            });
        }
    }

    @Override
    public void netSpeed(final String netSpeed) {
        if (ScreenRecordController.mServerStateChangeListener != null) {
            ScreenRecordController.mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ScreenRecordController.mServerStateChangeListener.acceptH264TcpNetSpeed(netSpeed);
                }
            });
        }
    }

}
