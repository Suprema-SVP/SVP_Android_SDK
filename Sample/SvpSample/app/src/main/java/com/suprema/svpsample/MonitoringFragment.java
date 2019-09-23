package com.suprema.svpsample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public  class MonitoringFragment extends Fragment {

    ListView mListview;
    ArrayAdapter<String> mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_monitoring, container, false);
        List<String> list = new ArrayList<>();

        mListview = (ListView)view.findViewById(R.id.listViewMonitoring);
        mAdapter = new ArrayAdapter<String>(view.getContext(),
                android.R.layout.simple_list_item_1, MainActivity.mEventArray);

        mListview.setAdapter(mAdapter);

        return view ;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    public MonitoringFragment() {

    }

    public void updateListView() {
        mAdapter.notifyDataSetChanged();
    }
}
