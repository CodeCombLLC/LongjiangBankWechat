package com.ufreedom.simplewebview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author JON
 */
@SuppressLint("SetJavaScriptEnabled")
public class FansWebActivity extends Activity implements OnClickListener {

    private TabMenu tabMenu;


    private final static String TAG = "FansWebActivity";
    public WebView webView;
    private Activity mContext;
    private Handler mHandler;
    private ProgressBar pb;
    private static long cacheTime = 1 * 60 * 60 * 1000;// webView缓存一小时
    private String baseUrl;
    private String downFileName;
    private UploadHandler mUploadHandler;

    private boolean allowCancelDown = false;
    private boolean closePageWhenDownStart = false;

    private ImageView /*imgBack,*/ imgClose;
    private TextView txtTitle;

    private int ViewType = 0;
    public static final int TYPE_NORMAL = 4; // 一般页面
    public boolean isDownUrl = false;
    private static final int SHOW_LOADING = 1;
    private static final int HIDE_LOADING = 2;
    private static final int LOADING_ERROR = 3;

    private static final int CLOSE_WEB = 4;

    private boolean isNeedChangeTitle = false;


    FansWebActivity fansWebActivity;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.common_webview_layout);

        tabMenu = new TabMenu(this, new OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("http://221.209.110.83:9989/Common");
                tabMenu.dismiss();
            }
        }, R.drawable.ic_home_white_36dp, "返回主页",
                16, Color.WHITE, Color.BLACK, R.style.PopupAnimation);
        
        fansWebActivity = this;
        initView();
        initData();
    }

    public void initView() {
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setProgressBarVisibility(false);
        mContext = this;
        Window window = this.getWindow();
        if (window != null) {
            View view = window.getDecorView();
            if (view != null) {
                view.setKeepScreenOn(true);
            }
        }

        pb = (ProgressBar) this.findViewById(R.id.loading_process_dialog_progressBar);
        pb.setMax(10000);

//        imgBack = (ImageView) this.findViewById(R.id.imgBack);
//        imgBack.setOnClickListener(this);
      
        imgClose = (ImageView) this.findViewById(R.id.imgClose);
        imgClose.setOnClickListener(this);
        txtTitle = (TextView) this.findViewById(R.id.txtTitle);

        isNeedChangeTitle = true;
        
        txtTitle.setText("网页");

        webView = (WebView) this.findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        initWebViewSettings(webView);
        syncStaticSettings(webSettings);

        if (hasJellyBeanMR1()) {
            try {
                Method meth = webSettings.getClass().getMethod("enablePlatformNotifications");
                if (meth != null) {
                    meth.invoke(webSettings);
                }
            } catch (Exception e) {
                // DLOG.exception(e);
            }
        }
    }

    public void initData() {
        clearCacheFolder(FansWebActivity.this.getCacheDir(), System.currentTimeMillis() - cacheTime);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SHOW_LOADING:
                        if (pb != null) {
                            pb.setVisibility(View.VISIBLE);
                        }
                        break;
                    case HIDE_LOADING:
                        if (pb != null) {
                            pb.setVisibility(View.GONE);
                        }
                        break;

                    case LOADING_ERROR:
                        if (pb != null) {
                            pb.setVisibility(View.GONE);
                            webView.loadUrl("file:///android_asset/nonetwork.html");
                        }
                        break;
                    case CLOSE_WEB:
                        if (closePageWhenDownStart) {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    finish();
                                }
                            });
                            closePageWhenDownStart = false;
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };
    //    Intent intent = this.getIntent();
        allowCancelDown = true;
      //  String url = intent.getStringExtra("url");

       /* if (intent.hasExtra("downfilename")) {
            downFileName = intent.getStringExtra("downfilename");
        }*/
        closePageWhenDownStart = false;
        ViewType = TYPE_NORMAL;

        isDownUrl = false;
        
  /*      if (url != null && url.endsWith(".apk")) {
            isDownUrl = true;
        } else {
            isDownUrl = false;
        }*/

        webView.loadUrl("http://221.209.110.83:9989/Common");
        baseUrl = "http://221.209.110.83:9989/Common";
    }


    private String mAppCachePath;

    @SuppressLint("NewApi")
    private void syncStaticSettings(WebSettings settings) {
        // Enable the built-in zoom
        settings.setBuiltInZoomControls(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);// 设置缓冲模式
        settings.setJavaScriptEnabled(true);// 设置是否支持JavaScript
        String oldUa = settings.getUserAgentString();
        settings.setUserAgentString("duomi-".concat(oldUa == null ? "" : oldUa));
        final PackageManager pm = mContext.getPackageManager();
        if (android.os.Build.VERSION.SDK_INT >= 13) {
            boolean supportsMultiTouch = pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH)
                    || pm.hasSystemFeature(PackageManager.FEATURE_FAKETOUCH_MULTITOUCH_DISTINCT);
            settings.setDisplayZoomControls(!supportsMultiTouch);

        } else {
            if (android.os.Build.VERSION.SDK_INT >= 11)
                settings.setDisplayZoomControls(true);
        }

        settings.setDefaultFontSize(16);
        settings.setDefaultFixedFontSize(13);

        // WebView inside Browser doesn't want initial focus to be set.
        settings.setNeedInitialFocus(false);

        //设置网页自适应屏幕
        settings.setUseWideViewPort(true);

        // 默认的是false，如果设置为true，必须重写WebChromeClint的onCreatWindow方法
        //如果为false，则默认为当前webview加载新的页面        

        // enable smooth transition for better performance during panning or
        // zooming
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            settings.setEnableSmoothTransition(true);
            // disable content url access
            settings.setAllowContentAccess(false);
        }

        // HTML5 API flags
        settings.setAppCacheEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        //settings.setWorkersEnabled(true);  // This only affects V8.

        // HTML5 configuration parametersettings.
        settings.setAppCacheMaxSize(1024 * 1024 * 50);
        settings.setAppCachePath(getAppCachePath());
        settings.setDatabasePath(mContext.getDir("databases", 0).getPath());
        settings.setGeolocationDatabasePath(mContext.getDir("geolocation", 0).getPath());

        settings.setGeolocationEnabled(true);//支持geo
        

    }

    private String getAppCachePath() {
        if (mAppCachePath == null) {
            mAppCachePath = mContext.getDir("appcache", 0).getPath();
        }
        return mAppCachePath;
    }

    private void initWebViewSettings(WebView w) {
        w.clearView();
        w.clearHistory();
        w.removeAllViews();
        w.setVerticalScrollBarEnabled(true);
        w.setHorizontalScrollBarEnabled(true);
        w.setMapTrackballToArrowKeys(false); // use trackball directly
        w.setWebViewClient(new MyWebViewClient());
        w.setWebChromeClient(new DuomiChromeClient());
        w.setDownloadListener(new DuomiWebViewDownLoadListener());

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == Controller.FILE_SELECTED && mUploadHandler != null) {
            // Chose a file from the file picker.
            mUploadHandler.onResult(resultCode, intent);
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
     //   getMenuInflater().inflate(R.menu.menu_main, menu);

      /*  menu.add(0,Menu.FIRST,0,"主页").setIcon(android.R.drawable.ic_menu_help);
        menu.add(0,Menu.FIRST+1,1,"我的订单").setIcon(android.R.drawable.ic_menu_save);
        setMenuBackground();
*/

        menu.add("menu");// 必须创建一项    
        return super.onCreateOptionsMenu(menu);
    }

    /** 拦截MENU */
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (tabMenu != null) {
            if (tabMenu.isShowing())
                tabMenu.dismiss();
            else {
                tabMenu.showAtLocation(findViewById(R.id.webview), Gravity.BOTTOM, 0, 0);
            }
        }
        return false;// 返回为true 则显示系统menu    
    }  
    

  /*  @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case Menu.FIRST:
               
                break;
            case Menu.FIRST + 1:
                
                break;
        }
        return true;
    }
*/
/*
    //菜单被显示之前的事件
    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
//            Toast.makeText(this,"菜单被显示之前的事件，可以在这里调整菜单",Toast.LENGTH_LONG).show();
        return true;
        //必须返回True才能调用onCreateOptionsMenu(Menu menu)方法。
    }
*/


 
    
    
    public static boolean hasJellyBeanMR1() {
        return android.os.Build.VERSION.SDK_INT >= 17;
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            if (imgClose.getVisibility() != View.VISIBLE) {
                imgClose.setVisibility(View.VISIBLE);
            }
            return;
        }
        super.onBackPressed();
    }

    private int clearCacheFolder(File dir, long numDays) {
        int deletedFiles = 0;
        if (dir != null && dir.isDirectory()) {
            try {
                for (File child : dir.listFiles()) {
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, numDays);
                    }
                    if (child.lastModified() < numDays && child.delete()) {
                        deletedFiles++;
                    }
                }
            } catch (Exception e) {
//                DLOG.exception(e);
            }
        }
        return deletedFiles;
    }

    public void preDealDownload(String downloadUrl) {
        try {
//            if (NetGateway.isWifiOnlyLimit()) {
//                DMG.showToast(RT.getString(R.string.layout_network_wifi));
//                return;
//            }
            URL url = new URL(downloadUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(30 * 1000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/octet-stream,application/vnd.android.package-archive");
            conn.setRequestProperty("Accept-Language", "zh-CN");
            conn.setRequestProperty("Referer", downloadUrl);
            conn.setRequestProperty("Charset", "UTF-8");
  //          conn.setRequestProperty("User-Agent", RT.getPhonInfo().userAgent);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.connect();
            if (conn.getResponseCode() == 200) {
                // mHandler.sendEmptyMessageDelayed(2, 100);
                long totalLength = conn.getContentLength();
                String contentType = conn.getContentType();
                Bundle data = new Bundle();
                data.putString("url", downloadUrl);
                data.putString("mimetype", contentType);
                data.putLong("contentLength", totalLength);
                data.putBoolean("downCancelAllow", allowCancelDown);
//                Intent intent = new Intent();
//                intent.setAction("com.duomi.apps.ad.AppDownloadService");
//                intent.putExtras(data);
//                try {
//                    RT.application.startService(intent);
//                    mHandler.sendEmptyMessage(CLOSE_WEB);
//                } catch (Exception e) {
//                    DLOG.exception(e);
//                }
            } else {
                mHandler.sendEmptyMessage(CLOSE_WEB);
                throw new RuntimeException("server no response ");
            }
        } catch (Exception e) {
//            DLOG.exception(e);
        }
    }

    // 如果不做任何处理，浏览网页，点击系统“Back”键，整个Browser会调用finish()而结束自身，
    // 如果希望浏览的网 页回退而不是推出浏览器，需要在当前Activity中处理并消费掉该Back事件。
    //    @Override
    //    public boolean onKeyUp(int keyCode, KeyEvent event) {
    //        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
    //            if (webView.canGoBack()) {
    //                if (pageIndex <= 1) {
    //                    this.finish();
    //                } else {
    //                    webView.goBack();
    //                    pageIndex--;
    //                }
    //            } else {
    //                clearWebView();
    //                this.finish();
    //            }
    //            return true;
    //        }
    //        if (RT.DEBUG) DLOG.e(TAG, "onKeyUp.......");
    //        return super.onKeyUp(keyCode, event);
    //    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {
            Uri uri = Uri.parse(url);
            String scheme = uri.getScheme();

            //调用拨号程序
            if (url.startsWith("mailto:") || url.startsWith("geo:") ||url.startsWith("tel:")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }
        

        if (url.indexOf(".apk") != -1) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        preDealDownload(url);
                    }
                }).start();
                return true;
            }

            if (!("http".equals(scheme) || "https".equals(scheme))) {
                Intent it = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(it);
                } catch (ActivityNotFoundException e) {
                    // handle exception, or just do nothing
                }
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
//            if (RT.DEBUG) DLOG.e(TAG, "onPageStarted>>url>>" + url);
//            if (RT.DEBUG) DLOG.e(TAG, "onPageStarted>>shareurl>>" + shareUrl);
            mHandler.sendEmptyMessage(SHOW_LOADING);
            // 如果是本地相对地址，且不是加载的最初的本地地址
            if (url.startsWith("file://") && !url.equals(baseUrl)) {
//                String host = ADHelper.ADWapPagePreLoader.getHostWithSchemaByLocalPath(baseUrl);// 得到绝对地址的主机部分（包括schema）
//                if (host != null) {
//                    url = host.concat(url.substring(7));// 将相对地址转成绝对地址
//                    webView.stopLoading();// 停止加载错误的地址
//                    webView.loadUrl(url);// 开始加载处理后的地址
//                    return;
//                }
            }
