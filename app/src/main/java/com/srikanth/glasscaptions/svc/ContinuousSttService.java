\
package com.srikanth.glasscaptions.svc;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import com.srikanth.glasscaptions.ui.TranscriptActivity;

import java.util.ArrayList;

public class ContinuousSttService extends Service implements RecognitionListener {

    private static final long RESTART_DELAY_MS = 300L;

    private SpeechRecognizer recognizer;
    private Intent recogIntent;
    private Handler handler;
    private PowerManager.WakeLock wakeLock;

    private void send(String action, String text) {
        Intent i = new Intent(action);
        i.putExtra(TranscriptActivity.EXTRA_TEXT, text);
        sendBroadcast(i);
    }

    @Override public void onCreate() {
        super.onCreate();
        handler = new Handler();

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GlassCaptions:stt");
        wakeLock.setReferenceCounted(false);
        try { wakeLock.acquire(); } catch (Throwable ignored) {}

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(this);

        recogIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recogIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recogIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recogIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        startListening();
    }

    private void startListening() {
        try {
            recognizer.startListening(recogIntent);
        } catch (Exception e) {
            scheduleRestart();
        }
    }

    private void scheduleRestart() {
        if (handler == null) return;
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override public void run() { startListening(); }
        }, RESTART_DELAY_MS);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override public void onDestroy() {
        super.onDestroy();
        if (recognizer != null) {
            try { recognizer.cancel(); recognizer.destroy(); } catch (Exception ignored) {}
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            try { wakeLock.release(); } catch (Exception ignored) {}
        }
    }

    // --- RecognitionListener ---
    @Override public void onReadyForSpeech(Bundle params) { }
    @Override public void onBeginningOfSpeech() { }
    @Override public void onRmsChanged(float rmsdB) { }
    @Override public void onBufferReceived(byte[] buf) { }
    @Override public void onEndOfSpeech() { }

    @Override public void onError(int error) {
        scheduleRestart();
    }

    @Override public void onResults(Bundle results) {
        ArrayList<String> list = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (list != null && !list.isEmpty()) {
            send(TranscriptActivity.ACTION_FINAL, list.get(0));
        }
        scheduleRestart();
    }

    @Override public void onPartialResults(Bundle partialResults) {
        ArrayList<String> list = partialResults.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION);
        if (list != null && !list.isEmpty()) {
            send(TranscriptActivity.ACTION_PARTIAL, list.get(0));
        }
    }

    @Override public void onEvent(int eventType, Bundle params) { }

    @Override public IBinder onBind(Intent intent) { return null; }
}
