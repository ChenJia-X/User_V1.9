package com.example.foolishfan.user_v10;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建文件 和 文件夹
 * Created by yu on 2016/8/21.
 * https://blog.csdn.net/man_help/article/details/52316362
 */
public class FileUtils {

    private static final String TAG = "FileUtils";

    public static final int FLAG_SUCCESS = 1;//创建成功
    public static final int FLAG_EXISTS = 2;//已存在
    public static final int FLAG_FAILED = 3;//创建失败


    /**
     * 创建 单个 文件
     *
     * @param filePath 待创建的文件路径
     * @return 结果码
     */
    public static int CreateFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            Log.e(TAG, "The file [ " + filePath + " ] has already exists");
            return FLAG_EXISTS;
        }
        if (filePath.endsWith(File.separator)) {// 以 路径分隔符 结束，说明是文件夹
            Log.e(TAG, "The file [ " + filePath + " ] can not be a directory");
            return FLAG_FAILED;
        }

        //判断父目录是否存在
        if (!file.getParentFile().exists()) {
            //父目录不存在 创建父目录
            Log.d(TAG, "creating parent directory...");
            if (!file.getParentFile().mkdirs()) {
                Log.e(TAG, "created parent directory failed.");
                return FLAG_FAILED;
            }
        }

        //创建目标文件
        try {
            if (file.createNewFile()) {//创建文件成功
                Log.i(TAG, "create file [ " + filePath + " ] success");
                return FLAG_SUCCESS;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "create file [ " + filePath + " ] failed");
            return FLAG_FAILED;
        }

        return FLAG_FAILED;
    }

    /**
     * 创建 文件夹
     *
     * @param dirPath 文件夹路径
     * @return 结果码
     */
    public static int createDir(String dirPath) {

        File dir = new File(dirPath);
        //文件夹是否已经存在
        if (dir.exists()) {
            Log.w(TAG, "The directory [ " + dirPath + " ] has already exists");
            return FLAG_EXISTS;
        }
        if (!dirPath.endsWith(File.separator)) {//不是以 路径分隔符 "/" 结束，则添加路径分隔符 "/"
            dirPath = dirPath + File.separator;
        }
        //创建文件夹
        if (dir.mkdirs()) {
            Log.d(TAG, "create directory [ " + dirPath + " ] success");
            return FLAG_SUCCESS;
        }

        Log.e(TAG, "create directory [ " + dirPath + " ] failed");
        return FLAG_FAILED;
    }

    /**
     * 遍历目录下的所有文件
     *
     * @param strPath
     * @return
     */
    public static List<String> getFileList(String strPath) {
        List<String> fileList = new ArrayList<>();
        File dir = new File(strPath);
        File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                fileList.add(fileName);
                /*if (files[i].isDirectory()) { // 判断是文件还是文件夹
                    getFileList(files[i].getAbsolutePath()); // 获取文件绝对路径
                } else if (fileName.endsWith("avi")) { // 判断文件名是否以.avi结尾
                    String strFileName = files[i].getAbsolutePath();
                    System.out.println("---" + strFileName);
                    fileList.add(files[i]);
                } else {
                    continue;
                }*/
            }

        }
        return fileList;
    }

    /**
     * 读取文本文件中的内容
     */
    public static String readTxtFile(String strFilePath) {
        String path = strFilePath;
        StringBuilder content = new StringBuilder(); //文件内容字符串
        //打开文件
        File file = new File(path);
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()) {
            Log.d("FileUtils", "The File doesn't not exist.");
        } else {
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader reader = new BufferedReader(fileReader);
                String line;
                //分行读取
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }

                reader.close();
                fileReader.close();
            } catch (java.io.FileNotFoundException e) {
                Log.d("TestFile", "The File doesn't not exist.");
            } catch (IOException e) {
                Log.d("TestFile", e.getMessage());
            }
        }
        return content.toString();
    }


}
