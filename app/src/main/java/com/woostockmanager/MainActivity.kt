package com.woostockmanager

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Наоѓање на копчињата во layout-от
        val btnAddStore: Button = findViewById(R.id.btnAddStore)
        val btnStoreList: Button = findViewById(R.id.btnStoreList)
        val btnRemoveStore: Button = findViewById(R.id.btnRemoveStore)
        val btnExit: Button = findViewById(R.id.btnExit)

        // Функционалност за секое копче
        btnAddStore.setOnClickListener {
            startActivity(Intent(this, AddStoreActivity::class.java))
        }

        btnStoreList.setOnClickListener {
            startActivity(Intent(this, StoreListActivity::class.java))
        }

        btnRemoveStore.setOnClickListener {
            startActivity(Intent(this, RemoveStoreActivity::class.java))
        }

        btnExit.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.exit_title))
                .setMessage(getString(R.string.exit_message))
                .setPositiveButton(getString(R.string.yes_button)) { _, _ -> finishAffinity() }
                .setNegativeButton(getString(R.string.no_button), null)
                .show()
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

}
