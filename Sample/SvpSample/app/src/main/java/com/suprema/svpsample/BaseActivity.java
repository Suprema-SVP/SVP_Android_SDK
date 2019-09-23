package com.suprema.svpsample;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(getLayoutId());
        ButterKnife.bind(this);
    }

    @LayoutRes
    public abstract int getLayoutId();

}