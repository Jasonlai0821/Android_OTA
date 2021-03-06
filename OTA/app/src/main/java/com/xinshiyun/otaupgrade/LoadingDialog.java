package com.xinshiyun.otaupgrade;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * ****************************************************************
 * Created by gefei on 2020/7/14
 * author gefei
 * Desc  加载中
 * ****************************************************************
 */
public class LoadingDialog extends Dialog{
    private static final String TAG = LoadingDialog.class.getSimpleName();

    private LoadingDialog dialog;
    private Context mContext;
    private MyHandler handler;

    private static class MyHandler extends Handler {
        private final WeakReference<Context> mActivity;

        private MyHandler(Context mActivity) {
            this.mActivity = new WeakReference(mActivity);
        }
    }

    public LoadingDialog(Context context, int theme) {
        super(context, theme);
        this.mContext = context;

    }

    public LoadingDialog(Context context) {
        super(context);
        this.mContext = context;
        handler = new MyHandler(context);
        Log.d(TAG,"LoadingDialog new()");
    }

    public LoadingDialog getDialog(String info) {
        Builder builder = new Builder(mContext);
        builder.setInfo(info);
        dialog = builder.create();
        if(!dialog.isShowing()){
            dialog.show();
        }
        dialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });
//        handler.postDelayed(new WeakRunnable(dialog), 100000);

        return dialog;
    }

    private static final class WeakRunnable implements Runnable {
        private WeakReference<Dialog> dialog;

        public WeakRunnable(Dialog dialog) {
            this.dialog = new WeakReference<Dialog>(dialog);
        }

        @Override
        public void run() {
            Dialog dialog = this.dialog.get();
            if (null != dialog && dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    public void dissMissDialog() {
        if(dialog!=null){
            dialog.dismiss();
        }
    }

    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {
        private Context context;
        private TextView txtInfo;
        private String info;

        public void setInfo(String info) {
            this.info = info;
        }

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * Create the custom dialog
         */
        public LoadingDialog create() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final LoadingDialog dialog = new LoadingDialog(context, R.style.Dialog);
            Log.d(TAG,"LoadingDialog create()");
            dialog.setCanceledOnTouchOutside(false);
//            dialog.setOnKeyListener(new OnKeyListener() {
//                @Override
//                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
//                        return true;
//                    } else {
//                        return false;
//                    }
//                }
//            });
            View layout = inflater.inflate(R.layout.view_login_dialog, null);
            txtInfo = (TextView) layout.findViewById(R.id.txt_info);
            if (info == null || info.equals("")) {
                txtInfo.setVisibility(View.GONE);
            } else {
                txtInfo.setText(info);
            }
            dialog.addContentView(layout, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            dialog.setContentView(layout);
            return dialog;
        }
    }

    public TimeOutLodingInterface timeOutLodingInterface;

    public interface TimeOutLodingInterface {
        public void timeOut();
    }


    public void setTimeOutLodingInterface(TimeOutLodingInterface timeOutLodingInterface) {
        this.timeOutLodingInterface = timeOutLodingInterface;
    }


}
