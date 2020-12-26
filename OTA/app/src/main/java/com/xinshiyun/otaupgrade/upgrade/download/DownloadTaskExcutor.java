package com.xinshiyun.otaupgrade.upgrade.download;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

public class DownloadTaskExcutor {

    private static final String TAG = DownloadTaskExcutor.class.getSimpleName();
    private DownloadManager downloadManager;
    private Context mContext;
    private static long total_size = -1;
    private static long cur_size = 0;
    private static String downloadFile = null;

    public DownloadTaskExcutor(Context context) {
        Log.d(TAG, "DownloadTaskExcutor()");
        mContext = context;
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public long start(DownloadInfo task) {
        long id = 0;
        Log.d(TAG, "DownloadTaskExcutor start() path:"+task.getSavePath());
        if (task == null) {
            throw new IllegalArgumentException("download task can not be null !");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            id =doDownloadUpdateImg(task);
            task.setId(id);
        }else{
            DownloadManager.Request request = new DownloadManager.Request(task.getUri());
            Calendar cal_now = Calendar.getInstance();
            long now = cal_now.getTimeInMillis();
            File file = new File(task.getSavePath() + "/update_" + now + getFileType(task.getUri()));

            request.setDestinationUri(Uri.fromFile(file));
            id = downloadManager.enqueue(request);
            String filePath = getPath(id);
            Log.d(TAG,"Download path: "+filePath);
            task.setId(id);
        }
        return id;
    }

    public int remove(long id) {
        Log.d(TAG,"Download remove()");
        if (id == -1) {
            throw new IllegalArgumentException("remove id can not be -1 !");
        }
        return downloadManager.remove(id);
    }

    public void stop(DownloadInfo task) {
        Log.d(TAG,"Download stop()");
        if (task == null) {
            throw new IllegalArgumentException("download task can not be null !");
        }
        downloadManager.remove(task.getId());
    }

    public int[] getBytesAndStatus(long downloadId) {
        Log.d(TAG,"getBytesAndStatus()");
        int[] bytesAndStatus = new int[] {-1, -1, 0, 0};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bytesAndStatus[0] = (int)cur_size;
            bytesAndStatus[1] = (int)total_size;
            if(cur_size < total_size){
                bytesAndStatus[2] = DownloadManager.STATUS_RUNNING;
            }else if(cur_size == total_size){
                bytesAndStatus[2] = DownloadManager.STATUS_SUCCESSFUL;
            }else if(total_size == -1){
                bytesAndStatus[2] = DownloadManager.STATUS_PENDING;
            }
            bytesAndStatus[3] = 0;
            Log.d(TAG,"getBytesAndStatus() VERSION_CODES.O");
        }else{
            DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
            Cursor c = null;
            try {
                c = downloadManager.query(query);
                if (c != null && c.moveToFirst()) {
                    bytesAndStatus[0] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    bytesAndStatus[1] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    bytesAndStatus[2] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    bytesAndStatus[3] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }

        return bytesAndStatus;
    }

    public String getPath(long downloadID) {
        String fileName = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            fileName = downloadFile;
            return fileName;
        }else{
            DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadID);
            Cursor c = null;
            c = downloadManager.query(query);
            c.moveToFirst();

            int fileUriIdx = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
            String fileUri = c.getString(fileUriIdx);

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                if (fileUri != null) {
                    fileName = Uri.parse(fileUri).getPath();
                }
            } else {
                //Android 7.0以上的方式：请求获取写入权限，这一步报错
                //过时的方式：DownloadManager.COLUMN_LOCAL_FILENAME
                int fileNameIdx = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                fileName = c.getString(fileNameIdx);
            }
            Log.d(TAG,"getPath() fileName:"+fileName);
            return fileName;
        }
        //return c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_FILENAME));
    }

    private String getFileType(Uri uri) {
        Log.d(TAG,"getFileType()");
        if (uri.toString().contains("zip")) {
            return ".zip";
        } else if (uri.toString().contains("img")) {
            return ".img";
        }
        return ".zip";
    }

    public int doDownloadUpdateImg(final DownloadInfo task)
    {
        Log.d(TAG,"doDownloadUpdateImg() VERSION_CODES.O");
        new Thread(new Runnable(){
            @Override
            public void run() {
                if (!task.getSavePath().equals(null)) {
                    URL url = null;
                    HttpURLConnection conn = null;
                    InputStream is = null;
                    FileOutputStream fos = null;
                    int len = 0;

                    try {
                        url = new URL(task.getUri().toString());
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(5000);
                        //获取到文件的大小
                        total_size = conn.getContentLength();
                        Log.d(TAG,"doDownloadUpdateImg() total_size ="+total_size);
                        is = conn.getInputStream();
                        Calendar cal_now = Calendar.getInstance();
                        long now = cal_now.getTimeInMillis();
                        //File file = new File(task.getSavePath() + "/update_" + now + getFileType(task.getUri()));
                        File file = new File(task.getSavePath() + "/update"+ getFileType(task.getUri()));
                        downloadFile = file.toString();
                        fos = new FileOutputStream(file);
                        BufferedInputStream bis = new BufferedInputStream(is);

                        byte[] buffer = new byte[1024];

                        while((len = bis.read(buffer)) != -1){
                            fos.write(buffer,0,len);
                            cur_size += len;
                        }

                        fos.close();
                        bis.close();
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        return 0;
    }
}
