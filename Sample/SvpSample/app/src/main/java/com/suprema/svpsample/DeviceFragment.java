package com.suprema.svpsample;

import android.content.Context;
import android.os.Bundle;
import android.support.design.button.MaterialButton;
import android.view.View;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.supremainc.sdk.model.Device;

import butterknife.BindView;
import butterknife.OnClick;


public class DeviceFragment extends BaseFragment {
    @BindView(R.id.deviceID)
    TextView mDeviceIDText;
    @BindView(R.id.modelName)
    TextView mDeviceModelText;
    @BindView(R.id.rebootButton)
    MaterialButton mButton;

    private OnDeviceFragmentListener mListener;

    public DeviceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, parent, savedInstanceState);

        Device dev = new Device();
        SVP.manager.getDeviceInfo(dev);

        mDeviceIDText.setText("Device ID: " + String.valueOf(dev.deviceId));
        mDeviceModelText.setText("Model: " + dev.modelName);

        return view;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_device;
    }

    @OnClick({R.id.rebootButton})
    public void onRebootClick(View view) {
        onButtonPressed(MainActivity.BUTTON_REBOOT);
    }

    public void onButtonPressed(int button) {
        if (mListener != null) {
            mListener.onFragmentButton(button);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnDeviceFragmentListener) {
            mListener = (OnDeviceFragmentListener) context;
        }
        else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnDeviceFragmentListener {
        // TODO: Update argument type and name
        void onFragmentButton(int button);
    }
}
