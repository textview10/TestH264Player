package com.test.testh264player.decode;

import android.media.MediaCodec;
import android.os.SystemClock;
import android.util.Log;

import com.test.testh264player.NormalPlayQueue;
import com.test.testh264player.bean.Frame;
import com.test.testh264player.play.AudioPlay;
import com.test.testh264player.play.VideoPlay;

import java.nio.ByteBuffer;

/**
 * Created by xu.wang
 * Date on  2017/11/29 14:04:57.
 *
 * @Desc 解码线程, 解码判断类型, 分开解析
 */

public class DecodeThread extends Thread {
    private NormalPlayQueue playQueue;
    private String TAG = "DecodeThread";
    private boolean isPlaying = true;
    private final AudioPlay audioPlay;
    private final VideoPlay videoPlay;

    public DecodeThread(MediaCodec mediaCodec, NormalPlayQueue playQueue) {
        this.playQueue = playQueue;
        audioPlay = new AudioPlay();
        videoPlay = new VideoPlay(mediaCodec);
    }

    @Override
    public void run() {
        while (isPlaying) {
            Frame frame = playQueue.takeByte();
            if (frame == null) {
                SystemClock.sleep(1);
                continue;
            }
            switch (frame.getType()) {
                case Frame.KEY_FRAME:
                case Frame.NORMAL_FRAME:
                    try {
                        videoPlay.decodeH264(frame.getBytes());
                    } catch (Exception e) {
                        Log.e(TAG, "frame Exception" + e.toString());
                    }
                    break;
                case Frame.SPSPPS:
                    try {
                        ByteBuffer bb = ByteBuffer.allocate(frame.getPps().length + frame.getSps().length);
                        bb.put(frame.getSps());
                        bb.put(frame.getPps());
                        videoPlay.decodeH264(bb.array());
                    } catch (Exception e) {
                        Log.e(TAG, "sps pps Exception" + e.toString());
                    }
                    break;
                case Frame.AUDIO_FRAME:
                    try {
                        audioPlay.playAudio(frame.getBytes(), 0, frame.getBytes().length);
                    } catch (Exception e) {
                        Log.e(TAG, "audio Exception" + e.toString());
                    }
                    break;
            }
        }
    }


    public void shutdown() {
        isPlaying = false;
        this.interrupt();
        if (audioPlay != null) audioPlay.release();
        if (videoPlay != null) videoPlay.release();
    }
}
