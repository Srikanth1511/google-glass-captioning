package com.srikanth.glasscaptions.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.srikanth.glasscaptions.svc.ContinuousSttService;

public class TranscriptActivity extends Activity {
    private TextView tv;         // center transcript
    private TextView tvStatus;   // bottom status

    private final BroadcastReceiver rx = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent i) {
            String t = i.getStringExtra(ContinuousSttService.EXTRA_TEXT);
            if (t == null) t = "";
            String a = i.getAction();

            if (ContinuousSttService.ACTION_PARTIAL.equals(a)) {
                tv.setText("\u2026 " + t);      // … partial
                tvStatus.setText("listening…");
            } else if (ContinuousSttService.ACTION_FINAL.equals(a)) {
                tv.setText(t);                  // final
                tvStatus.setText("final");
            }
        }
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);

        // ----- Build UI (no XML) -----
        FrameLayout root = new FrameLayout(this);

        tv = new TextView(this);
        tv.setTextSize(26);
        tv.setTextColor(Color.WHITE);
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundColor(Color.BLACK);
        tv.setText("Listening…");
        root.addView(tv, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        tvStatus = new TextView(this);
        tvStatus.setTextSize(14);
        tvStatus.setTextColor(Color.WHITE);
        tvStatus.setBackgroundColor(0x66000000); // translucent bg
        tvStatus.setText("Ready");
        int pad = (int)(getResources().getDisplayMetrics().density * 8);
        tvStatus.setPadding(pad, pad, pad, pad);

        FrameLayout.LayoutParams statusLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        statusLp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        statusLp.bottomMargin = pad;
        root.addView(tvStatus, statusLp);

        // Long-press anywhere to open options (Close)
        View optionsTarget = root; // could also be 'tv'
        optionsTarget.setOnLongClickListener(v -> { showOptions(); return true; });

        setContentView(root);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Ensure the service is running
        startService(new Intent(this, ContinuousSttService.class));
    }

    private void showOptions() {
        new AlertDialog.Builder(this)
                .setTitle("Glass Captions")
                .setItems(new CharSequence[]{"Close"}, (d, which) -> {
                    if (which == 0) {
                        // Stop background STT and exit
                        try { stopService(new Intent(this, ContinuousSttService.class)); } catch (Exception ignore) {}
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override protected void onResume() {
        super.onResume();
        IntentFilter f = new IntentFilter();
        f.addAction(ContinuousSttService.ACTION_PARTIAL);
        f.addAction(ContinuousSttService.ACTION_FINAL);
        registerReceiver(rx, f);
    }

    @Override protected void onPause() {
        super.onPause();
        try { unregisterReceiver(rx); } catch (Exception ignore) {}
    }
}
