package com.yc.ycretrofitutils;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yc.ycrertofitutils.YcRetrofitUtils;
import com.yc.ycrertofitutils.interfaces.OnCallBackListener;
import com.yc.ycretrofitutils.base.UrlConfig;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SecondaryPackagingActivity extends AppCompatActivity {

    private Map mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondary_packaging);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.test1, R.id.test2, R.id.test3, R.id.test4, R.id.test5})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.test1:

                break;
            case R.id.test2:

                break;
            case R.id.test3:
                break;
            case R.id.test4:
                break;
            case R.id.test5:
                break;
        }
    }
}
