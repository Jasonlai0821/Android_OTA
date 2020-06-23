package com.xinshiyun.otaupgrade;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.xinshiyun.otaupgrade.upgrade.OTAUpgradeInfo;
import com.xinshiyun.otaupgrade.upgrade.OTAUpgradeManager;
import com.xinshiyun.otaupgrade.upgrade.OTAUpgradeManager.OTASTATE;

import java.util.ArrayList;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private OTAUpgradeManager mOTAUpgradeManager = null;
    private OTAUpgradeInfoTTSListener mOTAUpgradeInfoTTSListener = null;
    private OTAUpgradeNofityUIListener mOTAUpgradeNofityUIListener = null;

    private ImageView imageView = null;
    private LinearLayout linUpdate = null;
    private ProgressBar progressBar = null;
    private Button mBtnSure = null;
    private Button mBtnCancle = null;
    private int mState = OTASTATE.OTA_IDLE;

    //private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_main);
        initView();

        initPermissions();

        mBtnSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick() mState="+mState);
                switch (mState) {
                    case OTASTATE.OTA_QUERYREQUEST_SUCCESS:
                        mOTAUpgradeManager.startDownload();
                        break;
                    case OTASTATE.OTA_DOWNLOADSUCCESS:
                        //showViewInstall();
                        mOTAUpgradeManager.startInstall();
                        break;
                    case OTASTATE.OTA_QUERYREQUEST_FAILED:
                    case OTASTATE.OTA_INSTALLFAILED:
                        exitOta();
                        finish();
                        break;
                }
            }
        });
        mBtnCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mState){
                    case OTASTATE.OTA_QUERYREQUEST_SUCCESS:
                    case OTASTATE.OTA_DOWNLOADSUCCESS:
                    case OTASTATE.OTA_INSTALLFAILED:
                        exitOta();
                        finish();
                        break;
                }
            }
        });
    }

    private void showViewCheckUpdate()
    {
        Log.d(TAG, "showViewCheckUpdate()");
        imageView.setImageResource((R.drawable.check_update));
        linUpdate.setVisibility(View.GONE);
        mBtnSure.setVisibility(View.GONE);
        mBtnCancle.setVisibility(View.GONE);
    }

    private void showViewUpdating(int percentage)
    {
        Log.d(TAG, "showViewUpdating() percentage:"+percentage);
        mBtnSure.setVisibility(View.GONE);
        mBtnCancle.setVisibility(View.GONE);
        final int per = percentage;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imageView.setImageResource((R.drawable.updating));
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(per);
                progressBar.setMax(100);

            }
        }, 10);

        mState = OTASTATE.OTA_DOWNLOADING;

        if(per == 100){
            mState = OTASTATE.OTA_DOWNLOADSUCCESS;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBtnSure.setVisibility(View.VISIBLE);
                    mBtnCancle.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);

                }
            }, 10);
        }
    }

    private void showViewUpdateFailed()
    {
        Log.d(TAG, "showViewUpdateFailed()");
        mState = OTASTATE.OTA_INSTALLFAILED;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imageView.setImageResource((R.drawable.update_faile));
                linUpdate.setVisibility(View.VISIBLE);
                mBtnSure.setVisibility(View.VISIBLE);
                mBtnCancle.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }
        }, 0);
    }

    private void showViewNoNewVersion()
    {
        Log.d(TAG, "showViewNoNewVersion()");
        imageView.setImageResource((R.drawable.no_date));
        linUpdate.setVisibility(View.VISIBLE);
        mBtnSure.setVisibility(View.VISIBLE);
        mBtnCancle.setVisibility(View.GONE);
        mState = OTASTATE.OTA_QUERYREQUEST_FAILED;
    }

    private void showViewNewVersion()
    {
        Log.d(TAG, "showViewNewVersion()");
        imageView.setImageResource((R.drawable.can_update));
        linUpdate.setVisibility(View.VISIBLE);
        mBtnSure.setVisibility(View.VISIBLE);
        mBtnCancle.setVisibility(View.VISIBLE);
        mState = OTASTATE.OTA_QUERYREQUEST_SUCCESS;
    }

    private void showViewInstall()
    {
        Log.d(TAG, "showViewInstall()");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imageView.setImageResource((R.drawable.install));
                linUpdate.setVisibility(View.VISIBLE);
                mBtnSure.setVisibility(View.GONE);
                mBtnCancle.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);

            }
        }, 0);
        mState = OTASTATE.OTA_INSTALLING;
    }

    public class OTAUpgradeInfoTTSListener implements OTAUpgradeManager.OTAUpgradeInfoTTSCallback{
        @Override
        public void onSpeakOfflineText(String text) {
            onSpeakOfflineQuery(text);
        }
    }

    public class OTAUpgradeNofityUIListener implements OTAUpgradeManager.OTAUpgradeNofityUICallback{

        @Override
        public void onNofityState(int state, int percentage, OTAUpgradeInfo info) {
            Log.d(TAG, "onNofityState() state =" + state);
            switch (state){
                case OTASTATE.OTA_QUERYREQUEST_SUCCESS:
                    showViewNewVersion();
                    break;
                case OTASTATE.OTA_QUERYREQUEST_FAILED:
                    showViewNoNewVersion();
                    break;
                case OTASTATE.OTA_DOWNLOADING:
                    showViewUpdating(percentage);
                    break;
                case OTASTATE.OTA_DOWNLOADSUCCESS:
                    showViewUpdating(100);
                    break;
                case OTASTATE.OTA_DOWNLOADFAILED:
                    showViewUpdateFailed();
                    break;
                case OTASTATE.OTA_INSTALLING:
                    showViewInstall();
                    break;
                case OTASTATE.OTA_INSTALLSUCCESS:
                    break;
                case OTASTATE.OTA_INSTALLFAILED:
                    showViewUpdateFailed();
                    break;
            }
        }
    }

    public void onSpeakOfflineQuery(String text) {
        Log.d(TAG, "Speak text:" + text);
    }

    private void initPermissions() {
        final String[] permissions = {
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        final ArrayList<String> denies = new ArrayList<>();
        for (String perm : permissions) {
            if (PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                denies.add(perm);
                //进入到这里代表没有权限.
            }
        }
        if (!denies.isEmpty()) {
            final String tmpList[] = new String[denies.size()];
            ActivityCompat.requestPermissions(this, denies.toArray(tmpList), 10);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
    }

    public void initView()
    {
        Log.d(TAG, "initView()");
        imageView = findViewById(R.id.image);
        linUpdate = findViewById(R.id.lin_update);
        progressBar = findViewById(R.id.progress);
        //可更新确定按钮
        mBtnSure = findViewById(R.id.btn_sure);
        mBtnCancle = findViewById(R.id.btn_cancel);
        showViewCheckUpdate();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d(TAG, "onPostResume()");
        mOTAUpgradeManager =OTAUpgradeManager.getInstance(this);
        mOTAUpgradeInfoTTSListener = new OTAUpgradeInfoTTSListener();
        mOTAUpgradeManager.setOTAUpgradeInfoTTSCallback(mOTAUpgradeInfoTTSListener);
        mOTAUpgradeNofityUIListener = new OTAUpgradeNofityUIListener();
        mOTAUpgradeManager.setOTAUpgradeNofityUICallback(mOTAUpgradeNofityUIListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exitOta();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void exitOta()
    {
        Log.d(TAG, "exitOta()");
        if(mOTAUpgradeManager != null){
            mOTAUpgradeManager.stopDownload();
            mOTAUpgradeManager.setOTAUpgradeInfoTTSCallback(null);
            mOTAUpgradeManager.setOTAUpgradeNofityUICallback(null);
            mOTAUpgradeManager.deInstance();
            mOTAUpgradeManager = null;
        }
    }
}
