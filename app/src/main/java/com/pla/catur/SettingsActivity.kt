package com.pla.catur

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        prefs = getSharedPreferences("caturPrefs", MODE_PRIVATE)

        val seekVol = findViewById<SeekBar>(R.id.seekVolume)
        val seekBoard = findViewById<SeekBar>(R.id.seekBoard)
        val txtSaved = findViewById<TextView>(R.id.txtSaved)

        seekVol.progress = prefs.getInt("volume", 80)
        seekBoard.progress = prefs.getInt("boardSize", 100)

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnSave).setOnClickListener {
            prefs.edit().apply {
                putInt("volume", seekVol.progress)
                putInt("boardSize", seekBoard.progress)
                apply()
            }
            txtSaved.visibility = View.VISIBLE
            txtSaved.postDelayed({ txtSaved.visibility = View.GONE }, 2000)
        }
    }
}