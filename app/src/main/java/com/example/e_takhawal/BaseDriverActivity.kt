package com.example.e_takhawal

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

abstract class BaseDriverActivity : AppCompatActivity() {

    protected lateinit var username: String // Username accessible dans les activités enfant

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        username = intent.getStringExtra("username") ?: ""
    }

    // Ajouter le menu
    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu_driver, menu) // Assurez-vous d'avoir un fichier de menu
        return true
    }

    // Gérer les actions du menu
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_my_trips -> {
                val intent = Intent(this, HistoryActivity::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
                true
            }
            R.id.nav_my_reservations -> {
                val intent = Intent(this, ReservationRequestsActivity::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
                true
            }
            R.id.nav_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
                true
            }
            R.id.nav_trip_history -> {
                val intent = Intent(this, HistoryActivity::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
                true
            }
            R.id.nav_statistics -> {
                val intent = Intent(this, HistoryActivity::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
                true
            }
            R.id.nav_notifications -> {
                val intent = Intent(this, HistoryActivity::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
                true
            }
            R.id.nav_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, SinginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
