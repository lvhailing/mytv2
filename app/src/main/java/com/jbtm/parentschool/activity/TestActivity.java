package com.jbtm.parentschool.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jbtm.parentschool.R;
import com.jbtm.parentschool.models.CommonModel;
import com.jbtm.parentschool.network.MyObserverAdapter;
import com.jbtm.parentschool.network.MyRemoteFactory;
import com.jbtm.parentschool.network.MyRequestProxy;
import com.jbtm.parentschool.network.model.DataModel;
import com.jbtm.parentschool.network.model.DataWrapper;
import com.jbtm.parentschool.network.model.ResultModel;
import com.jbtm.parentschool.utils.RequestUtil;
import com.jbtm.parentschool.utils.SPUtil;
import com.jbtm.parentschool.utils.ToastUtil;
import com.jbtm.parentschool.utils.ZXingUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class TestActivity extends AppCompatActivity {
    private TextView textView;
    private ImageView imageView;
    private EditText editText;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bitmap bitmap = (Bitmap) msg.obj;
            imageView.setImageBitmap(bitmap);
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        textView = findViewById(R.id.tv);
        imageView = findViewById(R.id.iv);
        editText = findViewById(R.id.et);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String url1 = "http://img0.imgtn.bdimg.com/it/u=2432380748,1284817624&fm=11&gp=0.jpg";
                Bitmap bitmap = getImageBitmap(url1);
                Message message = Message.obtain();
                message.obj = bitmap;
                handler.sendMessage(message);
            }
        }).start();
    }

    //用原生框架下载图片
    public Bitmap getImageBitmap(String url) {
        URL imgUrl;
        Bitmap bitmap = null;
        try {
            imgUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imgUrl.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public void makeQrCode(View view) {
        Bitmap bitmap = ZXingUtil.createQRImage("https://qr.alipay.com/bax04979wslvcoe3of1z007a",
                imageView.getWidth(), imageView.getHeight());
        imageView.setImageBitmap(bitmap);
    }

    public void login(View view) {
        String num = editText.getText().toString();
        Map<String, Object> params = new HashMap<>();
        params.put("phone", "18001122787");
        params.put("captcha", num);
        RequestUtil.getBasicMap(params);

        MyRemoteFactory.getInstance().getProxy(MyRequestProxy.class)
                .login(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserverAdapter<ResultModel<CommonModel>>() {
                    @Override
                    public void onMyError(Throwable e) {
//                        ToastUtil.showCustom("调接口失败");
                    }

                    @Override
                    public void onMySuccess(ResultModel<CommonModel> result) {

                        SPUtil.setToken(result.result.access_token);

                        ToastUtil.showCustom(result.result.access_token);
                    }
                });
    }

    public void captcha(View view) {
        Map<String, Object> params = new HashMap<>();
        params.put("phone", "18001122787");
        RequestUtil.getBasicMap(params);

        MyRemoteFactory.getInstance().getProxy(MyRequestProxy.class)
                .sendCaptcha(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserverAdapter<ResultModel>() {
                    @Override
                    public void onMyError(Throwable e) {
//                        ToastUtil.showCustom("调接口失败");
                    }

                    @Override
                    public void onMySuccess(ResultModel result) {
                        ToastUtil.showCustom("验证码发送成功");
                    }
                });
    }

    private void getBooks(String id) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "caijing");
        params.put("key", "cf2e8c721799bbc8f3c9d639a4d0a9e6");

        MyRemoteFactory.getInstance().getProxy(MyRequestProxy.class)
                .getBookListByPost(params)
//                .getBookListByGet()   //get请求样例
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserverAdapter<ResultModel<DataWrapper<DataModel>>>() {
                    @Override
                    public void onMyError(Throwable e) {
                        //server取单据失败
//                        ToastUtil.showCustom("调接口失败");
                    }

                    @Override
                    public void onMySuccess(ResultModel<DataWrapper<DataModel>> result) {
                        //server取单据成功
                        if (result != null && result.result != null
                                && result.result.data != null && result.result.data.size() > 0) {
                            List<DataModel> list = result.result.data;

                            textView.setText(list.get(1).category);

                            Glide.with(TestActivity.this)
                                    .load(list.get(0).thumbnail_pic_s)
                                    .into(imageView);
                        }
                    }
                });
    }
}
