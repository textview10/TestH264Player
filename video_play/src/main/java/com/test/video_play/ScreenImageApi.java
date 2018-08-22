package com.test.video_play;

/**
 * Created by wt on 2018/6/11.
 * 设备常用配置
 */
public class ScreenImageApi {
    public static final byte encodeVersion1 = 0x00;       //版本号1

    public class RECORD {   //录屏指令
        public static final int MAIN_CMD = 0xA2; //录屏主指令
        public static final int SEND_BUFF = 0x01;//音视频解析播放
    }

    public class SERVER {//服务端与客户端交互指令
        public static final int MAIN_CMD = 0xA0; //投屏回传主指令
        public static final int INITIAL_SUCCESS = 0x01;//服务端初始化成功
    }

}
