package com.supremainc.svpdemo.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.widget.Toast;

import com.supremainc.svpdemo.R;
import com.supremainc.svpdemo.SVP;
import com.supremainc.sdk.define.ErrorCode;
import com.supremainc.sdk.option.CardOption;
import com.supremainc.sdk.option.FingerprintOption;

public class OptionFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    ListPreference securityPreference;
    ListPreference fastModePreference;
    ListPreference sensitivityPreference;
    ListPreference sensorModePreference;
    ListPreference templateFormatPreference;
    ListPreference fingerScanTimePreference;
    ListPreference lfdLevelPreference;
    ListPreference cardScanTimePreference;
    ListPreference byteOrderPreference;

    SwitchPreferenceCompat advancedEnrollment;
    SwitchPreferenceCompat useFingerImage;

    FingerprintOption fingerOption = new FingerprintOption();
    CardOption cardOption = new CardOption();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String s) {
        SVP.manager.getFingerprintOption(fingerOption);

        setPreferencesFromResource(R.xml.option_preference, s);

        securityPreference = (ListPreference) findPreference("pref_finger_security_level");
        securityPreference.setSummary(securityPreference.getValue());

        fastModePreference = (ListPreference) findPreference("pref_finger_fast_mode");
        fastModePreference.setSummary(fastModePreference.getValue());

        sensitivityPreference = (ListPreference) findPreference("pref_finger_sensitivity");
        sensitivityPreference.setSummary(sensitivityPreference.getValue());

        sensorModePreference = (ListPreference) findPreference("pref_finger_sensor_mode");
        sensorModePreference.setSummary(sensorModePreference.getValue());

        templateFormatPreference = (ListPreference) findPreference("pref_finger_template_type");
        templateFormatPreference.setSummary(templateFormatPreference.getValue());

        fingerScanTimePreference = (ListPreference) findPreference("pref_finger_scan_timeout");
        fingerScanTimePreference.setSummary(fingerScanTimePreference.getValue());

        lfdLevelPreference = (ListPreference) findPreference("pref_finger_lfd_level");
        lfdLevelPreference.setSummary(lfdLevelPreference.getValue());

        cardScanTimePreference = (ListPreference) findPreference("pref_card_scan_timeout");
        cardScanTimePreference.setSummary(cardScanTimePreference.getValue());

        byteOrderPreference = (ListPreference) findPreference("pref_card_byte_order");
        byteOrderPreference.setSummary(byteOrderPreference.getValue());

        advancedEnrollment = (SwitchPreferenceCompat) findPreference("pref_finger_advanced_enrollment");
        advancedEnrollment.setChecked(fingerOption.useAdvancedEnrollment);

        useFingerImage = (SwitchPreferenceCompat) findPreference("pref_finger_image");
        useFingerImage.setChecked(fingerOption.useBitmapImage);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String value = "";

        value = pref.getString("pref_finger_security_level", getString(R.string.menu_finger_security_level_default));
        securityPreference.setSummary(value);
        if (value.equals("Normal"))             fingerOption.securityLevel = FingerprintOption.SECURITY_NORMAL;
        else if (value.equals("Secure"))        fingerOption.securityLevel = FingerprintOption.SECURITY_SECURE;
        else if (value.equals("More Secure"))   fingerOption.securityLevel = FingerprintOption.SECURITY_MORE_SECURE;

        value = pref.getString("pref_finger_fast_mode", getString(R.string.menu_finger_fast_default));
        fastModePreference.setSummary(value);
        if (value.equals("Auto"))           fingerOption.fastMode = FingerprintOption.FAST_MODE_AUTO;
        else if (value.equals("Normal"))    fingerOption.fastMode = FingerprintOption.FAST_MODE_NORMAL;
        else if (value.equals("Faster"))    fingerOption.fastMode = FingerprintOption.FAST_MODE_FASTER;
        else if (value.equals("Fastest"))   fingerOption.fastMode = FingerprintOption.FAST_MODE_FASTEST;

        value = pref.getString("pref_finger_sensitivity", getString(R.string.menu_finger_sensitive_default));
        sensitivityPreference.setSummary(value);
        fingerOption.sensitivity = Integer.parseInt(value);

        value = pref.getString("pref_finger_sensor_mode", getString(R.string.menu_finger_sensor_mode_default));
        sensorModePreference.setSummary(value);
        if (value.equals("Always On"))      fingerOption.sensorMode = FingerprintOption.SENSOR_MODE_ALWAYS_ON;
        else if (value.equals("Proximity")) fingerOption.sensorMode = FingerprintOption.SENSOR_MODE_PROXIMITY;

        value = pref.getString("pref_finger_template_type", getString(R.string.menu_finger_template_type_default));
        templateFormatPreference.setSummary(value);
        if (value.equals("Suprema"))        fingerOption.templateFormat = FingerprintOption.TEMPLATE_FORMAT_SUPREMA;
        else if (value.equals("ISO"))       fingerOption.templateFormat = FingerprintOption.TEMPLATE_FORMAT_ISO;
        else if (value.equals("ANSI"))      fingerOption.templateFormat = FingerprintOption.TEMPLATE_FORMAT_ANSI;

        value = pref.getString("pref_finger_scan_timeout", getString(R.string.menu_finger_scan_timeout_default));
        fingerScanTimePreference.setSummary(value);
        fingerOption.scanTimeout = Integer.parseInt(value);

        value = pref.getString("pref_finger_lfd_level", getString(R.string.menu_finger_lfd_level_default));
        lfdLevelPreference.setSummary(value);
        if (value.equals("OFF"))            fingerOption.lfdLevel = FingerprintOption.LFD_LEVEL_OFF;
        else if (value.equals("Low"))       fingerOption.lfdLevel = FingerprintOption.LFD_LEVEL_LOW;
        else if (value.equals("Middle"))    fingerOption.lfdLevel = FingerprintOption.LFD_LEVEL_MIDDLE;
        else if (value.equals("High"))      fingerOption.lfdLevel = FingerprintOption.LFD_LEVEL_HIGH;

        fingerOption.useAdvancedEnrollment = pref.getBoolean("pref_finger_advanced_enrollment", true);

        fingerOption.useBitmapImage = pref.getBoolean("pref_finger_image", false);

        value = pref.getString("pref_card_scan_timeout", getString(R.string.menu_card_scan_timeout_default));
        cardScanTimePreference.setSummary(value);
        cardOption.scanTimeout = Integer.parseInt(value);

        value = pref.getString("pref_card_byte_order", getString(R.string.menu_card_byte_order_default));
        byteOrderPreference.setSummary(value);
        if (value.equals("MSB"))        cardOption.byteOrder = CardOption.BYTE_ORDER_MSB;
        else if (value.equals("LSB"))   cardOption.byteOrder = CardOption.BYTE_ORDER_LSB;

        int fingerResult = SVP.manager.setFingerprintOption(fingerOption);

        int cardResult = SVP.manager.setCardOption(cardOption);

        if (fingerResult == ErrorCode.SUCCESS && cardResult == ErrorCode.SUCCESS)
            Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
        else if (fingerResult != ErrorCode.SUCCESS )
            Toast.makeText(getContext(), "Failed set finger option (error:" + fingerResult + ")", Toast.LENGTH_SHORT).show();
        else if (cardResult != ErrorCode.SUCCESS )
            Toast.makeText(getContext(), "Failed set card option (error:" + cardResult + ")", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

}
