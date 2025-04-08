package com.woostockmanager

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.*

class ProductListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var requestQueue: RequestQueue
    private val productList = mutableListOf<Product>()
    private lateinit var storeBaseUrl: String
    private lateinit var consumerKey: String
    private lateinit var consumerSecret: String
    private val retryCount = 0
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var searchEditText: EditText
    private lateinit var btnNext: Button
    private lateinit var categorySpinner: Spinner
    private lateinit var allProducts: MutableList<Product>
    private lateinit var displayedProducts: MutableList<Product>
    private var currentSearchResults: List<Int> = emptyList()
    private var currentSearchIndex = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        recyclerView = findViewById(R.id.recyclerViewProducts)
        recyclerView.layoutManager = LinearLayoutManager(this)

        productAdapter = ProductAdapter(productList,
            object : ProductUpdateListener {
                override fun onUpdateProduct(productId: Int, newQuantity: Int, newStockStatus: String) {
                    if (newQuantity != -1) {
                        updateProductQuantity(productId, newQuantity)
                    }
                    updateProductStockStatus(productId, newStockStatus)
                }
            }
        ) { product, position ->
            updateProduct(product)
        }

        recyclerView.adapter = productAdapter

        searchEditText = findViewById(R.id.searchEditText)
        btnNext = findViewById(R.id.btnNext)
        categorySpinner = findViewById(R.id.categorySpinner)

        btnNext.setOnClickListener {
            scrollToNextSearchResult()
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                startSearch()
                hideKeyboard()
                true
            } else {
                false
            }
        }

        requestQueue = Volley.newRequestQueue(this)

        val sharedPreferences = getSharedPreferences("stores", MODE_PRIVATE)
        val storeName = intent.getStringExtra("STORE_NAME") ?: ""

        if (storeName.isNotEmpty()) {
            val storesJson = sharedPreferences.getString("store_list", "[]") ?: "[]"
            val storesArray = JSONArray(storesJson)

            storeBaseUrl = ""
            consumerKey = ""
            consumerSecret = ""

            for (i in 0 until storesArray.length()) {
                val store = storesArray.getJSONObject(i)
                if (store.getString("name") == storeName) {
                    storeBaseUrl = store.getString("base_url")
                    consumerKey = store.getString("consumer_key")
                    consumerSecret = store.getString("consumer_secret")
                    break
                }
            }

            if (storeBaseUrl.isEmpty()) {
                Toast.makeText(this, getString(R.string.no_store_data_found) + " $storeName!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, getString(R.string.loaded_store) + " $storeName", Toast.LENGTH_SHORT).show()
                // Овде го повикуваме методот за вчитување на производите

                fetchProducts(storeBaseUrl, consumerKey, consumerSecret)
            }
        } else {
            Toast.makeText(this, getString(R.string.no_store_selected), Toast.LENGTH_SHORT).show()
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

    private fun fetchProducts(storeBaseUrl: String, consumerKey: String, consumerSecret: String, page: Int = 1) {
        if (page == 1) {
            loadingProgressBar = findViewById(R.id.loadingProgressBar)
            loadingProgressBar.visibility = View.VISIBLE  // Прикажи при прво вчитување

            allProducts = mutableListOf()
            displayedProducts = mutableListOf()
        }
        val url = "$storeBaseUrl/wp-json/wc/v3/products?status=publish&per_page=100&page=$page" +
                "&consumer_key=$consumerKey&consumer_secret=$consumerSecret"

        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                if (response.length() > 0) {
                    for (i in 0 until response.length()) {
                        val product = response.getJSONObject(i)
                        val id = product.getInt("id")
                        val title = product.getString("name")
                        val category = if (product.getJSONArray("categories").length() > 0)
                            product.getJSONArray("categories").getJSONObject(0).getString("name") else "Uncategorized"
                        val quantity = if (product.has("stock_quantity") && !product.isNull("stock_quantity"))
                            product.getInt("stock_quantity") else 0
                        val stockStatus = product.getString("stock_status")

                        val p = Product(id, title, category, quantity, stockStatus)
                        allProducts.add(p)
                    }

                    // Преземи следна страница ако има уште производи
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(500)
                        fetchProducts(storeBaseUrl, consumerKey, consumerSecret, page + 1)
                    }
                } else {
                    // Ако нема повеќе производи, освежи ја листата
                    loadingProgressBar.visibility = View.GONE
                    displayedProducts = allProducts.toMutableList()
                    productAdapter.updateList(displayedProducts)
                    setupCategoryFilter()
                }
            },
            { error ->

                if (retryCount < 3) { // Дозволи 3 обиди
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(1000) // Чекај 1 секунда пред повторен обид
                        fetchProducts(storeBaseUrl, consumerKey, consumerSecret, retryCount + 1)
                    }
                } else {
                    loadingProgressBar.visibility = View.GONE
                    Toast.makeText(this, getString(R.string.error_loading_products) + " ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        )
        requestQueue.add(jsonArrayRequest)
    }

    private fun updateProduct(product: Product) {
        val baseUrl = intent.getStringExtra("STORE_BASE_URL") ?: ""
        val key = intent.getStringExtra("CONSUMER_KEY") ?: ""
        val secret = intent.getStringExtra("CONSUMER_SECRET") ?: ""

        if (baseUrl.isEmpty()) return

        val url = "$baseUrl/wp-json/wc/v3/products/${product.id}?consumer_key=$key&consumer_secret=$secret"
        val updatedProduct = JSONObject().apply {
            put("stock_quantity", product.stockQuantity)
        }

        val request = object : JsonObjectRequest(Method.PUT, url, updatedProduct,
            Response.Listener { response ->
                Toast.makeText(this@ProductListActivity, getString(R.string.updated_successfully), Toast.LENGTH_SHORT).show()
                productAdapter.updateList(productList)
            },
            Response.ErrorListener { error ->
                Toast.makeText(this@ProductListActivity, getString(R.string.update_failed), Toast.LENGTH_SHORT).show()
            }) {}
        requestQueue.add(request)
    }

    fun updateProductQuantity(productId: Int, newQuantity: Int) {
        val url = "$storeBaseUrl/wp-json/wc/v3/products/$productId" +
                "?consumer_key=$consumerKey&consumer_secret=$consumerSecret"

        val jsonObject = JSONObject()
        jsonObject.put("stock_quantity", newQuantity)

        val request = JsonObjectRequest(Request.Method.PUT, url, jsonObject,
            { response ->
                Toast.makeText(this, getString(R.string.quantity_updated), Toast.LENGTH_SHORT).show()
            },
            { error ->
                Toast.makeText(this, getString(R.string.failed_quantity_update) + " ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
        requestQueue.add(request)
    }

    fun updateProductStockStatus(productId: Int, newStatus: String) {
        val url = "$storeBaseUrl/wp-json/wc/v3/products/$productId" +
                "?consumer_key=$consumerKey&consumer_secret=$consumerSecret"

        val jsonObject = JSONObject()
        jsonObject.put("stock_status", newStatus) // "instock" или "outofstock"

        val request = JsonObjectRequest(Request.Method.PUT, url, jsonObject,
            { response ->
                Toast.makeText(this, getString(R.string.stock_status_updated), Toast.LENGTH_SHORT).show()
            },
            { error ->
                Toast.makeText(this, getString(R.string.failed_stock_status_update) + " ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
        requestQueue.add(request)
    }

    private fun updateFilteredProducts(newList: List<Product>) {
        displayedProducts.clear()
        displayedProducts.addAll(newList)
        productAdapter.updateList(displayedProducts)
    }

    private fun startSearch() {
        val query = searchEditText.text.toString().trim().lowercase()
        if (query.isEmpty()) return

        currentSearchResults = displayedProducts.mapIndexedNotNull { index, product ->
            if (product.name.lowercase().contains(query)) index else null
        }

        currentSearchIndex = 0


        if (currentSearchResults.isEmpty()) {
            Toast.makeText(this, "No results found.", Toast.LENGTH_SHORT).show()
        } else {
            recyclerView.scrollToPosition(currentSearchResults[0])
            productAdapter.setHighlightedPosition(currentSearchResults[0])
        }
    }

    private fun scrollToNextSearchResult() {
        if (currentSearchResults.isNotEmpty()) {
            currentSearchIndex = (currentSearchIndex + 1) % currentSearchResults.size
            val pos = currentSearchResults[currentSearchIndex]
            recyclerView.scrollToPosition(pos)
            productAdapter.setHighlightedPosition(pos)
        }
    }


    private fun setupCategoryFilter() {
        val categories = allProducts.map { it.category }.distinct().sorted()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Categories") + categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = categorySpinner.selectedItem.toString()
                val filtered = if (selected == "Categories") allProducts else allProducts.filter { it.category == selected }
                updateFilteredProducts(filtered)
                // Clear search input
                searchEditText.text.clear()

                // Remove highlighted product
                productAdapter.setHighlightedPosition(null)

                // Reset scroll to top
                recyclerView.scrollToPosition(0)

                // Clear previous search state
                currentSearchResults = emptyList()
                currentSearchIndex = 0
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

}
