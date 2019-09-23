package com.suprema.svpsample;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.supremainc.sdk.callback.Event;
import com.supremainc.sdk.callback.Fingerprint;
import com.supremainc.sdk.callback.Input;
import com.supremainc.sdk.callback.Punch;
import com.supremainc.sdk.define.Card;
import com.supremainc.sdk.define.ErrorCode;
import com.supremainc.sdk.model.Finger;
import com.supremainc.sdk.model.FingerList;
import com.supremainc.sdk.model.FingerprintTemplate;
import com.supremainc.sdk.model.Versions;
import com.supremainc.sdk.option.CardOption;
import com.supremainc.sdk.option.FingerprintOption;
import com.supremainc.sdk.option.FirmwareOption;
import com.supremainc.sdk.service.DeviceListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity implements
        FingerprintFragment.OnFingerFragmentListener,
        UpgradeFragment.OnUpgradeFragmentListener,
        DeviceFragment.OnDeviceFragmentListener,
        ActivityCompat.OnRequestPermissionsResultCallback
{
    private static final String TAG = "SVP_Sample";

    public static final int SCAN_FINGER_DETECTED        = 0;
    public static final int SCAN_FINGER_IMAGE           = 1;
    public static final int SCAN_FINGER_COMPLETE        = 2;
    public static final int SCAN_FINGER_VERIFY_COMPLETE = 3;
    public static final int SCAN_CARD_COMPLETE          = 4;
    public static final int FINGER_IDENTIFIED           = 5;
    public static final int UPDATE_MONITORING           = 6;
    public static final int ERROR_DETECTED              = 7;
    public static final int SCAN_FINGER_UPDATE_COMPLETE = 8;
    public static final int FINGER_DELETE_IDENTIFIED    = 9;


    private static final int SOUND_AUTH_SUCCESS = 0;
    private static final int SOUND_AUTH_FAIL    = 1;
    private static final int SOUND_SCAN_FINGER  = 2;
    private static final int SOUND_SCAN_CARD    = 3;

    private static final int FINGER_TAB     = 0;
    private static final int CARD_TAB       = 1;
    private static final int OPTION_TAB     = 2;
    private static final int CONTROL_TAB    = 3;
    private static final int UPGRADE_TAB    = 4;
    private static final int DEVICE_TAB     = 5;
    private static final int MONITORING_TAB = 6;

    public static final int BUTTON_VERIFY       = 0;
    public static final int BUTTON_ENROLL       = 1;
    public static final int BUTTON_DELETE       = 2;
    public static final int BUTTON_UPDATE       = 3;
    public static final int BUTTON_SET_LIST     = 4;
    public static final int BUTTON_SET_USER     = 5;
    public static final int BUTTON_DELETE_ALL   = 6;
    public static final int BUTTON_UPGRADE      = 7;
    public static final int BUTTON_GET_FILE     = 8;
    public static final int BUTTON_SCAN_CARD    = 9;
    public static final int BUTTON_REBOOT       = 10;

    public SoundPool mSoundPool;
    public int[] mSoundId = new int[4];
    public static List<String> mEventArray = new ArrayList<>();
    private boolean mFirstScan = true;

    private DeviceListener mListener;
    private Toast mResultToast;
    private Bitmap mBitmap;

    @BindView(R.id.button1)
    Button mButton1;
    @BindView(R.id.button2)
    Button mButton2;
    @BindView(R.id.tab_list)
    TabLayout mListTab;
    @BindView(R.id.textSdkVersion)
    TextView mTextSdkVersion;
    @BindView(R.id.textFirmwareVersion)
    TextView mTextFirmwareVersion;

    private Map<String,User> mUserMap = new HashMap<>();
    private ArrayList<User> mUserArray = new ArrayList<>();

    private FragmentManager mFragmentManager = getSupportFragmentManager();
    private FingerprintFragment mFingerFragment = new FingerprintFragment();
    private CardFragment mCardFragment = new CardFragment();
    private OptionFragment mOptionFragment = new OptionFragment();
    private ControlFragment mControlFragment = new ControlFragment();
    private UpgradeFragment mUpgradeFragment = new UpgradeFragment();
    private MonitoringFragment mMonitoringFragment = new MonitoringFragment();
    private DeviceFragment mDeviceFragment = new DeviceFragment();

    private Finger mEnrollFinger = new Finger();

    private int mUniqueFingerId = 1000;

    private static class MainHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MainHandler(MainActivity activity)
        {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity act = mActivity.get();
            if(act != null) {
                switch (msg.what) {
                    case SCAN_FINGER_IMAGE:
                        act.fingerImageView();
                        break;
                    case SCAN_FINGER_COMPLETE:
                        act.enrollProgress(msg.obj);
                        break;
                    case SCAN_FINGER_VERIFY_COMPLETE:
                        act.scanFingerVerifyComplete(msg.obj);
                        break;
                    case SCAN_FINGER_UPDATE_COMPLETE:
                        act.updateProgress(msg.obj);
                        break;
                    case SCAN_CARD_COMPLETE:
                        act.scanCardComplete(msg.obj);
                        break;
                    case FINGER_IDENTIFIED:
                        act.setAuthResult(msg.arg1, msg.arg2);
                        break;
                    case UPDATE_MONITORING:
                        act.updateMonitoring();
                        break;
                    case ERROR_DETECTED:
                        act.showToastError(msg.arg1);
                        break;
                    case FINGER_DELETE_IDENTIFIED:
                        act.identifyComplete(msg.arg1, msg.arg2);
                        break;
                }
            }
        }
    }

    Handler mMainHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFragmentManager.beginTransaction().replace(R.id.fragment, mFingerFragment).commit();

        mMainHandler = new MainHandler(this);
        mResultToast = Toast.makeText(this, "identify success", Toast.LENGTH_SHORT);

        mButton1.setVisibility(View.INVISIBLE);
        mButton2.setVisibility(View.INVISIBLE);

        mListTab.addTab(mListTab.newTab().setText("Finger"));
        mListTab.addTab(mListTab.newTab().setText("Card"));
        mListTab.addTab(mListTab.newTab().setText("Option"));
        mListTab.addTab(mListTab.newTab().setText("Control"));
        mListTab.addTab(mListTab.newTab().setText("Upgrade"));
        mListTab.addTab(mListTab.newTab().setText("Device"));
        mListTab.addTab(mListTab.newTab().setText("Monitoring"));

        mEventArray.add("Start Monitoring");

        mListTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case FINGER_TAB:
                        controlService(false, true, false, false);
                        mFragmentManager.beginTransaction().replace(R.id.fragment, mFingerFragment).commit();
                        mFingerFragment.setAppUserCount("APP: " + mUserArray.size());
                        break;
                    case CARD_TAB:
                        mButton1.setText("scan"); mButton2.setText("clear");
                        controlService(true, false, true, true);
                        mFragmentManager.beginTransaction().replace(R.id.fragment, mCardFragment).commit();
                        break;
                    case OPTION_TAB:
                        mButton1.setText("set"); mButton2.setText("get");
                        controlService(false, false, true, true);
                        mFragmentManager.beginTransaction().replace(R.id.fragment, mOptionFragment).commit();
                        break;
                    case CONTROL_TAB:
                        controlService(false, false, false, false);
                        mFragmentManager.beginTransaction().replace(R.id.fragment, mControlFragment).commit();
                        break;
                    case UPGRADE_TAB:
                        controlService(false, false, false, false);
                        mFragmentManager.beginTransaction().replace(R.id.fragment, mUpgradeFragment).commit();
                        break;
                    case DEVICE_TAB:
                        controlService(true, true, false, false);
                        mFragmentManager.beginTransaction().replace(R.id.fragment, mDeviceFragment).commit();
                        break;
                    case MONITORING_TAB:
                        controlService(true, true, false, false);
                        mFragmentManager.beginTransaction().replace(R.id.fragment, mMonitoringFragment).commit();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        initSvpService();

        initSound();

        initVersion();

        initFingerList();

        initPermission();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public  void onDestroy() {
        SVP.manager.stop();
        super.onDestroy();
    }

    public void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    @OnClick({R.id.button1})
    public void onButton1Click(View view) {
        if( mListTab.getSelectedTabPosition() == OPTION_TAB ) {
            showToast(this, "Set option result - finger:" + setFingerOption() + ", card:" + setCardOption());
        }
        else if ( mListTab.getSelectedTabPosition() == CARD_TAB ) {
            showPopup(BUTTON_SCAN_CARD, R.string.scan_card, "scanCard");
            SVP.manager.scanCard();
        }
    }

    @OnClick({R.id.button2})
    public void onButton2Click(View view) {
        if( mListTab.getSelectedTabPosition() == OPTION_TAB ) {
            FingerprintOption fingerOption = new FingerprintOption();
            SVP.manager.getFingerprintOption(fingerOption);

            CardOption cardOption = new CardOption();
            SVP.manager.getCardOption(cardOption);

            showToast(this, "Get option success.");
        }
        else if ( mListTab.getSelectedTabPosition() == CARD_TAB ) {
            mCardFragment.setCardIDText("");
            mCardFragment.setCardTypeText("");
        }
    }

    @Override
    public void onFragmentButton(int button) {
        switch (button) {
            case BUTTON_VERIFY: // 1:1 Matching
                verifyScanFingerprint();
                break;
            case BUTTON_ENROLL:
                mFirstScan = true;
                enrollScanFingerprint();
                break;
            case BUTTON_UPDATE:
                mFirstScan = true;
                updateScanFingerprint();
                break;
            case BUTTON_DELETE:
                deleteScanFingerprint();
                break;
            case BUTTON_DELETE_ALL:
                deleteAllFinger();
                break;
            case BUTTON_SET_LIST:
                initFingerList();
                break;
            case BUTTON_SET_USER:
                setMaxUser();
                break;
            case BUTTON_UPGRADE:
                upgradeFirmware();
                break;
            case BUTTON_GET_FILE:
                getFirmwareFileList();
                break;
            case BUTTON_REBOOT:
                Switch useAutoStart = (Switch) findViewById(R.id.switchAutoRun);
                SVP.manager.setAutoStartApplication(useAutoStart.isChecked());

                showRebootPopup();
                break;
            default:
                break;
        }
    }

    private void showRebootPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to reboot ?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    SVP.manager.rebootDevice();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    private void initVersion() {
        Versions versions = new Versions();
        SVP.manager.getVersions(versions);

        mTextSdkVersion.setText("SDK: " + versions.sdkVersion);
        mTextFirmwareVersion.setText("FW: " + versions.firmwareVersion);
    }

    private int getFtpServerInfo(FirmwareOption option) {
        int result = ErrorCode.NO_ERROR;

        TextView editHost = findViewById(R.id.ftpHostEdit);
        TextView editPort = findViewById(R.id.ftpPortEdit);
        TextView editUser = findViewById(R.id.ftpUserNameEdit);
        TextView editPassword = findViewById(R.id.ftpPasswordEdit);

        if (editHost.getText().toString().isEmpty() ||
            editPort.getText().toString().isEmpty() ||
            editUser.getText().toString().isEmpty() ||
            editPassword.getText().toString().isEmpty() )
        {
            showToast(this, "Please enter the FTP server information");
            return result;
        }

        option.host = editHost.getText().toString();
        option.port = Integer.parseInt(editPort.getText().toString());
        option.username = editUser.getText().toString();
        option.password = editPassword.getText().toString();

        return ErrorCode.SUCCESS;
    }

    private int getFirmwareFileList() {
        int result = ErrorCode.NO_ERROR;
        FirmwareOption option = new FirmwareOption();
        ArrayList<String> fileList = new ArrayList<>();

        Switch useUsb = (Switch) findViewById(R.id.switchUsb);
        boolean isUSB = useUsb.isChecked();

        if (isUSB) {
            result = SVP.manager.getUsbFileList(fileList);
        }
        else {
            result = getFtpServerInfo(option);
            if (result != ErrorCode.SUCCESS)
                return result;

            SVP.manager.connectFtpServer(option);

            result = SVP.manager.getFtpFileList(fileList);

            SVP.manager.disconnectFtpServer();
        }

        if (result == ErrorCode.SUCCESS) {
            Spinner spinner = (Spinner)findViewById(R.id.firmwareNameSpinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item, fileList);

            spinner.setAdapter(adapter);
            spinner.setSelection(0);
        }

        return result;
    }

    private int upgradeFirmware() {
        Spinner spinner = (Spinner)findViewById(R.id.firmwareNameSpinner);
        if (spinner.getSelectedItem() == null) {
            showToast(this, "Select upgrade file");
            return 0;
        }

        FirmwareOption option = new FirmwareOption();
        int result = ErrorCode.NO_ERROR;

        option.fileName = spinner.getSelectedItem().toString();

        Switch useUsb = (Switch) findViewById(R.id.switchUsb);
        boolean isUSB = useUsb.isChecked();

        if(isUSB) {
            option.type = FirmwareOption.USB_UPGRADE;

            result = SVP.manager.upgradeFirmware(option);
        }
        else {
            option.type = FirmwareOption.FTP_UPGRADE;

            result = getFtpServerInfo(option);

            if (result != ErrorCode.SUCCESS)
                return result;

            result = SVP.manager.connectFtpServer(option);

            if (result == ErrorCode.SUCCESS) {
                result = SVP.manager.upgradeFirmware(option);
            }

            result = SVP.manager.disconnectFtpServer();
        }

        if (result == ErrorCode.SUCCESS) {
            showToast(this, "Firmware upgrade success");
            showRebootPopup();
        }
        else {
            showToast(this, "Firmware upgrade fail");
        }
        return result;
    }

    private void controlService(boolean runCardService, boolean runFingerService,
                                boolean showButton1, boolean showButton2) {
        if (runCardService)
            SVP.manager.resumeCardService();
        else
            SVP.manager.pauseCardService();

        if (runFingerService)
            SVP.manager.resumeFingerprintService();
        else
            SVP.manager.pauseFingerprintService();

        if (showButton1)
            mButton1.setVisibility(View.VISIBLE);
        else
            mButton1.setVisibility(View.INVISIBLE);

        if (showButton2)
            mButton2.setVisibility(View.VISIBLE);
        else
            mButton2.setVisibility(View.INVISIBLE);
    }

    private boolean validateUserId() {
        if (mFingerFragment.getUserId().length() == 0) {
            showToast(this, "Please input ID");
            return false;
        }
        return true;
    }

    private void enrollScanFingerprint() {
        if (validateUserId() == false)
            return;

        if (mFirstScan) {
            if (mUserMap.containsKey(mFingerFragment.getUserId())) {
                showToast(this, "Already registered user.");
                return;
            }

            showPopup(BUTTON_ENROLL, R.string.scan_finger, "scanFinger");
        }
        else {
            showPopup(BUTTON_ENROLL, R.string.rescan_finger, "scanFinger");
        }

        SVP.manager.scanFingerprint();
    }

    private void updateScanFingerprint() {
        if (validateUserId() == false)
            return;

        if (mFirstScan) {
            showPopup(BUTTON_UPDATE, R.string.scan_finger, "updateFinger");
        }
        else {
            showPopup(BUTTON_UPDATE, R.string.rescan_finger, "updateFinger");
        }

        SVP.manager.scanFingerprint();
    }

    private void verifyScanFingerprint() {
        if (validateUserId() == false)
            return;

        if (!mUserMap.containsKey(mFingerFragment.getUserId())) {
            showToast(this, mFingerFragment.getUserId() + " User is unregistered");
        }

        showPopup(BUTTON_VERIFY, R.string.verify_finger, "verifyFinger");

        SVP.manager.scanFingerprint();
    }

    private void deleteScanFingerprint() {
        showPopup(BUTTON_DELETE, R.string.scan_delete_finger, "deleteFinger");
    }

    private String findUserIDByFingerID(int fingerID) {
        Iterator it = mUserMap.values().iterator();
        String userID = "";

        while ((it.hasNext())) {
            User user = (User)it.next();
            for(int i =0 ; i < user.getNumOfFinger(); ++i) {
                if (user.getFingerID(i) == fingerID) {
                    userID = user.getUserID();
                    break;
                }
            }
        }
        return userID;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG,"onKeyDown - keyCode: " + keyCode);

        if (keyCode == 139) // Suprema Home Key
        {
            addEvent("[onKeyDown] clicked on home button");
        }

        return super.onKeyDown(keyCode, event);
    }

    public void fingerImageView() {
        if(mListTab.getSelectedTabPosition() == FINGER_TAB) {
            mFingerFragment.setImageView(mBitmap);
        }
    }

    public void enrollProgress(Object object) {
        FingerprintTemplate template = new FingerprintTemplate();
        template.setData((byte[])object,0);

        if (mFirstScan) {
            mEnrollFinger.clearData();

            mEnrollFinger.id = mUniqueFingerId++;
            mEnrollFinger.index = 0;
            mEnrollFinger.setTemplate(0,template.getTemplate());

            mFirstScan = false;
            enrollScanFingerprint();
        }
        else {
            mEnrollFinger.setTemplate(1,template.getTemplate());

            int result = SVP.manager.isSameFingerprint(mEnrollFinger.templates[0], mEnrollFinger.templates[1]);

            if (result == ErrorCode.SUCCESS) {

                result = SVP.manager.insertFinger(mEnrollFinger);

                Log.i(TAG, "Enroll Finger ID :" + mEnrollFinger.id + ", result:" + result);

                if (result == ErrorCode.SUCCESS) {
                    showToast(this, "Enroll success.");
                    addUser(mEnrollFinger);
                }
                else {
                    showToast(this, "Enroll fail. code:" + result);
                }
            }
            else {
                showToast(this, "Finger is not same.");
            }
        }
    }

    public void updateProgress (Object object) {
        FingerprintTemplate template = new FingerprintTemplate();
        template.setData((byte[])object,0);

        if (mFirstScan) {
            mEnrollFinger.clearData();

            User user = mUserMap.get(mFingerFragment.getUserId());

            mEnrollFinger.id = user.getFingerID(0); //
            mEnrollFinger.index = 0;
            mEnrollFinger.setTemplate(0,template.getTemplate());

            mFirstScan = false;
            updateScanFingerprint();
        }
        else {
            mEnrollFinger.setTemplate(1,template.getTemplate());

            int result = SVP.manager.isSameFingerprint(mEnrollFinger.templates[0], mEnrollFinger.templates[1]);

            if (result == ErrorCode.SUCCESS) {

                result = SVP.manager.updateFinger(mEnrollFinger);

                Log.i(TAG, "Update Finger ID :" + mEnrollFinger.id + ", result:" + result);

                if (result == ErrorCode.SUCCESS) {
                    showToast(this, "Update success.");
                    updateUser(mEnrollFinger);
                }
                else {
                    showToast(this, "Update fail.");
                }
            }
            else {
                showToast(this, "Finger is not same.");
            }
        }
    }

    public void showPopup(int type, int resourceId, String tag) {
        OneButtonDialog alertDialog = null;

        if (type == BUTTON_SCAN_CARD) {
            alertDialog = OneButtonDialog.newButtonDialog(resourceId, OneButtonDialog.CARD_DIALOG);
        }
        else if (type == BUTTON_ENROLL || type == BUTTON_UPDATE ||
                type == BUTTON_VERIFY || type == BUTTON_DELETE) {
            alertDialog = OneButtonDialog.newButtonDialog(resourceId, OneButtonDialog.FINGER_DIALOG);
        }

        if (alertDialog != null)
            alertDialog.show(mFragmentManager, tag);
    }

    public void hidePopup() {
        if (mFragmentManager.findFragmentByTag("scanCard") != null) {
            ((OneButtonDialog) mFragmentManager.findFragmentByTag("scanCard")).dismiss();
        }

        if (mFragmentManager.findFragmentByTag("scanFinger") != null) {
            ((OneButtonDialog) mFragmentManager.findFragmentByTag("scanFinger")).dismiss();
        }

        if (mFragmentManager.findFragmentByTag("verifyFinger") != null) {
            ((OneButtonDialog) mFragmentManager.findFragmentByTag("verifyFinger")).dismiss();
        }

        if (mFragmentManager.findFragmentByTag("updateFinger") != null) {
            ((OneButtonDialog) mFragmentManager.findFragmentByTag("updateFinger")).dismiss();
        }
    }

    public void showToastError(int result) {
        if (result == ErrorCode.ERR_EXTRACTION_FAIL_FINGERPRINT) {
            showToast(this, "Cannot extract template data.");
        }
        else if (result == ErrorCode.ERR_CANNOT_SCAN_FINGERPRINT) {
            showToast(this, "Cannot scan fingerprint.");
        }
        else if (result == ErrorCode.ERR_FINGERPRINT_SCAN_CANCELLED) {
            showToast(this, "Fingerprint scan canceled.");
        }
        else if (result == ErrorCode.ERR_CANNOT_SCAN_FINGERPRINT) {
            showToast(this, "Cannot scan fingerprint.");
        }
        else if (result == ErrorCode.ERR_SCAN_FEEDBACK_NON_CENTER_POSITION) {
            showToast(this, "Place your finger on the center of the sensor.");
        }
        else if (result == ErrorCode.ERR_SCAN_FEEDBACK_NON_FLAT) {
            showToast(this, "Place your finger flat against the sensor.");
        }
        else if (result == ErrorCode.ERR_SCAN_FEEDBACK_TOO_HUMID) {
            showToast(this, "Press slightly lighter and/or dry your finger.");
        }
        else if (result == ErrorCode.ERR_SCAN_FEEDBACK_TOO_DRY) {
            showToast(this, "Press slightly harder and/or breathe on your finger.");
        }
        else if (result == ErrorCode.ERR_SCAN_FEEDBACK_INTERNAL_ERROR) {
            showToast(this, "Scan finger internal error.");
        }
        else {
            showToast(this, "Unknown error - " + result);
        }

        hidePopup();
    }

    public void scanFingerVerifyComplete(Object object) {
        User user = mUserMap.get(mFingerFragment.getUserId());
        Finger finger = new Finger();
        finger.setTemplate(0, (byte[])object);

        if (user != null) {
            if (SVP.manager.verifyFingerprint(user.getFingerList(), finger) == ErrorCode.SUCCESS) {
                showToast(this, "Verify success.");
                soundPlay(SOUND_AUTH_SUCCESS);
            }
            else {
                showToast(this, "Verify fail.");
                soundPlay(SOUND_AUTH_FAIL);
            }
        }
        else {
            showToast(this, "Invalid user id.");
            soundPlay(SOUND_AUTH_FAIL);
        }
    }

    public void scanCardComplete(Object object) {
        soundPlay(SOUND_SCAN_CARD);

        if( mListTab.getSelectedTabPosition() == CARD_TAB ) {
            Punch punch = (Punch)object;
            if (punch.type == Punch.PUNCH_TYPE_WIEGAND)
                punch.type = Card.CARD_TYPE_WIEGAND;

            CharSequence[] cardType = {"Unknown","CSN","Secure","Access" ,"Wiegand"};

            mCardFragment.setCardIDText(punch.displayString);
            mCardFragment.setCardTypeText(cardType[punch.type]);
        }
    }

    public void identifyComplete(int result, int id) {
        Fragment frag = mFragmentManager.findFragmentByTag("deleteFinger");

        if (frag != null) {
            ((OneButtonDialog)frag).dismiss();

            if (result == ErrorCode.SUCCESS) {
                Finger finger = new Finger();
                finger.id = id;

                int deleteResult = SVP.manager.deleteFinger(finger);
                if (deleteResult == ErrorCode.SUCCESS) {
                    showToast(this, "Delete success");

                    deleteUser(id);
                }
                else {
                    showToast(this, "Delete fail");
                }
            }
            else {
                showToast(this, "Unregistered finger");
            }
        }
    }

    public void setAuthResult(int result, int id) {
        Log.i(TAG,"identify result:" + result +", index:"+ id);

        mResultToast.cancel();
        mResultToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);

        if (result == ErrorCode.SUCCESS) {
            soundPlay(SOUND_AUTH_SUCCESS);
            mResultToast.setText("id:" + findUserIDByFingerID(id) + ", user identify success");
        }
        else {
            soundPlay(SOUND_AUTH_FAIL);
            mResultToast.setText("identify fail");
        }
        mResultToast.show();
    }

    private void addUser(Finger enrollFinger) {
        String userID = mFingerFragment.getUserId();
        Finger finger = null;

        try {
            finger = (Finger)enrollFinger.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        User user = new User(userID);
        user.addFinger(finger);

        mUserMap.put(userID, user);
        mUserArray.add(user);

        mFingerFragment.setAppUserCount("APP: " + mUserArray.size());
        mFingerFragment.setSdkUserCount("SDK: " + mUserArray.size());

        Log.i(TAG, "[Enroll] user id:" + userID + ", finger id:" + finger.id);
    }

    private void updateUser(Finger updateFinger) {
        String userID = mFingerFragment.getUserId();

        Finger finger = null;
        try {
            finger = (Finger)updateFinger.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        User user = mUserMap.get(userID);
        user.updateFinger(finger);

        Log.i(TAG, "[Update] user id:" + userID + ", finger id:" + finger.id);
    }

    private void deleteUser(int fingerId) {
        mUserMap.remove(findUserIDByFingerID(fingerId));

        Iterator itr = mUserArray.iterator();
        while (itr.hasNext())
        {
            User user = (User)itr.next();
            for(int i =0 ; i < user.getNumOfFinger(); ++i) {
                if (user.getFingerID(i) == fingerId) {
                    Log.i(TAG, "[Delete] user id:" + user.getUserID() + ", finger id:" + fingerId);
                    itr.remove();
                    break;
                }
            }
        }

        mFingerFragment.setAppUserCount("APP: " + mUserArray.size());
        mFingerFragment.setSdkUserCount("SDK: " + mUserArray.size());
    }

    public int setMaxUser() {
        int userCount = mUserMap.size();
        if( userCount == 0) {
            showToast(this, "Please erroll user.");
            return -1;
        }

        FingerList list = new FingerList();
        for(int i = 0; i < Finger.MAX_NUM_OF_FINGER; i++) {
            Finger finger = new Finger();
            finger.id = i + 1;
            finger.index = 0;

            int index = (int)(Math.random() * userCount);

            finger.setTemplate(0, mUserArray.get(index).getFinger(0).getTemplate(0));
            finger.setTemplate(1, mUserArray.get(index).getFinger(0).getTemplate(1));

            list.addFinger(finger);
        }

        int result = SVP.manager.setFingerList(list);

        mFingerFragment.setSdkUserCount("SDK: " + list.fingers.size());

        return result;
    }

    public int deleteAllFinger() {
        int result = SVP.manager.deleteAllFinger();
        if (result == ErrorCode.SUCCESS) {
            showToast(this, "Delete all success");
        }

        mFingerFragment.setSdkUserCount("SDK: 0");
        return result;
    }

    public int initPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 110);
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 110);
            }
        }

        return permissionCheck;
    }

    public int initFingerList() {
        FingerList fingerList = new FingerList();
        Iterator it = mUserMap.values().iterator();

        while ((it.hasNext())) {
            User user = (User)it.next();
            for(int i =0 ; i < user.getNumOfFinger(); ++i) {
                fingerList.addFinger(user.getFinger(i));
            }
        }

        mFingerFragment.setSdkUserCount("SDK: " + fingerList.fingers.size());

        int result = SVP.manager.setFingerList(fingerList);

        if (result == ErrorCode.SUCCESS) {
            showToast(this, "Set finger list. count : " + fingerList.fingers.size());
        }
        else {
            showToast(this, "Set finger list fail.");
        }

        return result;
    }

    public int setFingerOption() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        Log.d(TAG, "sharedPreferences: " + pref.getAll());

        FingerprintOption option = new FingerprintOption();

        String value = "";

        value = pref.getString("pref_finger_security_level", getString(R.string.menu_finger_security_level_default));
        if (value.equals("Normal"))             option.securityLevel = FingerprintOption.SECURITY_NORMAL;
        else if (value.equals("Secure"))        option.securityLevel = FingerprintOption.SECURITY_SECURE;
        else if (value.equals("More Secure"))   option.securityLevel = FingerprintOption.SECURITY_MORE_SECURE;

        value = pref.getString("pref_finger_fast_mode", getString(R.string.menu_finger_fast_default));
        if (value.equals("Auto"))           option.fastMode = FingerprintOption.FAST_MODE_AUTO;
        else if (value.equals("Normal"))    option.fastMode = FingerprintOption.FAST_MODE_NORMAL;
        else if (value.equals("Faster"))    option.fastMode = FingerprintOption.FAST_MODE_FASTER;
        else if (value.equals("Fastest"))   option.fastMode = FingerprintOption.FAST_MODE_FASTEST;

        value = pref.getString("pref_finger_sensitivity", getString(R.string.menu_finger_sensitive_default));
        option.sensitivity = Integer.parseInt(value);

        value = pref.getString("pref_finger_sensor_mode", getString(R.string.menu_finger_sensor_mode_default));
        if (value.equals("Always On"))      option.sensorMode = FingerprintOption.SENSOR_MODE_ALWAYS_ON;
        else if (value.equals("Proximity")) option.sensorMode = FingerprintOption.SENSOR_MODE_PROXIMITY;

        value = pref.getString("pref_finger_template_type", getString(R.string.menu_finger_template_type_default));
        if (value.equals("Suprema"))        option.templateFormat = FingerprintOption.TEMPLATE_FORMAT_SUPREMA;
        else if (value.equals("ISO"))       option.templateFormat = FingerprintOption.TEMPLATE_FORMAT_ISO;
        else if (value.equals("ANSI"))      option.templateFormat = FingerprintOption.TEMPLATE_FORMAT_ANSI;

        value = pref.getString("pref_finger_scan_timeout", getString(R.string.menu_finger_scan_timeout_default));
        option.scanTimeout = Integer.parseInt(value);

        value = pref.getString("pref_finger_lfd_level", getString(R.string.menu_finger_lfd_level_default));
        if (value.equals("OFF"))            option.lfdLevel = FingerprintOption.LFD_LEVEL_OFF;
        else if (value.equals("Low"))       option.lfdLevel = FingerprintOption.LFD_LEVEL_LOW;
        else if (value.equals("Middle"))    option.lfdLevel = FingerprintOption.LFD_LEVEL_MIDDLE;
        else if (value.equals("High"))      option.lfdLevel = FingerprintOption.LFD_LEVEL_HIGH;

        option.useAdvancedEnrollment = pref.getBoolean("pref_finger_advanced_enrollment",false);

        option.useBitmapImage = pref.getBoolean("pref_finger_image",false);

        int result = SVP.manager.setFingerprintOption(option);
        return result;
    }

    public int setCardOption() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        CardOption option = new CardOption();
        String value = "";

        value = pref.getString("pref_card_scan_timeout", getString(R.string.menu_card_scan_timeout_default));
        option.scanTimeout = Integer.parseInt(value);

        value = pref.getString("pref_card_byte_order", getString(R.string.menu_card_byte_order_default));
        if (value.equals("MSB"))        option.byteOrder = CardOption.BYTE_ORDER_MSB;
        else if (value.equals("LSB"))   option.byteOrder = CardOption.BYTE_ORDER_LSB;

        int result = SVP.manager.setCardOption(option);
        return result;
    }

    private void soundPlay(int soundIndex) {
        if(mSoundId[soundIndex] != 0) {
            mSoundPool.play(mSoundId[soundIndex], 0.5f, 0.5f, 0, 0, 1f);
        }
    }

    private boolean initSound() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        mSoundPool = new SoundPool.Builder().setAudioAttributes(audioAttributes).setMaxStreams(1).build();

        mSoundId[SOUND_AUTH_SUCCESS] = mSoundPool.load(MainActivity.this, R.raw.auth_success, 0);
        mSoundId[SOUND_AUTH_FAIL] = mSoundPool.load(MainActivity.this, R.raw.auth_fail, 0);
        mSoundId[SOUND_SCAN_CARD] = mSoundPool.load(MainActivity.this, R.raw.card_read, 0);
        mSoundId[SOUND_SCAN_FINGER] = mSoundPool.load(MainActivity.this, R.raw.finger_scan, 0);

        return true;
    }

    private void addEvent(String event) {
        if (mEventArray.size() >= 1000){
            mEventArray.clear();
        }

        mEventArray.add(event);
        mMainHandler.sendEmptyMessage(UPDATE_MONITORING);
    }

    public void updateMonitoring() {
        if( mListTab.getSelectedTabPosition() == MONITORING_TAB ) {
            mMonitoringFragment.updateListView();
        }
    }

    private boolean initSvpService() {
        mListener = new DeviceListener() {
            @Override
            public void onFingerprintDetected(Fingerprint data) {
                soundPlay(SOUND_SCAN_FINGER);

                Log.i(TAG, "onFingerprintDetected");
                Log.i(TAG, "templateType:" + data.templateType);
                Log.i(TAG, "templateSize:" + data.templateSize);
                Log.i(TAG, "time :" + data.time);

                addEvent("[onFingerprintDetected] result: " + data.result);

                if(data.image != null) {
                    Log.i(TAG,"fingerprint bitmap image.");
                    mBitmap = data.image;
                    mMainHandler.sendEmptyMessage(SCAN_FINGER_IMAGE);
                }
            }

            @Override
            public void onFingerprintIdentified(Fingerprint data) {
                Log.i(TAG, "onFingerprintIdentified");
                Log.i(TAG, "result:" + data.result);
                Log.i(TAG, "id:" + data.id);
                Log.i(TAG, "templateSize:" + data.templateSize);
                Log.i(TAG, "quality:" + data.quality);
                Log.i(TAG, "time :" + data.time);

                addEvent("[onFingerprintIdentified] result: " + data.result + ", id:" + data.id);

                Message message = mMainHandler.obtainMessage();
                Fragment frag = mFragmentManager.findFragmentByTag("deleteFinger");
                if (frag == null)
                    message.what = FINGER_IDENTIFIED;
                else
                    message.what = FINGER_DELETE_IDENTIFIED;

                message.arg1 = data.result;
                message.arg2 = data.id;

                mMainHandler.sendMessage(message);
            }

            @Override
            public void onPunchDetected(Punch data) {
                Log.i(TAG, "onPunchDetected");
                Log.i(TAG, "result :" + data.result);
                Log.i(TAG, "type :" + data.type);
                Log.i(TAG, "time :" + data.time);
                Log.i(TAG, "data: " + Arrays.toString(data.data));
                Log.i(TAG, "hexString: " + data.toHexString());
                Log.i(TAG, "displayString :" + data.displayString);
                Log.i(TAG, "dataLength :" + data.dataLength);

                addEvent("[onPunchDetected] data: " + data.displayString);

                Message message = mMainHandler.obtainMessage();
                message.what = SCAN_CARD_COMPLETE;
                message.obj = data;

                mMainHandler.sendMessage(message);
            }

            @Override
            public void onFingerprintScanCompleted(Fingerprint data) {
                Log.i(TAG, "onFingerprintScanCompleted ");
                Log.i(TAG, "result :" + data.result);
                Log.i(TAG, "templateSize :" + data.templateSize);

                addEvent("[onFingerprintScanCompleted]");

                switch (data.result)
                {
                    case ErrorCode.ERR_FINGERPRINT_SCAN_TIMEOUT: {
                        hidePopup();
                    }
                    break;
                    case ErrorCode.SUCCESS: {
                        Message msg = Message.obtain();
                        msg.obj = data.template;

                        if (mFragmentManager.findFragmentByTag("scanFinger") != null) {
                            msg.what = SCAN_FINGER_COMPLETE;
                        }
                        else if (mFragmentManager.findFragmentByTag("verifyFinger") != null) {
                            msg.what = SCAN_FINGER_VERIFY_COMPLETE;
                        }
                        else if (mFragmentManager.findFragmentByTag("updateFinger") != null) {
                            msg.what = SCAN_FINGER_UPDATE_COMPLETE;
                        }

                        mMainHandler.sendMessage(msg);

                        hidePopup();
                    }
                    break;
                    default: {
                        Log.i(TAG,"onFingerprintScanCompleted Fail..." + data.result);

                        hidePopup();

                        Message msg = Message.obtain();
                        msg.what = ERROR_DETECTED;
                        msg.arg1 = data.result;
                        mMainHandler.sendMessage(msg);
                    }
                    break;
                }
            }

            @Override
            public void onCardScanCompleted(Punch data) {
                Log.i(TAG,"onCardScanCompleted");
                Log.i(TAG,"displayString :" + data.displayString);
                Log.i(TAG,"result :" + data.result);
                Log.i(TAG,"type :" + data.type);
                Log.i(TAG,"time :" + data.time);

                addEvent("[onCardScanCompleted] id: " + data.displayString);

                hidePopup();

                Message msg = Message.obtain();
                msg.obj = data;
                msg.what = SCAN_CARD_COMPLETE;
                mMainHandler.sendMessage(msg);
            }

            @Override
            public void onInputDetected(Input data) {
                Log.i(TAG,"onInputDetected");
                Log.i(TAG,"type :" + data.type);
                Log.i(TAG,"port :" + data.port);
                Log.i(TAG,"status :" + data.status);

                addEvent("[onInputDetected] type: " + data.type + ", port:" + data.port + ", status:" + data.status);
            }

            @Override
            public void onEventDetected(Event data) {
                Log.i(TAG, "onEventDetected");
                Log.i(TAG, "result:" + data.result);
                Log.i(TAG, "code:" + data.code);

                addEvent("[onInputDetected] result: " + data.result + ", code:" + data.code);
            }
        };

        SVP.manager.initialize(this, mListener);

        SVP.manager.run();

        Log.i(TAG,"Start SDK !!");

        return true;
    }
}





