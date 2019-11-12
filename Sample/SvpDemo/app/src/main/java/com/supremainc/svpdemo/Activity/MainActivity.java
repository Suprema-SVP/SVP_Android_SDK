package com.supremainc.svpdemo.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.supremainc.sdk.define.Channel;
import com.supremainc.sdk.define.Relay;
import com.supremainc.svpdemo.DatabaseHelper;
import com.supremainc.svpdemo.Dialog.OneButtonDialog;
import com.supremainc.svpdemo.Dialog.AuthDialog;
import com.supremainc.svpdemo.R;
import com.supremainc.svpdemo.SVP;
import com.supremainc.svpdemo.Data.User;
import com.supremainc.sdk.callback.Event;
import com.supremainc.sdk.callback.Fingerprint;
import com.supremainc.sdk.callback.Input;
import com.supremainc.sdk.callback.Punch;
import com.supremainc.sdk.define.ErrorCode;
import com.supremainc.sdk.model.Finger;
import com.supremainc.sdk.model.FingerprintTemplate;
import com.supremainc.sdk.service.DeviceListener;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "SvpDemo";

    public static final int SCAN_FINGER_COMPLETED   = 1;
    public static final int SCAN_CARD_COMPLETED = 2;
    public static final int FINGER_IDENTIFIED = 3;
    public static final int CARD_IDENTIFIED = 4;
    public static final int ERROR_DETECTED = 5;
    public static final int SCAN_FINGER_UPDATE_COMPLETED = 6;
    public static final int FINGER_DELETE_IDENTIFIED = 7;

    private static final int SOUND_AUTH_SUCCESS     = 0;
    private static final int SOUND_AUTH_FAIL        = 1;
    private static final int SOUND_SCAN_FINGER      = 2;
    private static final int SOUND_SCAN_CARD        = 3;

    public static final String SETTINGS = "android.settings.SETTINGS";
    public static final String SCREENSAVER_SETTINGS = "android.settings.DREAM_SETTINGS";

    public SoundPool mSoundPool;
    public int[] mSoundId = new int[4];

    public FragmentManager mFragmentManager = getSupportFragmentManager();
    private Finger mEnrollFinger = new Finger();

    public int mUniqueFingerId = 0;
    public int mUserCount = 0;

    private Button mBtnOpen;
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;

    private Handler mMainHandler = new MainHandler(this);

    public static MainActivity mMainActivity;

    public final DatabaseHelper mUserDatabase = new DatabaseHelper(this);

    private DeviceListener mListener = new DeviceListener() {

        @Override
        public void onFingerprintDetected(Fingerprint data) {
            soundPlay(SOUND_SCAN_FINGER);

            Log.i(TAG, "[onFingerprintDetected]");
            Log.i(TAG, "templateType:" + data.templateType);
            Log.i(TAG, "templateSize:" + data.templateSize);
            Log.i(TAG, "time :" + data.time);
        }

        @Override
        public void onFingerprintIdentified(Fingerprint data) {
            Log.i(TAG, "[onFingerprintIdentified]");
            Log.i(TAG, "result:" + data.result);
            Log.i(TAG, "id:" + data.id);
            Log.i(TAG, "templateSize:" + data.templateSize);
            Log.i(TAG, "quality:" + data.quality);
            Log.i(TAG, "time :" + data.time);

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
        public void onFingerprintScanCompleted(Fingerprint data) {
            Log.i(TAG, "[onFingerprintScanCompleted]");
            Log.i(TAG, "result :" + data.result);
            Log.i(TAG, "templateSize :" + data.templateSize);

            AddUserActivity.mAddUserActivity.fingerprintScanCompleted(data);
        }

        @Override
        public void onPunchDetected(Punch data) {
            soundPlay(SOUND_SCAN_CARD);

            Log.i(TAG, "[onPunchDetected]");
            Log.i(TAG, "displayString :" + data.displayString);
            Log.i(TAG, "result :" + data.result);
            Log.i(TAG, "type :" + data.type);
            Log.i(TAG, "time :" + data.time);

            switch (data.type) {
                case Punch.PUNCH_TYPE_MAGNETIC:
                case Punch.PUNCH_TYPE_CSN: {
                    Message message = mMainHandler.obtainMessage();
                    message.what = CARD_IDENTIFIED;
                    message.obj = data;
                    mMainHandler.sendMessage(message);
                    break;
                }
                case Punch.PUNCH_TYPE_WIEGAND: {
                    break;
                }
                case Punch.PUNCH_TYPE_ACCESS: {
                    break;
                }
            }
        }

        @Override
        public void onCardScanCompleted(Punch data) {
            Log.i(TAG, "[onCardScanCompleted]");
            Log.i(TAG, "displayString :" + data.displayString);
            Log.i(TAG, "result :" + data.result);
            Log.i(TAG, "type :" + data.type);
            Log.i(TAG, "time :" + data.time);

            Message msg = Message.obtain();
            msg.obj = data;
            msg.what = SCAN_CARD_COMPLETED;
            mMainHandler.sendMessage(msg);
        }

        @Override
        public void onInputDetected(Input data) {
            Log.i(TAG, "[onInputDetected]");
            Log.i(TAG, "type :" + data.type);
            Log.i(TAG, "port :" + data.port);
            Log.i(TAG, "status :" + data.status);
        }

        @Override
        public void onEventDetected(Event data) {
            Log.i(TAG, "[onEventDetected]");
            Log.i(TAG, "result:" + data.result);
            Log.i(TAG, "code:" + data.code);
        }
    };


    public static class MainHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MainHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity act = mActivity.get();
            if (act != null) {
                switch (msg.what) {
                    case SCAN_FINGER_COMPLETED:
                        act.enrollProgress(msg.obj, UserListActivity.ADD_USER);
                        break;
                    case SCAN_FINGER_UPDATE_COMPLETED:
                        act.enrollProgress(msg.obj, UserListActivity.DETAIL_USER);
                        break;
                    case SCAN_CARD_COMPLETED:
                        act.scanCardComplete(msg.obj);
                        break;
                    case FINGER_IDENTIFIED:
                        act.setFingerAuthResult(msg.arg1, msg.arg2);
                        break;
                    case CARD_IDENTIFIED:
                        act.setCardAuthResult(msg.obj);
                        break;
                    case ERROR_DETECTED:
                        act.showToast(msg.arg1);
                        break;
                    case FINGER_DELETE_IDENTIFIED:
                        act.identifyComplete(msg.arg1, msg.arg2);
                        break;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mDrawerLayout = findViewById(R.id.drawerLayout);

        navigationView = findViewById(R.id.drawer);
        navigationView.setNavigationItemSelectedListener(this);

        mMainActivity = this;
        mBtnOpen = findViewById(R.id.btnOpenDrawer);
        mBtnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(navigationView);
            }
        });

        initSvpService();
        initSound();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_user_list) {
            Intent intent = new Intent(MainActivity.this, UserListActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        }
        else if (id == R.id.nav_option) {
            Intent intent = new Intent(MainActivity.this, OptionActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        }
        else if (id == R.id.nav_screen_saver) {
            Intent intent = new Intent(SCREENSAVER_SETTINGS);
            if (!intentAvailable(intent)) {
                // Try opening the daydream settings activity directly: https://gist.github.com/reines/bc798a2cb539f51877bb279125092104
                intent = new Intent(Intent.ACTION_MAIN).setClassName(
                        "com.android.tv.settings",
                        "com.android.tv.settings.device.display.daydream.DaydreamActivity");
                if (!intentAvailable(intent)) {
                    // If all else fails, open the normal settings screen
                    intent = new Intent(SETTINGS);
                }
            }

            startActivity(intent);
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean intentAvailable(Intent intent) {
        PackageManager manager = this.getPackageManager();
        List<ResolveInfo> infos = manager.queryIntentActivities(intent, 0);
        return !infos.isEmpty();
    }

    private void soundPlay(int soundIndex) {
        if (mSoundId[soundIndex] != 0) {
            AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int currentVolume = audio.getStreamVolume(AudioManager.STREAM_RING);
            //0~7 => 0.0 ~ 1.0
            float playVolume = currentVolume * 1.42f;
            Log.i(TAG, "currentVolume " + currentVolume + ", volume: " + playVolume);
            mSoundPool.play(mSoundId[soundIndex], playVolume, playVolume, 0, 0, 1f);
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

    public void sendCommandMessage(Message msg) {
        mMainHandler.sendMessage(msg);
    }

    public void enrollProgress(Object object, int state) {
        FingerprintTemplate template = new FingerprintTemplate();
        template.setData((byte[]) object, 0);

        if (SVP.firstScan) {
            mEnrollFinger.clearData();
            mEnrollFinger.index = 0;
            mEnrollFinger.setTemplate(0, template.getTemplate());
            SVP.firstScan = false;

            if (state == UserListActivity.ADD_USER) {
                mEnrollFinger.id = ++mUniqueFingerId;
                AddUserActivity.mAddUserActivity.scanFingerprint("ENROLL");
            }
            else {
                String userId = AddUserActivity.mAddUserActivity.getActivityUserId();
                mEnrollFinger.id = SVP.userMap.get(userId).getFingerID(0);
                AddUserActivity.mAddUserActivity.scanFingerprint("UPDATE");
            }
        }
        else {
            mEnrollFinger.setTemplate(1, template.getTemplate());

            if (SVP.manager.isSameFingerprint(
                    mEnrollFinger.templates[0], mEnrollFinger.templates[1]) == ErrorCode.SUCCESS) {
                Log.i(TAG, "Enroll Finger ID :" + mEnrollFinger.id);

                SVP.newFinger = addFinger(mEnrollFinger);
                AddUserActivity.mAddUserActivity.changeStateColor();

                showToast(ErrorCode.SUCCESS);
            }
            else {
                showToast(ErrorCode.ERR_NOT_SAME_FINGERPRINT);
            }
        }
    }

    private Finger addFinger(Finger enrollFinger) {
        Finger finger = null;
        try {
            finger = (Finger) enrollFinger.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "[addFinger] finger id:" + finger.id);
        return finger;
    }

    public void afterSuccessProcess() {
        // Open Door
        SVP.manager.executeOutputAction(Channel.RELAY_PORT_0, Relay.ON);
        SVP.manager.executeOutputAction(Channel.RELAY_PORT_1, Relay.ON);

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                // Close Door
                SVP.manager.executeOutputAction(Channel.RELAY_PORT_0, Relay.OFF);
                SVP.manager.executeOutputAction(Channel.RELAY_PORT_1, Relay.OFF);
            }
        }, 1500);
    }

    private void setFingerAuthResult(int result, int id) {
        Log.i(TAG, "identify result:" + result + ", index:" + id);
        Intent intent = new Intent(MainActivity.this, AuthDialog.class);
        if (result == ErrorCode.SUCCESS) {
            soundPlay(SOUND_AUTH_SUCCESS);

            User user = findUserByFingerID(id);
            Log.i(TAG, user.getUserName() + "  " + user.getFingerID(0));

            intent.putExtra("id", user.getUserID());
            intent.putExtra("name", user.getUserName());
            startActivity(intent);

            afterSuccessProcess();
        }
        else {
            soundPlay(SOUND_AUTH_FAIL);
            intent.putExtra("id", "");
            intent.putExtra("name", "");
            startActivity(intent);
        }
    }

    private void setCardAuthResult(Object object) {
        boolean findCard = false;
        Punch punch = (Punch) object;
        String cardNum = punch.displayString;

        Iterator it = SVP.userArray.iterator();
        User user = null;
        while (it.hasNext()) {
            user = (User) it.next();
            if (cardNum.equals(user.getCardNumber())) {
                findCard = true;
                break;
            }
        }
        Intent intent = new Intent(MainActivity.this, AuthDialog.class);
        if (findCard) {
            soundPlay(SOUND_AUTH_SUCCESS);
            intent.putExtra("id", user.getUserID());
            intent.putExtra("name", user.getUserName());
            startActivity(intent);

            afterSuccessProcess();
        }
        else {
            soundPlay(SOUND_AUTH_FAIL);
            intent.putExtra("id", "");
            intent.putExtra("name", "");
            startActivity(intent);
        }
    }

    private User findUserByFingerID(int fingerID) {
        Iterator it = SVP.userMap.values().iterator();
        User user;
        while (it.hasNext()) {
            user = (User) it.next();
            for (int i = 0; i < user.getNumOfFinger(); ++i) {
                if (user.getFingerID(i) == fingerID) {
                    return user;
                }
            }
        }
        return null;
    }

    public void identifyComplete(int result, int id) {
        Fragment frag = mFragmentManager.findFragmentByTag("deleteFinger");

        if (frag != null) {
            ((OneButtonDialog) frag).dismiss();

            if (result == ErrorCode.SUCCESS) {
                Finger finger = new Finger();
                finger.id = id;

                int deleteResult = SVP.manager.deleteFinger(finger);
                if (deleteResult == ErrorCode.SUCCESS) {
                    Toast.makeText(this, "Delete Success", Toast.LENGTH_SHORT).show();
                    SVP.userMap.remove(findUserByFingerID(id).getUserID());
                }
                else {
                    Log.i(TAG, "delete fail..?");
                }
            }
            else {
                Toast.makeText(this, "Unregistered finger", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void scanCardComplete(Object object) {
        soundPlay(SOUND_SCAN_CARD);
        AddUserActivity.mAddUserActivity.setCardState((Punch) object);
    }

    private boolean initSvpService() {

        SVP.manager.initialize(this, mListener);

        //SVP.manager.setCardType(Card.RF_LOW_FREQUENCY);
        //SVP.manager.setCardType(Card.RF_HIGH_FREQUENCY);

        SVP.manager.run();

        //SVP.manager.setAutoStartApplication(true);

        SVP.manager.deleteAllFinger();
        SVP.userMap.clear();

        SVP.userArray = mUserDatabase.getAll();

        Log.d(TAG, "User List ======>");

        int maxFingerID = 0, curFingerID = 0;

        for (User user : SVP.userArray) {
            Log.d(TAG, "UserID: " + user.getUserID());
            Log.d(TAG, "UserName: " + user.getUserName());
            Log.d(TAG, "NumOfFinger: " + user.getNumOfFinger());
            Log.d(TAG, "CardID: " + user.getCardNumber());

            SVP.userMap.put(user.getUserID(), user);
            SVP.manager.insertFinger(user.getFinger(0));

            curFingerID = user.getFingerID(0);

            if (curFingerID > maxFingerID)
                maxFingerID = curFingerID;

            ++mUserCount;
                }

        mUniqueFingerId = maxFingerID;

        Log.d(TAG, "UniqueFingerId: " + mUniqueFingerId);

        Log.d(TAG, "Total User Count: " + mUserCount);

        Log.i(TAG, "Start Application !!");

        return true;
    }

    public void showToast(int result) {
        Toast toast = new Toast(MainActivity.this);
        View view = View.inflate(MainActivity.this, R.layout.toast_layout1, null);
        TextView textToast = view.findViewById(R.id.textToast1);
        toast.setView(view);

        String textMessage = "";

        if (result == ErrorCode.ERR_CANNOT_SCAN_FINGERPRINT) {
            textMessage = "Cannot scan fingerprint.";
        }
        else if (result == ErrorCode.ERR_FINGERPRINT_SCAN_CANCELLED) {
            textMessage = "Fingerprint scan canceled.";
        }
        else if (result == ErrorCode.ERR_CANNOT_SCAN_FINGERPRINT) {
            textMessage = "Cannot scan fingerprint.";
        }
        else if (result == ErrorCode.ERR_SCAN_FEEDBACK_NON_CENTER_POSITION) {
            textMessage = "Place your finger on the center of the sensor.";
        }
        else if (result == ErrorCode.ERR_SCAN_FEEDBACK_NON_FLAT) {
            textMessage = "Place your finger flat against the sensor.";
        }
        else if (result == ErrorCode.ERR_SCAN_FEEDBACK_TOO_HUMID) {
            textMessage = "Press slightly lighter and/or dry your finger.";
        }
        else if (result == ErrorCode.ERR_SCAN_FEEDBACK_TOO_DRY) {
            textMessage = "Press slightly harder and/or breathe on your finger.";
        }
        else if (result == ErrorCode.ERR_SCAN_FEEDBACK_INTERNAL_ERROR) {
            textMessage = "Scan finger internal error.";
        }
        else if (result == ErrorCode.ERR_NOT_SAME_FINGERPRINT){
            textMessage = "Finger is not same.";
        }
        else if (result == AddUserActivity.NO_ID) {
            textMessage = "Please input the ID";
        }
        else if (result == AddUserActivity.DUPLICATED_ID) {
            textMessage = "ID already exists";
        }
        else if (result == AddUserActivity.NO_FINGERPRINT) {
            textMessage = "Please Enroll Fingerprint";
        }
        else if(result == ErrorCode.SUCCESS) {
            textMessage = "Success!";
        }
        else {
            textMessage = "Unknown error - " + result;
        }

        textToast.setText(textMessage);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    public void onBackPressed() {
        if (this.mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (int i = 0; i < navigationView.getMenu().size(); i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }

        SVP.manager.resumeCardService();
        SVP.manager.resumeFingerprintService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SVP.manager.pauseFingerprintService();
        SVP.manager.pauseCardService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SVP.manager.pauseFingerprintService();
        SVP.manager.pauseCardService();
        SVP.manager.stop();
    }

}
