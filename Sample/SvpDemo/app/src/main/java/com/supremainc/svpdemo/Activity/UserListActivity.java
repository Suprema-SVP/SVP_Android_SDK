package com.supremainc.svpdemo.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.supremainc.svpdemo.R;
import com.supremainc.svpdemo.SVP;
import com.supremainc.svpdemo.Data.User;
import com.supremainc.svpdemo.Data.UserAdapter;

import java.util.ArrayList;

public class UserListActivity extends AppCompatActivity {

    public static final int ADD_USER = 1;
    public static final int DETAIL_USER = 2;

    private ArrayList<User> mUserList;
    private ListView listView = null;
    private ArrayAdapter<User> adapter;

    private EditText mEdit;
    private Button mBtnBack;
    private Button mBtnSearch;
    private Button mBtnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_user_list);

        mEdit = findViewById(R.id.user_count_search);
        listView = findViewById(R.id.user_list);
        mBtnBack = findViewById(R.id.btnBack);
        mBtnSearch = findViewById(R.id.btnSearch);
        mBtnAdd = findViewById(R.id.btnAdd);

        mUserList = SVP.userArray;
        adapter = new UserAdapter(this, mUserList);
        listView.setAdapter(adapter);

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });

        mBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEdit.setText(null);
                mEdit.setHint("Input ID or Name");
                mEdit.setHintTextColor(Color.GRAY);
                mEdit.setFocusableInTouchMode(true);
                mEdit.requestFocus();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = (User)listView.getItemAtPosition(position);

                Intent intent = new Intent(UserListActivity.this, AddUserActivity.class);
                intent.putExtra("state", DETAIL_USER);
                intent.putExtra("id", user.getUserID());
                intent.putExtra("name", user.getUserName());
                intent.putExtra("cardNumber", user.getCardNumber());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });

        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserListActivity.this, AddUserActivity.class);
                intent.putExtra("state", ADD_USER);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });

        mEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String filterText = s.toString() ;
                ((UserAdapter)listView.getAdapter()).getFilter().filter(filterText) ;
            }
        });

    } // onCreate()


    @Override
    protected void onResume(){
        super.onResume();

        listView.setAdapter(adapter);

        mEdit.setText(null);
        mEdit.setFocusableInTouchMode(false);
        mEdit.setFocusable(false);
        mEdit.setHintTextColor(Color.WHITE);
        mEdit.setHint("USER (" + mUserList.size() + ")");

        SVP.manager.pauseFingerprintService();
        SVP.manager.pauseCardService();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
