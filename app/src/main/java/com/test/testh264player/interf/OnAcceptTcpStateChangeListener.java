package com.test.testh264player.interf;

/**
 * Created by xu.wang
 * Date on  2018/4/18 09:13:42.
 *
 * @Desc
 */

public interface OnAcceptTcpStateChangeListener {
    void acceptTcpConnect();    //接收到客户端的Tcp连接

    void acceptTcpDisconnect(Exception e); //接收到客户端的Tcp断开连接
}
