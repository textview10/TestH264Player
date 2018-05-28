package com.test.testh264player.decode;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

/**
 * Created by xu.wang
 * Date on  2018/5/28 17:21:33.
 *
 * @Desc
 */

public class AudioMediaCodec {
    public static final int DEFAULT_FREQUENCY = 44100;
    public static final int DEFAULT_MAX_BPS = 64;
    public static final int DEFAULT_MIN_BPS = 32;
    public static final int DEFAULT_ADTS = 0;
    public static final String DEFAULT_MIME = "audio/mp4a-latm";
    public static final int DEFAULT_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int DEFAULT_AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLC;
    public static final int DEFAULT_CHANNEL_COUNT = 1;
    public static final boolean DEFAULT_AEC = false;


    public static AudioTrack getAudioTrack() {
        AudioTrack mPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, DEFAULT_FREQUENCY, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, 2048, AudioTrack.MODE_STREAM);//
        return mPlayer;
    }

}
