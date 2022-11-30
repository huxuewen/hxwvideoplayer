package com.hxw.hxwvideoplayer.ui;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hxw.hxwvideoplayer.R;
import com.hxw.hxwvideoplayer.databinding.PopupVideoSpeedBinding;
import com.hxw.hxwvideoplayer.utils.DipAndPx;

import java.util.Timer;
import java.util.TimerTask;

public class VideoSpeedPopup extends PopupWindow implements View.OnClickListener {
    private static final int COMPLETED = 0;
    protected DismissTimerTask mDismissTimerTask;
    private TextView speed_0_5, speedOne, speedTwo, speedThree, speedFour, speedFive;
    private SpeedChangeListener speedChangeListener;
    private Context mC;
    private Timer mDismissTimer;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == COMPLETED) {
                dismiss();

            }
        }
    };

    private PopupVideoSpeedBinding binding;

    public VideoSpeedPopup(Context context) {
        super(context);
        binding = PopupVideoSpeedBinding.inflate(LayoutInflater.from(context));
        mC = context;
        setContentView(binding.getRoot());

        setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        setWidth(DipAndPx.dip2px(context, 200));
        speed_0_5 = binding.popSpeed05;
        speedOne = binding.popSpeed1;
        speedTwo = binding.popSpeed125;
        speedThree = binding.popSpeed15;
        speedFour = binding.popSpeed175;
        speedFive = binding.popSpeed2;
        setOutsideTouchable(true);
        //不设置该属性，弹窗于屏幕边框会有缝隙并且背景不是半透明
        setBackgroundDrawable(new BitmapDrawable());
        speed_0_5.setOnClickListener(this);
        speedOne.setOnClickListener(this);
        speedTwo.setOnClickListener(this);
        speedThree.setOnClickListener(this);
        speedFour.setOnClickListener(this);
        speedFive.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (speedChangeListener != null) {
            int id = v.getId();
            if (id == R.id.pop_speed_0_5) {
                speed_0_5.setTextColor(mC.getResources().getColor(R.color.orange));
                speedOne.setTextColor(mC.getResources().getColor(R.color.white));
                speedTwo.setTextColor(mC.getResources().getColor(R.color.white));
                speedThree.setTextColor(mC.getResources().getColor(R.color.white));
                speedFour.setTextColor(mC.getResources().getColor(R.color.white));
                speedFive.setTextColor(mC.getResources().getColor(R.color.white));
                speedChangeListener.speedChange(0.5f);
            } else if (id == R.id.pop_speed_1) {
                speed_0_5.setTextColor(mC.getResources().getColor(R.color.white));
                speedOne.setTextColor(mC.getResources().getColor(R.color.orange));
                speedTwo.setTextColor(mC.getResources().getColor(R.color.white));
                speedThree.setTextColor(mC.getResources().getColor(R.color.white));
                speedFour.setTextColor(mC.getResources().getColor(R.color.white));
                speedFive.setTextColor(mC.getResources().getColor(R.color.white));
                speedChangeListener.speedChange(1f);
            } else if (id == R.id.pop_speed_1_25) {
                speed_0_5.setTextColor(mC.getResources().getColor(R.color.white));
                speedOne.setTextColor(mC.getResources().getColor(R.color.white));
                speedTwo.setTextColor(mC.getResources().getColor(R.color.orange));
                speedThree.setTextColor(mC.getResources().getColor(R.color.white));
                speedFour.setTextColor(mC.getResources().getColor(R.color.white));
                speedFive.setTextColor(mC.getResources().getColor(R.color.white));
                speedChangeListener.speedChange(1.25f);
            } else if (id == R.id.pop_speed_1_5) {
                speed_0_5.setTextColor(mC.getResources().getColor(R.color.white));
                speedOne.setTextColor(mC.getResources().getColor(R.color.white));
                speedTwo.setTextColor(mC.getResources().getColor(R.color.white));
                speedThree.setTextColor(mC.getResources().getColor(R.color.orange));
                speedFour.setTextColor(mC.getResources().getColor(R.color.white));
                speedFive.setTextColor(mC.getResources().getColor(R.color.white));
                speedChangeListener.speedChange(1.5f);
            } else if (id == R.id.pop_speed_1_75) {
                speed_0_5.setTextColor(mC.getResources().getColor(R.color.white));
                speedOne.setTextColor(mC.getResources().getColor(R.color.white));
                speedTwo.setTextColor(mC.getResources().getColor(R.color.white));
                speedThree.setTextColor(mC.getResources().getColor(R.color.white));
                speedFour.setTextColor(mC.getResources().getColor(R.color.orange));
                speedFive.setTextColor(mC.getResources().getColor(R.color.white));
                speedChangeListener.speedChange(1.75f);
            } else if (id == R.id.pop_speed_2) {
                speed_0_5.setTextColor(mC.getResources().getColor(R.color.white));
                speedOne.setTextColor(mC.getResources().getColor(R.color.white));
                speedTwo.setTextColor(mC.getResources().getColor(R.color.white));
                speedThree.setTextColor(mC.getResources().getColor(R.color.white));
                speedFour.setTextColor(mC.getResources().getColor(R.color.white));
                speedFive.setTextColor(mC.getResources().getColor(R.color.orange));
                speedChangeListener.speedChange(2f);
            }
        }
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        startDismissTimer();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        cancelDismissTimer();
    }

    public void startDismissTimer() {
        cancelDismissTimer();
        mDismissTimer = new Timer();
        mDismissTimerTask = new DismissTimerTask();
        mDismissTimer.schedule(mDismissTimerTask, 2500);
    }

    public void cancelDismissTimer() {
        if (mDismissTimer != null) {
            mDismissTimer.cancel();
        }
        if (mDismissTimerTask != null) {
            mDismissTimerTask.cancel();
        }

    }

    public SpeedChangeListener getSpeedChangeListener() {
        return speedChangeListener;
    }

    public void setSpeedChangeListener(SpeedChangeListener speedChangeListener) {
        this.speedChangeListener = speedChangeListener;
    }

    public interface SpeedChangeListener {
        void speedChange(float speed);
    }

    public class DismissTimerTask extends TimerTask {

        @Override
        public void run() {
            Message message = new Message();
            message.what = COMPLETED;
            handler.sendMessage(message);
        }
    }
}
