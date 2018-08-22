package com.test.video_play.utils;

import android.util.Log;

import com.test.video_play.entity.ReceiveData;
import com.test.video_play.entity.ReceiveHeader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by xu.wang
 * Date on  2018/08/22 14:47:47.
 *
 * @Desc 根据协议解析数据
 */

public class AnalyticDataUtils {
    private OnAnalyticDataListener mListener;
    private volatile int readLength = 0;
    private Timer timer;
    private boolean isCalculate = false;

    /**
     * 分析头部数据
     */
    public ReceiveHeader analysisHeader(byte[] header) {
        //实现数组之间的复制
        //bytes：源数组
        //srcPos：源数组要复制的起始位置
        //dest：目的数组
        //destPos：目的数组放置的起始位置
        //length：复制的长度
        byte[] buff = new byte[4];
        System.arraycopy(header, 1, buff, 0, 4);
        final int mainCmd = ByteUtil.bytesToInt(buff);       //主指令  1`5
        buff = new byte[4];
        System.arraycopy(header, 5, buff, 0, 4);
        final int subCmd = ByteUtil.bytesToInt(buff);    //子指令  5`9
        buff = new byte[4];
        System.arraycopy(header, 9, buff, 0, 4);
        int stringBodyLength = ByteUtil.bytesToInt(buff);//文本数据 9 ~ 13;
        buff = new byte[4];
        System.arraycopy(header, 013, buff, 0, 4);
        int byteBodySize = ByteUtil.bytesToInt(buff);//byte数据 13^17
        return new ReceiveHeader(mainCmd, subCmd, header[0], stringBodyLength, byteBodySize);
    }


    /**
     * 解析数据
     * @param is
     * @param receiveHeader
     * @return
     * @throws IOException
     */
    public ReceiveData analyticData(InputStream is, ReceiveHeader receiveHeader) throws IOException {
        byte[] sendBody = null;
        byte[] buff = null;
        //文本长度
        if (receiveHeader.getStringBodylength() != 0) {
            sendBody = readByte(is, receiveHeader.getStringBodylength());
        }
        //音视频长度
        if (receiveHeader.getBuffSize() != 0) {
            buff = readByte(is, receiveHeader.getBuffSize());
        }
        ReceiveData data = new ReceiveData();
        data.setHeader(receiveHeader);
//        data.setSendBody(sendBody == null ? "" : new String(sendBody));
        data.setBuff(buff);
        return data;
    }

    /**
     * 保证从流里读到指定长度数据
     *
     * @param is
     * @param readSize
     * @return
     * @throws Exception
     */
    public byte[] readByte(InputStream is, int readSize) throws IOException {
        byte[] buff = new byte[readSize];
        int len = 0;
        int eachLen = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (len < readSize) {
            eachLen = is.read(buff);
            if (eachLen != -1) {
                if (isCalculate) readLength += eachLen;
                len += eachLen;
                baos.write(buff, 0, eachLen);
            } else {
                baos.close();
                throw new IOException();
            }
            if (len < readSize) {
                buff = new byte[readSize - len];
            }
        }
        byte[] b = baos.toByteArray();
        baos.close();
        return b;
    }


    public interface OnAnalyticDataListener {
//        void onSuccess(ReceiveData data);

        void netSpeed(String msg);

    }

    public void setOnAnalyticDataListener(OnAnalyticDataListener listener) {
        this.mListener = listener;
    }

    public void startNetSpeedCalculate() {
        stop();
        readLength = 0;
        isCalculate = true;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.netSpeed((readLength / 1024) + " kb/s");
                    readLength = 0;
                }
            }
        }, 1000, 1000);
    }

    public void stop() {
        isCalculate = false;
        try {
            if (timer != null) timer.cancel();
        } catch (Exception e) {
        }
    }
}
