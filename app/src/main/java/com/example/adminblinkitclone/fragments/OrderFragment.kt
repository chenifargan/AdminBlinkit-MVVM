package com.example.adminblinkitclone.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController

import com.example.adminblinkitclone.R
import com.example.adminblinkitclone.adapter.AdapterOrders
import com.example.adminblinkitclone.databinding.FragmentOrderBinding
import com.example.adminblinkitclone.models.OrderedItems
import com.example.adminblinkitclone.viewmodels.AdminViewModel
import kotlinx.coroutines.launch
import java.lang.StringBuilder


class OrderFragment : Fragment() {

    private lateinit var binding : FragmentOrderBinding
    private  val viewModel : AdminViewModel by viewModels()
    private lateinit var adapterOrders : AdapterOrders


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrderBinding.inflate(layoutInflater)
        setStatusBarColor()
        getAllOrders()
        return binding.root
    }

    private fun onOrderItemViewClicked(orderedItems: OrderedItems) {

        val bundle = Bundle()
        bundle.putInt("status",orderedItems.itemStatus!!)
        bundle.putString("orderId",orderedItems.orderId!!)
        bundle.putString("userAddress",orderedItems.userAddress!!)

        findNavController().navigate(R.id.action_orderFragment_to_orderDetailFragment,bundle)

    }

    private fun getAllOrders() {
        binding.shimmerViewContainer.visibility =View.VISIBLE
        lifecycleScope.launch {
            viewModel.getAllOrders().collect{orderList->
                if(orderList.isNotEmpty()){
                    val orderedList =ArrayList<OrderedItems>()
                    for (orders in orderList){
                        val title= StringBuilder()
                        var totalPrice =0
                        for (product in orders.orderList!!){
                            val price = product.productPrice?.let {
                                it.removeSuffix("$").toIntOrNull()
                            } ?: 0
                            val item = product.productCount?:0
                            totalPrice +=(price*item)
                            title.append("${product.productCategory} ,")
                        }
                        val orderedItems = OrderedItems( orders.orderId,orders.orderDate,orders.orderStatus,title.toString(),totalPrice,orders.userAddress)
                        orderedList.add(orderedItems)
                    }
                    adapterOrders= AdapterOrders(requireContext(),::onOrderItemViewClicked)
                    binding.rvOrders.adapter=adapterOrders
                    adapterOrders.differ.submitList(orderedList)
                    binding.shimmerViewContainer.visibility =View.GONE

                }

            }
        }

    }



    private fun setStatusBarColor(){
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.yellow)
            statusBarColor=statusBarColors
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }

        }
    }
}