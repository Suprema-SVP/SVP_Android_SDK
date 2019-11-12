package com.supremainc.svpdemo.Dialog;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import com.supremainc.svpdemo.R;

import java.util.Timer;
import java.util.TimerTask;

public class AuthDialog extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent intent = getIntent();
        String userId = intent.getExtras().getString("id");
        String userName = intent.getExtras().getString("name");

        if(userId.length()==0)
            setContentView(R.layout.pop_access_denied);
        else
            setContentView(R.layout.pop_access_success);

        TextView textUserId = findViewById(R.id.textUserId);
        textUserId.setText(userId);

        TextView textUserName = findViewById(R.id.textUserName);
        textUserName.setText(userName);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        },1600);
    }
}
