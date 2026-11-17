package com.pla.catur

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private lateinit var speech: SpeechHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = getSharedPreferences("caturPrefs", MODE_PRIVATE)
        speech = SpeechHelper(this)

        findViewById<Button>(R.id.btnPlayAi).setOnClickListener {
            speech.speak("Lawan AI. Pilih tingkat kesulitan.")
            showDifficultyDialog()
        }
        findViewById<Button>(R.id.btnPlayLocal).setOnClickListener {
            speech.speak("Mode satu lawan satu")
            startActivity(Intent(this, GameActivity::class.java).apply {
                putExtra("mode", "local")
            })
        }
        findViewById<Button>(R.id.btnShop).setOnClickListener {
            startActivity(Intent(this, ShopActivity::class.java))
        }
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<Button>(R.id.btnHelp).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.help)
                .setMessage(R.string.help_text)
                .setPositiveButton(R.string.understand) { d, _ -> d.dismiss() }
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        val level = prefs.getInt("level", 1)
        val coins = prefs.getInt("coins", 0)
        val wins = prefs.getInt("wins", 0)
        findViewById<TextView>(R.id.txtLevel).text = getString(R.string.level, level)
        findViewById<TextView>(R.id.txtCoins).text = getString(R.string.coins, coins)
        findViewById<TextView>(R.id.txtWins).text = getString(R.string.wins, wins)
    }

    private fun showDifficultyDialog() {
        val items = arrayOf(getString(R.string.easy), getString(R.string.medium), getString(R.string.hard))
        AlertDialog.Builder(this)
            .setTitle(R.string.difficulty)
            .setItems(items) { _, which ->
                val d = when (which) { 0 -> "easy"; 1 -> "medium"; else -> "hard" }
                startActivity(Intent(this, GameActivity::class.java).apply {
                    putExtra("mode", "ai")
                    putExtra("difficulty", d)
                })
            }
            .show()
    }

    override fun onDestroy() { speech.shutdown(); super.onDestroy() }
}