package com.example.chats;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.chats.common.Common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;


public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private String url = Common.url;
    ValueCallback mFilePathCallback = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.loadUrl(url);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClientClass());

        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback filePathCallback, FileChooserParams fileChooserParams) {
                mFilePathCallback = filePathCallback;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");

                // 파일 n개 선택 가능하도록 처리
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

                startActivityForResult(intent, 0);
                return true;
            }
        });

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(!task.isSuccessful()){
                            Log.w("FCM Log", "getInstanceId faild", task.getException());
                            return;
                        }
                        // Get new FCM registration token
                        String token = task.getResult();
                        Log.d("FCM Log", "FCM 토근 : " + token);
//                        Toast myToast = Toast.makeText(getApplicationContext(), token, Toast.LENGTH_LONG);
//                        myToast.show();
                        sendRegistrationToServer(token);
                    }
                });
    }

    // 안드로이드 웹뷰에서 파일 첨부하기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("resultCode:: ", String.valueOf(resultCode));
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {

            // 파일 n개 선택한 경우
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && data != null && data.getClipData() != null && data.getClipData().getItemCount() > 0) {
                int count = data.getClipData().getItemCount();

                Uri[] uriArr = new Uri[count];
                for (int i = 0; i < count; i++) {
                    uriArr[i] = data.getClipData().getItemAt(i).getUri();
                }

                mFilePathCallback.onReceiveValue(uriArr);

            } else if (data != null && data.getData() != null) {
                // 파일 1개 선택한 경우
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mFilePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                } else {
                    mFilePathCallback.onReceiveValue(new Uri[]{data.getData()});
                }
            }

            mFilePathCallback = null;

        } else {
            mFilePathCallback.onReceiveValue(null);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

            return super.shouldOverrideUrlLoading(view, request);

        }
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
        System.out.println("token : " + token);
        webView.loadUrl(Common.url+"/?token="+token);



    }
}