package com.jbtm.parentschool.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jbtm.parentschool.R;
import com.jbtm.parentschool.utils.SPUtil;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();

//        ImageView imageView = findViewById(R.id.iv);

//        SPUtil.setToken("e6516bbee198e9d61ae07bc08ebb961f");

//        Glide.with(this).load(R.mipmap.splash).into(imageView);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(SPUtil.getToken())) {
                    //说明未登录，去登录页面
                    PersonalInformationActivity.startActivity(SplashActivity.this, 1);
                } else {
                    //去首页
                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                }
                finish();
            }
        }, 2000);
    }
}
