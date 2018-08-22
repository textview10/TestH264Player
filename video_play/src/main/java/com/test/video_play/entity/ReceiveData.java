package com.test.video_play.entity;

/**
 * Created by xu.wang
 * Date on  2018/08/22 14:47:47.
 *
 * @Desc 返回一组解析后的数据
 */

public class ReceiveData {
    private ReceiveHeader header;
    private byte[] buff;

    public ReceiveHeader getHeader() {
        return header;
    }

    public void setHeader(ReceiveHeader header) {
        this.header = header;
    }

    public byte[] getBuff() {
        return buff;
    }

    public void setBuff(byte[] buff) {
        this.buff = buff;
    }
}
