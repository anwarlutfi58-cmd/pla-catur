package com.pla.catur

import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Spatial Audio: memposisikan suara kiri/kanan sesuai kolom papan.
 * Kolom 0-1 = kiri, 2-5 = tengah, 6-7 = kanan.
 */
class SpatialSoundHelper {
    private val leftTone = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
    private val rightTone = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
    private val centerTone = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
    var volume = 80

    fun playMove(col: Int) {
        val (left, right) = when {
            col <= 1 -> Pair(100, 20)
            col >= 6 -> Pair(20, 100)
            col <= 3 -> Pair(75, 40)
            col >= 4 -> Pair(40, 75)
            else -> Pair(60, 60)
        }
        val tone = when {
            col <= 1 -> leftTone
            col >= 6 -> rightTone
            else -> centerTone
        }
        val l = (left * volume / 100)
        val r = (right * volume / 100)
        try {
            tone.setStereoVolume(l / 100f, r / 100f)
            tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 80)
        } catch (e: Exception) { }
    }

    fun playCapture(col: Int) {
        val tone = if (col <= 3) leftTone else rightTone
        val l = if (col <= 3) volume / 100f else volume / 300f
        val r = if (col > 3) volume / 100f else volume / 300f
        try {
            tone.setStereoVolume(l, r)
            tone.startTone(ToneGenerator.TONE_PROP_ACK, 200)
        } catch (e: Exception) { }
    }

    fun playCheck(col: Int) {
        val tone = if (col <= 3) leftTone else rightTone
        try {
            tone.setStereoVolume(volume / 100f, volume / 100f)
            tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 300)
        } catch (e: Exception) { }
    }

    fun release() { leftTone.release(); rightTone.release(); centerTone.release() }
}