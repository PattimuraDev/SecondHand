package com.example.secondhand.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.secondhand.R
import com.example.secondhand.model.GetBuyerProductResponseItem
import kotlinx.android.synthetic.main.item_adapter_buyer_product.view.*

class BuyerProductAdapter(private val onClick: (GetBuyerProductResponseItem) -> Unit) :
    RecyclerView.Adapter<BuyerProductAdapter.ViewHolder>() {

    private var listBuyerProduct: List<GetBuyerProductResponseItem>? = null
    fun setDataBuyerProduct(list: List<GetBuyerProductResponseItem>) {
        this.listBuyerProduct = list
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_adapter_buyer_product, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            card_product_nama.text = listBuyerProduct!![position].name
            card_product_harga.text = "Harga: Rp. ${listBuyerProduct!![position].base_price}"
            Glide.with(card_image_produk.context)
                .load(listBuyerProduct!![position].image_url)
                .error(R.drawable.ic_launcher_background)
                .override(100, 100)
                .into(card_image_produk)

            card_product.setOnClickListener {
                onClick(listBuyerProduct!![position])
            }

            card_product_kategori.text = ""
            if (listBuyerProduct!![position].Categories!!.isNotEmpty()) {
                for (i in listBuyerProduct!![position].Categories!!.indices) {
                    if (listBuyerProduct!![position].Categories!!.lastIndex == 0) {
                        card_product_kategori.text =
                            "Kategori: " + listBuyerProduct!![position].Categories!![i].name
                        break
                    }
                    if (i == 0) {
                        card_product_kategori.text =
                            "Kategori: " + listBuyerProduct!![position].Categories!![i].name + ", "
                    } else if (i != listBuyerProduct!![position].Categories!!.lastIndex && i > 0) {
                        card_product_kategori.text =
                            card_product_kategori.text.toString() + listBuyerProduct!![position].Categories!![i].name + ", "
                    } else {
                        card_product_kategori.text =
                            card_product_kategori.text.toString() + listBuyerProduct!![position].Categories!![i].name
                    }
                }
            } else {
                card_product_kategori.text = "Kategori: Belum ada kategori"
            }
        }
    }

    override fun getItemCount(): Int {
        return if (listBuyerProduct.isNullOrEmpty()) {
            0
        } else {
            listBuyerProduct!!.size
        }
    }
}