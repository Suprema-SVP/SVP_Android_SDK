package com.suprema.svpsample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.supremainc.sdk.define.Channel;
import com.supremainc.sdk.define.LedColor;
import com.supremainc.sdk.define.Relay;

import java.util.Timer;
import java.util.TimerTask;

public  class ControlFragment extends Fragment {

    private static int ROW_COUNT = 2;

    TextView[] mTitle = new TextView[ROW_COUNT];
    TextView[] mValue = new TextView[ROW_COUNT];
    SwitchCompat[] mSwitchValue = new SwitchCompat[ROW_COUNT];

    private int mLedColor = 0;
    private Timer mTimer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_control, container, false);
        mTimer = new Timer();

        if (view != null) {
            mTitle[0] = view.findViewById(R.id.option_title1);
            mTitle[0].setText(R.string.led_test);

            mTitle[1] = view.findViewById(R.id.option_title2);
            mTitle[1].setText(R.string.relay_test);

            mValue[0] = view.findViewById(R.id.option_value1);
            mValue[1] = view.findViewById(R.id.option_value2);

            mSwitchValue[0] = view.findViewById(R.id.option_switch1);
            mSwitchValue[0].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    excuteLed(isChecked);
                }
            });

            mSwitchValue[1] = view.findViewById(R.id.option_switch2);
            mSwitchValue[1].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    excuteRelay(isChecked);
                }
            });
        }

        return view ;
    }

    @Override
    public void onPause() {
        super.onPause();
        mTimer.cancel();
    }

    public ControlFragment() {

    }

    public void excuteLed(boolean on) {
        if (on) {
            TimerTask tt = new TimerTask() {
                LedColor color = LedColor.LED_COLOR_OFF;
                @Override
                public void run() {
                    switch (mLedColor) {
                        case 0:
                            color = LedColor.LED_COLOR_RED;
                            break;
                        case 1:
                            color = LedColor.LED_COLOR_YELLOW;
                            break;
                        case 2:
                            color = LedColor.LED_COLOR_GREEN;
                            break;
                        case 3:
                            color = LedColor.LED_COLOR_CYAN;
                            break;
                        case 4:
                            color = LedColor.LED_COLOR_BLUE;
                            break;
                        case 5:
                            color = LedColor.LED_COLOR_MAGENTA;
                            break;
                        case 6:
                            color = LedColor.LED_COLOR_WHITE;
                            break;
                    }

                    SVP.manager.executeLedAction(color);
                    mLedColor++;

                    if (mLedColor > 6)
                        mLedColor = 0;
                }
            };
            mTimer = new Timer();
            mTimer.schedule(tt,0,800);
        }
        else {
            SVP.manager.executeLedAction(LedColor.LED_COLOR_OFF);
            mTimer.cancel();
        }
    }

    public void excuteRelay(boolean on) {
        if (on) {
            SVP.manager.executeOutputAction(Channel.RELAY_PORT_0, Relay.ON);
            SVP.manager.executeOutputAction(Channel.RELAY_PORT_1, Relay.ON);
        }
        else {
            SVP.manager.executeOutputAction(Channel.RELAY_PORT_0, Relay.OFF);
            SVP.manager.executeOutputAction(Channel.RELAY_PORT_1, Relay.OFF);
        }
    }
}
