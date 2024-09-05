package com.example.whatnew

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.example.whatnew.databinding.ActivitySettingsBinding
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // إعداد ViewBinding
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the toolbar as the action bar
        setSupportActionBar(binding.toolbar)  // Set the Toolbar as the ActionBar

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE)

        // استرجاع البلد المحفوظ مسبقًا
        val selectedCountry = sharedPreferences.getString("selected_country", "")

        // تحديث حالة RadioButtons بناءً على البلد المحفوظ
        when (selectedCountry) {
            "us" -> binding.radioUs.isChecked = true
            "gb" -> binding.radioGb.isChecked = true
            "ca" -> binding.radioCa.isChecked = true
            else -> binding.radioAll.isChecked = true // الخيار الافتراضي
        }

        // حفظ البلد عند النقر على زر الحفظ
        binding.saveButton.setOnClickListener {
            val chosenCountry = when {
                binding.radioUs.isChecked -> "us"
                binding.radioGb.isChecked -> "gb"
                binding.radioCa.isChecked -> "ca"
                else -> "" // إذا كان الخيار "الكل"
            }
            saveSelectedCountry(chosenCountry)

            // الانتقال إلى صفحة الأخبار وإعادة تحميل الأخبار
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish() // اغلق صفحة الإعدادات
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
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveSelectedCountry(country: String) {
        val editor = sharedPreferences.edit()
        editor.putString("selected_country", country)
        editor.apply() // حفظ البلد في SharedPreferences
    }
}