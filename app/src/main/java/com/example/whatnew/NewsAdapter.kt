package com.example.whatnew

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.whatnew.databinding.ArticleListItemBinding
import com.google.firebase.firestore.FirebaseFirestore

class NewsAdapter(
    private val activity: Activity,
    private val articles: ArrayList<Article>,
    private val onFavoriteClick: (Int, Boolean) -> Unit // Lambda for favorite icon click
) : RecyclerView.Adapter<NewsAdapter.NewsVH>() {

    private val firestore = FirebaseFirestore.getInstance() // Initialize Firestore
    private val favoritesMap = mutableMapOf<String, Boolean>() // To store favorite state

    init {
        checkFavorites() // Check favorites when adapter is initialized
    }

    class NewsVH(val binding: ArticleListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsVH {
        val b = ArticleListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsVH(b)
    }

    override fun getItemCount() = articles.size

    override fun onBindViewHolder(holder: NewsVH, position: Int) {
        val article = articles[position]
        val url = article.url
        holder.binding.articleTv.text = article.title

        // Load article image using Glide
        Glide
            .with(holder.binding.articleImage.context)
            .load(article.urlToImage)
            .error(R.drawable.broken_image)
            .transition(DrawableTransitionOptions.withCrossFade(600))
            .into(holder.binding.articleImage)

        // Check if article is already a favorite
        val isFavorite = favoritesMap[article.url] == true
        holder.binding.favoriteIcon.setImageResource(
            if (isFavorite) R.drawable.favorite else R.drawable.ic_favorite_outline
        )
        holder.binding.favoriteIcon.tag = isFavorite

        // Set click listener for article container
        holder.binding.articleContainer.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, url.toUri())
            activity.startActivity(i)
        }

        // Set click listener for share button
        holder.binding.shareFab.setOnClickListener {
            ShareCompat
                .IntentBuilder(activity)
                .setType("text/plain")
                .setChooserTitle("Share article with")
                .setText(url)
                .startChooser()
        }

        // Handle favorite icon click
        holder.binding.favoriteIcon.setOnClickListener {
            val isFavoriteNow = holder.binding.favoriteIcon.tag as? Boolean ?: false
            if (isFavoriteNow) {
                holder.binding.favoriteIcon.setImageResource(R.drawable.ic_favorite_outline) // Set outline heart icon
                holder.binding.favoriteIcon.tag = false
                removeFromFavorites(article) // Remove from Firestore
            } else {
                holder.binding.favoriteIcon.setImageResource(R.drawable.favorite) // Set filled heart icon
                holder.binding.favoriteIcon.tag = true
                addToFavorites(article) // Add to Firestore
            }
        }
    }

    private fun addToFavorites(article: Article) {
        val favoriteArticle = hashMapOf(
            "title" to article.title,
            "url" to article.url,
            "urlToImage" to article.urlToImage
        )

        firestore.collection("favorites")
            .add(favoriteArticle)
            .addOnSuccessListener {
                Log.d("Firestore", "Article added to favorites")
                favoritesMap[article.url] = true
                notifyDataSetChanged() // Refresh the list to update icons
            }
            .addOnFailureListener { e -> Log.w("Firestore", "Error adding article", e) }
    }

    private fun removeFromFavorites(article: Article) {
        firestore.collection("favorites")
            .whereEqualTo("url", article.url)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    firestore.collection("favorites").document(document.id).delete()
                        .addOnSuccessListener {
                            Log.d("Firestore", "Article removed from favorites")
                            favoritesMap[article.url] = false
                            notifyDataSetChanged() // Refresh the list to update icons
                        }
                        .addOnFailureListener { e -> Log.w("Firestore", "Error removing article", e) }
                }
            }
            .addOnFailureListener { e -> Log.w("Firestore", "Error getting documents: ", e) }
    }

    private fun checkFavorites() {
        firestore.collection("favorites")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val url = document.getString("url") ?: continue
                    favoritesMap[url] = true
                }
                notifyDataSetChanged() // Refresh the list to update icons
            }
            .addOnFailureListener { e -> Log.w("Firestore", "Error checking favorites: ", e) }
    }
}