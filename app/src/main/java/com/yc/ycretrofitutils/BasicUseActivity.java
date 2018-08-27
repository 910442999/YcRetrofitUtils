package com.yc.ycretrofitutils;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BasicUseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_use);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.test1, R.id.test2, R.id.test3, R.id.test4, R.id.test5, R.id.test6})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.test1:
                HashMap  mMap = new HashMap();
                mMap.put("key", "4a216a3fde4361f175aa2678dada199b");
                mMap.put("type", "top");
                break;
            case R.id.test2:

                break;
            case R.id.test3:

                break;
            case R.id.test4:

                break;

            case R.id.test5:

                break;

            case R.id.test6:



                break;
        }
    }
}
