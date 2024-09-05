package com.example.whatnew

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.whatnew.databinding.ActivityNewsTypeBinding

class NewsTypeActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityNewsTypeBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewsTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // إعداد SharedPreferences
        sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE)

        // تعيين OnClickListener لكل زر
        binding.btnGeneral.setOnClickListener(this)
        binding.btnBusiness.setOnClickListener(this)
        binding.btnEntertainment.setOnClickListener(this)
        binding.btnHealth.setOnClickListener(this)
        binding.btnScience.setOnClickListener(this)
        binding.btnSports.setOnClickListener(this)
        binding.btnTechnology.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val category = when (v?.id) {
            R.id.btnGeneral -> "general"
            R.id.btnBusiness -> "business"
            R.id.btnEntertainment -> "entertainment"
            R.id.btnHealth -> "health"
            R.id.btnScience -> "science"
            R.id.btnSports -> "sports"
            R.id.btnTechnology -> "technology"
            else -> "general"
        }

        saveSelectedCategory(category)

        // الانتقال إلى الصفحة الرئيسية وتحميل الأخبار
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun saveSelectedCategory(category: String) {
        val editor = sharedPreferences.edit()
        editor.putString("selected_category", category)
        editor.apply() // حفظ الفئة في SharedPreferences
    }
}
