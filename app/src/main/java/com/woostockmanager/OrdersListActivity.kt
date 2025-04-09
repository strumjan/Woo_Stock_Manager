package com.woostockmanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class OrdersListActivity : AppCompatActivity() {

    private lateinit var ordersRecyclerView: RecyclerView
    private lateinit var periodSpinner: Spinner
    private lateinit var ordersAdapter: OrdersAdapter
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var emptyText: TextView

    private val ordersList = mutableListOf<Order>()
    private lateinit var storeBaseUrl: String
    private lateinit var consumerKey: String
    private lateinit var consumerSecret: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders_list)

        ordersRecyclerView = findViewById(R.id.ordersRecyclerView)
        periodSpinner = findViewById(R.id.periodSpinner)
        loadingProgressBar = findViewById(R.id.loadingOrdersProgressBar)
        emptyText = findViewById(R.id.emptyOrdersText)

        ordersRecyclerView.layoutManager = LinearLayoutManager(this)

        ordersAdapter = OrdersAdapter(ordersList) { orderId ->
            resendOrderNotification(orderId)
        }
        ordersRecyclerView.adapter = ordersAdapter

        val storeName = intent.getStringExtra("STORE_NAME") ?: ""
        val sharedPreferences = getSharedPreferences("stores", MODE_PRIVATE)
        val storesJson = sharedPreferences.getString("store_list", "[]") ?: "[]"
        val storesArray = JSONArray(storesJson)

        for (i in 0 until storesArray.length()) {
            val store = storesArray.getJSONObject(i)
            if (store.getString("name") == storeName) {
                storeBaseUrl = store.getString("base_url")
                consumerKey = store.getString("consumer_key")
                consumerSecret = store.getString("consumer_secret")
                break
            }
        }

        val periods = listOf("Last 24 Hours", "Last 7 Days", "Last Month")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, periods)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        periodSpinner.adapter = spinnerAdapter

        periodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, id: Long) {
                loadOrders(getDateFilterForPeriod(pos))
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
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

    private fun getDateFilterForPeriod(pos: Int): String {
        val calendar = Calendar.getInstance()
        when (pos) {
            0 -> calendar.add(Calendar.HOUR_OF_DAY, -24)
            1 -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            2 -> calendar.add(Calendar.MONTH, -1)
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    private fun loadOrders(afterDate: String) {
        loadingProgressBar.visibility = View.VISIBLE
        emptyText.visibility = View.GONE

        val url = "$storeBaseUrl/wp-json/wc/v3/orders?after=${afterDate}Z&consumer_key=$consumerKey&consumer_secret=$consumerSecret"
        Log.d("WooDebug", "URL: $url")

        val request = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                ordersList.clear()

                for (i in 0 until response.length()) {
                    val obj = response.getJSONObject(i)
                    val id = obj.getInt("id")
                    val date = obj.getString("date_created")
                    val status = obj.getString("status")
                    val total = obj.getString("total")
                    val currency = obj.getString("currency")
                    ordersList.add(Order(id, date, status, total, currency))
                }

                loadingProgressBar.visibility = View.GONE
                ordersAdapter.notifyDataSetChanged()

                if (ordersList.isEmpty()) {
                    emptyText.visibility = View.VISIBLE
                } else {
                    emptyText.visibility = View.GONE
                }
            },
            { error ->
                loadingProgressBar.visibility = View.GONE
                emptyText.visibility = View.GONE
                Toast.makeText(this, "Failed to load orders: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun resendOrderNotification(orderId: Int) {
        val url = "$storeBaseUrl/wp-json/custom/v1/orders/$orderId/resend?consumer_key=$consumerKey&consumer_secret=$consumerSecret"

        val request = JsonObjectRequest(
            Request.Method.POST, url, null,
            { response ->
                val message = response.optString("message", "Notification sent successfully.")
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            },
            { error ->
                val errorMsg = error.message ?: "Unknown error"
                Toast.makeText(this, "Failed to resend notification: $errorMsg", Toast.LENGTH_LONG).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

}
