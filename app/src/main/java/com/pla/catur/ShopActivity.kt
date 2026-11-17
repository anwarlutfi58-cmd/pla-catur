package com.pla.catur

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

data class ShopItem(val id: String, val name: String, val price: Int)

class ShopActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private val items = listOf(
        ShopItem("wood_classic", "Papan Kayu Klasik", 0),
        ShopItem("marble_dark", "Papan Marmer Gelap", 500),
        ShopItem("gold_royal", "Papan Emas Kerajaan", 1000),
        ShopItem("piece_classic", "Bidak Klasik", 0),
        ShopItem("piece_nusantara", "Bidak Nusantara", 800)
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)
        prefs = getSharedPreferences("caturPrefs", MODE_PRIVATE)

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
        renderShop()
    }

    private fun renderShop() {
        val container = findViewById<LinearLayout>(R.id.shopContainer)
        container.removeAllViews()
        val coins = prefs.getInt("coins", 0)
        findViewById<TextView>(R.id.txtCoins).text = "Saldo: Rp $coins"
        val owned = prefs.getStringSet("owned", setOf("wood_classic", "piece_classic")) ?: emptySet()
        val equipped = prefs.getString("equippedBoard", "wood_classic")
        for (it in items) {
            val v = LayoutInflater.from(this).inflate(R.layout.item_shop, container, false)
            v.findViewById<TextView>(R.id.itemName).text = it.name
            v.findViewById<TextView>(R.id.itemPrice).text = if (it.price == 0) "Gratis" else "Rp ${it.price}"
            val btn = v.findViewById<Button>(R.id.itemBtn)
            btn.text = when {
                equipped == it.id -> getString(R.string.owned)
                owned.contains(it.id) -> getString(R.string.equip)
                else -> getString(R.string.buy)
            }
            btn.setOnClickListener {
                if (equipped == it.id) return@setOnClickListener
                if (owned.contains(it.id)) {
                    prefs.edit().putString("equippedBoard", it.id).apply()
                    renderShop()
                } else {
                    if (coins >= it.price) {
                        val newCoins = coins - it.price
                        prefs.edit()
                            .putInt("coins", newCoins)
                            .putStringSet("owned", owned + it.id)
                            .apply()
                        renderShop()
                    }
                }
            }
            container.addView(v)
        }
    }
}