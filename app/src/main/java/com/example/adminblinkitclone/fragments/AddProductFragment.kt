package com.example.adminblinkitclone.fragments

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.adminblinkitclone.activity.AdminMainActivity
import com.example.adminblinkitclone.Constants
import com.example.adminblinkitclone.R
import com.example.adminblinkitclone.Utils
import com.example.adminblinkitclone.adapter.AdapterSelectedImage
import com.example.adminblinkitclone.databinding.FragmentAddProductBinding
import com.example.adminblinkitclone.models.Product
import com.example.adminblinkitclone.viewmodels.AdminViewModel
import kotlinx.coroutines.launch


class AddProductFragment : Fragment() {
    private val viewModel : AdminViewModel by viewModels()
    private lateinit var binding: FragmentAddProductBinding
    private val imageUris : ArrayList<Uri> = arrayListOf()
    val selectImage =     registerForActivityResult(ActivityResultContracts.GetMultipleContents()){listOfUri->
        val fiveImages = listOfUri.take(5)
        imageUris.clear()
        imageUris.addAll(fiveImages)
        binding.rvProductImages.adapter = AdapterSelectedImage(imageUris)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddProductBinding.inflate(layoutInflater)
        setStatusBarColor()
        setAutoCompleteTextViews()
        onImageSelectClicked()
        onAddButtonClicked()
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun onAddButtonClicked() {
    binding.btnAddProduct.setOnClickListener {
        Utils.showDialog(requireContext(),"Uploading images.... ")
        val productTitle = binding.etProductTitle.text.toString()
        val productQuantity = binding.etProductQuntity.text.toString()
        val productUnit = binding.etProductUnit.text.toString()
        val productPrice = binding.etProductPrice.text.toString()
        val productStock = binding.etProductStock.text.toString()
        val productCategory = binding.etProductCategory.text.toString()
        val productType = binding.etProductType.text.toString()

        if(productUnit.isEmpty()||productCategory.isEmpty()||productType.isEmpty()||productStock.isEmpty()
            ||productPrice.isEmpty()||productQuantity.isEmpty()||productTitle.isEmpty()){
            Utils.apply {
                hideDialog()
                showToast(requireContext(),"Empty fields are not allowed")
            }


        }
        else if(imageUris.isEmpty()){
            Utils.apply {
                hideDialog()
                showToast(requireContext(),"please upload some images")
            }
        }
        else{
            val product = Product(
                productTitle=productTitle,
                productQuantity = productQuantity.toInt(),
                productUnit = productUnit,
                productPrice = productPrice.toInt(),
                productStock = productStock.toInt(),
                productCategory = productCategory,
                productType = productType,
                itemCount = 0,
                adminUid = Utils.getCurrentUserId(),
                productRandomId = Utils.getRandomId()
                
            )
            saveImage(product)
        }


    }
    }

    private fun saveImage(product: Product) {
    viewModel.saveImageInDB(imageUris)
        lifecycleScope.launch {
            viewModel.isImagesUploaded.collect{
                if(it){
                    Utils.apply {
                        hideDialog()
                        showToast(requireContext(),"image saved")
                    }
                    getUrls(product)
                }
            }
        }
    }

    private fun getUrls(product: Product) {
        Utils.showDialog(requireContext(),"Publishing product.....")
        lifecycleScope.launch {
            viewModel.downloadedUrls.collect{
                val urls = it
                product.productImageUris = urls
                saveProduct(product)

            }
        }

    }

    private fun saveProduct(product: Product) {
        viewModel.saveProduct(product)
        lifecycleScope.launch {
            viewModel.isProductSaved.collect{
                if(it){
                    Utils.hideDialog()
                    startActivity(Intent(requireContext(), AdminMainActivity::class.java))
                    Utils.showToast(requireContext(),"Product is live ")
                }
        }
        }


    }

    private fun onImageSelectClicked() {
        binding.btnSelectImage.setOnClickListener{
        selectImage.launch("image/*")
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
    private fun setAutoCompleteTextViews(){
        val units = ArrayAdapter(requireContext(),R.layout.show_list,Constants.allUnitOfProducts)
        val category = ArrayAdapter(requireContext(),R.layout.show_list,Constants.allProductsCategory)
        val productType= ArrayAdapter(requireContext(),R.layout.show_list,Constants.allProductType)
        binding.apply {
            etProductUnit.setAdapter(units)
            etProductCategory.setAdapter(category)
            etProductType.setAdapter(productType)
        }

    }
}