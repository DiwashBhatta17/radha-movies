package com.diwash.radhamovies;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.os.Build;
import android.graphics.Color;
import android.media.AudioManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;
import java.util.HashSet;
import java.util.Arrays;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MainActivity extends Activity {
    private static final String HOME="https://radhamovies.netlify.app/";
    private WebView web;
    private FrameLayout root,full;
    private View custom;
    private WebChromeClient.CustomViewCallback customCallback;
    private AudioManager audio;
    private TextView hint;
    private float startY,startX,startBrightness;
    private int startVolume;
    private boolean edge,adjusting;
    private long lastTap;
    private float lastTapX;
    private int previousOrientation;

    static boolean home(Uri uri) { return "https".equals(uri.getScheme()) && "radhamovies.netlify.app".equals(uri.getHost()); }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        audio=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        root=new FrameLayout(this);root.setBackgroundColor(Color.BLACK);setContentView(root);
        root.setOnApplyWindowInsetsListener((v,i)->{if(custom!=null)v.setPadding(0,0,0,0);else v.setPadding(i.getSystemWindowInsetLeft(),i.getSystemWindowInsetTop(),i.getSystemWindowInsetRight(),i.getSystemWindowInsetBottom());return i;});
        web=new WebView(this);root.addView(web,new FrameLayout.LayoutParams(-1,-1));
        WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);
        settings.setSupportMultipleWindows(true);settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setMediaPlaybackRequiresUserGesture(true);settings.setUserAgentString(settings.getUserAgentString()+" RadhaMoviesAndroid/1.0");
        CookieManager.getInstance().setAcceptThirdPartyCookies(web,true);
        installPlayerControls();
        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){
                String scheme=r.getUrl().getScheme();if(!"https".equals(scheme)&&!"http".equals(scheme))return true;
                return r.isForMainFrame()&&!home(r.getUrl());
            }
            @Override public void onPageFinished(WebView v,String url){ if(home(Uri.parse(url)))v.evaluateJavascript("document.documentElement.dataset.nativeApp='android'",null); }
            @Override public void onReceivedError(WebView v,WebResourceRequest r,WebResourceError e){if(r.isForMainFrame())Toast.makeText(MainActivity.this,"Unable to load. Check your connection and reopen Radha Movies.",Toast.LENGTH_LONG).show();}
        });
        web.setWebChromeClient(new WebChromeClient(){
            @Override public boolean onCreateWindow(WebView v,boolean dialog,boolean gesture,Message result){return false;}
            @Override public void onPermissionRequest(PermissionRequest request){request.deny();}
            @Override public void onShowCustomView(View view,CustomViewCallback callback){
                if(custom!=null){callback.onCustomViewHidden();return;}previousOrientation=getRequestedOrientation();custom=view;customCallback=callback;
                root.setPadding(0,0,0,0);
                full=new FrameLayout(MainActivity.this);full.setBackgroundColor(Color.BLACK);full.addView(view,new FrameLayout.LayoutParams(-1,-1));root.addView(full,new FrameLayout.LayoutParams(-1,-1));
                hint=new TextView(MainActivity.this);hint.setTextColor(Color.WHITE);hint.setBackgroundColor(0xAA101111);hint.setPadding(28,18,28,18);hint.setText("Swipe left edge: brightness · right edge: volume");
                FrameLayout.LayoutParams hp=new FrameLayout.LayoutParams(-2,-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL);full.addView(hint,hp);hint.postDelayed(()->{if(hint!=null)hint.setVisibility(View.INVISIBLE);},4000);
                LinearLayout actions=new LinearLayout(MainActivity.this);
                Button rotate=new Button(MainActivity.this);rotate.setText("Rotate");rotate.setContentDescription("Rotate fullscreen video");rotate.setOnClickListener(v->setRequestedOrientation(getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE?ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT:ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE));actions.addView(rotate);
                Button close=new Button(MainActivity.this);close.setText("Exit");close.setContentDescription("Exit fullscreen");close.setOnClickListener(v->exitFull());actions.addView(close);
                full.addView(actions,new FrameLayout.LayoutParams(-2,-2,Gravity.TOP|Gravity.END));
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                setFullscreenBars(true);
            }
            @Override public void onHideCustomView(){exitFull();}
        });
        web.setDownloadListener((url,ua,disposition,type,length)->Toast.makeText(this,"Downloads from the player are blocked.",Toast.LENGTH_SHORT).show());
        web.loadUrl(HOME);
    }
    private void setFullscreenBars(boolean enabled){
        if(enabled)getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(Build.VERSION.SDK_INT>=30){
            WindowInsetsController controller=getWindow().getInsetsController();
            if(controller!=null){if(enabled){controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);controller.hide(WindowInsets.Type.systemBars());}else controller.show(WindowInsets.Type.systemBars());}
        }else getWindow().getDecorView().setSystemUiVisibility(enabled?View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION:View.SYSTEM_UI_FLAG_VISIBLE);
        root.requestApplyInsets();
    }
    private void exitFull(){if(custom==null)return;WebChromeClient.CustomViewCallback callback=customCallback;customCallback=null;custom=null;root.removeView(full);full=null;hint=null;setFullscreenBars(false);setRequestedOrientation(previousOrientation);if(callback!=null)callback.onCustomViewHidden();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused&&custom!=null)setFullscreenBars(true);}
    @Override public boolean dispatchTouchEvent(android.view.MotionEvent e){
        if(custom==null)return super.dispatchTouchEvent(e);
        float width=root.getWidth(),height=root.getHeight();
        if(e.getActionMasked()==MotionEvent.ACTION_DOWN){startX=e.getX();startY=e.getY();edge=startX<width*.18f||startX>width*.82f;adjusting=false;startVolume=audio.getStreamVolume(AudioManager.STREAM_MUSIC);startBrightness=getWindow().getAttributes().screenBrightness;if(startBrightness<0)startBrightness=.5f;}
        if(edge&&e.getActionMasked()==MotionEvent.ACTION_MOVE&&Math.abs(e.getY()-startY)>24){
            if(!adjusting){MotionEvent cancel=MotionEvent.obtain(e);cancel.setAction(MotionEvent.ACTION_CANCEL);super.dispatchTouchEvent(cancel);cancel.recycle();adjusting=true;}
            float delta=(startY-e.getY())/height;String text;
            if(startX>width/2){int max=audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC),level=Math.max(0,Math.min(max,startVolume+Math.round(delta*max*2)));audio.setStreamVolume(AudioManager.STREAM_MUSIC,level,0);text="Volume "+Math.round(100f*level/max)+"%";}
            else{WindowManager.LayoutParams params=getWindow().getAttributes();params.screenBrightness=Math.max(.03f,Math.min(1,startBrightness+delta*2));getWindow().setAttributes(params);text="Brightness "+Math.round(params.screenBrightness*100)+"%";}
            if(hint!=null){hint.setText(text);hint.setVisibility(View.VISIBLE);}return true;
        }
        if(e.getActionMasked()==MotionEvent.ACTION_UP){if(adjusting){if(hint!=null)hint.postDelayed(()->{if(hint!=null)hint.setVisibility(View.INVISIBLE);},800);return true;}
            long now=System.currentTimeMillis();if(startY>height*.2f&&startY<height*.8f&&Math.abs(e.getY()-startY)<24&&now-lastTap<300&&Math.abs(e.getX()-lastTapX)<80){final int seconds=e.getX()<width/2?-10:10;if(home(Uri.parse(web.getUrl()==null?HOME:web.getUrl())))web.evaluateJavascript("window.radhaSeek && window.radhaSeek("+seconds+")",null);lastTap=0;return true;}lastTap=now;lastTapX=e.getX();}
        return super.dispatchTouchEvent(e);
    }
    @Override public void onBackPressed(){if(custom!=null){exitFull();return;}web.evaluateJavascript("(()=>{const p=document.querySelector('#player');if(p&&p.open){p.close();return true;}return false;})()",value->{if(!"true".equals(value))finish();});}
    @Override protected void onPause(){super.onPause();web.onPause();}
    @Override protected void onResume(){super.onResume();if(web!=null)web.onResume();}
    @Override protected void onDestroy(){web.destroy();super.onDestroy();}
    private void installPlayerControls(){
        if(!WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT))return;
        try(InputStream input=getAssets().open("player-controls.js");ByteArrayOutputStream output=new ByteArrayOutputStream()){
            byte[] buffer=new byte[4096];int count;while((count=input.read(buffer))!=-1)output.write(buffer,0,count);
            WebViewCompat.addDocumentStartJavaScript(web,output.toString("UTF-8"),new HashSet<>(Arrays.asList("https://vidy.st","https://www.vidy.st")));
        }catch(java.io.IOException error){android.util.Log.w("RadhaMovies","Player controls unavailable",error);}
    }
}
