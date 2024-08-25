package com.example.adminblinkitclone.viewmodels

import android.app.NotificationChannel
import android.app.NotificationManager
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.adminblinkitclone.Utils
import com.example.adminblinkitclone.api.ApiUtilities
import com.example.adminblinkitclone.models.CartProductTable
import com.example.adminblinkitclone.models.FCMMessage
import com.example.adminblinkitclone.models.Notification
import com.example.adminblinkitclone.models.Orders
import com.example.adminblinkitclone.models.Product
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

class AdminViewModel: ViewModel() {

    private val _isImagesUploaded = MutableStateFlow(false)
    var isImagesUploaded: StateFlow<Boolean> = _isImagesUploaded

    private val _downloadedUrls = MutableStateFlow<ArrayList<String?>>(arrayListOf())
    var downloadedUrls : StateFlow<ArrayList<String?>> = _downloadedUrls


    private val _isProductSaved = MutableStateFlow(false)
    var isProductSaved: StateFlow<Boolean> = _isProductSaved



    fun saveImageInDB(imageUri: ArrayList<Uri>){
        val downloadUrls = ArrayList<String?>()
        imageUri.forEach{uri ->
            val imageRef = FirebaseStorage.getInstance().reference.child(Utils.getCurrentUserId()).child("images").child(UUID.randomUUID().toString())
            imageRef.putFile(uri).continueWithTask {
                imageRef.downloadUrl
            }.addOnCompleteListener { task->
                val url =task.result
                downloadUrls.add(url.toString())
                if(downloadUrls.size==imageUri.size){
                    _isImagesUploaded.value=true
                    _downloadedUrls.value=downloadUrls
                }
            }
        }



    }
    fun saveProduct(product: Product){
        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts/${product.productRandomId}").setValue(product)
            .addOnSuccessListener {
                FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${product.productCategory}/${product.productRandomId}").setValue(product)
                    .addOnSuccessListener {
                        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType/${product.productType}/${product.productRandomId}").setValue(product)
                            .addOnSuccessListener {
                                _isProductSaved.value = true
                            }

                    }

            }
    }
    fun logOutUser(){
        FirebaseAuth.getInstance().signOut()
    }
    fun fetchAllTheProducts(category:String): Flow<List<Product>> = callbackFlow{
        val db =     FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts")
        val eventListener = object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()
                for (product in snapshot.children) {
                    val prod = product.getValue(Product::class.java)
                    if(category=="All" || prod?.productCategory==category){
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
        awaitClose {
            db.removeEventListener(eventListener)
        }
    }
    fun getAllOrders():Flow<List<Orders>> = callbackFlow {
        val db= FirebaseDatabase.getInstance().getReference("Admins").child("Orders").orderByChild("orderStatus")
        val eventListener = object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderList = ArrayList<Orders>()
                for (orders in snapshot.children){
                    val order = orders.getValue(Orders::class.java)
                    if(order!=null){
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
        awaitClose{
            db.removeEventListener(eventListener)
        }

    }
    fun savingUpdateProducts(product: Product){
        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts/${product.productRandomId}").setValue(product)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${product.productCategory}/${product.productRandomId}").setValue(product)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType/${product.productType}/${product.productRandomId}").setValue(product)

    }
    fun getOrderedProducts(orderId :String):Flow<List<CartProductTable>> = callbackFlow{
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("Orders").child(orderId)
        val eventListener = object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val order = snapshot.getValue(Orders::class.java)
                trySend(order?.orderList!!)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        db.addValueEventListener(eventListener)
        awaitClose{
            db.removeEventListener(eventListener)
        }
    }
    fun updateOrderStatus(orderId: String,status:Int){
        FirebaseDatabase.getInstance().getReference("Admins").child("Orders").child(orderId).child("orderStatus").setValue(status)

    }
    suspend fun sendNotification(orderId: String, title: String, message: String) {
        val getToken = FirebaseDatabase.getInstance().getReference("Admins").child("Orders").child(orderId).child("orderingUserId").get()

        getToken.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("task1",task.result.getValue(String::class.java).toString())
                val userUid = task.result.getValue(String::class.java)
                val userToken = FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(userUid!!).child("userToken").get()
                userToken.addOnCompleteListener {task2->
                    if(task2!=null){
                        Log.d("task2",task2.result.getValue(String::class.java).toString())
                    val notification = Notification(title = title, body = message)
                    val message = FCMMessage(
                        message = FCMMessage.Message(
                            token = task2.result.getValue(String::class.java), // or use `topic` or `condition` if applicable
                            notification = notification
                        )
                    )

                    ApiUtilities.notificationApi.sendNotification(message).enqueue(object :
                        Callback<FCMMessage> {
                        override fun onResponse(call: Call<FCMMessage>, response: Response<FCMMessage>) {
                            if (response.isSuccessful) {


                                Log.d("GGG", "send notification")
                                Log.d("GGG", task2.result.getValue(String::class.java).toString())

                            } else {
                                Log.d("GGG", "not send notification, response code: ${response.code()}")
                                Log.d("GGG", "error: ${response.errorBody()?.string()}")
                            }
                        }

                        override fun onFailure(call: Call<FCMMessage>, t: Throwable) {
                            Log.d("ggg", "error: ${t.message}")
                        }
                    })
                }

                 else {
                    Log.d("ggg", "Token is null")
                }}}
             else {
                Log.d("ggg", "Failed to get token: ${task.exception?.message}")
            }
        }
    }

}