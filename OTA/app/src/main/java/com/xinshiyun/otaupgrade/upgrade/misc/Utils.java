package com.xinshiyun.otaupgrade.upgrade.misc;
import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Utils {

    public static boolean checkVersion(Context mContext,String updateVersion){
        boolean ret = false;
        String currentVersion = "";
        long current = -1,update = -1;

        currentVersion = SysProperties.getVersion(mContext).replace(".", "");
        try{
            String upver = updateVersion.substring(1,updateVersion.length());
            String curver = currentVersion.substring(1,currentVersion.length());
            update = Long.valueOf(upver);
            current = Long.valueOf(curver);
        }catch (Exception e){
            e.printStackTrace();
        }

        if(update > current){
            ret = true;
        }
        return ret;
    }

    public static boolean copyFile(String oldPath, String newPath) {
        try {
            File oldFile = new File(oldPath);
            if (!oldFile.exists()) {
                return false;
            } else if (!oldFile.isFile()) {
                return false;
            } else if (!oldFile.canRead()) {
                return false;
            }

            FileInputStream fileInputStream = new FileInputStream(oldPath);    //读入原文件
            FileOutputStream fileOutputStream = new FileOutputStream(newPath);
            byte[] buffer = new byte[1024];
            int byteRead;
            while ((byteRead = fileInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void deletefile(String path) {
        File file = new File(path);
        deletefile(file);
    }
    /**
     * 删除文件
     *
     * @param file
     */
    public static void deletefile(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deletefile(files[i]);
            }
        }
        file.delete();
    }

    public static String getFileName(String path)
    {
        String filename;
        File file = new File(path);
        filename = "/data/"+file.getName();
        return filename;
    }

    public static void deleteSysUpgradeFile(String path)
    {
        File file = new File(path);

        if(file != null && file.exists() && file.isDirectory()){
            File[] fs = file.listFiles();

            if(fs != null){
                for (File f : fs) {
                    if (f.exists()) {
                        if (f.getName().contains("downloadfile")||f.getName().contains("AIUI") || f.getName().contains("update_")) {
                            if (f.getName().contains("bin") || f.getName().contains("img")||f.getName().contains("zip")) {
                                if(f.delete()){
                                }else{
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
