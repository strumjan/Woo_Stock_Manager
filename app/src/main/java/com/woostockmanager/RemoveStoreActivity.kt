package com.woostockmanager

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import androidx.core.graphics.toColorInt
import androidx.core.content.edit


class RemoveStoreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remove_store)

        val warningTextView: TextView = findViewById(R.id.warningTextView)
        val listView: ListView = findViewById(R.id.storeListView)

        warningTextView.text = getString(R.string.remove_store_warning)
        warningTextView.setTextColor("#FF9800".toColorInt()) // Портокалова боја

        val stores = getStores()
        if (stores.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_stores_to_remove), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, stores)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedStore = stores[position]
            confirmStoreRemoval(selectedStore)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add_store -> {
                val intent = Intent(this, AddStoreActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_store_list -> {
                val intent = Intent(this, StoreListActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_remove_store -> {
                val intent = Intent(this, RemoveStoreActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_exit -> {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.exit_title))
                    .setMessage(getString(R.string.exit_message))
                    .setPositiveButton(getString(R.string.yes_button)) { _, _ -> finishAffinity() }
                    .setNegativeButton(getString(R.string.no_button), null)
                    .show()
            }


        }
        return super.onOptionsItemSelected(item)
    }

    private fun getStores(): List<String> {
        val sharedPreferences = getSharedPreferences("stores", MODE_PRIVATE)
        val storesJson = sharedPreferences.getString("store_list", "[]") ?: "[]"
        val storesArray = JSONArray(storesJson)

        val storeNames = mutableListOf<String>()
        for (i in 0 until storesArray.length()) {
            val store = storesArray.getJSONObject(i)
            storeNames.add(store.getString("name"))
        }
        return storeNames
    }

    private fun confirmStoreRemoval(storeName: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_remove_store))
            .setMessage(getString(R.string.confirm_remove_store_message, storeName))
            .setPositiveButton(getString(R.string.yes_button)) { _, _ ->
                removeStore(storeName)
            }
            .setNegativeButton(getString(R.string.no_button), null)
            .show()
    }

    private fun removeStore(storeName: String) {
        val sharedPreferences = getSharedPreferences("stores", MODE_PRIVATE)
        val storesJson = sharedPreferences.getString("store_list", "[]") ?: "[]"
        val storesArray = JSONArray(storesJson)

        val newStoresArray = JSONArray()
        for (i in 0 until storesArray.length()) {
            val store = storesArray.getJSONObject(i)
            if (store.getString("name") != storeName) {
                newStoresArray.put(store)
            }
        }

        sharedPreferences.edit { putString("store_list", newStoresArray.toString()) }
        Toast.makeText(this, getString(R.string.store_removed, storeName), Toast.LENGTH_SHORT).show()
        val intent = Intent(this, StoreListActivity::class.java)
        startActivity(intent)
        finish() // Враќање назад по бришењето
    }
}
