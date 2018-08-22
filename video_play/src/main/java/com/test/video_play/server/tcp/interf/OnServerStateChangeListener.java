package com.test.video_play.server.tcp.interf;

/**
 * Created by xu.wang
 * Date on  2018/08/22 14:47:47.
 *
 * @Desc    监听连接回调
 */

public abstract class OnServerStateChangeListener {
    //接收到客户端的Tcp连接
    public abstract void acceptH264TcpConnect();

    /**
     * by wt
     * 接收到客户端的Tcp断开连接
     *
     * @param e           异常提示
     */
    public abstract void acceptH264TcpDisConnect(Exception e);

    //读数据的时间
    public void acceptH264TcpNetSpeed(String netSpeed) {

    }

    public abstract void exception();

}
