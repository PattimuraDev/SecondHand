package com.example.secondhand.view.fragment

import android.content.ContentResolver
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.secondhand.R
import com.example.secondhand.datastore.UserLoginTokenManager
import com.example.secondhand.helper.DaftarJualProductSayaItemClickListener
import com.example.secondhand.model.GetSellerProductItem
import com.example.secondhand.model.SellerProductUpdateRequest
import com.example.secondhand.view.activity.LengkapiInfoAkun
import com.example.secondhand.view.activity.LoginActivity
import com.example.secondhand.view.adapter.SellerProductAdapter
import com.example.secondhand.viewmodel.SellerProductViewModel
import com.example.secondhand.viewmodel.SellerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.custom_edit_data_product_dialog_alert.view.*
import kotlinx.android.synthetic.main.fragment_daftar_jual.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@AndroidEntryPoint
class DaftarJualFragment : Fragment(), DaftarJualProductSayaItemClickListener {
    private lateinit var userLoginTokenManager: UserLoginTokenManager
    private lateinit var adapter: SellerProductAdapter

    private var imageMultiPart: MultipartBody.Part? = null
    private var imageUri: Uri? = Uri.EMPTY
    private var imageFile: File? = null

    private lateinit var selectedCategory: BooleanArray
    private var categoryList = arrayListOf<Int>()
    private var availableCategory = arrayOf(
        "Makanan",
        "Minuman",
        "Fashion",
        "Alat dapur",
        "Kesehatan",
        "Olahraga",
        "Hobi",
        "Kendaraan",
        "Lainnya"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_daftar_jual, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModelSeller = ViewModelProvider(this)[SellerViewModel::class.java]

        userLoginTokenManager = UserLoginTokenManager(requireContext())

        userLoginTokenManager.isUser.asLiveData().observe(viewLifecycleOwner) { isUser ->
            if (isUser) {
                daftar_jual_saya_belum_login_section.isInvisible = true
                userLoginTokenManager.accessToken.asLiveData().observe(viewLifecycleOwner) {
                    viewModelSeller.getSellerData(it)
                }

                initView()
            } else {
                daftar_jual_saya_title.isInvisible = true
                daftar_jual_saya_profile_penjual_section.isInvisible = true
                daftar_jual_saya_filter_produk_section.isInvisible = true
                rv_daftar_jual_saya.isInvisible = true
                daftar_jual_saya_progress_bar.isInvisible = true

                daftar_jual_saya_to_login.setOnClickListener {
                    startActivity(Intent(activity, LoginActivity::class.java))
                }


            }
        }

    }

    private fun initView() {
        val viewModelSeller = ViewModelProvider(this)[SellerViewModel::class.java]
        daftar_jual_saya_edit_profile_penjual_button.setOnClickListener {
            startActivity(Intent(activity, LengkapiInfoAkun::class.java))
        }
        viewModelSeller.seller.observe(viewLifecycleOwner) {
            daftar_jual_saya_nama_penjual.text = it.full_name
            daftar_jual_saya_kota_penjual.text = it.city
            Glide.with(daftar_jual_saya_image_penjual.context)
                .load(it.image_url)
                .error(R.drawable.ic_launcher_background)
                .override(72, 72)
                .into(daftar_jual_saya_image_penjual)
        }

        initRecyclerView()
    }

