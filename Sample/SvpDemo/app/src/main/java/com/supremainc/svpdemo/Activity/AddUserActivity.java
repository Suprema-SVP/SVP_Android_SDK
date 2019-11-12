package com.supremainc.svpdemo.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.supremainc.svpdemo.DatabaseHelper;
import com.supremainc.svpdemo.Dialog.BottomDialog;
import com.supremainc.svpdemo.Dialog.OneButtonDialog;
import com.supremainc.svpdemo.R;
import com.supremainc.svpdemo.SVP;
import com.supremainc.svpdemo.Data.User;
import com.supremainc.sdk.callback.Fingerprint;
import com.supremainc.sdk.callback.Punch;
import com.supremainc.sdk.define.ErrorCode;

import java.util.Iterator;

public class AddUserActivity extends AppCompatActivity {

    public static final String TAG = "AddUserActivity";
    public static final int NO_ID = 100;
    public static final int DUPLICATED_ID = 101;
    public static final int NO_FINGERPRINT = 102;

    private String mUserId;
    private String mUserName;
    private String mCardNumber;

    private TextView mTextTitle;
    private TextView mTextFingerPrintState;
    private TextView mTextCardState;
    private EditText mEditId;
    private EditText mEditName;
    private Button mBtnBack;
    private Button mBtnEnrollFinger;
    private Button mBtnEnrollCard;
    private Button mBtnComplete;
    private Button mBtnMore;

    private int mActivityState;

    public OneButtonDialog mAlertDialog;

    private int mUserCount = MainActivity.mMainActivity.mUserCount;
    private DatabaseHelper mUserDatabase =  MainActivity.mMainActivity.mUserDatabase;

    public static AddUserActivity mAddUserActivity;

    public FragmentManager mFragmentManager = getSupportFragmentManager();

