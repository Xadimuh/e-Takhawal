package com.example.e_takhawal

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.firebase.database.*
import java.util.*

class PassengerActivity : BasePassengerActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var tripsRecyclerView: RecyclerView
    private lateinit var tripsList: MutableList<Trip>
    private lateinit var tripsAdapter: TripsAdapter
    private lateinit var database: DatabaseReference
    private lateinit var welcomeText: TextView
    private lateinit var viewReservationsButton: Button
    private lateinit var searchButton: Button

    private lateinit var inputSource: EditText
    private lateinit var inputDestination: EditText
    private lateinit var inputDate: EditText
    private lateinit var inputTime: EditText
    private lateinit var reverseButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passager)

        // Initialisation des vues
        mapView = findViewById(R.id.map_view)
        tripsRecyclerView = findViewById(R.id.trips_recycler_view)
        welcomeText = findViewById(R.id.welcome_text_passenger)
        viewReservationsButton = findViewById(R.id.view_reservations_button)
        searchButton = findViewById(R.id.search_button)
        inputSource = findViewById(R.id.input_source)
        inputDestination = findViewById(R.id.input_destination)
        inputDate = findViewById(R.id.input_date)
        inputTime = findViewById(R.id.input_time)
        reverseButton = findViewById(R.id.reverse_button)

        // Configuration de MapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Initialisation de la liste des trajets
        tripsList = mutableListOf()

        // Initialiser l'adaptateur et RecyclerView
        tripsAdapter = TripsAdapter(tripsList, ::onTripAction, "passenger")
        tripsRecyclerView.layoutManager = LinearLayoutManager(this)
        tripsRecyclerView.adapter = tripsAdapter

        // Référence à la base de données Firebase
        database = FirebaseDatabase.getInstance().reference

        // Récupérer le nom d'utilisateur passé par l'activité précédente
        val username = intent.getStringExtra("username") ?: "Passager"
        welcomeText.text = "Bienvenue, $username !"

        // Charger les trajets disponibles
        loadTrips()

        // Rechercher les trajets
        searchButton.setOnClickListener { searchTrips() }

        // Inverser l'origine et la destination
        reverseButton.setOnClickListener {
            val sourceText = inputSource.text.toString()
            val destinationText = inputDestination.text.toString()
            inputSource.setText(destinationText)
            inputDestination.setText(sourceText)
        }

        // Bouton pour voir les réservations
        viewReservationsButton.setOnClickListener {
            val intent = Intent(this, ReservationsActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        // DatePicker pour le champ Date
        inputDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val formattedDate = "$dayOfMonth/${month + 1}/$year"
                    inputDate.setText(formattedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // TimePicker pour le champ Heure
        inputTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePickerDialog = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                    inputTime.setText(formattedTime)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePickerDialog.show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        // Coordonnées pour Dakar
        val dakar = com.google.android.gms.maps.model.LatLng(14.6928, -17.4467) // Latitude et Longitude de Dakar

        // Configurer la caméra pour centrer sur Dakar avec un zoom adapté
        googleMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(dakar, 12f))

        // Exemple d'ajout d'un marqueur pour Dakar
        googleMap.addMarker(
            com.google.android.gms.maps.model.MarkerOptions()
                .position(dakar)
                .title("Dakar, Sénégal")
        )

        // Exemple de tracé d'un itinéraire dans Dakar
        val point1 = com.google.android.gms.maps.model.LatLng(14.7167, -17.4677) // Place de l'Indépendance
        val point2 = com.google.android.gms.maps.model.LatLng(14.6842, -17.4662) // Université Cheikh Anta Diop
        val polylineOptions = com.google.android.gms.maps.model.PolylineOptions()
            .add(point1)
            .add(point2)
            .color(android.graphics.Color.BLUE) // Couleur de la ligne

        googleMap.addPolyline(polylineOptions)
    }

    private fun loadTrips() {
        // Charger tous les trajets disponibles
        database.child("trips")
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
                    Toast.makeText(this@PassengerActivity, "Erreur lors du chargement des trajets", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun searchTrips() {
        val source = inputSource.text.toString()
        val destination = inputDestination.text.toString()
        val date = inputDate.text.toString()
        val time = inputTime.text.toString()

        if (source.isBlank() || destination.isBlank() || date.isBlank() || time.isBlank()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            return
        }

        // Rechercher les trajets correspondant aux critères dans Firebase
        database.child("trips")
            .orderByChild("origin")
            .equalTo(source)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    tripsList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val trip = dataSnapshot.getValue(Trip::class.java)
                        if (trip != null && trip.destination == destination && trip.date == date && trip.time == time) {
                            tripsList.add(trip)
                        }
                    }
                    if (tripsList.isEmpty()) {
                        Toast.makeText(this@PassengerActivity, "Aucun trajet trouvé", Toast.LENGTH_SHORT).show()
                    }
                    tripsAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@PassengerActivity, "Erreur lors de la recherche : ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun onTripAction(trip: Trip, action: String) {
        when (action) {
            "reserve" -> {
                val username = intent.getStringExtra("username") ?: "Utilisateur"
                reserveTrip(trip, username)
            }
        }
    }

    private fun reserveTrip(trip: Trip, username: String) {
        val tripRef = database.child("trips").child(trip.id)

        // Vérifier le nombre de sièges disponibles avant de permettre la réservation
        tripRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val availableSeats = try {
                    snapshot.child("availableSeats").getValue(Int::class.java) ?: 0
                } catch (e: Exception) {
                    snapshot.child("availableSeats").getValue(String::class.java)?.toIntOrNull() ?: 0
                }

                if (availableSeats > 0) {
                    // Si des sièges sont disponibles, procéder à la réservation
                    val reservationId = database.child("reservations").push().key ?: ""
                    val reservation = Reservation(reservationId, trip.id, username, trip.origin, trip.destination, trip.date, trip.time, "Pending")

                    database.child("reservations").child(reservationId).setValue(reservation)
                        .addOnSuccessListener {
                            // Réduire le nombre de sièges disponibles
                            tripRef.child("availableSeats").setValue(availableSeats - 1)

                            Toast.makeText(this@PassengerActivity, "Réservation réussie ! En attente de confirmation.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@PassengerActivity, "Erreur lors de la réservation", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Aucun siège disponible
                    Toast.makeText(this@PassengerActivity, "Il n'y a plus de sièges disponibles pour ce trajet.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PassengerActivity, "Erreur lors de la vérification des sièges disponibles.", Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}
