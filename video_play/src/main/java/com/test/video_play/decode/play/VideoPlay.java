package com.test.video_play.decode.play;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by xu.wang
 * Date on  2018/08/22 14:47:47.
 *
 * @Desc 使用MdeieCodec解析H264, 并显示到Surface
 */

public class VideoPlay {
    private static final String TAG = "VideoPlay";
    private MediaCodec mVideoMediaCodec;
//    private OnFrameChangeListener mAcceptBuffListener;

    public VideoPlay(MediaCodec mediaCodec) {
        this.mVideoMediaCodec = mediaCodec;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void decodeH264(byte[] buff) {
        boolean mStopFlag = false;
        //存放目标文件的数据
        ByteBuffer[] inputBuffers = mVideoMediaCodec.getInputBuffers();
        //解码后的数据，包含每一个buffer的元数据信息，例如偏差，在相关解码器中有效的数据大小
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        long timeoutUs = 10000; //微秒
        byte[] marker0 = new byte[]{0, 0, 0, 1};
        byte[] dummyFrame = new byte[]{0x00, 0x00, 0x01, 0x20};
        byte[] streamBuffer = null;
        streamBuffer = buff;
        int bytes_cnt = 0;
        while (mStopFlag == false) {
            bytes_cnt = streamBuffer.length;
            if (bytes_cnt == 0) {
                streamBuffer = dummyFrame;
            }

            int startIndex = 0;
            int remaining = bytes_cnt;
            while (true) {
                if (remaining == 0 || startIndex >= remaining) {
                    break;
                }
                int nextFrameStart = KMPMatch(marker0, streamBuffer, startIndex + 2, remaining);
                if (nextFrameStart == -1) {
                    nextFrameStart = remaining;
                } else {
                }

                int inIndex = mVideoMediaCodec.dequeueInputBuffer(timeoutUs);
                if (inIndex >= 0) {
                    ByteBuffer byteBuffer = inputBuffers[inIndex];
                    byteBuffer.clear();
                    byteBuffer.put(streamBuffer, startIndex, nextFrameStart - startIndex);
                    //在给指定Index的inputbuffer[]填充数据后，调用这个函数把数据传给解码器
                    mVideoMediaCodec.queueInputBuffer(inIndex, 0, nextFrameStart - startIndex, 0, 0);
                    startIndex = nextFrameStart;
                } else {
                    continue;
                }

                int outIndex = mVideoMediaCodec.dequeueOutputBuffer(info, timeoutUs);
//                switch (outIndex) {
//                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
//                        MediaFormat outputFormat = mVideoMediaCodec.getOutputFormat();
//                        Integer width = outputFormat.getInteger(MediaFormat.KEY_WIDTH);
//                        Integer height = outputFormat.getInteger(MediaFormat.KEY_HEIGHT);
//                        Log.d(TAG, "INFO_OUTPUT_FORMAT_CHANGED == width = " + width + "height = " + height);
////                        if (mAcceptBuffListener != null) mAcceptBuffListener.onFrameSize(width, height);
//                        break;
//                }

                if (outIndex >= 0) {
                    //帧控制是不在这种情况下工作，因为没有PTS H264是可用的
//                    while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
//                        try {
//                            Thread.sleep(100);
//                            Log.e(TAG, "sleep 100");
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
                    SystemClock.sleep(1);
//                    Log.e(TAG, "decode a frame time =" + (System.currentTimeMillis() - startMs));
                    boolean doRender = (info.size != 0);
                    //对outputbuffer的处理完后，调用这个函数把buffer重新返回给codec类。
                    mVideoMediaCodec.releaseOutputBuffer(outIndex, doRender);

                } else {
                }
            }
            mStopFlag = true;
        }
    }

//    public void decodeH264new(Byte[] buff, boolean isKeyFrame) {
//        Log.e(TAG, "onFrame start");
//        // Get input buffer index
//        ByteBuffer[] inputBuffers = mVideoMediaCodec.getInputBuffers();
//        int inputBufferIndex = mVideoMediaCodec.dequeueInputBuffer(100);
//
//        Log.e("Media", "onFrame index:" + inputBufferIndex);
//        if (inputBufferIndex >= 0) {
//            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
//            inputBuffer.clear();
//            inputBuffer.put(buff, 0, buff.length);
//            mVideoMediaCodec.queueInputBuffer(inputBufferIndex, 0, length, mCount
//                    14 * TIME_INTERNAL, 0);
//            mCount++;
//        } else {
//            return false;
//        }
//
//        // Get output buffer index
//        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
//        int outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 100);
//        while (outputBufferIndex >= 0) {
//            mCodec.releaseOutputBuffer(outputBufferIndex, true);
//            outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);
//        }
//        Log.e("Media", "onFrame end");
//        return true;
//    }

    private byte[] getBytes(InputStream is) throws IOException {
        int len;
        int size = 1024;
        byte[] buf;
        if (is instanceof ByteArrayInputStream) {
            size = is.available();
            buf = new byte[size];
            len = is.read(buf, 0, size);
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            buf = new byte[size];
            while ((len = is.read(buf, 0, size)) != -1)
                bos.write(buf, 0, len);
            buf = bos.toByteArray();
        }
        return buf;
    }

    private int KMPMatch(byte[] pattern, byte[] bytes, int start, int remain) {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int[] lsp = computeLspTable(pattern);

        int j = 0;  // Number of chars matched in pattern
        for (int i = start; i < remain; i++) {
            while (j > 0 && bytes[i] != pattern[j]) {
                // Fall back in the pattern
                j = lsp[j - 1];  // Strictly decreasing
            }
            if (bytes[i] == pattern[j]) {
                // Next char matched, increment position
                j++;
                if (j == pattern.length)
                    return i - (j - 1);
            }
        }

        return -1;  // Not found
    }

    private int[] computeLspTable(byte[] pattern) {
        int[] lsp = new int[pattern.length];
        lsp[0] = 0;  // Base case
        for (int i = 1; i < pattern.length; i++) {
            // Start by assuming we're extending the previous LSP
            int j = lsp[i - 1];
            while (j > 0 && pattern[i] != pattern[j])
                j = lsp[j - 1];
            if (pattern[i] == pattern[j])
                j++;
            lsp[i] = j;
        }
        return lsp;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void release() {
        if (mVideoMediaCodec != null) {
            mVideoMediaCodec.stop();
            mVideoMediaCodec.release();
            mVideoMediaCodec = null;
        }
    }

//    public void setOnFrameChangeListener(OnFrameChangeListener listener) {
//        this.mAcceptBuffListener = listener;
//    }

    public interface OnFrameChangeListener {
        void onFrameSize(int width, int height);
    }
}
