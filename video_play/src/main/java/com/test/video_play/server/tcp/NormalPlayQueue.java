package com.test.video_play.server.tcp;

import android.util.Log;

import com.test.video_play.entity.Frame;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by xu.wang
 * Date on  2018/08/22 14:47:47.
 *
 * @Desc 缓存区
 */

public class NormalPlayQueue {
    private ArrayBlockingQueue<Frame> mPlayQueue;
    private String TAG = "NormalPlayQueue";
    private static final int SCAN_MAX_TIME = 5;     //仲裁次数,每循环SCAN_MAX_TIME 次,每次sleep(DEFAULT_SLEEP_TIME),会执行一次检查网速的代码
    private static final int DEFAULT_SLEEP_TIME = 200;  //
    private static final int DEFAULT_NEGATIVE_COUNT = 3;    //循环SCAN_MAX_TIME 次,有 DEFAULT_NEGATIVE_COUNT 次输入queue的帧小于取走的帧

    private static final int NORMAL_FRAME_BUFFER_SIZE = 800; //缓存区大小
    private int mFullQueueCount = NORMAL_FRAME_BUFFER_SIZE;
    private AtomicInteger mTotalFrameCount = new AtomicInteger(0);  //总个数
    private AtomicInteger mGiveUpFrameCount = new AtomicInteger(0);  //总个数
    private AtomicInteger mKeyFrameCount = new AtomicInteger(0);  //队列里Key帧的总个数...

    private AtomicInteger mInFrameCount = new AtomicInteger(0);  //进入总个数
    private AtomicInteger mOutFrameCount = new AtomicInteger(0);  //输出总个数

    private ScanThread mScanThread;
    private volatile boolean mScanFlag;
    private boolean isDebug = false;

    public NormalPlayQueue() {
        mScanFlag = true;
        mScanThread = new ScanThread();
        mScanThread.start();
        mPlayQueue = new ArrayBlockingQueue<Frame>(NORMAL_FRAME_BUFFER_SIZE, true);
    }


    public Frame takeByte() {
        try {
            Frame frame = mPlayQueue.take();
            showLog("playqueue count = " + mPlayQueue.size());
            if (frame.getType() == Frame.KEY_FRAME) mKeyFrameCount.getAndDecrement();
            mOutFrameCount.getAndIncrement();
            mTotalFrameCount.getAndDecrement();
            return frame;
        } catch (InterruptedException e) {
            showLog("take bytes exception" + e.toString());
            return null;
        }
    }

    public void putByte(Frame frame) {
        if (frame.getType() == Frame.KEY_FRAME) mKeyFrameCount.getAndIncrement();
        abandonData();
        try {
            mPlayQueue.put(frame);
            mInFrameCount.getAndIncrement();
            mTotalFrameCount.getAndIncrement();
        } catch (InterruptedException e) {
            showLog("put bytes exception" + e.toString());
        }
    }

    public void stop() {
        mScanFlag = false;
        mTotalFrameCount.set(0);
        mGiveUpFrameCount.set(0);
        if (mPlayQueue != null) {
            mPlayQueue.clear();
        }
    }

    private void abandonData() {
        if (mTotalFrameCount.get() >= (mFullQueueCount / 3)) {
            showLog("队列里的帧数太多,开始丢帧..");
            //从队列头部开始搜索，删除最早发现的连续P帧
            boolean pFrameDelete = false;
            boolean start = false;
            for (Frame frame : mPlayQueue) {
                if (!start) showLog("丢掉了下一个KEY_FRAME前的所有INTER_FRAME..");
                if (frame.getType() == Frame.NORMAL_FRAME) {
                    start = true;
                }
                if (start) {
                    if (frame.getType() == Frame.NORMAL_FRAME) {
                        mPlayQueue.remove(frame);
                        mTotalFrameCount.getAndDecrement();
                        mGiveUpFrameCount.getAndIncrement();
                        pFrameDelete = true;
                    } else if (frame.getType() == Frame.KEY_FRAME) {
                        if (mKeyFrameCount.get() > 5) {
                            Log.d(TAG, "丢掉了一个关键帧.. total" + mKeyFrameCount.get());
                            mPlayQueue.remove(frame);
                            mKeyFrameCount.getAndDecrement();
                            continue;
                        }
                        break;
                    }
                }
            }
            boolean kFrameDelete = false;
            //从队列头部开始搜索，删除最早发现的I帧
            if (!pFrameDelete) {
                for (Frame frame : mPlayQueue) {
                    if (frame.getType() == Frame.KEY_FRAME) {
                        mPlayQueue.remove(frame);
                        Log.d(TAG, "丢掉了一个关键帧..");
                        mTotalFrameCount.getAndDecrement();
                        mGiveUpFrameCount.getAndIncrement();
                        mKeyFrameCount.getAndDecrement();
                        kFrameDelete = true;
                        break;
                    }
                }
            }
            //从队列头部开始搜索，删除音频
            if (!pFrameDelete && !kFrameDelete) {
                for (Frame frame : mPlayQueue) {
                    if (frame.getType() == Frame.AUDIO_FRAME) {
                        mPlayQueue.remove(frame);
                        mTotalFrameCount.getAndDecrement();
                        mGiveUpFrameCount.getAndIncrement();
                        break;
                    }
                }
            }
        }
    }

    private class ScanThread extends Thread {

        private int mCurrentScanTime = 0;
        private ArrayList<ScanSnapShot> mScanSnapShotList = new ArrayList<>();

        @Override
        public void run() {
            while (mScanFlag) {
                //达到仲裁次数了
                if (mCurrentScanTime == SCAN_MAX_TIME) {
                    int averageDif = 0;
                    int negativeCounter = 0;
                    String strLog = "";
                    for (int i = 0; i < SCAN_MAX_TIME; i++) {
                        int dif = mScanSnapShotList.get(i).outCount - mScanSnapShotList.get(i).inCount;
                        if (dif < 0) {
                            negativeCounter++;
                        }
                        averageDif += dif;
                        strLog = strLog + String.format("n%d:%d  ", i, dif);
                    }
                    Log.d(TAG, strLog);
                    if (negativeCounter >= DEFAULT_NEGATIVE_COUNT || averageDif < -100) {
                        //坏
                        showLog("Bad Send Speed.");

                    } else {
                        //好
                        showLog("Good Send Speed.");

                    }
                    //清空
                    mScanSnapShotList.clear();

                }
                mScanSnapShotList.add(new ScanSnapShot(mInFrameCount.get(), mOutFrameCount.get()));
                mInFrameCount.set(0);
                mOutFrameCount.set(0);
                mCurrentScanTime++;
                try {
                    Thread.sleep(DEFAULT_SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ScanSnapShot {
        public int inCount;
        public int outCount;

        public ScanSnapShot(int inCount, int outCount) {
            this.inCount = inCount;
            this.outCount = outCount;
        }
    }

    private void showLog(String msg) {
        if (isDebug) Log.i("NormalSendQueue", "" + msg);
    }
}