    public Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_add_user);

        mTextTitle = findViewById(R.id.toolbarTitle);
        mEditId = findViewById(R.id.textEditId);
        mEditId.setText(Integer.toString(mUserCount + 1));
        mEditName = findViewById(R.id.textEditName);

        mTextCardState = findViewById(R.id.textCardState);
        mTextFingerPrintState = findViewById(R.id.textFingerprintState);

        mBtnBack = findViewById(R.id.btnBack);
        mBtnEnrollFinger = findViewById(R.id.btnEnrollFinger);
        mBtnEnrollCard = findViewById(R.id.btnEnrollCard);
        mBtnComplete = findViewById(R.id.btnComplete);
        mBtnMore = findViewById(R.id.btnMore);
        mAddUserActivity = this;

        intent = getIntent();

        mActivityState = intent.getExtras().getInt("state");

        if (mActivityState == UserListActivity.DETAIL_USER)
            changeUiForDetail();

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });

        mBtnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomDialog bottomDialog = new BottomDialog();
                bottomDialog.show(mFragmentManager, "bottomDialog");
            }
        });

        mBtnEnrollCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog = OneButtonDialog.newButtonDialog(R.string.scan_card, OneButtonDialog.CARD_DIALOG);
                mAlertDialog.show(mFragmentManager, "scanCard");
                SVP.manager.scanCard();
            }
        });

        mBtnEnrollFinger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SVP.firstScan = true;
                if (mActivityState == UserListActivity.DETAIL_USER)
                    scanFingerprint("UPDATE");
                else
                    scanFingerprint("ENROLL");
            }
        });

        mBtnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActivityState == UserListActivity.ADD_USER) {
                    mUserId = mEditId.getText().toString();
                    mUserName = mEditName.getText().toString();
                    mCardNumber = mTextCardState.getText().toString();

                    String fingerprintState = mTextFingerPrintState.getText().toString();

                    // invalid ID
                    if (mUserId.length() == 0) {
                        MainActivity.mMainActivity.showToast(NO_ID);
                        return;
                    }
                    // duplicated ID
                    if (idDuplicationCheck(mUserId)) {
                        MainActivity.mMainActivity.showToast(DUPLICATED_ID);
                        return;
                    }
                    // failed to enroll finger
                    if (fingerprintState.equals("Enroll Fingerprint")) {
                        MainActivity.mMainActivity.showToast(NO_FINGERPRINT);
                        return;
                    }

                    User user = new User(mUserId, mUserName, mCardNumber);
                    user.addFinger(SVP.newFinger, null);

                    SVP.manager.insertFinger(SVP.newFinger);

                    SVP.userMap.put(mUserId, user);
                    SVP.userArray.add(user);

                    mUserDatabase.add(user);

                    if (user.getNumOfFinger() != 0) {
                        Log.i(TAG, "[Enroll] user id:" + user.getUserID() + ", finger id:" + user.getFingerID(0));
                    }
                    MainActivity.mMainActivity.mUserCount += 1;
                    SVP.newFinger = null;
                    finish();

                }
                else {
                    String id = intent.getExtras().getString("id");
                    User user = getUserFromMap(id);
                    mUserName = mEditName.getText().toString();
                    mCardNumber = mTextCardState.getText().toString();

                    user.setUserName(mUserName);
                    user.setCardNumber(mCardNumber);

                    // update finger
                    if (SVP.newFinger != null) {
                        SVP.manager.updateFinger(SVP.newFinger);
                        user.updateFinger(SVP.newFinger, null);
                    }

                    mUserDatabase.update(user);

                    SVP.newFinger = null;
                    finish();
                }
            }
        });

    }
    // onCreate

    public void changeUiForUpdate() {
        mEditName.setFocusableInTouchMode(true);
        mEditName.requestFocus();
        mBtnEnrollCard.setEnabled(true);
        mBtnEnrollFinger.setEnabled(true);
        mBtnComplete.setVisibility(View.VISIBLE);
        mBtnMore.setVisibility(View.GONE);
    }

    public void changeUiForDetail() {
        mEditId.setText(intent.getExtras().getString("id"));
        mEditName.setText(intent.getExtras().getString("name"));
        mTextCardState.setText(intent.getExtras().getString("cardNumber"));

        mTextTitle.setText("User Detail");
        mEditId.setFocusableInTouchMode(false);
        mEditName.setFocusableInTouchMode(false);
        mBtnEnrollCard.setEnabled(false);
        mBtnEnrollFinger.setEnabled(false);
        mBtnComplete.setVisibility(View.GONE);
        mBtnMore.setVisibility(View.VISIBLE);

        if(!mTextCardState.equals(""))
            mTextCardState.setTextColor(Color.BLUE);

        changeStateColor();
    }

    public void changeStateColor() {
        TextView fingerprint = findViewById(R.id.textFingerprintState);
        fingerprint.setText("Enrolled");
        fingerprint.setTextColor(Color.BLUE);
    }

    public boolean idDuplicationCheck(String id) {
        Iterator it = SVP.userArray.iterator();
        User user;
        while (it.hasNext()) {
            user = (User) it.next();
            if (id.equals(user.getUserID()))
                return true;
        }
        return false;
    }

    public void deleteUser() {
        String userId = intent.getExtras().getString("id");
        SVP.manager.deleteFinger(getUserFromMap(userId).getFinger(0));

        SVP.userArray.remove(getUserFromMap(userId));
        SVP.userMap.remove(userId);

        mUserDatabase.delete(userId);
    }

    public User getUserFromMap(String userId) {
        return SVP.userMap.get(userId);
    }

    public void setCardState(Punch data) {
        if (mFragmentManager.findFragmentByTag("scanCard") != null) {
            ((OneButtonDialog) mFragmentManager.findFragmentByTag("scanCard")).dismiss();
        }
        mTextCardState.setText(data.displayString);
        mTextCardState.setTextColor(Color.BLUE);
    }

    public void scanFingerprint(String mode) {
        if (SVP.firstScan) {
            mAlertDialog = OneButtonDialog.newButtonDialog(
                    R.string.scan_finger, OneButtonDialog.FINGER_DIALOG);
        }
        else {
            mAlertDialog = OneButtonDialog.newButtonDialog(
                    R.string.rescan_finger, OneButtonDialog.FINGER_DIALOG);
        }

        if (mode.equals("ENROLL"))
            mAlertDialog.show(mFragmentManager, "scanFinger");
        else if (mode.equals("UPDATE"))
            mAlertDialog.show(mFragmentManager, "updateFinger");

        SVP.manager.scanFingerprint();
    }

    public void fingerprintScanCompleted(Fingerprint data) {
        switch (data.result) {
            case ErrorCode.ERR_FINGERPRINT_SCAN_TIMEOUT:
                Fragment frag = mFragmentManager.findFragmentByTag("scanFinger");
                if (frag == null) {
                    frag = mFragmentManager.findFragmentByTag("updateFinger");
                    if (frag == null) {
                        Log.i(TAG, "Error timeout.");
                        return;
                    }
                }
                ((OneButtonDialog) frag).dismiss();
                break;
            case ErrorCode.SUCCESS:
                String tag = "";
                int command = 0;
                if (mFragmentManager.findFragmentByTag("scanFinger") != null) {
                    tag = "scanFinger";
                    command = MainActivity.mMainActivity.SCAN_FINGER_COMPLETED;
                }
                else if (mFragmentManager.findFragmentByTag("updateFinger") != null) {
                    tag = "updateFinger";
                    command = MainActivity.mMainActivity.SCAN_FINGER_UPDATE_COMPLETED;
                }

                if (!tag.isEmpty()) {
                    ((OneButtonDialog) mFragmentManager.findFragmentByTag(tag)).dismiss();

                    Message msg = Message.obtain();
                    msg.obj = data.template;
                    msg.what = command;
                    MainActivity.mMainActivity.sendCommandMessage(msg);
                }
                break;
            default:
                Log.i(TAG, "onFingerprintScanCompleted Fail..." + data.result);

                if (mFragmentManager.findFragmentByTag("scanFinger") != null) {
                    ((OneButtonDialog) mFragmentManager.findFragmentByTag("scanFinger")).dismiss();
                }

                if (mFragmentManager.findFragmentByTag("updateFinger") != null) {
                    ((OneButtonDialog) mFragmentManager.findFragmentByTag("updateFinger")).dismiss();
                }

                Message msg = Message.obtain();
                msg.what = MainActivity.mMainActivity.ERROR_DETECTED;
                msg.arg1 = data.result;
                MainActivity.mMainActivity.sendCommandMessage(msg);

                break;
        }
    }

    public String getActivityUserId() {
        String userId = intent.getExtras().getString("id");
        return userId;
    }

    @Override
    public void onResume() {
        super.onResume();
        SVP.manager.pauseFingerprintService();
        SVP.manager.pauseCardService();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
