package com.suprema.svpsample;

import android.content.Context;
import android.os.Bundle;
import android.support.design.button.MaterialButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UpgradeFragment.OnUpgradeFragmentListener} interface
 * to handle interaction events.
 * Use the {@link UpgradeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UpgradeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    MaterialButton mUpgradeButton;
    MaterialButton mGetFileButton;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnUpgradeFragmentListener mListener;

    public UpgradeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UpgradeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UpgradeFragment newInstance(String param1, String param2) {
        UpgradeFragment fragment = new UpgradeFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_upgrade, container, false);

        mUpgradeButton = (MaterialButton) view.findViewById(R.id.upgradeFirmware);
        mUpgradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed(MainActivity.BUTTON_UPGRADE);
            }
        });

        mGetFileButton = (MaterialButton) view.findViewById(R.id.getFirmwareFile);
        mGetFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed(MainActivity.BUTTON_GET_FILE);
            }
        });

        /*
        TextView editHost = view.findViewById(R.id.ftpHostEdit);
        TextView editPort = view.findViewById(R.id.ftpPortEdit);
        TextView editUser = view.findViewById(R.id.ftpUserNameEdit);
        TextView editPassword = view.findViewById(R.id.ftpPasswordEdit);
        editHost.setText("192.168.14.52");
        editPort.setText("21");
        editUser.setText("jbkim");
        editPassword.setText("jbkim");
        */

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(int button) {
        if (mListener != null) {
            mListener.onFragmentButton(button);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnUpgradeFragmentListener) {
            mListener = (OnUpgradeFragmentListener) context;
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

    public interface OnUpgradeFragmentListener {
        // TODO: Update argument type and name
        void onFragmentButton(int button);
    }

}
