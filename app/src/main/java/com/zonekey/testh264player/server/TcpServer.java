package com.zonekey.testh264player.server;

import android.util.Log;

import com.zonekey.testh264player.interf.OnAcceptBuffListener;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by xu.wang
 * Date on  2017/11/29 12:03:10.
 *
 * @Desc
 */

public class TcpServer {
    private ServerSocket serverSocket;
    private int tcpPort = 11111;
    private boolean isAccept = true;
    private OnAcceptBuffListener mListener;

    public void startServer() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    serverSocket = new ServerSocket();
                    serverSocket.setReuseAddress(true);
                    InetSocketAddress socketAddress = new InetSocketAddress(tcpPort);
                    serverSocket.bind(socketAddress);
                    while (isAccept) {
                        Socket socket = serverSocket.accept();
                        AcceptH264MsgThread acceptH264MsgThread = new AcceptH264MsgThread(socket.getInputStream(),socket.getOutputStream(), mListener);
                        acceptH264MsgThread.start();
                    }
                } catch (Exception e) {
                    Log.e("TcpServer", "" + e.toString());
                }

            }
        }.start();
    }

    public void setOnAccepttBuffListener(OnAcceptBuffListener listener) {
        this.mListener = listener;
    }
}
