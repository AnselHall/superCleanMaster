package com.yzy.supercleanmaster.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.umeng.update.UmengUpdateAgent;
import com.yzy.supercleanmaster.R;
import com.yzy.supercleanmaster.base.BaseFragment;
import com.yzy.supercleanmaster.model.SDCardInfo;
import com.yzy.supercleanmaster.ui.AutoStartManageActivity;
import com.yzy.supercleanmaster.ui.MemoryCleanActivity;
import com.yzy.supercleanmaster.ui.RubbishCleanActivity;
import com.yzy.supercleanmaster.ui.SoftwareManageActivity;
import com.yzy.supercleanmaster.utils.AppUtil;
import com.yzy.supercleanmaster.utils.StorageUtil;
import com.yzy.supercleanmaster.widget.circleprogress.ArcProgress;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainFragment extends BaseFragment {

    //@formatter:off

    /**展示存储空间的进度条*/
    @InjectView(R.id.storage_progress)
    ArcProgress storage_progress;

    /**显示内存信息的进度条*/
    @InjectView(R.id.memory_process)
    ArcProgress memory_process;

    @InjectView(R.id.capacity)
    TextView capacity;

    Context mContext;

    /**内存信息的计时器*/
    private Timer timer;
    /**存储空间进度条的计时器*/
    private Timer timer2;

    //@formatter:on

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, view);
        mContext = getActivity();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fillData();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        UmengUpdateAgent.update(getActivity());
    }

    private void fillData() {
        timer = null;
        timer2 = null;
        timer = new Timer();
        timer2 = new Timer();

        long availMemory = AppUtil.getAvailMemory(mContext);
        long totalMemory = AppUtil.getTotalMemory(mContext);
        final double x = (((totalMemory - availMemory) / (double) totalMemory) * 100);
        //   memory_process.setProgress((int) x);

        memory_process.setProgress(0);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (memory_process.getProgress() >= (int) x) {
                            timer.cancel();
                        } else {
                            memory_process.setProgress(memory_process.getProgress() + 1);
                        }
                    }
                });
            }
        }, 50, 20);

        SDCardInfo mSDCardInfo = StorageUtil.getSDCardInfo();
        SDCardInfo mSystemInfo = StorageUtil.getSystemSpaceInfo(mContext);

        /*可用空间，包括系统空间和SD卡存储空间*/
        long mAvailableBlock;
        /*总共空间*/
        long TotalBlocks;
        if (mSDCardInfo != null) {
            mAvailableBlock = mSDCardInfo.free + mSystemInfo.free;
            TotalBlocks = mSDCardInfo.total + mSystemInfo.total;
        } else {
            /*没有SD卡  所有的存储空间信息就是系统的存储空间信息*/
            mAvailableBlock = mSystemInfo.free;
            TotalBlocks = mSystemInfo.total;
        }

        final double percentStore = (((TotalBlocks - mAvailableBlock) / (double) TotalBlocks) * 100);

        capacity.setText(StorageUtil.convertStorage(TotalBlocks - mAvailableBlock) + "/" + StorageUtil.convertStorage(TotalBlocks));
        storage_progress.setProgress(0);

        timer2.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (storage_progress.getProgress() >= (int) percentStore) {
                            timer2.cancel();
                        } else {
                            storage_progress.setProgress(storage_progress.getProgress() + 1);
                        }
                    }
                });
            }
        }, 50, 20);
    }

    @OnClick(R.id.card1)
    void speedUp() {
        startActivity(MemoryCleanActivity.class);
    }

    @OnClick(R.id.card2)
    void rubbishClean() {
        startActivity(RubbishCleanActivity.class);
    }

    @OnClick(R.id.card3)
    void AutoStartManage() {
        startActivity(AutoStartManageActivity.class);
    }

    @OnClick(R.id.card4)
    void SoftwareManage() {
        startActivity(SoftwareManageActivity.class);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        timer2.cancel();
        super.onDestroy();
    }
}
