\
package com.srikanth.glasscaptions.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.srikanth.glasscaptions.R;
import com.srikanth.glasscaptions.svc.ContinuousSttService;

public class TranscriptActivity extends Activity {

    public static final String ACTION_PARTIAL = "com.srikanth.glasscaptions.ACTION_PARTIAL";
    public static final String ACTION_FINAL   = "com.srikanth.glasscaptions.ACTION_FINAL";
    public static final String EXTRA_TEXT     = "text";

    private TextView tvTranscript, tvStatus;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context ctx, Intent intent) {
            String txt = intent.getStringExtra(EXTRA_TEXT);
            if (txt == null) return;

            if (ACTION_PARTIAL.equals(intent.getAction())) {
                tvTranscript.setText(txt + " …");
                tvStatus.setText(R.string.status_listening);
            } else if (ACTION_FINAL.equals(intent.getAction())) {
                tvTranscript.setText(txt);
                tvStatus.setText(R.string.status_final);
            }
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcript);
        tvTranscript = findViewById(R.id.textTranscript);
        tvStatus     = findViewById(R.id.textStatus);

        // Ensure the service is running
        startService(new Intent(this, ContinuousSttService.class));
    }

    @Override protected void onResume() {
        super.onResume();
        IntentFilter f = new IntentFilter();
        f.addAction(ACTION_PARTIAL);
        f.addAction(ACTION_FINAL);
        registerReceiver(receiver, f);
    }

    @Override protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    // --- Glass-style options menu (tap to open, swipe to scroll, tap to select) ---
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_transcript, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_close) {
            // Stop service and finish the activity
            stopService(new Intent(this, ContinuousSttService.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
