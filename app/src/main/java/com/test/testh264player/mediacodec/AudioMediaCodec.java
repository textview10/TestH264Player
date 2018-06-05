package com.test.testh264player.mediacodec;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by xu.wang
 * Date on  2018/5/28 17:21:33.
 *
 * @Desc
 */

public class AudioMediaCodec {
    private static final String TAG = "AudioMediaCodec";
    public static final int DEFAULT_FREQUENCY = 44100;
    public static final int DEFAULT_MAX_BPS = 64;
    public static final int DEFAULT_MIN_BPS = 32;
    public static final int DEFAULT_ADTS = 1;
    public static final String DEFAULT_MIME = "audio/mp4a-latm";
    public static final int DEFAULT_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int DEFAULT_AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLC;
    public static final int DEFAULT_CHANNEL_COUNT = 2;
    public static final boolean DEFAULT_AEC = false;


    public static AudioTrack getAudioTrack() {
        int minBuffSize = AudioTrack.getMinBufferSize(DEFAULT_FREQUENCY, DEFAULT_CHANNEL_COUNT, DEFAULT_AUDIO_ENCODING);
        if (minBuffSize == AudioTrack.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid parameter !");
        }
        AudioTrack mPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, DEFAULT_FREQUENCY, AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, 2048, AudioTrack.MODE_STREAM);//
        if (mPlayer.getState() == AudioTrack.STATE_UNINITIALIZED) {
            Log.e(TAG, "AudioTrack initialize fail !");
        }
        return mPlayer;
    }

    public static MediaCodec getAudioMediaCodec() {
        try {
            //需要解码数据的类型
            MediaCodec mDecoder = MediaCodec.createDecoderByType(DEFAULT_MIME);
            //初始化解码器
            //MediaFormat用于描述音视频数据的相关参数
            MediaFormat mediaFormat = new MediaFormat();
            //数据类型
            mediaFormat.setString(MediaFormat.KEY_MIME, DEFAULT_MIME);
            mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, DEFAULT_AUDIO_ENCODING);
            //声道个数
            mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, DEFAULT_CHANNEL_COUNT);
            //采样率
            mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, DEFAULT_FREQUENCY);
            //比特率
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, DEFAULT_MAX_BPS * 1024);
            //用来标记AAC是否有adts头，1->有
            mediaFormat.setInteger(MediaFormat.KEY_IS_ADTS, DEFAULT_ADTS);
            //用来标记aac的类型
            mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, DEFAULT_AAC_PROFILE);
            //ByteBuffer key（暂时不了解该参数的含义，但必须设置）
            byte[] data = new byte[]{(byte) 0x11, (byte) 0x90};
            ByteBuffer csd_0 = ByteBuffer.wrap(data);
            mediaFormat.setByteBuffer("csd-0", csd_0);
            //解码器配置
            mDecoder.configure(mediaFormat, null, null, 0);
            return mDecoder;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "AudioMediaCodec initial error...");
        }

        return null;
    }
}
