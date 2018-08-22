package com.test.video_play.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by wt on 2018/6/15.
 * 获取IP工具类
 */
public class AboutNetUtils {
    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                //当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
            return null;
        }
        return null;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    // TODO: 2018/6/28 获取设备名称
    public static String getDeviceModel() {
        return Build.MODEL;
    }

    // TODO: 2018/6/28 获取无线网名称
//    public static String getWinfeName(Context context) {
//        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//        int wifiState = wifiMgr.getWifiState();
//        WifiInfo info = wifiMgr.getConnectionInfo();
//        if (info==null|| TextUtils.isEmpty(info.getSSID())){
//            return null;
//        }
//        //获取Android版本号
//        int sdkInt = Build.VERSION.SDK_INT;
//        if (sdkInt >= 17) {
//            if (info.getSSID().startsWith("\"") && info.getSSID().endsWith("\"")) {
//
//                 return info.getSSID().substring(1, info.getSSID().length() - 1);
//            }
//        }
//        return info != null ? info.getSSID() : null;
//    }

    public  static boolean isNetWorkConnected(Context context) {
        // TODO Auto-generated method stub
        try{
            ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(connectivity != null){
                NetworkInfo netWorkinfo = connectivity.getActiveNetworkInfo();
                if(netWorkinfo != null && netWorkinfo.isAvailable()){
                    if(netWorkinfo.getState() == NetworkInfo.State.CONNECTED){
                        return true;
                    }
                }
            }
        }catch(Exception e){
            Log.e("UdpService : ",e.toString());
            return false;
        }
        return false;
    }

    // TODO: 2018/7/12 获取本地所有ip地址
    public static String getLocalIpAddress() {
        String address = null;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        address = inetAddress.getHostAddress().toString();
                        //ipV6
                        if (!address.contains("::")) {
                            return address;
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("getIpAddress Exception", ex.toString());
        }
        return null;
    }

}
