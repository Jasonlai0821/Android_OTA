package com.xinshiyun.otaupgrade.upgrade.download;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.util.Calendar;

public class DownloadTaskExcutor {

    private static final String TAG = DownloadTaskExcutor.class.getSimpleName();
    private DownloadManager downloadManager;
    private Context mContext;

    public DownloadTaskExcutor(Context context) {
        Log.d(TAG, "DownloadTaskExcutor()");
        mContext = context;
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public long start(DownloadInfo task) {
        Log.d(TAG, "DownloadTaskExcutor start()");
        if (task == null) {
            throw new IllegalArgumentException("download task can not be null !");
        }
        DownloadManager.Request request = new DownloadManager.Request(task.getUri());
        Calendar cal_now = Calendar.getInstance();
        long now = cal_now.getTimeInMillis();
        File file = new File(task.getSavePath() + "/update_" + now + getFileType(task.getUri()));

        request.setDestinationUri(Uri.fromFile(file));
        long id = downloadManager.enqueue(request);
        String filePath = getPath(id);
        Log.d(TAG,"Download path: "+filePath);
        task.setId(id);
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
        return bytesAndStatus;
    }

    public String getPath(long downloadID) {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadID);
        Cursor c = null;
        c = downloadManager.query(query);
        c.moveToFirst();

        int fileUriIdx = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
        String fileUri = c.getString(fileUriIdx);
        String fileName = null;
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

}
