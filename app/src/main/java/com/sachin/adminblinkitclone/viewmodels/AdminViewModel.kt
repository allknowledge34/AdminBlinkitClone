package com.sachin.adminblinkitclone.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.sachin.adminblinkitclone.Utils
import com.sachin.adminblinkitclone.models.CartProducts
import com.sachin.adminblinkitclone.models.Orders
import com.sachin.adminblinkitclone.models.Product
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

class AdminViewModel : ViewModel() {

    private  val _isImageUploaded = MutableStateFlow(false)
    var isImageUpload: StateFlow<Boolean> = _isImageUploaded

    private  val _downloadedUrls = MutableStateFlow<ArrayList<String?>>(arrayListOf())
    var downloadedUrls : StateFlow<ArrayList<String?>> = _downloadedUrls

    private val  _isProductSaved = MutableStateFlow(false)
    var isProductSaved: StateFlow<Boolean> = _isProductSaved

    fun saveImageInDB(imageUri : ArrayList<Uri>){
        val downloadUrls = ArrayList<String?>()

        imageUri.forEach { uri ->
            val imageRef = FirebaseStorage.getInstance().reference
                .child(Utils.getCurrentUserId()).child("images").child(UUID.randomUUID().toString())
            imageRef.putFile(uri).continueWithTask{
                imageRef.downloadUrl
            }.addOnCompleteListener { task->
                val url = task.result
                downloadUrls.add(url.toString())

                if (downloadUrls.size == imageUri.size){
                    _isImageUploaded.value = true
                    _downloadedUrls.value = downloadUrls
                }
            }
        }


    }

    fun saveProduct(product: Product) {

        FirebaseDatabase.getInstance().getReference("Admins")
            .child("AllProduct/${product.productRandomId}").setValue(product)
            .addOnSuccessListener {
                FirebaseDatabase.getInstance().getReference("Admins")
                    .child("ProductCategory/${product.productCategory}/${product.productRandomId}").setValue(product)
                    .addOnSuccessListener {
                        FirebaseDatabase.getInstance().getReference("Admins")
                            .child("ProductType/${product.productType}/${product.productRandomId}").setValue(product)
                            .addOnSuccessListener {
                                _isProductSaved.value = true
                            }
                    }
            }
    }

    fun fetchAllTheProduct(category: String): Flow<List<Product>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("Admins")
            .child("AllProduct")

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()
                for (product in snapshot.children){
                    val prod = product.getValue(Product::class.java)
                    if (category == "All" || prod?.productCategory == category){
                        products.add(prod!!)
                    }

                }
                trySend(products)

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        db.addValueEventListener(eventListener)

        awaitClose{db.removeEventListener(eventListener)}
    }

    fun saveingUpdateProducts(product: Product){
        FirebaseDatabase.getInstance().getReference("Admins").child("AllProduct/${product.productRandomId}").setValue(product)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${product.productCategory}/${product.productRandomId}").setValue(product)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType/${product.productType}/${product.productRandomId}").setValue(product)
    }
    fun getAllOrders() : Flow<List<Orders>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("Orders").orderByChild("orderStatus")

        val eventListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderList = ArrayList<Orders>()
                for (orders in snapshot.children){
                    val order = orders.getValue(Orders::class.java)
                    if (order != null) {
                        orderList.add(order)
                    }

                }
                trySend(orderList)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        db.addValueEventListener(eventListener)
        awaitClose{db.removeEventListener(eventListener)}
    }
    fun getOrderedProducts(orderId : String) : Flow<List<CartProducts>> = callbackFlow{
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("Orders").child(orderId)
        val eventListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val order = snapshot.getValue(Orders::class.java)
                trySend(order?.orderList!!)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        db.addValueEventListener(eventListener)
        awaitClose { db.removeEventListener(eventListener) }
    }

    fun updateOrderStatus(orderId: String, status : Int){
        FirebaseDatabase.getInstance().getReference("Admins").child("Orders").child(orderId).child("orderStatus").setValue(status)
    }
    fun logOutUser(){
        FirebaseAuth.getInstance().signOut()
    }

}