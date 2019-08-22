package com.example.okhttptest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.okhttptest.Interceptor.CacheInterceptor;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    /**创建一个定时的线程池**/
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
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

    private Cache cache;

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
        init();
    }

    public void init() {
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
                cancelCall();
//                getDateByOffline();
//                getDateByCache();
//                getCache();
//                getDate();
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

    /**
     * 支持缓存的get请求，只有仅仅使用缓存或网络
     */
    private void getCache(){

        /**
         * 初始化缓存
         */
        File sdcache = getExternalCacheDir();
        int cacheSize = 10 * 1024 * 1024;
        cache = new Cache(sdcache.getAbsoluteFile(), cacheSize);

        OkHttpClient okHttpClient = new OkHttpClient();
        OkHttpClient mOkHttpClient = okHttpClient.newBuilder()
                .addNetworkInterceptor(new CacheInterceptor())
                .cache(cache)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        /**
         * CacheControl.FORCE_CACHE; //仅仅使用缓存
         * CacheControl.FORCE_NETWORK;//仅仅使用网络
         */
        Request request = new Request.Builder()
                .url("http://www.baidu.com")
                .build();
//        final Request request = new Request.Builder()
//                .url("http://www.baidu.com")
//                .cacheControl(CacheControl.FORCE_NETWORK)
//                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (null != response.cacheResponse()) {
                    String str = response.cacheResponse().toString();
                    showLog("cache--->" + str);
                } else {
                    response.body().string();
                    String str=response.networkResponse().toString();
                    showLog("network--->" + str);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "请求成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 第一种类型（有网和没有网都是先读缓存）
     * 如果cache没有过期会直接返回cache而不会发起网络请求，若过期会自动发起网络请求。
     */
    private void getDateByCache(){
        /**
         * 初始化缓存
         */
        File sdcache = getExternalCacheDir();
        int cacheSize = 10 * 1024 * 1024;
        cache = new Cache(sdcache.getAbsoluteFile(), cacheSize);

        /**创建拦截器**/
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Response response = chain.proceed(request);
                String cacheControl = request.cacheControl().toString();
                if (TextUtils.isEmpty(cacheControl)) {
                    cacheControl = "public, max-age=60";
                }
                return response.newBuilder()
                        .header("Cache-Control", cacheControl)
                        .removeHeader("Pragma")
                        .build();
            }
        };

        /**创建OkHttpClient，并添加拦截器和缓存代码**/
        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor)
                .cache(cache).build();

        Request request = new Request.Builder()
                .url("http://www.baidu.com")
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (null != response.cacheResponse()) {
                    String str = response.cacheResponse().toString();
                    showLog("cache--->" + str);
                } else {
                    response.body().string();
                    String str=response.networkResponse().toString();
                    showLog("network--->" + str);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "请求成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    /**
     * 第二种类型（离线可以缓存，在线就获取最新数据）
     */
    private void getDateByOffline(){
        /**
         * 初始化缓存
         */
        File sdcache = getExternalCacheDir();
        int cacheSize = 10 * 1024 * 1024;
        cache = new Cache(sdcache.getAbsoluteFile(), cacheSize);

        /**
         * 有网时候的缓存
         */
        final Interceptor NetCacheInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Response response = chain.proceed(request);
                int onlineCacheTime = 0;//在线的时候的缓存过期时间，如果想要不缓存，直接时间设置为0
                return response.newBuilder()
                        .header("Cache-Control", "public, max-age="+onlineCacheTime)
                        .removeHeader("Pragma")
                        .build();
            }
        };
        /**
         * 没有网时候的缓存
         */
        final Interceptor OfflineCacheInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                if (!isNetworkAvailable(MainActivity.this)) {
                    int offlineCacheTime = 60;//离线的时候的缓存的过期时间
                    request = request.newBuilder()
//                        .cacheControl(new CacheControl
//                                .Builder()
//                                .maxStale(60,TimeUnit.SECONDS)
//                                .onlyIfCached()
//                                .build()
//                        ) 两种方式结果是一样的，写法不同
                            .header("Cache-Control", "public, only-if-cached, max-stale=" + offlineCacheTime)
                            .build();
                }
                return chain.proceed(request);
            }
        };

        /**创建OkHttpClient，并添加拦截器和缓存代码**/
        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(NetCacheInterceptor)
                .addInterceptor(OfflineCacheInterceptor)
                .cache(cache)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        final Request request = new Request.Builder()
                .url("http://www.baidu.com")
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (null != response.cacheResponse()) {
                    String str = response.cacheResponse().toString();
                    showLog("cache--->" + str);
                } else {
                    response.body().string();
                    String str=response.networkResponse().toString();
                    showLog("network--->" + str);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "请求成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    /**
     * 取消call
     * 使用Call.cancel()可以节约网络资源，另外不管同步还是异步的call都可以取消。 
     */
    private  void cancelCall(){
        OkHttpClient mOkHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://www.baidu.com")
                .cacheControl(CacheControl.FORCE_NETWORK)
                .build();
        Call call = null;
        call = mOkHttpClient.newCall(request);
        final Call finalCall = call;
        //100毫秒后取消call
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                finalCall.cancel();
            }
        }, 1, TimeUnit.MILLISECONDS);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call,final Response response) {
                if (null != response.cacheResponse()) {
                    String str = response.cacheResponse().toString();
                    showLog("cache--->" + str);
                } else {
                    try {
                        response.body().string();
                    } catch (IOException e) {
                        showLog("IOException");
                        e.printStackTrace();
                    }
                    String str = response.networkResponse().toString();
                    showLog("network--->" + str);
                }
            }
        });
        showLog("是否取消成功"+call.isCanceled());
    }

    /**
     * 判断网络是否连接
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    private void showLog(String message) {
        Log.e("TAG", message);
    }

}
