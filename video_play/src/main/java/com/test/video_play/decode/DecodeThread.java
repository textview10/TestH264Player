package com.test.video_play.decode;

import android.media.MediaCodec;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.test.video_play.decode.play.AudioPlay;
import com.test.video_play.decode.play.VideoPlay;
import com.test.video_play.entity.Frame;
import com.test.video_play.server.tcp.NormalPlayQueue;

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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public DecodeThread(MediaCodec mediaCodec, NormalPlayQueue playQueue) {
        this.playQueue = playQueue;
        audioPlay = new AudioPlay();
        videoPlay = new VideoPlay(mediaCodec);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
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
                        Log.i(TAG, "receive a frame count");
                    } catch (Exception e) {
                        Log.e(TAG, "frame Exception" + e.toString());
                    }
                    break;
                case Frame.SPSPPS:
                    try {
                        ByteBuffer bb = ByteBuffer.allocate(frame.getPps().length + frame.getSps().length);
                        bb.put(frame.getSps());
                        bb.put(frame.getPps());
                        Log.e(TAG, "receive Sps pps");
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
        Log.i(TAG, "DecodeThread shutdown");
        isPlaying = false;
        this.interrupt();
        if (audioPlay != null) audioPlay.release();
        if (videoPlay != null) videoPlay.release();
    }
}
