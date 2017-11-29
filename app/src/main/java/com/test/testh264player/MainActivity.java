package com.test.testh264player;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.test.testh264player.interf.OnAcceptBuffListener;
import com.test.testh264player.server.TcpServer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    private SurfaceView mSurface = null;
    private SurfaceHolder mSurfaceHolder;
    private Thread mDecodeThread;
    private MediaCodec mCodec;

    private static final int VIDEO_WIDTH = 1920;
    private static final int VIDEO_HEIGHT = 1088;
    private int FrameRate = 30;
    private Boolean UseSPSandPPS = false;
    private NormalPlayQueue mPlayqueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mSurface = findViewById(R.id.surfaceview);
        startServer();
        mSurfaceHolder = mSurface.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    //通过多媒体格式名创建一个可用的解码器
                    mCodec = MediaCodec.createDecoderByType("video/avc");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //初始化编码器
                final MediaFormat mediaformat = MediaFormat.createVideoFormat("video/avc", VIDEO_WIDTH, VIDEO_HEIGHT);
                //获取h264中的pps及sps数据
                if (UseSPSandPPS) {
                    byte[] header_sps = {0, 0, 0, 1, 103, 66, 0, 42, (byte) 149, (byte) 168, 30, 0, (byte) 137, (byte) 249, 102, (byte) 224, 32, 32, 32, 64};
                    byte[] header_pps = {0, 0, 0, 1, 104, (byte) 206, 60, (byte) 128, 0, 0, 0, 1, 6, (byte) 229, 1, (byte) 151, (byte) 128};
                    mediaformat.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
                    mediaformat.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
                }
                //设置帧率
                mediaformat.setInteger(MediaFormat.KEY_FRAME_RATE, FrameRate);
                //https://developer.android.com/reference/android/media/MediaFormat.html#KEY_MAX_INPUT_SIZE
                //设置配置参数，参数介绍 ：
                // format	如果为解码器，此处表示输入数据的格式；如果为编码器，此处表示输出数据的格式。
                //surface	指定一个surface，可用作decode的输出渲染。
                //crypto	如果需要给媒体数据加密，此处指定一个crypto类.
                //   flags	如果正在配置的对象是用作编码器，此处加上CONFIGURE_FLAG_ENCODE 标签。
                mCodec.configure(mediaformat, holder.getSurface(), null, 0);
                startDecodingThread();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCodec.stop();
                mCodec.release();
            }
        });
    }

    private void startServer() {
        mPlayqueue = new NormalPlayQueue();
        TcpServer tcpServer = new TcpServer();
        tcpServer.setOnAccepttBuffListener(new MyAcceptH264Listener());
        tcpServer.startServer();
    }

    private void startDecodingThread() {
        mCodec.start();
        mDecodeThread = new DecodeThread(mCodec,mPlayqueue);
        mDecodeThread.start();
    }

    class MyAcceptH264Listener implements OnAcceptBuffListener {

        @Override
        public void acceptBuff(byte[] buff) {
            mPlayqueue.putByte(buff);
        }
    }
}
