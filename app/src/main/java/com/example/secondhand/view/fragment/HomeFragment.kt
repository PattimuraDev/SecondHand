package com.example.secondhand.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.secondhand.R
import com.example.secondhand.helper.isOnline
import com.example.secondhand.model.GetBuyerProductResponseItem
import com.example.secondhand.model.RoomBuyerProduct
import com.example.secondhand.view.activity.DetailActivity
import com.example.secondhand.view.activity.WishlistActivity
import com.example.secondhand.view.adapter.BannerAdapter
import com.example.secondhand.view.adapter.BuyerProductAdapter
import com.example.secondhand.view.adapter.SearchResultAdapter
import com.example.secondhand.viewmodel.BannerViewModel
import com.example.secondhand.viewmodel.BuyerProductViewModel
import com.example.secondhand.viewmodel.RoomBuyerProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var adapterBanner: BannerAdapter
    private lateinit var adapter: BuyerProductAdapter
    private lateinit var searchProductResultAdapter: SearchResultAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        banner()
    }

    private fun initView() {
        val viewModelBuyerProduct = ViewModelProvider(this)[BuyerProductViewModel::class.java]
        val viewModelRoomBuyerProduct =
            ViewModelProvider(this)[RoomBuyerProductViewModel::class.java]

        viewModelBuyerProduct.getAllBuyerProduct()
        adapter = BuyerProductAdapter {
            val pindah = Intent(activity, DetailActivity::class.java)
            pindah.putExtra("detailbarang", it)
            startActivity(pindah)
        }
        rv_product_home.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rv_product_home.adapter = adapter

        home_to_wishlist_button.setOnClickListener {
            startActivity(Intent(requireContext(), WishlistActivity::class.java))
        }

        // initialize adapter and layout manager for searching activity
        searchProductResultAdapter = SearchResultAdapter {
            val pindah = Intent(activity, DetailActivity::class.java)
            pindah.putExtra("detailbarangsearchresult", it)
            startActivity(pindah)
        }
        home_list_search_result.layoutManager = LinearLayoutManager(requireContext())
        home_list_search_result.adapter = searchProductResultAdapter


        // prevent user to click these button while load data from server
        home_telusuri_kategori_elektronik_button.isClickable = false
        home_telusuri_kategori_semua_button.isClickable = false
        home_telusuri_kategori_semua_button.isClickable = false
        home_telusuri_kategori_semua_button.isSelected = true

        if (!isOnline(requireContext())) {
            viewModelRoomBuyerProduct.roomBuyerProduct.observe(viewLifecycleOwner) {
                if (it.listBuyerProduct!!.isNotEmpty()) {
                    adapter.setDataBuyerProduct(it.listBuyerProduct)
                    rv_product_home_progress_bar.isInvisible = true
                    adapter.notifyDataSetChanged()
                } else {
                    rv_product_home_progress_bar.isInvisible = true
                }
            }
        } else {
            viewModelBuyerProduct.buyerProduct.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    adapter.setDataBuyerProduct(it)
                    rv_product_home_progress_bar.isInvisible = true
                    adapter.notifyDataSetChanged()

                    //activate "telusuri kategori" button
                    home_telusuri_kategori_elektronik_button.isClickable = true
                    home_telusuri_kategori_semua_button.isClickable = true


                    //adding buyer product data to room database
                    viewModelRoomBuyerProduct.insertBuyerProductList(RoomBuyerProduct(null, it))
                }
            }
        }

        home_telusuri_kategori_semua_button.setOnClickListener {
            //button state
            home_telusuri_kategori_semua_button.isSelected = true
            home_telusuri_kategori_elektronik_button.isSelected = false
            home_telusuri_kategori_lainnya_button.isSelected = false
            home_telusuri_kategori_hobbi_button.isSelected = false
            home_telusuri_kategori_makanan_button.isSelected = false

            viewModelBuyerProduct.buyerProduct.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    adapter.setDataBuyerProduct(it)
                    rv_product_home_progress_bar.isInvisible = true
                    adapter.notifyDataSetChanged()
                }
            }
        }

        home_telusuri_kategori_elektronik_button.setOnClickListener {
            home_telusuri_kategori_elektronik_button.isSelected = true
            home_telusuri_kategori_semua_button.isSelected = false
            home_telusuri_kategori_lainnya_button.isSelected = false
            home_telusuri_kategori_hobbi_button.isSelected = false
            home_telusuri_kategori_makanan_button.isSelected = false

            viewModelBuyerProduct.buyerProduct.observe(viewLifecycleOwner) {
                val listProduct: MutableList<GetBuyerProductResponseItem> = mutableListOf()
                if (it.isNotEmpty()) {
                    for (i in it.indices) {
                        for (j in it[i].Categories!!.indices) {
                            if (it[i].Categories!![j].name.lowercase().contains("elektronik")) {
                                listProduct += it[i]
                            }
                        }
                    }
                    adapter.setDataBuyerProduct(listProduct)
                    rv_product_home_progress_bar.isInvisible = true
                    adapter.notifyDataSetChanged()
                }
            }
        }

        home_telusuri_kategori_makanan_button.setOnClickListener {
            home_telusuri_kategori_lainnya_button.isSelected = false
            home_telusuri_kategori_elektronik_button.isSelected = false
            home_telusuri_kategori_semua_button.isSelected = false
            home_telusuri_kategori_hobbi_button.isSelected = false
            home_telusuri_kategori_makanan_button.isSelected = true

            viewModelBuyerProduct.buyerProduct.observe(viewLifecycleOwner) {
                val listProduct: MutableList<GetBuyerProductResponseItem> = mutableListOf()
                if (it.isNotEmpty()) {
                    for (i in it.indices) {
                        if (it[i].Categories!!.isNotEmpty()) {
                            for (j in it[i].Categories!!.indices) {
                                val name = it[i].Categories!![j].name
                                if (name.lowercase().contains("makanan")) {
                                    listProduct += it[i]
                                }
                            }
                        }

                    }
                    adapter.setDataBuyerProduct(listProduct)
                    rv_product_home_progress_bar.isInvisible = true
                    adapter.notifyDataSetChanged()
                }
            }
        }

        home_telusuri_kategori_hobbi_button.setOnClickListener {
            home_telusuri_kategori_lainnya_button.isSelected = false
            home_telusuri_kategori_elektronik_button.isSelected = false
            home_telusuri_kategori_semua_button.isSelected = false
            home_telusuri_kategori_hobbi_button.isSelected = true
            home_telusuri_kategori_makanan_button.isSelected = false

            viewModelBuyerProduct.buyerProduct.observe(viewLifecycleOwner) {
                val listProduct: MutableList<GetBuyerProductResponseItem> = mutableListOf()
                if (it.isNotEmpty()) {
                    for (i in it.indices) {
                        if (it[i].Categories!!.isNotEmpty()) {
                            for (j in it[i].Categories!!.indices) {
                                val name = it[i].Categories!![j].name
                                if (name.lowercase().contains("hobi")) {
                                    listProduct += it[i]
                                }
                            }
                        }

                    }
                    adapter.setDataBuyerProduct(listProduct)
                    rv_product_home_progress_bar.isInvisible = true
                    adapter.notifyDataSetChanged()
                }
            }
        }

        home_telusuri_kategori_lainnya_button.setOnClickListener {
            home_telusuri_kategori_lainnya_button.isSelected = true
            home_telusuri_kategori_elektronik_button.isSelected = false
            home_telusuri_kategori_semua_button.isSelected = false
            home_telusuri_kategori_hobbi_button.isSelected = false
            home_telusuri_kategori_makanan_button.isSelected = false

            viewModelBuyerProduct.buyerProduct.observe(viewLifecycleOwner) {
                val listProduct: MutableList<GetBuyerProductResponseItem> = mutableListOf()
                if (it.isNotEmpty()) {
                    for (i in it.indices) {
                        if (it[i].Categories!!.isNotEmpty()) {
                            for (j in it[i].Categories!!.indices) {
                                val name = it[i].Categories!![j].name
                                if (!name.lowercase().contains("eletronik") &&
                                    !name.lowercase().contains("kendaraan") &&
                                    !name.lowercase().contains("hobi")
                                ) {
                                    listProduct += it[i]
                                }
                            }
                        }

                    }
                    adapter.setDataBuyerProduct(listProduct)
                    rv_product_home_progress_bar.isInvisible = true
                    adapter.notifyDataSetChanged()
                }
            }
        }

        home_list_search_result.isInvisible = true
        home_search_bar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            val viewModelSearch =
                ViewModelProvider(this@HomeFragment)[BuyerProductViewModel::class.java]

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query!!.isNotEmpty()) {
                    vpHomeImageBanner.isInvisible = true
                    home_telusuri_kategori_section.isInvisible = true
                    rv_product_home_section.isInvisible = true
                    home_list_search_result.isInvisible = false

                    home_search_bar.clearFocus()

                    viewModelSearch.getBuyerProductSearchResult(query)

                    viewModelSearch.searchResult.observe(viewLifecycleOwner) { value ->
                        if (value.isNotEmpty()) {
                            searchProductResultAdapter.setDataBuyerProductSearch(value)
                            searchProductResultAdapter.notifyDataSetChanged()
                            home_search_no_data_animation.isInvisible = true
                        } else {
                            searchProductResultAdapter.clearBuyerProductSearchData()
                            searchProductResultAdapter.notifyDataSetChanged()
                            home_search_no_data_animation.isInvisible = false
                        }
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return if (newText!!.isEmpty()) {
                    vpHomeImageBanner.isInvisible = false
                    home_telusuri_kategori_section.isInvisible = false
                    rv_product_home_section.isInvisible = false
                    home_list_search_result.isInvisible = true
                    home_search_no_data_animation.isInvisible = true
                    true
                } else {
                    false
                }
            }
        })
    }

    private fun banner() {
        val viewModelbanner = ViewModelProvider(this)[BannerViewModel::class.java]
        viewModelbanner.banner.observe(requireActivity()) {
            adapterBanner = BannerAdapter(requireContext(), it)
            vpHomeImageBanner.adapter = adapterBanner
        }
        viewModelbanner.getAllBanner()
    }

}