package com.example.adminblinkitclone.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.adminblinkitclone.Constants
import com.example.adminblinkitclone.R
import com.example.adminblinkitclone.Utils
import com.example.adminblinkitclone.activity.AuthMainActivity
import com.example.adminblinkitclone.adapter.AdapterProduct
import com.example.adminblinkitclone.adapter.CategoriesAdapter
import com.example.adminblinkitclone.databinding.EditProductLayoutBinding
import com.example.adminblinkitclone.databinding.FragmentHomeBinding
import com.example.adminblinkitclone.models.Categories
import com.example.adminblinkitclone.models.Product
import com.example.adminblinkitclone.viewmodels.AdminViewModel
import com.example.adminblinkitclone.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    val viewModel :AdminViewModel by viewModels()
    private lateinit var binding : FragmentHomeBinding
    private lateinit var adapterProduct: AdapterProduct
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        setStatusBarColor()
        setCategories()
        onLogOutClicked()
        searchProducts()
        getAllTheProducts("All")
        return binding.root
    }

    private fun onLogOutClicked() {
        binding.tbHomeFragment.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.menuLogOut->{
                    logOut()
                    true

                }

                else -> {false}
            }
        }
    }

    private fun logOut() {
        val builder = AlertDialog.Builder(requireContext())
        val alertDialog =builder.create()

        builder.setTitle("Log out")
            .setMessage("Do you want log out?")
            .setPositiveButton("Yes"){_,_->
                viewModel.logOutUser()
                startActivity(Intent(requireContext(),AuthMainActivity::class.java))
                requireActivity().finish()
            }
            .setNegativeButton("No"){_,_->
                alertDialog.dismiss()
            }
            .show()
            .setCancelable(false)
    }

    private fun searchProducts() {
        binding.searchEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val quary = s.toString().trim()
                adapterProduct.getFilter().filter(quary)
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun getAllTheProducts(category: String) {
        binding.shimmerViewContainer.visibility =View.VISIBLE
        lifecycleScope.launch {
            viewModel.fetchAllTheProducts(category).collect{
                if (it.isEmpty()){
                    binding.rvProducts.visibility = View.GONE
                    binding.tvText.visibility = View.VISIBLE
                }
                else{
                    binding.rvProducts.visibility = View.VISIBLE
                    binding.tvText.visibility = View.GONE
                }
                adapterProduct = AdapterProduct(::onEditButtonClicked)
                binding.rvProducts.adapter= adapterProduct
                adapterProduct.differ.submitList(it)
                adapterProduct.originalList =it as ArrayList<Product>
                binding.shimmerViewContainer.visibility =View.GONE

            }
        }



    }

    private fun setCategories() {
        val categoryList = ArrayList<Categories>()
        for (i in 0 until Constants.allProductsCategoryIcon.size)
            categoryList.add(Categories(Constants.allProductsCategory[i],Constants.allProductsCategoryIcon[i]))
        binding.rvCategories.adapter=CategoriesAdapter(categoryList,::onCategoryClicked)

    }
     private fun onCategoryClicked(categories: Categories){
        getAllTheProducts(categories.category)
    }
    private fun onEditButtonClicked(product:Product){
        val editProduct = EditProductLayoutBinding.inflate(LayoutInflater.from(requireContext()))
        editProduct.apply {
            etProductTitle.setText(product.productTitle)
            etProductQuntity.setText(product.productQuantity.toString())
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
               etProductTitle.isEnabled = true
               etProductQuntity.isEnabled = true
              etProductUnit.isEnabled = true
                etProductPrice.isEnabled = true
                etProductStock.isEnabled = true
              etProductCategory.isEnabled = true
               etProductType.isEnabled = true
            }



        }
        setAutoCompleteTextViews(editProduct)
        editProduct.btnSave.setOnClickListener {
            lifecycleScope.launch {
                product.productTitle = editProduct.etProductTitle.text.toString()
                product.productQuantity = editProduct.etProductQuntity.text.toString().toInt()
                product.productUnit = editProduct.etProductUnit.text.toString()
                product.productPrice = editProduct.etProductPrice.text.toString().toInt()
                product.productStock = editProduct.etProductStock.text.toString().toInt()
                product.productCategory = editProduct.etProductCategory.text.toString()
                product.productType = editProduct.etProductType.text.toString()
                viewModel.savingUpdateProducts(product)

            }
            Utils.showToast(requireContext(),"Saved changes!")
            alertDialog.dismiss()



        }

    }

    private fun setAutoCompleteTextViews(editProduct: EditProductLayoutBinding) {
        val units = ArrayAdapter(requireContext(),R.layout.show_list,Constants.allUnitOfProducts)
        val category = ArrayAdapter(requireContext(),R.layout.show_list,Constants.allProductsCategory)
        val productType= ArrayAdapter(requireContext(),R.layout.show_list,Constants.allProductType)
        editProduct.apply {
            etProductUnit.setAdapter(units)
            etProductCategory.setAdapter(category)
            etProductType.setAdapter(productType)
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