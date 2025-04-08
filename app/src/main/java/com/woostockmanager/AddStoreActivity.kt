package com.woostockmanager

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

class AddStoreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_store)

        val editTextStoreName = findViewById<EditText>(R.id.editTextStoreName)
        val editTextBaseUrl = findViewById<EditText>(R.id.editTextBaseUrl)
        val editTextConsumerKey = findViewById<EditText>(R.id.editTextConsumerKey)
        val editTextConsumerSecret = findViewById<EditText>(R.id.editTextConsumerSecret)
        val buttonSave = findViewById<Button>(R.id.buttonSaveStore)
        val buttonCancel = findViewById<Button>(R.id.buttonCancel)

        buttonSave.setOnClickListener {
            val storeName = editTextStoreName.text.toString().trim()
            val baseUrl = editTextBaseUrl.text.toString().trim()
            val consumerKey = editTextConsumerKey.text.toString().trim()
            val consumerSecret = editTextConsumerSecret.text.toString().trim()

            if (storeName.isEmpty() || baseUrl.isEmpty() || consumerKey.isEmpty() || consumerSecret.isEmpty()) {
                Toast.makeText(this, getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveStore(storeName, baseUrl, consumerKey, consumerSecret)
        }

        buttonCancel.setOnClickListener {
            finish()
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

    private fun saveStore(name: String, baseUrl: String, key: String, secret: String) {
        val sharedPreferences = getSharedPreferences("stores", MODE_PRIVATE)
        sharedPreferences.edit {

            // Прочитаме претходно зачувани продавници
            val storesJson = sharedPreferences.getString("store_list", "[]") ?: "[]"
            val storesArray = JSONArray(storesJson)

            val editTextStoreName = findViewById<EditText>(R.id.editTextStoreName)

            // Проверка дали продавницата веќе постои
            for (i in 0 until storesArray.length()) {
                val store = storesArray.getJSONObject(i)
                if (store.getString("name") == name) {
                    editTextStoreName.error = getString(R.string.store_with_same_name)
                    editTextStoreName.requestFocus()
                    return
                }
            }

            // Додаваме нова продавница
            val newStore = JSONObject().apply {
                put("name", name)
                put("base_url", baseUrl)
                put("consumer_key", key)
                put("consumer_secret", secret)
            }
            storesArray.put(newStore)

            // Ги зачувуваме продавниците назад
            putString("store_list", storesArray.toString())
        }

        Toast.makeText(this, getString(R.string.store_saved), Toast.LENGTH_SHORT).show()

        val intent = Intent(this, StoreListActivity::class.java)
        startActivity(intent)
        finish()
    }

}
