package com.yc.ycretrofitutils;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yc.ycrertofitutils.YcRetrofitUtils;
import com.yc.ycrertofitutils.interfaces.OnRequestCallBackListener;
import com.yc.ycretrofitutils.base.UrlConfig;
import com.yc.ycretrofitutils.model.NewsBean;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Flowable;

import static com.yc.ycretrofitutils.MyApplication.basicUseService;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.test1)
    TextView mTest1;
    @BindView(R.id.test2)
    TextView mTest2;
    HashMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.test1, R.id.test2})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.test1:
                mMap = new HashMap();
                mMap.put("key", "4a216a3fde4361f175aa2678dada199b");
                mMap.put("type", "top");
                YcRetrofitUtils.get(UrlConfig.NEWS_URL, mMap, new OnRequestCallBackListener<String>() {

                    @Override
                    public void onSuccess(String body, String tag) {

                        Toast.makeText(MainActivity.this, (String) body, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(String e, String tag) {

                    }
                });

                //                //二次封装
                //                startActivity(new Intent(this,BasicUseActivity.class));
                break;
            case R.id.test2:
                mMap = new HashMap();
                mMap.put("key", "4a216a3fde4361f175aa2678dada199b");
                mMap.put("type", "top");
                //                BasicUseService basicUseService = YcRetrofitUtils.getRetrofit().create(BasicUseService.class);
                Flowable login = basicUseService.login("toutiao/index", mMap);
                YcRetrofitUtils.requestCallBack(login, "", new OnRequestCallBackListener<NewsBean>() {
                    @Override
                    public void onSuccess(NewsBean body, String tag) {
                        if (body instanceof NewsBean) {
                            Toast.makeText(MainActivity.this, body.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailed(String e, String tag) {

                    }
                });

                //                startActivity(new Intent(this,SecondaryPackagingActivity.class));

                break;
        }

    }

    public static <T> T get(Class<T> clz, Object o) {
        if (clz.isInstance(o)) {
            return clz.cast(o);
        }
        return null;
    }

}
