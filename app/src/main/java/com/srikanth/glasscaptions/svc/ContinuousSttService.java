package com.srikanth.glasscaptions.svc;


import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

public class ContinuousSttService extends Service {
    private static final String TAG = "ContinuousStt";
    public static final String ACTION_PARTIAL = "stt.PARTIAL";
    public static final String ACTION_FINAL   = "stt.FINAL";
    public static final String EXTRA_TEXT     = "text";

    private SpeechRecognizer sr;
    private Intent sttIntent;
    private boolean isListening = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override public void onCreate() {
        super.onCreate();
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Log.e(TAG, "Speech recognition not available");
            stopSelf();
            return;
        }
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(listener());

        sttIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        sttIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        sttIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        startListeningLoop();
    }

    private void startListeningLoop() {
        if (isListening) return;
        try {
            isListening = true;
            sr.startListening(sttIntent);
            Log.i(TAG, "startListening()");
        } catch (Exception e) {
            Log.e(TAG, "startListening() failed", e);
            isListening = false;
            retry(400);
        }
    }

    private void retry(long delayMs) {
        handler.postDelayed(new Runnable() {
            @Override public void run() { startListeningLoop(); }
        }, delayMs);
    }

    private RecognitionListener listener() {
        return new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle b) { }
            @Override public void onBeginningOfSpeech() { }
            @Override public void onRmsChanged(float rmsdB) { }
            @Override public void onBufferReceived(byte[] buffer) { }

            @Override public void onPartialResults(Bundle b) {
                ArrayList<String> list = b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (list != null && !list.isEmpty()) {
                    broadcast(ACTION_PARTIAL, list.get(0));
                }
            }

            @Override public void onResults(Bundle b) {
                ArrayList<String> list = b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (list != null && !list.isEmpty()) {
                    broadcast(ACTION_FINAL, list.get(0));
                }
                isListening = false;
                startListeningLoop();
            }

            @Override public void onError(int error) {
                Log.w(TAG, "onError: " + error);
                isListening = false;
                retry(300);
            }

            @Override public void onEndOfSpeech() { }
            @Override public void onEvent(int eventType, Bundle params) { }
        };
    }

    private void broadcast(String action, String text) {
        Intent i = new Intent(action);
        i.putExtra(EXTRA_TEXT, text);
        sendBroadcast(i);
    }

    @Override public int onStartCommand(Intent i, int flags, int id) {
        return START_STICKY;
    }

    @Override public void onDestroy() {
        super.onDestroy();
        try { sr.cancel(); } catch (Exception ignored) {}
        try { sr.destroy(); } catch (Exception ignored) {}
        isListening = false;
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}