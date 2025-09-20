# GlassCaptions Starter (Glass XE / Android 4.4)

Minimal, hands-free captions app for Google Glass Explorer Edition.

- Package: `com.srikanth.glasscaptions`
- Continuous `SpeechRecognizer` service 
- Fullscreen Activity that displays captions


## Build
- Open in Android Studio (use JDK 8).
- Compile/target SDK 19 (Glass XE).
- Install to device: `adb install -r Glass-XE-Captions-V  .apk`. The prebuilt app can be found app > output >apk > debug > Glass-XE-Captions-Vxx. Where xx represent teh version number 

## Use
- Launch **Glass Captions**.
- Speak; partial results appear with `…`, and a 'Listening..' tag at the bottom. 


## Notes
- Requires network for Google Speech on KitKat.
- Keeps CPU awake via a partial wake lock while listening.
