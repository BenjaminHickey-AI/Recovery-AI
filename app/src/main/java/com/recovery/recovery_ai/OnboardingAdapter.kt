package com.recovery.recovery_ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent

data class OnboardingPage(
    val layoutRes: Int,
    val bgRes: Int
)

class OnboardingAdapter(
    private val pages: List<OnboardingPage>
) : RecyclerView.Adapter<OnboardingAdapter.PageVH>() {

    inner class PageVH(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun getItemViewType(position: Int): Int {
        return pages[position].layoutRes
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(viewType, parent, false)
        return PageVH(view)
    }

    override fun onBindViewHolder(holder: PageVH, position: Int) {
        val page = pages[position]

        holder.itemView.findViewById<ImageView?>(R.id.imgBackground)?.setImageResource(page.bgRes)

        holder.itemView.findViewById<TextView?>(R.id.btnSkip)?.setOnClickListener {
            (holder.itemView.parent as? androidx.viewpager2.widget.ViewPager2)?.currentItem = pages.size - 1
        }

        holder.itemView.findViewById<Button?>(R.id.btnCreateAccount)?.setOnClickListener {
            val ctx = holder.itemView.context
            val intent = Intent(ctx, CreateAccountActivity::class.java)
            ctx.startActivity(intent)

        }

        holder.itemView.findViewById<Button?>(R.id.btnSignIn)?.setOnClickListener {
            // TODO later
        }
    }

    override fun getItemCount(): Int = pages.size
}