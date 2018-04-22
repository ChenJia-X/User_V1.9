package com.example.foolishfan.user_v10;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by man on 2017/7/26.
 */

public class Utils {

    public static void showToast(final Context context, final String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(final Context context, final int resourceId) {
        Toast.makeText(context, resourceId, Toast.LENGTH_SHORT).show();
    }

    public static ProgressDialog showProgressDialog(Context context, String message) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        return dialog;
    }

    /**
     * 对byte进行溢出处理，获得正确（无符号型byte）的值，
     * 并将2个2byte合并为一个short。
     *
     * @param b1 byte
     * @param b2 byte
     * @return short
     */
    public static int getUnsignedShort(byte b1, byte b2) {
        return ((b1 & 0xff) << 8) | (b2 & 0xff);
    }

    /**
     * 将一个short拆成2个byte
     *
     * @param aShort short
     * @return byte[]
     */
    public static byte[] short2Bytes(short aShort) {
        return new byte[]{(byte) ((aShort & 0xffff) >> 8), (byte) (aShort & 0xff)};
    }

    /**
     * 将整数转化成BCD码，
     * 比如17->0001 0111(23)
     * 99->1001 1001(-103)
     *
     * @param aByte byte
     * @return byte
     * by Chen Jia
     */
    public static byte byte2bcd(byte aByte) {
        byte temp1 = (byte) (aByte / 10);//前4位
        byte temp2 = (byte) (aByte % 10);//后4位
        return (byte) ((temp1 << 4) | (temp2));
    }

    public static byte[] byte2bcd(byte[] bytes) {
        byte[] bcd = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            bcd[i] = byte2bcd(bytes[i]);
        }
        return bcd;
    }

    /**
     * 将BCD码转化为正常形式
     * 比如 23(17的BCD码)->17 (0001 0111->0001 0001)
     * -103（99的BCD码）->99 (1001 1001->0110 0011)做不到
     *
     * @param aByte byte
     * @return byte
     * by Chen Jia
     */
    public static byte bcd2byte(byte aByte) {
        byte temp1 = (byte) ((aByte >> 4) & 0x0f);//前4位,&0x0f运算后把首位的符号位也变0了。
        byte temp2 = (byte) (aByte & 0x0f);//后4位
        return (byte) (temp1 * 10 + temp2);
    }

    public static byte[] bcd2byte(byte[] bytes) {
        byte[] aByte = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            aByte[i] = bcd2byte(bytes[i]);
        }
        return aByte;
    }

    /**
     * Converts the given hex-color-string to rgb.
     *
     * @param hex
     * @return
     */
    public static int rgb(String hex) {
        int color = (int) Long.parseLong(hex.replace("#", ""), 16);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color >> 0) & 0xFF;
        return Color.rgb(r, g, b);
    }

    public static final int[] MATERIAL_RGB = {
            rgb("#e74c3c"), rgb("#2ecc71"), rgb("#3498db")
    };


    /**
     * 将长时间格式字符串转换为时间 yyyy-MM-dd HH:mm:ss
     *
     * @param strDate
     * @return
     */
    public static Date strToDateLong(String strDate, String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }

    public static String int2Month(int month) {
        switch (month) {
            case 1:
                return "January";
            case 2:
                return "February";
            case 3:
                return "March";
            case 4:
                return "April";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "August";
            case 9:
                return "September";
            case 10:
                return "October";
            case 11:
                return "November";
            default:
                return "December";
        }
    }

    /**
     * ---------------------------------------------------------------------------------------
     * 以下为前人方法，未经过验证
     */

    public String bcd2Str(byte[] bytes) {
        StringBuffer temp = new StringBuffer(bytes.length * 2);
        char[] temp1 = {'A', 'B', 'C', 'D', 'E', 'F'};

        for (int i = 0; i < bytes.length; i++) {
            if (((bytes[i] & 0xf0) >> 4) <= 9) {
                temp.append((byte) ((bytes[i] & 0xf0) >> 4));
            } else {
                temp.append(temp1[((bytes[i] & 0xf0) >> 4) - 10]);
            }
            if ((bytes[i] & 0x0f) <= 9) {
                temp.append((byte) (bytes[i] & 0x0f));
            } else {
                temp.append(temp1[(bytes[i] & 0x0f) - 10]);
            }
        }
        return temp.toString();
    }

    /**
     * BCD码转16进制的String
     * 比如10011001->99
     * 11111111->FF
     *
     * @param bytes
     * @return
     */
    public String bcd2HexStr(byte[] bytes) {
        StringBuilder temp = new StringBuilder(bytes.length * 2);
        char[] temp1 = {'A', 'B', 'C', 'D', 'E', 'F'};

        for (byte aByte : bytes) {
            //byte前四位
            if (((aByte & 0xf0) >> 4) <= 9) {
                temp.append((byte) ((aByte & 0xf0) >> 4));
            } else {
                temp.append(temp1[((aByte & 0xf0) >> 4) - 10]);
            }
            //byte后四位
            if ((aByte & 0x0f) <= 9) {
                temp.append((byte) (aByte & 0x0f));
            } else {
                temp.append(temp1[(aByte & 0x0f) - 10]);
            }
        }
        return temp.toString();
    }

    /**
     * 将两个ASCII字符合成一个字节； 如："EF"--> 0xEF
     *
     * @param src0 byte
     * @param src1 byte
     * @return byte
     */
    public static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[]{src0})).byteValue();
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1})).byteValue();
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }

    //dean add
    public String toHexString(String s) {
        String str = "";

        for (int i = 0; i < s.length() / 2; i++) {
            str += "0X" + s.substring(2 * i, 2 * i + 2);
            if (i != (s.length() / 2 - 1)) {
                str += ",";
            }
        }

        return str;//0x表示十六进制
    }

    /**
     * 将16进制的数字的字符转化为对应的数值。
     * 比如'F'->(byte)15
     *
     * @param c char
     * @return byte
     */
    private byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    /**
     * 十六进制串转化为 十六进制数组
     * 20141105AB -> 0x20,0x14,0x11,0x05,0xab
     *
     * @param hex
     * @return
     */
    public byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] chars = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(chars[pos]) << 4 | toByte(chars[pos + 1]));
        }
        return result;
    }

    /**
     * 将指定字符串src，以每两个字符分割转换为16进制形式 如："2B44EFD9" --> byte[]{0x2B, 0x44, 0xEF, 0xD9}
     *
     * @param src String
     * @return byte[]
     */
    public static byte[] HexString2Bytes(String src) {
        byte[] ret = new byte[8];
        byte[] tmp = src.getBytes();
        for (int i = 0; i < src.length() / 2; i++) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        return ret;
    }

    /**
     * @param b byte[]
     * @return String
     */
    public static String Bytes2HexString(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }

    private byte[] getBytes(char[] chars) {
        Charset cs = Charset.forName("UTF-8");
        CharBuffer cb = CharBuffer.allocate(chars.length);
        cb.put(chars);
        cb.flip();
        ByteBuffer bb = cs.encode(cb);

        return bb.array();
    }

    private char[] getChars(byte[] bytes) {
        Charset cs = Charset.forName("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        bb.flip();
        CharBuffer cb = cs.decode(bb);

        return cb.array();
    }
}
