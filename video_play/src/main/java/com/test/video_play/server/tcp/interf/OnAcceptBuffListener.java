package com.test.video_play.server.tcp.interf;


import com.test.video_play.entity.Frame;

/**
 * Created by xu.wang
 * Date on  2018/08/22 14:47:47.
 *
 * @Desc    关于帧类型回调
 */

public interface OnAcceptBuffListener {
    void acceptBuff(Frame frame);
}
