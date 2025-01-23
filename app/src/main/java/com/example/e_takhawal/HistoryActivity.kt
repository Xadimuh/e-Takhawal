package com.example.e_takhawal

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class HistoryActivity : AppCompatActivity()  {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tripsList: MutableList<Trip>
    private lateinit var tripsAdapter: TripsAdapter
    private lateinit var database: DatabaseReference
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Initialiser la RecyclerView
        recyclerView = findViewById(R.id.recycler_view_history)
        recyclerView.layoutManager = LinearLayoutManager(this)

        tripsList = mutableListOf()
        val userRole = "driver"
        // Passer la fonction onTripAction correctement
        tripsAdapter = TripsAdapter(tripsList, { trip, action -> onTripAction(trip, action) }, userRole)
        recyclerView.adapter = tripsAdapter

        database = FirebaseDatabase.getInstance().reference

        // Récupérer le username du conducteur
        username = intent.getStringExtra("username") ?: ""

        if (username.isNotEmpty()) {
            loadHistoryTrips(username)
        } else {
            Toast.makeText(this, "Nom d'utilisateur non trouvé", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadHistoryTrips(username: String) {
        // Charger les trajets de l'utilisateur depuis Firebase
        database.child("trips")
            .orderByChild("username")
            .equalTo(username)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    tripsList.clear() // Réinitialiser la liste
                    for (dataSnapshot in snapshot.children) {
                        val trip = dataSnapshot.getValue(Trip::class.java)
                        if (trip != null) {
                            tripsList.add(trip) // Ajouter le trajet à la liste
                        }
                    }
                    tripsAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@HistoryActivity, "Erreur lors du chargement des trajets", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Fonction appelée lors du clic sur un trajet
    private fun onTripAction(trip: Trip, action: String) {
        when (action) {
            "view" -> {
                Toast.makeText(this, "Vous avez cliqué sur ${trip.origin} - ${trip.destination}", Toast.LENGTH_SHORT).show()
            }
            // Vous pouvez ajouter d'autres actions ici
        }
    }
}
