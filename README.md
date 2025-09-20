# GlassCaptions Starter (Glass XE / Android 4.4)

Minimal, hands-free captions app for Google Glass Explorer Edition.

- Package: `com.srikanth.glasscaptions`
- Continuous `SpeechRecognizer` service with partial + final broadcasts
- Fullscreen Activity that displays captions
- Glass-style **Options Menu** with **Close** action (tap touchpad to open)

## Build
- Open in Android Studio (use JDK 8).
- Compile/target SDK 19 (Glass XE).
- Install to device: `adb install -r app-debug.apk`.

## Use
- Launch **Glass Captions**.
- Speak; partial results appear with `…`, final replaces them.
- Tap touchpad → options menu → **Close** to exit (stops service).

## Notes
- Requires network for Google Speech on KitKat.
- Keeps CPU awake via a partial wake lock while listening.
