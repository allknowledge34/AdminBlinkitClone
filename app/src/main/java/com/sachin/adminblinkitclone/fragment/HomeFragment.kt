package com.sachin.adminblinkitclone.fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.sachin.adminblinkitclone.Constants
import com.sachin.adminblinkitclone.R
import com.sachin.adminblinkitclone.Utils
import com.sachin.adminblinkitclone.activity.AuthMainActivity
import com.sachin.adminblinkitclone.adapters.AdapterProduct
import com.sachin.adminblinkitclone.adapters.CategoriesAdapter
import com.sachin.adminblinkitclone.databinding.EditProductLayoutBinding
import com.sachin.adminblinkitclone.databinding.FragmentHomeBinding
import com.sachin.adminblinkitclone.models.Categories
import com.sachin.adminblinkitclone.models.Product
import com.sachin.adminblinkitclone.viewmodels.AdminViewModel
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {
    val viewModel: AdminViewModel by viewModels()
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapterProduct: AdapterProduct

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        adapterProduct = AdapterProduct(::onEditButtonClicked)
        binding.rvProduct.adapter = adapterProduct

        setStatusBarColor()
        setCategories()
        serachProduct()
        onLogOut()
        getAlltheProduct("All")
        return binding.root
    }

    private fun onLogOut() {
        binding.tbHomeFragment.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.menuLogout -> {
                    logOutUser()
                    true
                }

                else -> {false}
            }
        }
    }

    private fun logOutUser() {
            val bundle = AlertDialog.Builder(requireContext())
            val alertDialog = bundle.create()
            bundle.setTitle("Log Out")
                .setMessage("Do You Want To Log Out")
                .setPositiveButton("Yes"){_,_ ->
                    viewModel.logOutUser()
                    startActivity(Intent(requireContext(), AuthMainActivity::class.java))
                    requireActivity().finish()
                }
                .setNegativeButton("No"){_,_ ->
                    alertDialog.dismiss()
                }
                .show()
                .setCancelable(false)

    }

    private fun serachProduct() {
        binding.searchEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                adapterProduct.getFilter().filter(query)
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    private fun getAlltheProduct(category: String) {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        lifecycleScope.launch {
            viewModel.fetchAllTheProduct(category).collect {

                if (it.isEmpty()){
                    binding.rvProduct.visibility = View.GONE
                    binding.tvText.visibility = View.VISIBLE
                }
                else {
                    binding.rvProduct.visibility = View.VISIBLE
                    binding.tvText.visibility = View.GONE
                }
                adapterProduct = AdapterProduct(::onEditButtonClicked)
                binding.rvProduct.adapter = adapterProduct
                adapterProduct.differ.submitList(it)
                adapterProduct.orginalList = it as ArrayList<Product>

                binding.shimmerViewContainer.visibility = View.GONE
            }
        }
    }

    private fun setCategories() {
        val categoryList = ArrayList<Categories>()

        for (i in 0 until Constants.allProductCategoryIcon.size) {
            categoryList.add(Categories(Constants.allProductCategory[i], Constants.allProductCategoryIcon[i]))
        }

        binding.rvCategories.adapter = CategoriesAdapter(categoryList, ::onCategoryClicked)
    }
    private fun onCategoryClicked(categories: Categories){
        getAlltheProduct(categories.category)
    }

    private fun onEditButtonClicked(product: Product){
        val editProduct = EditProductLayoutBinding.inflate(LayoutInflater.from(requireContext()))
        editProduct.apply {
            etProductTitle.setText(product.productTitle)
            etProductQuantity.setText(product.productQuantity.toString())
            etProductUnit.setText(product.productUnit)
            etProductPrice.setText(product.productPrice.toString())
            etProductStock.setText(product.productStock.toString())
            etProductCategory.setText(product.productCategory)
            etProductType.setText(product.productType)
        }

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(editProduct.root)
            .create()
        alertDialog.show()

        editProduct.btnEdit.setOnClickListener {
            editProduct.apply {


                editProduct.etProductTitle.isEnabled = true
                editProduct.etProductQuantity.isEnabled = true
                editProduct.etProductUnit.isEnabled = true
                editProduct.etProductPrice.isEnabled = true
                editProduct.etProductStock.isEnabled = true
                editProduct.etProductCategory.isEnabled = true
                editProduct.etProductType.isEnabled = true
            }
        }
        setAutoCompleteTextViews(editProduct)

        editProduct.btnSave.setOnClickListener {
            lifecycleScope.launch {
                product.productTitle = editProduct.etProductTitle.text.toString()
                product.productQuantity = editProduct.etProductQuantity.text.toString().toInt()
                product.productUnit = editProduct.etProductUnit.text.toString()
                product.productPrice = editProduct.etProductPrice.text.toString().toInt()
                product.productStock = editProduct.etProductStock.text.toString().toInt()
                product.productCategory = editProduct.etProductCategory.text.toString()
                product.productType = editProduct.etProductType.text.toString()
                viewModel.saveingUpdateProducts(product)
            }


            Utils.showToast(requireContext(), message = "Saved Changes")
            alertDialog.dismiss()
        }
    }

    private fun setAutoCompleteTextViews(editProduct: EditProductLayoutBinding) {
        val units = ArrayAdapter(requireContext(), R.layout.show_list, Constants.allUnitsOfProducts)
        val category = ArrayAdapter(requireContext(), R.layout.show_list, Constants.allProductCategory)
        val productType = ArrayAdapter(requireContext(), R.layout.show_list, Constants.allProductType)

        editProduct.apply {
            etProductUnit.setAdapter(units)
            etProductCategory.setAdapter(category)
            etProductType.setAdapter(productType)
        }
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


