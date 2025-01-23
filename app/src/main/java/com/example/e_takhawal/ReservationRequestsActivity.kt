package com.example.e_takhawal

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class ReservationRequestsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var requestsList: MutableList<Reservation>
    private lateinit var requestsAdapter: ReservationRequestsAdapter
    private lateinit var database: DatabaseReference
    private lateinit var tripId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation_requests)

        // Initialisation des vues
        recyclerView = findViewById(R.id.recycler_view)
        requestsList = mutableListOf()

        requestsAdapter = ReservationRequestsAdapter(requestsList, ::onRequestAction)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = requestsAdapter

        database = FirebaseDatabase.getInstance().reference

        // Récupérer l'ID du trajet passé par l'activité précédente
        tripId = intent.getStringExtra("tripId") ?: ""

        // Charger les demandes de réservation pour ce trajet
        loadReservationRequests()
    }

    private fun loadReservationRequests() {
        // Charger les réservations pour le trajet spécifique
        database.child("reservations")
            .orderByChild("tripId")
            .equalTo(tripId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    requestsList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val reservation = dataSnapshot.getValue(Reservation::class.java)
                        if (reservation != null) {
                            requestsList.add(reservation)
                        }
                    }
                    requestsAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ReservationRequestsActivity, "Erreur lors du chargement des demandes", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun onRequestAction(reservation: Reservation, action: String) {
        when (action) {
            "accept" -> {
                acceptReservation(reservation.reservationId)
            }
            "reject" -> {
                rejectReservation(reservation.reservationId)
            }
        }
    }

    private fun acceptReservation(reservationId: String) {
        val tripRef = database.child("trips").child(tripId)

        // Vérifier le nombre de sièges disponibles avant de mettre à jour la réservation
        tripRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val availableSeats = try {
                    snapshot.child("availableSeats").getValue(Int::class.java) ?: 0
                } catch (e: Exception) {
                    snapshot.child("availableSeats").getValue(String::class.java)?.toIntOrNull() ?: 0
                }

                if (availableSeats > 0) {
                    // Mettre à jour le statut de la réservation
                    val reservationRef = database.child("reservations").child(reservationId)
                    reservationRef.child("status").setValue("Accepted").addOnSuccessListener {
                        // Réduire le nombre de sièges disponibles
                        tripRef.child("availableSeats").setValue(availableSeats - 1)
                        Toast.makeText(this@ReservationRequestsActivity, "Réservation acceptée", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this@ReservationRequestsActivity, "Erreur lors de l'acceptation de la réservation", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Aucun siège disponible
                    Toast.makeText(this@ReservationRequestsActivity, "Aucun siège disponible", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ReservationRequestsActivity, "Erreur lors de la vérification des sièges disponibles", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun rejectReservation(reservationId: String) {
        val reservationRef = database.child("reservations").child(reservationId)
        reservationRef.child("status").setValue("Rejected").addOnSuccessListener {
            Toast.makeText(this, "Réservation refusée", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Erreur lors du refus de la réservation", Toast.LENGTH_SHORT).show()
        }
    }
}