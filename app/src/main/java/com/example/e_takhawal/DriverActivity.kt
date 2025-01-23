package com.example.e_takhawal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DriverActivity : BaseDriverActivity() {

    private lateinit var tripsRecyclerView: RecyclerView
    private lateinit var tripsList: MutableList<Trip>
    private lateinit var tripsAdapter: TripsAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var addTripButton: Button
    private lateinit var welcomeText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver)

        // Initialisation des vues
        tripsRecyclerView = findViewById(R.id.trips_recycler_view)
        addTripButton = findViewById(R.id.add_trip_button)
        welcomeText = findViewById(R.id.welcome_text_driver)

        tripsList = mutableListOf()
        tripsAdapter = TripsAdapter(tripsList, ::onTripAction, "driver")

        tripsRecyclerView.layoutManager = LinearLayoutManager(this)
        tripsRecyclerView.adapter = tripsAdapter

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val username = intent.getStringExtra("username") ?: "Utilisateur"
        welcomeText.text = "Bonjour, $username !"

        loadTrips()

        // Bouton pour ajouter un trajet
        addTripButton.setOnClickListener {
            val intent = Intent(this, AddTripActivity::class.java)
            intent.putExtra("username", username)  // Passer le username via l'Intent
            intent.putExtra("role", "driver")
            startActivity(intent)
        }
    }

    private fun loadTrips() {
        // Utilisation du username comme critère de recherche dans la base de données
        val username = intent.getStringExtra("username") ?: ""
        if (username.isNotEmpty()) {
            // Filtrer les trajets par 'username' dans la base de données
            database.child("trips")
                .orderByChild("username") // Utilisation du champ 'username' pour le filtrage
                .equalTo(username)  // Comparaison avec le 'username' récupéré
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        tripsList.clear()
                        for (dataSnapshot in snapshot.children) {
                            val trip = dataSnapshot.getValue(Trip::class.java)
                            if (trip != null) {
                                tripsList.add(trip)
                            }
                        }
                        tripsAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@DriverActivity, "Erreur lors du chargement des trajets", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun onTripAction(trip: Trip, action: String) {
        when (action) {
            "edit" -> {
                val intent = Intent(this, EditTripActivity::class.java)
                intent.putExtra("tripId", trip.id)
                startActivity(intent)
            }
            "delete" -> {
                database.child("trips").child(trip.id).removeValue().addOnSuccessListener {
                    Toast.makeText(this, "Trajet supprimé", Toast.LENGTH_SHORT).show()
                }
            }
            "view_requests" -> {
                // Redirection vers la page de demandes de réservation
                val intent = Intent(this, ReservationRequestsActivity::class.java)
                intent.putExtra("tripId", trip.id)  // Passer l'ID du trajet
                startActivity(intent)
            }
        }
    }

    // Inflater le menu
    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu_driver, menu)  // Assurez-vous que le nom du fichier menu est correct
        return true
    }

    // Gérer les actions du menu
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        val username = intent.getStringExtra("username") ?: "" // Récupérer le username actuel

        return when (item.itemId) {
            R.id.nav_my_trips -> {
                // Action pour "Mes trajets"
                val intent = Intent(this, HistoryActivity::class.java)
                intent.putExtra("username", username) // Passer le username
                startActivity(intent)
                true
            }
            R.id.nav_my_reservations -> {
                // Action pour "Mes réservations"
                val intent = Intent(this, ReservationsActivity::class.java)
                intent.putExtra("username", username) // Passer le username
                startActivity(intent)
                true
            }
            R.id.nav_profile -> {
                // Action pour "Mon profil"
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("username", username) // Passer le username
                startActivity(intent)
                true
            }
            R.id.nav_trip_history -> {
                // Action pour "Historique des trajets"
                val intent = Intent(this, HistoryActivity::class.java)
                intent.putExtra("username", username) // Passer le username
                startActivity(intent)
                true
            }
            R.id.nav_statistics -> {
                // Action pour "Statistiques"
                val intent = Intent(this, HistoryActivity::class.java)
                intent.putExtra("username", username) // Passer le username
                startActivity(intent)
                true
            }
            R.id.nav_notifications -> {
                // Action pour "Notifications"
                val intent = Intent(this, HistoryActivity::class.java)
                intent.putExtra("username", username) // Passer le username
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
