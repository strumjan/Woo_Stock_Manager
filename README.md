Woo Stock Manager is an application designed exclusively for managing your product inventory. It communicates with your stores via REST API.

Add New Store - Add a store. You need the Store name, it is only for you to recognize the store. The store URL is the basic URL that is set in your WordPress settings, for example: https://mystore.com or similar. Then you need a consumer key and consumer secret that you create in your WooCommerce settings. Be careful when creating to assign both read and write permissions.

Store List - List of all stores that you manage. By clicking on the store name, all products from that store that have the PUBLISH status will be loaded. In addition to the title of each product, the category it belongs to, the quantity and the stock status will be displayed. To make it easier to search for products, you have a selector in the upper part with all the categories that that store has, so you can click on a category to list only the products from that category. You also have a Search field where you can type a term you are looking for, for example white, and by clicking on the search field on the keyboard or enter it will find all products that contain that term and locate the first one. Then with the NEXT button you can move further until you find the product you want to change. If the product has a defined quantity, for example 2, 5, 14, then you can change the quantity to the desired number and click UPDATE. If you have only defined a stock status: onstock, outofstock or onbackorder, then you can change that status and click UPDATE. It is performed only for that product.

Remove Store - Remove a store from the list. Click on the name of the store and it will ask you to confirm that you want to delete it. The deletion is irreversible.

Exit - Exit the application.

Note: The application was created because I often get asked to just change the stock status of several stores at the same time, and opening the administration for each one separately is slower than like this where all my stores are in a pile and only the products are loaded.
