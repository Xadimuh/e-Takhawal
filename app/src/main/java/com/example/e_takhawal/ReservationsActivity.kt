package com.example.e_takhawal

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class ReservationsActivity : AppCompatActivity() {

    private lateinit var reservationsRecyclerView: RecyclerView
    private lateinit var reservationsList: MutableList<Reservation>
    private lateinit var reservationsAdapter: ReservationsAdapter
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservations)

        // Initialisation des vues
        reservationsRecyclerView = findViewById(R.id.recycler_view)
        reservationsList = mutableListOf()

        reservationsAdapter = ReservationsAdapter(reservationsList)
        reservationsRecyclerView.layoutManager = LinearLayoutManager(this)
        reservationsRecyclerView.adapter = reservationsAdapter

        // Référence à la base de données
        database = FirebaseDatabase.getInstance().reference

        // Charger les réservations de l'utilisateur
        loadReservations()
    }

    private fun loadReservations() {
        val username = intent.getStringExtra("username") ?: "Utilisateur"

        // Récupérer les réservations associées à l'utilisateur
        database.child("reservations")
            .orderByChild("username")
            .equalTo(username)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    reservationsList.clear()
                    if (snapshot.exists()) {
                        for (dataSnapshot in snapshot.children) {
                            val reservation = dataSnapshot.getValue(Reservation::class.java)
                            if (reservation != null) {
                                reservationsList.add(reservation)
                            }
                        }
                        reservationsAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@ReservationsActivity, "Aucune réservation trouvée", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ReservationsActivity, "Erreur : ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

}
