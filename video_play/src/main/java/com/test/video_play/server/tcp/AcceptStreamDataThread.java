package com.test.video_play.server.tcp;

import android.os.SystemClock;
import android.util.Log;

import com.test.video_play.ScreenImageApi;
import com.test.video_play.entity.Frame;
import com.test.video_play.entity.ReceiveData;
import com.test.video_play.entity.ReceiveHeader;
import com.test.video_play.server.tcp.interf.OnAcceptBuffListener;
import com.test.video_play.utils.AnalyticDataUtils;
import com.test.video_play.utils.DecodeUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by xu.wang
 * Date on  2018/08/22 14:47:47.
 *
 * @Desc 接收消息, 执行操作线程
 */

public class AcceptStreamDataThread extends Thread implements AnalyticDataUtils.OnAnalyticDataListener {
    private InputStream InputStream;
    private OutputStream outputStream;
    private Socket socket;
    private volatile boolean startFlag = true;
    private OnAcceptBuffListener listener;
    private OnTcpChangeListener mTcpListener;

    private DecodeUtils mDecoderUtils;
    private AnalyticDataUtils mAnalyticDataUtils;

    //当前投屏线程
    private String TAG = "AcceptStreamDataThread";

    public AcceptStreamDataThread(Socket socket, OnAcceptBuffListener
            listener, OnTcpChangeListener tcpListener) {
        this.socket = socket;
        try {
            this.InputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();
        } catch (Exception e) {

        }
        this.listener = listener;
        this.mTcpListener = tcpListener;
        mDecoderUtils = new DecodeUtils();
        mAnalyticDataUtils = new AnalyticDataUtils();
        mAnalyticDataUtils.setOnAnalyticDataListener(this);
        mDecoderUtils.setOnVideoListener(new DecodeUtils.OnVideoListener() {
            @Override
            public void onSpsPps(byte[] sps, byte[] pps) {
                Frame spsPpsFrame = new Frame();
                spsPpsFrame.setType(Frame.SPSPPS);
                spsPpsFrame.setSps(sps);
                spsPpsFrame.setPps(pps);
                Log.d("AcceptH264MsgThread", "sps pps ...");
                AcceptStreamDataThread.this.listener.acceptBuff(spsPpsFrame);
            }

            @Override
            public void onVideo(byte[] video, int type) {
                Frame frame = new Frame();
                switch (type) {
                    case Frame.KEY_FRAME:
                        frame.setType(Frame.KEY_FRAME);
                        frame.setBytes(video);
                        AcceptStreamDataThread.this.listener.acceptBuff(frame);
                        break;
                    case Frame.NORMAL_FRAME:
                        frame.setType(Frame.NORMAL_FRAME);
                        frame.setBytes(video);
                        AcceptStreamDataThread.this.listener.acceptBuff(frame);
                        break;
                    case Frame.AUDIO_FRAME:
                        frame.setType(Frame.AUDIO_FRAME);
                        frame.setBytes(video);
                        AcceptStreamDataThread.this.listener.acceptBuff(frame);
                        break;
                    default:
                        Log.e("AcceptH264MsgThread", "other video...");
                        break;
                }

            }
        });
    }

    public void sendStartMessage() {
        //告诉客户端,可以开始投屏了
        try {
            outputStream.write("ok".getBytes());
        } catch (IOException e) {
            if (mTcpListener != null) {
                mTcpListener.disconnect(e);
            }
        }
    }

    @Override
    public void run() {
        super.run();
        if (mTcpListener != null) {
            mTcpListener.connect();
        }
        sendStartMessage();
        mAnalyticDataUtils.startNetSpeedCalculate();
        readMessage();
        mAnalyticDataUtils.stop();
    }

    //读取数据
    private void readMessage() {
        try {
            while (startFlag) {
                //开始接收客户端发过来的数据
                byte[] header = mAnalyticDataUtils.readByte(InputStream, 18);
                //数据如果为空，则休眠，防止cpu空转,  0.0 不可能会出现的,会一直阻塞在之前
                if (header == null || header.length == 0) {
                    SystemClock.sleep(1);
                    continue;
                }
                // 根据协议分析数据头
                ReceiveHeader receiveHeader = mAnalyticDataUtils.analysisHeader(header);
                if (receiveHeader.getStringBodylength() == 0 && receiveHeader.getBuffSize() == 0) {
                    SystemClock.sleep(1);
                    continue;
                }
                if (receiveHeader.getEncodeVersion() != ScreenImageApi.encodeVersion1) {
                    Log.e(TAG, "接收到的数据格式不对...");
                    continue;
                }
                ReceiveData receiveData = mAnalyticDataUtils.analyticData(InputStream, receiveHeader);
                if (receiveData == null || receiveData.getBuff() == null) {
                    continue;
                }
                //区分音视频
                mDecoderUtils.isCategory(receiveData.getBuff());
            }
        } catch (Exception e) {
            if (mTcpListener != null) {
                Log.e(TAG, "readMessage Exception = " + e.toString());
                mTcpListener.disconnect(e);
            }
        } finally {
            startFlag = false;
            try {
                socket.close();
            } catch (IOException e) {
                mTcpListener.disconnect(e);
            }
        }
    }

    public void shutdown() {
        startFlag = false;
        if (mAnalyticDataUtils != null) mAnalyticDataUtils.stop();
        this.interrupt();
    }

    @Override
    public void netSpeed(String msg) {
        if (mTcpListener != null ) mTcpListener.netSpeed(msg);
    }

    public interface OnTcpChangeListener {
        void disconnect(Exception e);

        void connect();

        void netSpeed(String netSpeed);
    }
}
