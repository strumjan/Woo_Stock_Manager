package com.woostockmanager

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray

class StoreListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_list)

        val listView: ListView = findViewById(R.id.storeListView)
        val stores = getStores()

        if (stores.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_store_list_message), Toast.LENGTH_LONG).show()
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, stores)
        listView.adapter = adapter

        // Клик на продавница
        listView.setOnItemClickListener { _, _, position, _ ->
            val storeName = stores[position]

            val intent = Intent(this, ProductListActivity::class.java).apply {
                putExtra("STORE_NAME", storeName)
            }
            startActivity(intent)
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
}
