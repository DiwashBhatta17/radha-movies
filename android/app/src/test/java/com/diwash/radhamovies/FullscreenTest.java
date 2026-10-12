package com.diwash.radhamovies;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebChromeClient;
import android.widget.Button;
import android.widget.FrameLayout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk={28,35})
public class FullscreenTest {
  private Button findButton(View view,String text){if(view instanceof Button&&text.contentEquals(((Button)view).getText()))return (Button)view;if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++){Button result=findButton(group.getChildAt(i),text);if(result!=null)return result;}}return null;}
  @Test public void fullscreenFillsRootAndRestoresOrientation(){
    try(ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup()){
      MainActivity activity=controller.get();activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      FrameLayout content=activity.findViewById(android.R.id.content);FrameLayout root=(FrameLayout)content.getChildAt(0);WebView web=(WebView)root.getChildAt(0);WebChromeClient chrome=web.getWebChromeClient();
      root.setPadding(0,30,0,40);int[] exits={0};View video=new FrameLayout(activity);
      chrome.onShowCustomView(video,()->exits[0]++);
      assertEquals(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE,activity.getRequestedOrientation());
      assertEquals(0,root.getPaddingTop());assertEquals(0,root.getPaddingBottom());assertNotNull(video.getParent());
      assertTrue((activity.getWindow().getAttributes().flags&WindowManager.LayoutParams.FLAG_FULLSCREEN)!=0);
      assertNotNull(findButton(root,"Rotate"));assertNotNull(findButton(root,"Exit"));
      activity.getResources().getConfiguration().orientation=android.content.res.Configuration.ORIENTATION_LANDSCAPE;
      findButton(root,"Rotate").performClick();assertEquals(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT,activity.getRequestedOrientation());
      activity.getResources().getConfiguration().orientation=android.content.res.Configuration.ORIENTATION_PORTRAIT;
      findButton(root,"Rotate").performClick();assertEquals(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE,activity.getRequestedOrientation());
      activity.onBackPressed();assertEquals(1,exits[0]);assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,activity.getRequestedOrientation());
      assertEquals(1,root.getChildCount());assertEquals(0,activity.getWindow().getAttributes().flags&WindowManager.LayoutParams.FLAG_FULLSCREEN);
      chrome.onHideCustomView();assertEquals(1,exits[0]);
      chrome.onShowCustomView(new FrameLayout(activity),()->exits[0]++);findButton(root,"Exit").performClick();assertEquals(2,exits[0]);
    }
  }
  @Test public void popupDenialDoesNotRejectFullscreen(){
    try(ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup()){
      MainActivity activity=controller.get();FrameLayout root=(FrameLayout)((FrameLayout)activity.findViewById(android.R.id.content)).getChildAt(0);WebView web=(WebView)root.getChildAt(0);
      assertFalse(web.getWebChromeClient().onCreateWindow(web,false,true,new android.os.Message()));
      web.getWebChromeClient().onShowCustomView(new FrameLayout(activity),()->{});assertEquals(2,root.getChildCount());
      web.getWebChromeClient().onHideCustomView();
    }
  }
  @Test public void doubleTapSeeksWithoutClosingFullscreen(){
    try(ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup()){
      MainActivity activity=controller.get();FrameLayout root=(FrameLayout)((FrameLayout)activity.findViewById(android.R.id.content)).getChildAt(0);WebView web=(WebView)root.getChildAt(0);int[] exits={0};
      web.getWebChromeClient().onShowCustomView(new FrameLayout(activity),()->exits[0]++);root.layout(0,0,1000,600);
      for(int i=0;i<2;i++){
        android.view.MotionEvent down=android.view.MotionEvent.obtain(0,0,android.view.MotionEvent.ACTION_DOWN,750,300,0);activity.dispatchTouchEvent(down);down.recycle();
        android.view.MotionEvent up=android.view.MotionEvent.obtain(0,10,android.view.MotionEvent.ACTION_UP,750,300,0);activity.dispatchTouchEvent(up);up.recycle();
      }
      assertEquals(0,exits[0]);assertEquals(2,root.getChildCount());
      assertTrue(org.robolectric.Shadows.shadowOf(web).getLastEvaluatedJavascript().contains("radhaSeek(10)"));
      web.getWebChromeClient().onHideCustomView();
    }
  }
}
