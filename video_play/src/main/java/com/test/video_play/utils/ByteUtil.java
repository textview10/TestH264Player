package com.test.video_play.utils;

/**
 * Created by xu.wang
 * Date on  2018/08/22 14:47:47.
 *
 * @Desc
 */
public class ByteUtil {
    /**
     * 将int转为长度为4的byte数组
     *
     * @param length
     * @return
     */
    public static byte[] int2Bytes(int length) {
        byte[] result = new byte[4];
        result[0] = (byte) length;
        result[1] = (byte) (length >> 8);
        result[2] = (byte) (length >> 16);
        result[3] = (byte) (length >> 24);
        return result;
    }

    //转成2个字节
    public static byte[] short2Bytes(short size) {
        byte[] result = new byte[2];
        result[0] = (byte) size;
        result[1] = (byte) (size >> 8);
        return result;
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用
     *
     * @param src byte数组
     * @return int数值
     */
    public static int bytesToInt(byte[] src) {
        int value;
        value = (int) ((src[0] & 0xFF)
                | ((src[1] & 0xFF) << 8)
                | ((src[2] & 0xFF) << 16)
                | ((src[3] & 0xFF) << 24));
        return value;
    }

    // TODO: 2018/6/11 wt byte转short
    public static short bytesToShort(byte[] src) {
        short value;
        value = (short) ((src[0] & 0xFF)
                | ((src[1] & 0xFF) << 8));
        return value;
    }


    /**
     * 获得校验码
     *
     * @param bytes 根据通讯协议的前12个字节
     * @return
     */
    public static byte getCheckCode(byte[] bytes) {
        byte b = 0x00;
        for (int i = 0; i < bytes.length; i++) {
            b ^= bytes[i];
        }
        return b;
    }
}
