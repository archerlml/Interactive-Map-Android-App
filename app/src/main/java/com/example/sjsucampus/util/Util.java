package com.example.sjsucampus.util;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewParent;

import com.example.sjsucampus.BaseApplication;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

public class Util {
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuffer digestSB = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            int lowNibble = bytes[i] & 0x0f;
            int highNibble = (bytes[i] >> 4) & 0x0f;
            digestSB.append(Integer.toHexString(highNibble));
            digestSB.append(Integer.toHexString(lowNibble));
        }
        return digestSB.toString();
    }

    public static byte[] hexToBytes(String hexString) {
        byte[] b = new byte[hexString.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(hexString.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    public static String getClassNameByStackIndex(int index) {
        try {
            String name = Thread.currentThread().getStackTrace()[index]
                    .getClassName();
            int dot = name.lastIndexOf('.');
            return name.substring(dot + 1);
        } catch (Exception e) {
        }
        return "";
    }

    public static String getHostFunctionName(int index) {
        try {
            return Thread.currentThread().getStackTrace()[index]
                    .getMethodName();
        } catch (Exception e) {
        }
        return "unknown method";
    }

    public static String getString(Object... objects) {
        if (objects == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object o : objects) {
            if (o != null) {
                sb.append(o.toString());
            }
        }
        return sb.toString();
    }

    public static byte[] getUtf8Bytes(String s) {
        if (s == null) {
            return null;
        }
        try {
            return s.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
        }
        return s.getBytes();
    }

    public static String getUtf8String(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            return new String(bytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
        }
        return null;
    }

    public static String getSHA1(String content, Integer salt) {
        byte[] md5 = digest("SHA1", content.getBytes(), salt);
        return bytesToHex(md5);
    }

    public static String getSHA1(byte[] content, Integer salt) {
        byte[] md5 = digest("SHA1", content, salt);
        return bytesToHex(md5);
    }

    public static byte[] digest(String algorithm, byte[] content, Integer salt) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(
                    "Can't find the SHA1 algorithm in the java.security package");
        }

        if (salt != null) {
            String saltString = String.valueOf(salt);
            md.update(saltString.getBytes());
        }
        md.update(content);
        return md.digest();
    }

    public static byte[] getBytes(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        byte[] result = null;
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            result = outStream.toByteArray();
        } catch (Exception e) {
        }
        return result;
    }

    public static void setBytes(OutputStream outputStream, byte[] bytes) {
        if (outputStream == null || bytes == null) {
            return;
        }
        try {
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
            }
        }
        return;
    }

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean isEmpty(Collection c) {
        return c == null || c.size() == 0;
    }

    public static String getPath(Object... file) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < file.length; i++) {
            if (file[i] == null) {
                continue;
            }
            String fileName = file[i].toString();
            sb.append(fileName);
            if (!fileName.endsWith(File.pathSeparator)) {
                sb.append(File.pathSeparator);
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
    }

    public static boolean isChildViewOf(View view, int parentId) {
        ViewParent parent = view.getParent();
        while (parent != null && parent instanceof View) {
            if (((View) parent).getId() == parentId) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    public static String objToJson(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
        }
        return "";
    }

    public static <T> T jsonToObj(String json, Class<?> cls) {
        try {
            return (T) new ObjectMapper().readValue(json, cls);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static double convertDpToPixel(double dp) {
        Resources resources = BaseApplication.get().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        double px = dp * ((double) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static double convertPixelsToDp(double px) {
        Resources resources = BaseApplication.get().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        double dp = px / ((double) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    public static <T> T getJsonValueAs(String json, String path, Class<?> clz) {
        JsonNode node = null;
        try {
            node = new ObjectMapper().readTree(json).at(path);
            if (String.class.equals(clz)) {
                return (T) node.asText();
            } else if (Integer.class.equals(clz)) {
                return (T) (Integer) node.asInt();
            } else if (Double.class.equals(clz)) {
                return (T) (Double) node.asDouble();
            } else if (Boolean.class.equals(clz)) {
                return (T) (Boolean) node.asBoolean();
            }
        } catch (IOException e) {

        }
        return null;
    }

    public static int getRes(String name) {
        Resources resources = BaseApplication.get().getResources();
        final int resourceId = resources.getIdentifier(name, "drawable",
                BaseApplication.get().getPackageName());
        return resourceId;
    }

}
