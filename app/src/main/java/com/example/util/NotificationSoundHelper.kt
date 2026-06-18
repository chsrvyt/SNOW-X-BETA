package com.example.util

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

object NotificationSoundHelper {
    private const val TAG = "NotificationSoundHelper"
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ToneGenerator", e)
        }
    }

    /**
     * Plays a rich academic success sci-fi alert chime.
     */
    fun playCompleteChime() {
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            }
            // Double pleasant chime
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
            Thread {
                try {
                    Thread.sleep(180)
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 200)
                } catch (e: Exception) {
                    // ignore
                }
            }.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error playing complete chime", e)
        }
    }

    /**
     * Plays a soft confirmation tick sound for click actions.
     */
    fun playStartClickSfx() {
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            }
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_CONFIRM, 100)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing click SFX", e)
        }
    }
}
