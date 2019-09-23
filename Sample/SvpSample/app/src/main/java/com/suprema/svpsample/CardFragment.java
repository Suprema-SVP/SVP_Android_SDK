package com.suprema.svpsample;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import butterknife.BindView;

public class CardFragment extends BaseFragment {
    @BindView(R.id.cardIDData)
    TextView mCardIDText;
    @BindView(R.id.cardTypeData)
    TextView mCardTypeText;

    public CardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SVP.manager.resumeCardService();
        SVP.manager.pauseFingerprintService();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_card;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setCardIDText(CharSequence id) {
        mCardIDText.setText(id);
    }

    public void setCardTypeText(CharSequence type) {
        mCardTypeText.setText(type);
    }
}
