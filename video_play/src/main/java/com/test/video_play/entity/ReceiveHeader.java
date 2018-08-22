package com.test.video_play.entity;

/**
 * Created by xu.wang
 * Date on  2018/08/22 14:47:47.
 *
 * @Desc 接收并解析出数据的头信息
 */

public class ReceiveHeader {
    private int mainCmd;
    private int subCmd;
    private byte encodeVersion;
    private int stringBodylength;
    private int buffSize;

    public ReceiveHeader(int mainCmd, int subCmd, byte encodeVersion, int stringBodylength, int buffSize) {
        this.mainCmd = mainCmd;
        this.subCmd = subCmd;
        this.encodeVersion = encodeVersion;
        this.stringBodylength = stringBodylength;
        this.buffSize = buffSize;
    }

    public int getMainCmd() {
        return mainCmd;
    }

    public void setMainCmd(int mainCmd) {
        this.mainCmd = mainCmd;
    }

    public int getSubCmd() {
        return subCmd;
    }

    public void setSubCmd(int subCmd) {
        this.subCmd = subCmd;
    }

    public byte getEncodeVersion() {
        return encodeVersion;
    }

    public void setEncodeVersion(byte encodeVersion) {
        this.encodeVersion = encodeVersion;
    }

    public int getStringBodylength() {
        return stringBodylength;
    }

    public void setStringBodylength(int stringBodylength) {
        this.stringBodylength = stringBodylength;
    }

    public int getBuffSize() {
        return buffSize;
    }

    public void setBuffSize(int buffSize) {
        this.buffSize = buffSize;
    }
}
