package com.pla.catur

import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Spatial Audio: memposisikan suara kiri/kanan sesuai kolom papan.
 * Menggunakan ToneGenerator bawaan Android (tanpa file suara).
 */
class SpatialSoundHelper {
    private var toneGenerator: ToneGenerator? = null
    var volume = 80

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, volume)
        } catch (e: Exception) {
            // Abaikan jika gagal inisialisasi
        }
    }

    private fun playTone(type: Int, durationMs: Int) {
        try {
            toneGenerator?.startTone(type, durationMs)
        } catch (e: Exception) {
            // Abaikan
        }
    }

    fun playMove(col: Int) {
        // Karena ToneGenerator tidak mendukung setStereoVolume secara langsung di semua device,
        // kita hanya memainkan suara standar. Efek spasial secara penuh membutuhkan SoundPool + file audio.
        playTone(ToneGenerator.TONE_PROP_BEEP2, 80)
    }

    fun playCapture(col: Int) {
        playTone(ToneGenerator.TONE_PROP_ACK, 200)
    }

    fun playCheck(col: Int) {
        playTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 300)
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}