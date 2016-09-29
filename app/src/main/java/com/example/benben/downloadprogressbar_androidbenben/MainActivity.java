package com.example.benben.downloadprogressbar_androidbenben;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.example.benben.downloadprogressbarlibrary.DownLoadProgressBar;

public class MainActivity extends AppCompatActivity {


    private DownLoadProgressBar mProgressBar;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mProgressBar.setProgress(msg.arg1);
            if (msg.arg1 == 100) {
                mProgressBar.finishLoad();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = (DownLoadProgressBar) findViewById(R.id.main_down_load);
        mProgressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mProgressBar.isFinish()) {
                    mProgressBar.toggle();
                }
            }
        });

        downLoad();
    }

    private void downLoad() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    try {
                        Thread.sleep(200);
                        Message message = mHandler.obtainMessage();
                        message.arg1 = i + 1;
                        mHandler.sendMessage(message);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }


}
