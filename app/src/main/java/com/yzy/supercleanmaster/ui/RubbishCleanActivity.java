package com.yzy.supercleanmaster.ui;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.text.format.Formatter;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.etiennelawlor.quickreturn.library.enums.QuickReturnType;
import com.etiennelawlor.quickreturn.library.listeners.QuickReturnListViewOnScrollListener;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.yzy.supercleanmaster.R;
import com.yzy.supercleanmaster.adapter.RubbishMemoryAdapter;
import com.yzy.supercleanmaster.base.BaseSwipeBackActivity;
import com.yzy.supercleanmaster.model.CacheListItem;
import com.yzy.supercleanmaster.model.StorageSize;
import com.yzy.supercleanmaster.service.CleanerService;
import com.yzy.supercleanmaster.utils.StorageUtil;
import com.yzy.supercleanmaster.utils.SystemBarTintManager;
import com.yzy.supercleanmaster.utils.UIElementsHelper;
import com.yzy.supercleanmaster.widget.textcounter.CounterView;
import com.yzy.supercleanmaster.widget.textcounter.formatters.DecimalFormatter;

import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;
import butterknife.OnClick;

/**
 * 垃圾清理 Activity，
 */
public class RubbishCleanActivity extends BaseSwipeBackActivity implements OnDismissCallback, CleanerService.OnActionListener {

//    protected static final int SCANING = 5;
//
//    protected static final int SCAN_FINIFSH = 6;
//    protected static final int PROCESS_MAX = 8;
//    protected static final int PROCESS_PROCESS = 9;
//
//    private static final int INITIAL_DELAY_MILLIS = 300;
//    SwingBottomInAnimationAdapter swingBottomInAnimationAdapter;

    private CleanerService mCleanerService;

    /**是否已经扫描完，true：已经扫描完毕  false：还没扫描完*/
    private boolean mAlreadyScanned = false;

    private boolean mAlreadyCleaned = false;

    @InjectView(R.id.listview)
    ListView mListView;

    @InjectView(R.id.empty)
    TextView mEmptyView;

    @InjectView(R.id.header)
    RelativeLayout header;

    @InjectView(R.id.textCounter)
    CounterView textCounter;
    @InjectView(R.id.suffix)
    TextView suffix;

    @InjectView(R.id.progressBar)
    View mProgressBar;
    @InjectView(R.id.progressBarText)
    TextView mProgressBarText;

    RubbishMemoryAdapter rubbishMemoryAdapter;

    /**缓存列表，需要清理的垃圾*/
    List<CacheListItem> mCacheListItem = new ArrayList<>();

    /**就是下方一键清理按钮*/
    @InjectView(R.id.bottom_lin)
    LinearLayout ll_bottom;

    @InjectView(R.id.clear_button)
    Button clearButton;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCleanerService = ((CleanerService.CleanerServiceBinder) service).getService();
            mCleanerService.setOnActionListener(RubbishCleanActivity.this);

            if (!mCleanerService.isScanning() && !mAlreadyScanned) {
                mCleanerService.scanCache();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCleanerService.setOnActionListener(null);
            mCleanerService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rublish_clean);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        int footerHeight = mContext.getResources().getDimensionPixelSize(R.dimen.footer_height);

        mListView.setEmptyView(mEmptyView);
        rubbishMemoryAdapter = new RubbishMemoryAdapter(mContext, mCacheListItem);
        mListView.setAdapter(rubbishMemoryAdapter);
        mListView.setOnItemClickListener(rubbishMemoryAdapter);
        //ListView 滑动监听
        mListView.setOnScrollListener(new QuickReturnListViewOnScrollListener(QuickReturnType.FOOTER, null, 0, ll_bottom, footerHeight));
        //绑定清理服务，在服务中执行清理
        bindService(new Intent(mContext, CleanerService.class),
                mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDismiss(@NonNull ViewGroup viewGroup, @NonNull int[] ints) {

    }

    @Override
    public void onScanStarted(Context context) {
        mProgressBarText.setText(R.string.scanning);
        showProgressBar(true);
    }

    @Override
    public void onScanProgressUpdated(Context context, int current, int max) {
        mProgressBarText.setText(getString(R.string.scanning_m_of_n, current, max));

    }

    @Override
    public void onScanCompleted(Context context, List<CacheListItem> apps) {

        showProgressBar(false);

        mCacheListItem.clear();
        mCacheListItem.addAll(apps);
        rubbishMemoryAdapter.notifyDataSetChanged();

        header.setVisibility(View.GONE);

        if (apps.size() > 0) {
            header.setVisibility(View.VISIBLE);
            ll_bottom.setVisibility(View.VISIBLE);

            long medMemory = mCleanerService != null ? mCleanerService.getCacheSize() : 0;

            //初始化最上方垃圾信息
            StorageSize mStorageSize = StorageUtil.convertStorageSize(medMemory);
            textCounter.setAutoFormat(false);
            textCounter.setFormatter(new DecimalFormatter());
            textCounter.setAutoStart(false);
            textCounter.setStartValue(0f);
            textCounter.setEndValue(mStorageSize.value);
            textCounter.setIncrement(5f); // the amount the number increments at each time interval
            textCounter.setTimeInterval(50); // the time interval (ms) at which the text changes
            suffix.setText(mStorageSize.suffix);
            //  textCounter.setSuffix(mStorageSize.suffix);
            textCounter.start();
        } else {
            header.setVisibility(View.GONE);
            ll_bottom.setVisibility(View.GONE);
        }

        if (!mAlreadyScanned) {
            mAlreadyScanned = true;
        }
    }

    @Override
    public void onCleanStarted(Context context) {
        if (isProgressBarVisible()) {
            showProgressBar(false);
        }

        if (!RubbishCleanActivity.this.isFinishing()) {
            showDialogLoading();
        }
    }

    @Override
    public void onCleanCompleted(Context context, long cacheSize) {
        dismissDialogLoading();
        Toast.makeText(context, context.getString(R.string.cleaned, Formatter.formatShortFileSize(
                mContext, cacheSize)), Toast.LENGTH_LONG).show();
        header.setVisibility(View.GONE);
        ll_bottom.setVisibility(View.GONE);
        mCacheListItem.clear();
        rubbishMemoryAdapter.notifyDataSetChanged();
    }

    /**
     * Apply KitKat specific translucency.
     */
    private void applyKitKatTranslucency() {

        // KitKat translucent navigation/status bar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager mTintManager = new SystemBarTintManager(this);
            mTintManager.setStatusBarTintEnabled(true);
            mTintManager.setNavigationBarTintEnabled(true);
            // mTintManager.setTintColor(0xF00099CC);

            mTintManager.setTintDrawable(UIElementsHelper
                    .getGeneralActionBarBackground(this));

            getActionBar().setBackgroundDrawable(
                    UIElementsHelper.getGeneralActionBarBackground(this));

        }

    }


    @OnClick(R.id.clear_button)
    public void onClickClear() {

        if (mCleanerService != null && !mCleanerService.isScanning() &&
                !mCleanerService.isCleaning() && mCleanerService.getCacheSize() > 0) {

            mAlreadyCleaned = false;

            mCleanerService.cleanCache();
        }
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }


    private boolean isProgressBarVisible() {
        return mProgressBar.getVisibility() == View.VISIBLE;
    }

    private void showProgressBar(boolean show) {
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.startAnimation(AnimationUtils.loadAnimation(
                    mContext, android.R.anim.fade_out));
            mProgressBar.setVisibility(View.GONE);
        }
    }

    public void onDestroy() {
        unbindService(mServiceConnection);
        super.onDestroy();
    }

}
