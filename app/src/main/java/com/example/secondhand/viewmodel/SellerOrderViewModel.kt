package com.example.secondhand.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.secondhand.model.GetSellerOrderResponseItem
import com.example.secondhand.network.ApiServices
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class SellerOrderViewModel @Inject constructor(api: ApiServices) : ViewModel() {
    private val responseMsg = MutableLiveData<Boolean>()
    val responseMessage: LiveData<Boolean> = responseMsg
    val apiServices = api

    private val liveDataSellerOrder = MutableLiveData<List<GetSellerOrderResponseItem>>()
    val sellerOrder: LiveData<List<GetSellerOrderResponseItem>> = liveDataSellerOrder

    private val liveDataSellerOrderById = MutableLiveData<GetSellerOrderResponseItem>()
    val sellerOrderById: LiveData<GetSellerOrderResponseItem> = liveDataSellerOrderById

    fun updateOrderStatus(token: String, id: Int, statusOrder: RequestBody) {
        apiServices.updateStatusOrder(token, id, statusOrder)
            .enqueue(object : Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    responseMsg.value = response.isSuccessful
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    responseMsg.value = false
                }

            })
    }

    fun getAllSellerOrder(token: String) {
        apiServices.getAllSellerOrder(token)
            .enqueue(object : Callback<List<GetSellerOrderResponseItem>> {
                override fun onResponse(
                    call: Call<List<GetSellerOrderResponseItem>>,
                    response: Response<List<GetSellerOrderResponseItem>>
                ) {
                    if (response.isSuccessful) {
                        liveDataSellerOrder.value = response.body()
                    }
                }

                override fun onFailure(call: Call<List<GetSellerOrderResponseItem>>, t: Throwable) {
                    //
                }

            })
    }

    fun getSellerOrderById(token: String, id: Int) {
        apiServices.getSellerOrderById(token, id)
            .enqueue(object : Callback<GetSellerOrderResponseItem> {
                override fun onResponse(
                    call: Call<GetSellerOrderResponseItem>,
                    response: Response<GetSellerOrderResponseItem>
                ) {
                    if (response.isSuccessful) {
                        liveDataSellerOrderById.value = response.body()
                    } else {
                        // nothing to do
                    }
                }

                override fun onFailure(call: Call<GetSellerOrderResponseItem>, t: Throwable) {
                    // nothing to do
                }

            })
    }
}