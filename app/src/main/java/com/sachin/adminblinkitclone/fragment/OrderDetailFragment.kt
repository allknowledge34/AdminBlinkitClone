package com.sachin.adminblinkitclone.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sachin.adminblinkitclone.R
import com.sachin.adminblinkitclone.Utils
import com.sachin.adminblinkitclone.adapters.AdapterCartProducts
import com.sachin.adminblinkitclone.databinding.FragmentOrderDetailBinding
import com.sachin.adminblinkitclone.viewmodels.AdminViewModel
import kotlinx.coroutines.launch

class OrderDetailFragment : Fragment() {
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var binding : FragmentOrderDetailBinding
    private lateinit var adapterCartProducts: AdapterCartProducts
    private var status =0
    private var currentStatus =0
    private var orderId = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrderDetailBinding.inflate(layoutInflater)
        setStatusBarColor()
        getValues()
        settingStatus(status)
        onBackButtonClicked()
        onChangeStatusButtonClicked()
        lifecycleScope.launch {
            getOrderedProducts()
        }
        return binding.root
    }

    private fun onChangeStatusButtonClicked() {
        binding.btnChangeStatus.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.menuInflater.inflate(R.menu.menu_popup, popupMenu.menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener { menu->
                when(menu.itemId){

                    R.id.menuReceived ->{
                        currentStatus = 1
                        if(currentStatus > status){
                            status = 1
                            settingStatus(1)
                            viewModel.updateOrderStatus(orderId, 1)
                        }
                        else{
                            Utils.showToast(requireContext(), "Order is already received...")
                        }
                        true
                    }
                    R.id.menuDispatched ->{
                        currentStatus = 2
                        if(currentStatus > status){
                            status =2
                            settingStatus(2)
                            viewModel.updateOrderStatus(orderId, 2)
                        }
                        else{
                            Utils.showToast(requireContext(), "Order is already dispatched...")
                        }
                        true
                    }
                    R.id.menuDelivered ->{
                        currentStatus = 3
                        if(currentStatus > status){
                            status =3
                            settingStatus(3)
                            viewModel.updateOrderStatus(orderId, 3)
                        }
                        true
                    }
                    else -> {false}
                }
            }
        }
    }

    private fun onBackButtonClicked() {
        binding.tbOrderDetailFragment.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_orderDetailFragment_to_orderFragment)
        }
    }

    suspend fun getOrderedProducts() {
        viewModel.getOrderedProducts(orderId).collect{cartList->
            adapterCartProducts = AdapterCartProducts()
            binding.rvProductsItems.adapter = adapterCartProducts
            adapterCartProducts.differ.submitList(cartList)
        }
    }

    private fun settingStatus(status : Int) {
        val statusToView= mapOf(
            0 to listOf(binding.iv1),
            1 to listOf(binding.iv1, binding.iv2, binding.view1),
            2 to listOf(binding.iv1, binding.iv2, binding.iv3, binding.view1, binding.view2),
            3 to listOf(binding.iv1, binding.iv2, binding.iv3, binding.iv4, binding.view1, binding.view2, binding.view3)

        )
        val viewsToTint = statusToView.getOrDefault(status, emptyList())

        for (view in viewsToTint){
            view.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.green)
        }


    }

    private fun getValues() {
        val bundle = arguments
        status=bundle?.getInt("status")!!
        orderId = bundle.getString("orderId").toString()
        binding.tvUserAddress.text = bundle.getString("userAddress").toString()
    }
    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.yellow)
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}