    private fun initRecyclerView() {
        userLoginTokenManager = UserLoginTokenManager(requireContext())
        val viewModelSellerProduct = ViewModelProvider(this)[SellerProductViewModel::class.java]
        userLoginTokenManager.accessToken.asLiveData().observe(viewLifecycleOwner) {
            viewModelSellerProduct.getAllSellerProduct(it)
        }
        adapter = SellerProductAdapter(this@DaftarJualFragment)
        rv_daftar_jual_saya.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rv_daftar_jual_saya.adapter = adapter

        viewModelSellerProduct.sellerProduct.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                adapter.setDataSellerProduct(it)
                daftar_jual_saya_progress_bar.isInvisible = true
                adapter.notifyDataSetChanged()
            } else {
                daftar_jual_saya_progress_bar.isInvisible = true
            }
        }

    }

    override fun editProductInDaftarJualSaya(item: GetSellerProductItem, position: Int) {
        userLoginTokenManager = UserLoginTokenManager(requireContext())
        val viewModelSellerProduct = ViewModelProvider(this)[SellerProductViewModel::class.java]

        val customDialogEdit = LayoutInflater.from(requireContext()).inflate(
            R.layout.custom_edit_data_product_dialog_alert, null, false
        )
        val editDataDialog = AlertDialog.Builder(requireContext())
            .setView(customDialogEdit)
            .create()

        customDialogEdit.edit_product_kategori.setOnClickListener {
            initMultiChoicesAlertDialog(customDialogEdit)
        }

        customDialogEdit.edit_product_foto_button.setOnClickListener {
            openGallery()
            customDialogEdit.edit_product_foto.setImageURI(imageUri)
        }

        customDialogEdit.edit_product_update_button.setOnClickListener {
            val namaBaruProduct = customDialogEdit.edit_product_nama.text.toString().toRequestBody("multipart/form-data".toMediaType())
            val hargaBaruProduct = customDialogEdit.edit_product_harga.text.toString()
                .toRequestBody("multipart/form-data".toMediaType())
            val lokasiBaruToko = customDialogEdit.edit_product_lokasi_toko.text.toString()
                .toRequestBody("multipart/form-data".toMediaType())
            val deskripsiBaruProduct = customDialogEdit.edit_product_deskripsi.text.toString()
                .toRequestBody("multipart/form-data".toMediaType())

            val kategoriList = ArrayList<MultipartBody.Part>()
            if(categoryList.isNotEmpty()){
                for(i in categoryList.indices){
                    kategoriList.add(MultipartBody.Part.createFormData("category_ids", categoryList[i].toString()))
                }
            }

            if (
                customDialogEdit.edit_product_harga.text.isNotEmpty() &&
                customDialogEdit.edit_product_kategori.text.isNotEmpty() &&
                customDialogEdit.edit_product_lokasi_toko.text.isNotEmpty() &&
                customDialogEdit.edit_product_deskripsi.text.isNotEmpty() &&
                customDialogEdit.edit_product_nama.text.isNotEmpty() &&
                imageFile != null
            ) {
                userLoginTokenManager.accessToken.asLiveData().observe(viewLifecycleOwner) {
                    val file = (imageFile as File)
                    val requestImageFile = file.asRequestBody("multipart/form-data".toMediaType())
                    val imageMultipart =
                        MultipartBody.Part.createFormData("image", file.name, requestImageFile)
                    viewModelSellerProduct.updateProductInDaftarJualSaya(
                        it,
                        item.id,
                        SellerProductUpdateRequest(
                            hargaBaruProduct,
                            kategoriList,
                            deskripsiBaruProduct,
                            imageMultipart,
                            lokasiBaruToko,
                            namaBaruProduct
                        )
                    )
                    viewModelSellerProduct.responseMessage.observe(viewLifecycleOwner) { responseMsg ->
                        if (responseMsg) {
                            Toast.makeText(
                                requireContext(),
                                "Berhasil diupdate",
                                Toast.LENGTH_SHORT
                            ).show()
                            editDataDialog.dismiss()
                        } else {
                            Toast.makeText(requireContext(), "Gagal diupdate", Toast.LENGTH_SHORT)
                                .show()
                            editDataDialog.dismiss()
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT)
                    .show()
            }

        }

        editDataDialog.show()
    }

    override fun deleteProductFromDaftarJualSaya(item: GetSellerProductItem, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Produk")
            .setMessage("Anda yakin ingin menghapus produk ini?")
            .setNegativeButton("Batal") { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
            }
            .setPositiveButton("Ya") { _: DialogInterface, _: Int ->
                userLoginTokenManager = UserLoginTokenManager(requireContext())
                val viewModelSellerProduct =
                    ViewModelProvider(this)[SellerProductViewModel::class.java]
                userLoginTokenManager.accessToken.asLiveData()
                    .observe(viewLifecycleOwner) { accessToken ->
                        viewModelSellerProduct.deleteProductFromDaftarJualSaya(accessToken, item.id)
                    }
                adapter.deleteSellerProductByPosition(position)
                adapter.notifyItemRemoved(position)
            }.show()

    }

    private fun initMultiChoicesAlertDialog(view: View){
        selectedCategory = BooleanArray(availableCategory.size)
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Pilih kategori (maks. 4)")
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setMultiChoiceItems(availableCategory, selectedCategory
        ) { _: DialogInterface, i: Int, b: Boolean ->
            if (b) {
                if(categoryList.isEmpty()){
                    categoryList.add(i)
                }else{
                    if(categoryList.size >= 4){
                        Toast.makeText(requireContext(), "Maksimal 4 kategori", Toast.LENGTH_SHORT).show()
                    }else{
                        categoryList.add(i)
                    }
                }
            } else {
                categoryList.remove(Integer.valueOf(i))
            }
        }
        alertDialogBuilder.setPositiveButton("OK"
        ) { _, _ ->
            val stringBuilder = StringBuilder()
            for (j in 0 until categoryList.size) {
                stringBuilder.append(availableCategory[categoryList[j]])
                if (j != categoryList.size - 1) {
                    stringBuilder.append(", ")
                }
            }
            view.edit_product_kategori.text = stringBuilder.toString()
        }

        alertDialogBuilder.setNegativeButton("Cancel"
        ) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        alertDialogBuilder.setNeutralButton("Clear All"
        ) { _, _ ->
            for (j in selectedCategory.indices) {
                selectedCategory[j] = false
                categoryList.clear()
                view.edit_product_kategori.text = ""
            }
        }
        alertDialogBuilder.show()
    }

    private fun openGallery(){
        getContent.launch("image/*")
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val contentResolver: ContentResolver = context!!.contentResolver
                val type = contentResolver.getType(it)
                imageUri = it

                val tempFile = File.createTempFile("temp-", null, null)
                imageFile = tempFile
                val inputstream = contentResolver.openInputStream(uri)
                tempFile.outputStream().use { result ->
                    inputstream?.copyTo(result)
                }
                val requestBody: RequestBody = tempFile.asRequestBody(type?.toMediaType())
                imageMultiPart =
                    MultipartBody.Part.createFormData("image", tempFile.name, requestBody)
            }
        }
}