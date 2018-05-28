package com.test.testh264player;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.test.testh264player.bean.Frame;
import com.test.testh264player.decode.VIdeoMediaCodec;
import com.test.testh264player.interf.OnAcceptBuffListener;
import com.test.testh264player.interf.OnAcceptTcpStateChangeListener;
import com.test.testh264player.server.TcpServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private SurfaceView mSurface = null;
    private SurfaceHolder mSurfaceHolder;
    private DecodeThread mDecodeThread;

    private NormalPlayQueue mPlayqueue;
    private TcpServer tcpServer;
    private VIdeoMediaCodec VIdeoMediaCodec;
    private FileOutputStream fos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mSurface = findViewById(R.id.surfaceview);
        initialFIle();
        startServer();
        mSurfaceHolder = mSurface.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initialMediaCodec(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (VIdeoMediaCodec != null) VIdeoMediaCodec.release();
            }
        });
    }

    private void initialMediaCodec(SurfaceHolder holder) {
        VIdeoMediaCodec = new VIdeoMediaCodec(holder, null, null);
        mDecodeThread = new DecodeThread(VIdeoMediaCodec.getCodec(), mPlayqueue);
        VIdeoMediaCodec.start();
        mDecodeThread.start();
    }

    private void startServer() {
        mPlayqueue = new NormalPlayQueue();
        tcpServer = new TcpServer();
        tcpServer.setOnAccepttBuffListener(new MyAcceptH264Listener());
        tcpServer.setOnTcpConnectListener(new MyAcceptTcpStateListener());
        tcpServer.startServer();
    }

    //接收到H264buff的回调
    class MyAcceptH264Listener implements OnAcceptBuffListener {

        @Override
        public void acceptBuff(Frame frame) {
//            if (frame.getType() == Frame.AUDIO_FRAME) {
//                try {
//                    fos.write(frame.getBytes());
//                } catch (IOException e) {
//                    Log.e("MAInActivity", "Exception =" + e.toString());
//                }
//                return;
//            }
            mPlayqueue.putByte(frame);
        }
    }

    //客户端Tcp连接状态的回调...
    class MyAcceptTcpStateListener implements OnAcceptTcpStateChangeListener {

        @Override
        public void acceptTcpConnect() {    //接收到客户端的连接...
            Log.e(TAG, "accept a tcp connect...");
        }

        @Override
        public void acceptTcpDisConnect(Exception e) {  //客户端的连接断开...
            Log.e(TAG, "acceptTcpConnect exception = " + e.toString());
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (mPlayqueue != null) mPlayqueue.stop();
        if (VIdeoMediaCodec != null) VIdeoMediaCodec.release();
        if (mDecodeThread != null) mDecodeThread.shutdown();
        if (tcpServer != null) tcpServer.stopServer();
    }

    private void initialFIle() {
        File file = new File(Environment.getExternalStorageDirectory(), "111.pcm");
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            fos = new FileOutputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
