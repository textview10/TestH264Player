package com.test.video_play.server.tcp;

import android.text.TextUtils;

import com.test.video_play.ScreenImageApi;
import com.test.video_play.utils.ByteUtil;

import java.nio.ByteBuffer;

/**
 * Created by xu.wang
 * Date on  2018/08/22 14:47:47.
 *
 * @Desc 传输数据格式
 */

public class EncodeV1 {
    private int mainCmd;
    private int subCmd;
    private String sendBody;
    private byte[] sendBuffer;    //要发送的内容

    /**
     * by wt
     *
     * @param mainCmd  主指令
     * @param subCmd   子指令
     * @param sendBody 文本内容
     * @param sendBuffer 音视频内容
     */
    public EncodeV1(int mainCmd, int subCmd, String sendBody, byte[] sendBuffer) {
        this.mainCmd = mainCmd;
        this.subCmd = subCmd;
        this.sendBody = sendBody;
        this.sendBuffer = sendBuffer;
    }
    public byte[] buildSendContent() {
        int bodyLength = 0;
        int bodyByte = 0;
        ByteBuffer bb = null;
        //文本数据
        if (!TextUtils.isEmpty(sendBody)) {
            bodyLength = sendBody.getBytes().length;
        }
        //音视频数据
        if (sendBuffer.length != 0) {
            bodyByte = sendBuffer.length;
        }
        //创建内存缓冲区
        bb = ByteBuffer.allocate(18 + bodyLength + bodyByte);
        bb.put(ScreenImageApi.encodeVersion1); //0-1编码版本
        bb.put(ByteUtil.int2Bytes(mainCmd));  //1-5  主指令
        bb.put(ByteUtil.int2Bytes(subCmd));   //5-9  子指令
        bb.put(ByteUtil.int2Bytes(bodyLength));  //9-13位,文本数据长度
        bb.put(ByteUtil.int2Bytes(bodyByte));  //13-17位,音视频数据长度
        byte[] tempb = bb.array();
        bb.put(ByteUtil.getCheckCode(tempb));
        //数据字节数组
        if (bodyLength != 0) {
            bb.put(sendBody.getBytes());
        }
        if (sendBuffer.length != 0) {
            bb.put(sendBuffer);
        }
        return bb.array();
    }

}
