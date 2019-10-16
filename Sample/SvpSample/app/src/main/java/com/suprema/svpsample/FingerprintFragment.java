package com.suprema.svpsample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FingerprintFragment.OnFingerFragmentListener} interface
 * to handle interaction events.
 * Use the {@link FingerprintFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FingerprintFragment extends BaseFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    @BindView(R.id.verfiyFinger)
    MaterialButton mVerifyButton;
    @BindView(R.id.enrollFinger)
    MaterialButton mEnrollButton;
    @BindView(R.id.deleteAllFinger)
    MaterialButton mDeleteAllButton;
    @BindView(R.id.setFingerList)
    MaterialButton mSetListButton;
    @BindView(R.id.setMaxUser)
    MaterialButton mSetUserButton;
    @BindView(R.id.updateFinger)
    MaterialButton mUpdateButton;
    @BindView(R.id.delete1Finger)
    MaterialButton mDeleteOneButton;

    @BindView(R.id.templateImage)
    ImageView mTemplateView;

    @BindView(R.id.editID)
    TextView mEditID;
    @BindView(R.id.textUserCount)
    TextView mTextUserCount;
    @BindView(R.id.textSdkUserCount)
    TextView mTextSdkUserCount;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFingerFragmentListener mListener;

    public FingerprintFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FingerprintFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FingerprintFragment newInstance(String param1, String param2) {
        FingerprintFragment fragment = new FingerprintFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTextView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_fingerprint;
    }

    @OnClick({R.id.verfiyFinger})
    public void onVerfiyFingerClick(View view) {
        onButtonPressed(MainActivity.BUTTON_VERIFY);
    }

    @OnClick({R.id.enrollFinger})
    public void onEnrollFingerClick(View view) {
        onButtonPressed(MainActivity.BUTTON_ENROLL);
    }

    @OnClick({R.id.deleteAllFinger})
    public void onDeleteAllFingerClick(View view) {
        onButtonPressed(MainActivity.BUTTON_DELETE_ALL);
    }

    @OnClick({R.id.setFingerList})
    public void onSetFingerListClick(View view) {
        onButtonPressed(MainActivity.BUTTON_SET_LIST);
    }

    @OnClick({R.id.setMaxUser})
    public void onSetMaxUserClick(View view) {
        onButtonPressed(MainActivity.BUTTON_SET_USER);
    }

    @OnClick({R.id.updateFinger})
    public void onUpdateFingerClick(View view) {
        onButtonPressed(MainActivity.BUTTON_UPDATE);
    }

    @OnClick({R.id.delete1Finger})
    public void onDeleteFingerClick(View view) {
        onButtonPressed(MainActivity.BUTTON_DELETE);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(int button) {
        if (mListener != null) {
            mListener.onFragmentButton(button);
        }
    }

    public void setImageView(Bitmap bitmap) {
        if (bitmap != null) {
            Drawable ob = new BitmapDrawable(getResources(), bitmap);
            mTemplateView.setBackground(ob);
        }
    }

    public void updateTextView() {
        if (mTextUserCount != null) {
            mTextUserCount.setText("APP: " + SVP.userArray.size());
        }
        if (mTextSdkUserCount != null) {
            mTextSdkUserCount.setText("SDK: " + SVP.sdkUserCount);
        }
    }

    public String getUserId() {
        String userId = null;

        if (mEditID != null) {
            userId = mEditID.getText().toString();
        }
        return userId;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFingerFragmentListener) {
            mListener = (OnFingerFragmentListener) context;
        }
        else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

    public interface OnFingerFragmentListener {
        // TODO: Update argument type and name
        void onFragmentButton(int button);
    }

}
