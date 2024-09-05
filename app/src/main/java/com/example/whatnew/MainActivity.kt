package com.example.whatnew

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.whatnew.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setSupportActionBar(binding.toolbar)

        if (auth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        loadNews()

        binding.swipeRefresh.setOnRefreshListener {
            loadNews()
        }
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
                val intent = Intent(this, NewsTypeActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadNews() {
        val sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE)
        val selectedCountry = sharedPreferences.getString("selected_country", "all") ?: "all" // استخدم القيمة الافتراضية "all" إذا كانت `null`
        val selectedCategory = sharedPreferences.getString("selected_category", "general") ?: "general" // استخدم القيمة الافتراضية "general" إذا كانت `null`

        val retrofit = Retrofit.Builder()
            .baseUrl("https://newsapi.org")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val newsService = retrofit.create(NewsCallable::class.java)
        val apiKey = "2fc4998785dd4cdf9f0298c3c617c4ae" // استخدم مفتاح الـAPI الصحيح

        // استدعاء موحد للـAPI مع الدولة والفئة المحددة
        newsService.getNewsByCountryAndCategory(selectedCountry, selectedCategory, apiKey).enqueue(object : Callback<News> {
            override fun onResponse(call: Call<News>, response: Response<News>) {
                handleNewsResponse(response)
            }

            override fun onFailure(call: Call<News>, t: Throwable) {
                handleNewsFailure(t)
            }
        })
    }

    private fun handleNewsResponse(response: Response<News>) {
        if (response.isSuccessful && response.body() != null) {
            val news = response.body()
            val articles = news?.articles ?: ArrayList()
            showNews(articles)
        } else {
            Log.e("MainActivity", "Failed to load news: ${response.errorBody()?.string()}")
        }
        binding.progressBar.isVisible = false
        binding.swipeRefresh.isRefreshing = false
    }

    private fun handleNewsFailure(t: Throwable) {
        Log.e("MainActivity", "Error fetching news", t)
        binding.progressBar.isVisible = false
        binding.swipeRefresh.isRefreshing = false
    }

    private fun showNews(articles: ArrayList<Article>) {
        val adapter = NewsAdapter(this, articles) { position, isFavorite ->
            val article = articles[position]
            if (isFavorite) {
                addToFavorites(article)
            } else {
                removeFromFavorites(article)
            }
        }
        binding.newsRv.adapter = adapter
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
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding article", e)
            }
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
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error removing article", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error getting documents: ", e)
            }
    }
}
