package com.example.whatnew

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatnew.databinding.ActivityFavoritesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup ViewBinding
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the Toolbar as the ActionBar
        setSupportActionBar(binding.toolbar)

        // Initialize RecyclerView
        binding.favoritesRv.layoutManager = LinearLayoutManager(this)

        loadFavorites()
    }

    private fun loadFavorites() {
        firestore.collection("favorites")
            .get()
            .addOnSuccessListener { documents ->
                val articles = ArrayList<Article>()
                for (document in documents) {
                    val title = document.getString("title") ?: ""
                    val url = document.getString("url") ?: ""
                    val urlToImage = document.getString("urlToImage") ?: ""
                    articles.add(Article(title, url, urlToImage))
                }
                displayFavorites(articles)
            }
            .addOnFailureListener { e -> Log.w("Firestore", "Error getting documents: ", e) }
    }

    private fun displayFavorites(articles: ArrayList<Article>) {
        val adapter = NewsAdapter(this, articles) { position, _ ->
            val article = articles[position]
            val i = Intent(Intent.ACTION_VIEW, article.url.toUri())
            startActivity(i)
        }
        binding.favoritesRv.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            R.id.action_favorite -> {
                val intent = Intent(this, FavoritesActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}