//            shareUrl = url;
            super.onPageStarted(view, url, favicon);
        }

        public void onPageFinished(WebView view, String url) {
//            if (RT.DEBUG) DLOG.e(TAG, "onPageFinished>>url" + url);
            super.onPageFinished(view, url);
//            shareUrl = url;
            mHandler.sendEmptyMessage(HIDE_LOADING);
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//            if (RT.DEBUG) DLOG.e(TAG, "onReceivedError>>failingUrl:" + failingUrl);
            super.onReceivedError(view, errorCode, description, failingUrl);
            mHandler.sendEmptyMessage(LOADING_ERROR);
        }
        
        
    }

    private class DuomiWebViewDownLoadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                                    long contentLength) {
//            if (RT.DEBUG) DLOG.e(TAG, "onDownloadStart>>url:" + url);
//            if (NetGateway.isWifiOnlyLimit()) {
//                DMG.showToast(RT.getString(R.string.layout_network_wifi));
//                return;
//            }
            Bundle data = new Bundle();
            data.putString("url", url);
            data.putString("filename", downFileName);
            data.putString("mimetype", mimetype);
            data.putLong("contentLength", contentLength);
            data.putBoolean("downCancelAllow", allowCancelDown);
            Intent intent = new Intent();
            intent.setAction("com.duomi.apps.ad.AppDownloadService");
            intent.putExtras(data);
            try {
                startService(intent);
            } catch (Exception e) {
//                DLOG.exception(e);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //webView.loadUrl("about:blank");// 解决网页内置音乐无法停止问题
        clearWebView();
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
       /*     case R.id.imgBack:
                onBackPressed();
                break;*/
            case R.id.imgClose:
                finish();
                break;
        }
    }

    private void clearWebView() {
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.clearAnimation();
            webView.clearCache(true);
            webView.clearChildFocus(webView);
            webView.clearDisappearingChildren();
            webView.clearFocus();
            webView.clearFormData();
            webView.clearHistory();
            webView.clearMatches();
            webView.clearSslPreferences();
            webView.clearView();
        }

    }

    class Controller {
        final static int FILE_SELECTED = 4;

        Activity getActivity() {
            return FansWebActivity.this;
        }
    }

    class UploadHandler {
        /*
         * 用于文件上传.
         */
        private ValueCallback<Uri> mUploadMessage;
        private String mCameraFilePath;
        private boolean mHandled;
        private boolean mCaughtActivityNotFoundException;
        private Controller mController;

        public UploadHandler(Controller controller) {
            mController = controller;
        }

        public String getFilePath() {
            return mCameraFilePath;
        }

        boolean handled() {
            return mHandled;
        }

        public void onResult(int resultCode, Intent intent) {
            if (resultCode == Activity.RESULT_CANCELED && mCaughtActivityNotFoundException) {
                // Couldn't resolve an activity, we are going to try again so skip
                // this result.
                mCaughtActivityNotFoundException = false;
                return;
            }
            Uri result = (intent == null || resultCode != Activity.RESULT_OK) ? null : intent.getData();

            // As we ask the camera to save the result of the user taking
            // a picture, the camera application does not return anything other
            // than RESULT_OK. So we need to check whether the file we expected
            // was written to disk in the in the case that we
            // did not get an intent returned but did get a RESULT_OK. If it was,
            // we assume that this result has came back from the camera.
            if (result == null && intent == null && resultCode == Activity.RESULT_OK) {
                File cameraFile = new File(mCameraFilePath);
                if (cameraFile.exists()) {
                    result = Uri.fromFile(cameraFile);
                    // Broadcast to the media scanner that we have a new photo
                    // so it will be added into the gallery for the user.
                    mController.getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
                }
            }
            mUploadMessage.onReceiveValue(result);
            mHandled = true;
            mCaughtActivityNotFoundException = false;
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            final String imageMimeType = "image/*";
            final String videoMimeType = "video/*";
            final String audioMimeType = "audio/*";
            final String mediaSourceKey = "capture";
            final String mediaSourceValueCamera = "camera";
            final String mediaSourceValueFileSystem = "filesystem";
            final String mediaSourceValueCamcorder = "camcorder";
            final String mediaSourceValueMicrophone = "microphone";
            // According to the spec, media source can be 'filesystem' or 'camera' or 'camcorder'
            // or 'microphone' and the default value should be 'filesystem'.
            String mediaSource = mediaSourceValueFileSystem;
            if (mUploadMessage != null) {
                // Already a file picker operation in progress.
                return;
            }
            mUploadMessage = uploadMsg;
            // Parse the accept type.
            String params[] = acceptType.split(";");
            String mimeType = params[0];
            if (capture.length() > 0) {
                mediaSource = capture;
            }
            if (capture.equals(mediaSourceValueFileSystem)) {
                // To maintain backwards compatibility with the previous implementation
                // of the media capture API, if the value of the 'capture' attribute is
                // "filesystem", we should examine the accept-type for a MIME type that
                // may specify a different capture value.
                for (String p : params) {
                    String[] keyValue = p.split("=");
                    if (keyValue.length == 2) {
                        // Process key=value parameters.
                        if (mediaSourceKey.equals(keyValue[0])) {
                            mediaSource = keyValue[1];
                        }
                    }
                }
            }
            //Ensure it is not still set from a previous upload.
            mCameraFilePath = null;
            if (mimeType.equals(imageMimeType)) {
                if (mediaSource.equals(mediaSourceValueCamera)) {
                    // Specified 'image/*' and requested the camera, so go ahead and launch the
                    // camera directly.
                    startActivity(createCameraIntent());
                    return;
                } else {
                    // Specified just 'image/*', capture=filesystem, or an invalid capture parameter.
                    // In all these cases we show a traditional picker filetered on accept type
                    // so launch an intent for both the Camera and image/* OPENABLE.
                    Intent chooser = createChooserIntent(createCameraIntent());
                    chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(imageMimeType));
                    startActivity(chooser);
                    return;
                }
            } else if (mimeType.equals(videoMimeType)) {
                if (mediaSource.equals(mediaSourceValueCamcorder)) {
                    // Specified 'video/*' and requested the camcorder, so go ahead and launch the
                    // camcorder directly.
                    startActivity(createCamcorderIntent());
                    return;
                } else {
                    // Specified just 'video/*', capture=filesystem or an invalid capture parameter.
                    // In all these cases we show an intent for the traditional file picker, filtered
                    // on accept type so launch an intent for both camcorder and video/* OPENABLE.
                    Intent chooser = createChooserIntent(createCamcorderIntent());
                    chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(videoMimeType));
                    startActivity(chooser);
                    return;
                }
            } else if (mimeType.equals(audioMimeType)) {
                if (mediaSource.equals(mediaSourceValueMicrophone)) {
                    // Specified 'audio/*' and requested microphone, so go ahead and launch the sound
                    // recorder.
                    startActivity(createSoundRecorderIntent());
                    return;
                } else {
                    // Specified just 'audio/*',  capture=filesystem of an invalid capture parameter.
                    // In all these cases so go ahead and launch an intent for both the sound
                    // recorder and audio/* OPENABLE.
                    Intent chooser = createChooserIntent(createSoundRecorderIntent());
                    chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(audioMimeType));
                    startActivity(chooser);
                    return;
                }
            }
            // No special handling based on the accept type was necessary, so trigger the default
            // file upload chooser.
            startActivity(createDefaultOpenableIntent());
        }

        private void startActivity(Intent intent) {
            try {
                mController.getActivity().startActivityForResult(intent, Controller.FILE_SELECTED);
            } catch (ActivityNotFoundException e) {
                // No installed app was able to handle the intent that
                // we sent, so fallback to the default file upload control.
                try {
                    mCaughtActivityNotFoundException = true;
                    mController.getActivity().startActivityForResult(createDefaultOpenableIntent(),
                            Controller.FILE_SELECTED);
                } catch (ActivityNotFoundException e2) {
                    // Nothing can return us a file, so file upload is effectively disabled.
//                    Toast.makeText(mController.getActivity(), R.string.webview_uploads_disabled, Toast.LENGTH_LONG)
//                            .show();
                }
            }
        }

        private Intent createDefaultOpenableIntent() {
            // Create and return a chooser with the default OPENABLE
            // actions including the camera, camcorder and sound
            // recorder where available.
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            Intent chooser = createChooserIntent(createCameraIntent(), createCamcorderIntent(),
                    createSoundRecorderIntent());
            chooser.putExtra(Intent.EXTRA_INTENT, i);
            return chooser;
        }

        private Intent createChooserIntent(Intent... intents) {
            Intent chooser = new Intent(Intent.ACTION_CHOOSER);
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
            chooser.putExtra(Intent.EXTRA_TITLE, "选择文件上传");
            // mController.getActivity().getResources().getString(R.string.webview_choose_upload));
            return chooser;
        }

        private Intent createOpenableIntent(String type) {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType(type);
            return i;
        }

        private Intent createCameraIntent() {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File externalDataDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File cameraDataDir = new File(externalDataDir.getAbsolutePath() + File.separator + "browser-photos");
            cameraDataDir.mkdirs();
            mCameraFilePath = cameraDataDir.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".jpg";
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCameraFilePath)));
            return cameraIntent;
        }

        private Intent createCamcorderIntent() {
            return new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        }

        private Intent createSoundRecorderIntent() {
            return new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        }
    }

    class DuomiChromeClient extends WebChromeClient {
        public DuomiChromeClient() {
        }

        public void onProgressChanged(WebView view, final int progress) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (ViewType == TYPE_NORMAL) {
                        if (progress * 100 == pb.getMax()) {
                            pb.setVisibility(View.GONE);
                            return;
                        }
                        pb.setVisibility(View.VISIBLE);
                        pb.setProgress(progress * 100);
                    } else {
                        pb.setVisibility(View.GONE);
                    }
                    Log.e(TAG, "onProgressChanged......." + progress);

                }

            });
        }

        @Override
        public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
            Log.e(TAG, "onReceivedTouchIconUrl......." + url + "," + precomposed);
            super.onReceivedTouchIconUrl(view, url, precomposed);
        }

        @Override
        public void onReceivedTitle(WebView view, String tit) {
            if (isNeedChangeTitle && !isEmpty(tit)) {
                txtTitle.setText(tit);
            }
        }

        @Override
        public void onCloseWindow(WebView window) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    FansWebActivity.this.finish();
                }
            });
            super.onCloseWindow(window);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {


            AlertDialog.Builder builder = new AlertDialog.Builder(fansWebActivity);
            builder.setTitle("温馨提示");
            builder.setMessage(message);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    result.confirm();
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });
            builder.show();
            return true;
        }

        // Android 2.x
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooser(uploadMsg, "");
        }

        // Android 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            openFileChooser(uploadMsg, "", "filesystem");
        }

        // Android 4.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {

            mUploadHandler = new UploadHandler(new Controller());
            mUploadHandler.openFileChooser(uploadMsg, acceptType, capture);
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            super.onGeolocationPermissionsShowPrompt(origin, callback);
            callback.invoke(origin, true, false);
        }
    }

   

    /**
     * 判断字符是否为空
     *
     * @param str
     * @return boolean
     */
    public static boolean isEmpty(String str) {
        if (str == null || "".equals(str.trim()) || "null".equalsIgnoreCase(str))
            return true;
        else return false;
    }
}