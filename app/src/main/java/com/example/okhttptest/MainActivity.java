package com.example.okhttptest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    /**
     * get请求
     */
    private static final int GET = 1;
    /**
     * post请求
     */
    private static final int POST = 2;
    /**
     * 定义上传文件类型
     */
    public final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");
    private Button btn_get_post;
    private TextView tv_result;
    private ImageView image;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case GET:
                    //获取数据
                    tv_result.setText((String) msg.obj);
                    break;
                case POST:
                    //获取数据
                    tv_result.setText((String) msg.obj);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_get_post = (Button) findViewById(R.id.btn_get_post);
        tv_result = (TextView) findViewById(R.id.tv_result);
        image = (ImageView) findViewById(R.id.image);
        btn_get_post.setOnClickListener(this);
        /**
         * 动态获取权限，Android 6.0 新特性，一些保护权限，除了要在AndroidManifest中声明权限，还要使用如下代码动态获取
         */
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_get_post:
                tv_result.setText("");
                if (image != null){
                    image.setImageDrawable(null);
                }
                getDate();
//                getAsynFile();
//                postAsynFile();
//                postAsynHttp();
//                getDateFromGet();
//                getDateFromPost();
                break;
        }
    }

    /**
     * 使用get请求网络数据
     */
    private void getDateFromGet() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    String result = getSyncHttp();
                    Log.e("TAG", result);
                    Message msg = Message.obtain();
                    msg.what = GET;
                    msg.obj = result;
                    handler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

//    /**
//     * 使用post请求网络数据
//     */
//    private void getDateFromPost() {
//        new Thread(){
//            @Override
//            public void run() {
//                super.run();
//                try {
//                    String result = post("http://api.m.mtime.cn/PageSubArea/TrailerList.api", "");
//                    Log.e("TAG", result);
//                    Message msg = Message.obtain();
//                    msg.what = POST;
//                    msg.obj = result;
//                    handler.sendMessage(msg);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//
//    }


    /**
     * 异步GET请求
     */
    private void getAsynHttp() {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://www.baidu.com")
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showLog(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String str = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplication(), str, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    /**
     * 同步GET请求
     * @return
     * @throws IOException
     */
    private String getSyncHttp() throws IOException{
        OkHttpClient mOkHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://www.baidu.com")
                .build();
        Call call = mOkHttpClient.newCall(request);
        Response mResponse=call.execute();
        if (mResponse.isSuccessful()) {
            return mResponse.body().string();
        } else {
            throw new IOException("Unexpected code " + mResponse);
        }
    }

    /**
     *异步POST请求
     */
    private void postAsynHttp() {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("topicId", "1002")
                .add("maxReply", "-1")
                .add("reqApp", "1")
                .build();

        Request request = new Request.Builder()
                .url("http://61.129.89.191/SoarAPI/api/SoarTopic")
                .post(formBody)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showLog(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String str = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 异步上传文件
     */
    private void postAsynFile() {
        OkHttpClient mOkHttpClient=new OkHttpClient();
        File file = new File("/sdcard/demo.txt");
        Request request = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .post(RequestBody.create(MEDIA_TYPE_MARKDOWN, file))
                .build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showLog(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                showLog(response.body().string());
            }
        });
    }

    /**
     * 异步下载图片
     */
    private void getAsynFile() {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://img-my.csdn.net/uploads/201309/01/1378037128_5291.jpg")
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showLog(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final byte[] data = response.body().bytes();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        image.setImageBitmap(bmp);
                    }
                });
            }
        });
    }

    /**
     * okHttp网络请求之封装GET请求
     */
    private void getDate(){
        OkHttpManager.getInstance(this).getAsynHttp("http://www.baidu.com", new ReqCallBack<String>() {
            @Override
            public void onReqSuccess(String result) {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReqFailed(String errorMsg) {
                showLog(errorMsg);
            }
        });
    }

    private void showLog(String message) {
        Log.e("TAG", message);
    }

}
