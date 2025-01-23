package com.example.e_takhawal

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

open class BasePassengerActivity : AppCompatActivity() {

    protected lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        username = intent.getStringExtra("username") ?: "Passager"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu_passenger, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_view_trips -> {
                // Action pour voir les trajets disponibles
                val intent = Intent(this, PassengerActivity::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
                true
            }
            R.id.nav_my_reservations -> {
                // Action pour "Mes réservations"
                val intent = Intent(this, ReservationsActivity::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
                true
            }
            R.id.nav_profile -> {
                // Action pour "Mon profil"
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
                true
            }
            R.id.nav_logout -> {
                // Action pour "Déconnexion"
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
