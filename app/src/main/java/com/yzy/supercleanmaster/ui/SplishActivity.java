package com.yzy.supercleanmaster.ui;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.yzy.supercleanmaster.R;
import com.yzy.supercleanmaster.base.BaseActivity;
import com.yzy.supercleanmaster.service.CleanerService;
import com.yzy.supercleanmaster.service.CoreService;
import com.yzy.supercleanmaster.utils.SharedPreferencesUtils;

import java.util.Random;


public class SplishActivity extends BaseActivity {

    /**
     * 淡入动画
     */
    private Animation mFadeIn;
    /**
     * 放大动画
     */
    private Animation mFadeInScale;
    /**
     * 淡出动画
     */
    private Animation mFadeOut;

    //  @InjectView(R.id.image)
    ImageView mImageView;

    public static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splish);
        mImageView = (ImageView) findViewById(R.id.image);
        int index = new Random().nextInt(2);
        if (index == 1) {
            mImageView.setImageResource(R.drawable.entrance3);
        } else {
            mImageView.setImageResource(R.drawable.entrance2);
        }
        startService(new Intent(this, CoreService.class));
        startService(new Intent(this, CleanerService.class));

        if (!SharedPreferencesUtils.isShortCut(mContext)) {
            createShortCut();
        }

        initAnim();
        setListener();
    }

    /**
     * 创建快捷方式
     */
    private void createShortCut() {
        //创建快捷方式的Intent
        Intent shortCutIntent = new Intent();
        shortCutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        //快捷方式的名称
        shortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "一键加速");
        //不允许重复创建
        shortCutIntent.putExtra("duplicate", false);
        //快捷方式的图标
        shortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapFactory.decodeResource(getResources(), R.drawable.short_cut_icon));
        Intent enterIntent = new Intent();
        enterIntent.setAction("com.yzy.shortcut");
        enterIntent.addCategory("android.intent.category.DEFAULT");
        //点击快捷图片，运行的程序主入口：enterIntent
        shortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, enterIntent);
        //发送广播
        sendBroadcast(shortCutIntent);

        SharedPreferencesUtils.setIsShortCut(mContext, true);
    }

    private void initAnim() {
        mFadeIn = AnimationUtils.loadAnimation(this, R.anim.welcome_fade_in);
        mFadeIn.setDuration(500);

        mFadeInScale = AnimationUtils.loadAnimation(this,
                R.anim.welcome_fade_in_scale);
        mFadeInScale.setDuration(2000);

        mFadeOut = AnimationUtils.loadAnimation(this, R.anim.welcome_fade_out);
        mFadeOut.setDuration(500);

        mImageView.startAnimation(mFadeIn);
    }


    /**
     * 监听事件
     */
    public void setListener() {
        /**
         * 动画切换原理:开始时是用第一个渐现动画,当第一个动画结束时开始第二个放大动画,当第二个动画结束时调用第三个渐隐动画,
         * 第三个动画结束时修改显示的内容并且重新调用第一个动画,从而达到循环效果
         */
        mFadeIn.setAnimationListener(new AnimationListener() {

            public void onAnimationStart(Animation animation) {

            }

            public void onAnimationRepeat(Animation animation) {

            }

            public void onAnimationEnd(Animation animation) {
                mImageView.startAnimation(mFadeInScale);
            }
        });
        mFadeInScale.setAnimationListener(new AnimationListener() {

            public void onAnimationStart(Animation animation) {

            }

            public void onAnimationRepeat(Animation animation) {

            }

            public void onAnimationEnd(Animation animation) {
                startActivity(MainActivity.class);
                finish();
                // mImageView.startAnimation(mFadeOut);
            }
        });
        mFadeOut.setAnimationListener(new AnimationListener() {

            public void onAnimationStart(Animation animation) {

            }

            public void onAnimationRepeat(Animation animation) {

            }

            public void onAnimationEnd(Animation animation) {
                // startActivity(MainActivity.class);
            }
        });
    }
}
