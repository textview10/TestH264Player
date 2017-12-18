package com.test.testh264player;

import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by xu.wang
 * Date on  2017/11/29 14:47:47.
 *
 * @Desc
 */

public class NormalPlayQueue {
    private ArrayBlockingQueue<byte[]> mPlayQueue;
    private String TAG = "NormalPlayQueue";
    private static final int NORMAL_FRAME_BUFFER_SIZE = 150; //缓存区大小

    public NormalPlayQueue() {
        mPlayQueue = new ArrayBlockingQueue<byte[]>(NORMAL_FRAME_BUFFER_SIZE, true);
    }


    public byte[] takeByte() {
        try {
            if (mPlayQueue.size() >= 50) {
                Log.e(TAG, "too much frame in NormalPlayQueue" + mPlayQueue.size());
            }
            return mPlayQueue.take();
        } catch (InterruptedException e) {
            Log.e(TAG, "take bytes exception" + e.toString());
            return null;
        }
    }

    public void putByte(byte[] bytes) {
        try {
            mPlayQueue.put(bytes);
        } catch (InterruptedException e) {
            Log.e(TAG, "put bytes exception" + e.toString());
        }
    }

    public void stop() {
        if (mPlayQueue != null) {
            mPlayQueue.clear();
        }
    }

}
