package com.example.e_takhawal

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import java.util.*

class AddTripActivity : BaseDriverActivity(), OnMapReadyCallback {

    private lateinit var database: DatabaseReference
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var tripsRecyclerView: RecyclerView
    private lateinit var tripsAdapter: TripsAdapter
    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_trip)

        // Initialiser Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Initialiser MapView
        mapView = findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Initialiser Google Places API
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyC50upEZRV9fEHOaEQvxiKycsg8xJms6rw")
        }
        placesClient = Places.createClient(this)

        // Liaison des vues XML
        // Liaison des vues XML
        val sourceInput: AutoCompleteTextView = findViewById(R.id.input_source)
        val destinationInput: AutoCompleteTextView = findViewById(R.id.input_destination)

        val dateInput: EditText = findViewById(R.id.input_date)
        val timeInput: EditText = findViewById(R.id.input_time)
        val availableSeatsInput: EditText = findViewById(R.id.input_available_seats)
        val carInfoInput: EditText = findViewById(R.id.input_car_info)
        val addTripButton: Button = findViewById(R.id.add_trip_button)
        val reverseButton: ImageButton = findViewById(R.id.reverse_button)

        // RecyclerView
        tripsRecyclerView = findViewById(R.id.trips_recycler_view)
        tripsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Récupérer le username passé via l'Intent
        val username = intent.getStringExtra("username") ?: ""

        // Vérification si le username existe
        if (username.isEmpty()) {
            Toast.makeText(this, "Erreur: vous devez être connecté pour ajouter un trajet", Toast.LENGTH_SHORT).show()
            return
        }

        // Afficher les trajets existants
        loadTrips(username)

        // DatePicker pour le champ Date
        dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val formattedDate = "$dayOfMonth/${month + 1}/$year"
                    dateInput.setText(formattedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // TimePicker pour le champ Heure
        timeInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePickerDialog = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                    timeInput.setText(formattedTime)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePickerDialog.show()
        }

        // Action pour le bouton "Ajouter un trajet"
        addTripButton.setOnClickListener {
            val source = sourceInput.text.toString().trim()
            val destination = destinationInput.text.toString().trim()
            val date = dateInput.text.toString().trim()
            val time = timeInput.text.toString().trim()
            val availableSeatsString = availableSeatsInput.text.toString().trim()
            val carInfo = carInfoInput.text.toString().trim()

            // Vérification des champs vides
            if (TextUtils.isEmpty(source) || TextUtils.isEmpty(destination) || TextUtils.isEmpty(date) ||
                TextUtils.isEmpty(time) || TextUtils.isEmpty(availableSeatsString) || TextUtils.isEmpty(carInfo)) {
                Toast.makeText(this, "Tous les champs doivent être remplis!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Vérification si la valeur des places disponibles est un nombre
            val availableSeats: Int
            try {
                availableSeats = availableSeatsString.toInt()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Le nombre de places disponibles doit être un entier valide!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ajouter le trajet à la base de données
            val tripId = database.push().key ?: ""
            val trip = Trip(
                id = tripId,
                username = username,
                origin = source,
                destination = destination,
                date = date,
                time = time,
                availableSeats = availableSeats,
                carInfo = carInfo
            )

            database.child("trips").child(tripId).setValue(trip)
                .addOnSuccessListener {
                    Toast.makeText(this@AddTripActivity, "Trajet ajouté avec succès!", Toast.LENGTH_SHORT).show()
                    loadTrips(username) // Recharger les trajets après ajout
                }
                .addOnFailureListener {
                    Toast.makeText(this@AddTripActivity, "Erreur lors de l'ajout du trajet", Toast.LENGTH_SHORT).show()
                }
        }

        // Action pour inverser Source et Destination
        reverseButton.setOnClickListener {
            val temp = sourceInput.text.toString()
            sourceInput.setText(destinationInput.text.toString())
            destinationInput.setText(temp)
        }

        // Gérer les suggestions pour les champs Source et Destination
        setupAutocomplete(sourceInput)
        setupAutocomplete(destinationInput)
    }

    // Fonction pour gérer l'autocomplétion des champs source et destination
    private fun setupAutocomplete(input: AutoCompleteTextView) {
        input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    fetchPlaceSuggestions(query, input)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun fetchPlaceSuggestions(query: String, input: AutoCompleteTextView) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val predictions = response.autocompletePredictions
                val suggestions = predictions.map { it.getPrimaryText(null).toString() }

                if (suggestions.isNotEmpty()) {
                    val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, suggestions)
                    input.setAdapter(adapter)
                    input.showDropDown()  // Afficher les suggestions
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erreur lors de la récupération des suggestions", Toast.LENGTH_SHORT).show()
            }
    }


    // Fonction pour charger les trajets existants de la base de données
    private fun loadTrips(username: String) {
        database.child("trips").orderByChild("username").equalTo(username).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val trips = mutableListOf<Trip>()
                for (data in snapshot.children) {
                    val trip = data.getValue(Trip::class.java)
                    trip?.let { trips.add(it) }
                }

                // Affichage des trajets dans le RecyclerView
                tripsAdapter = TripsAdapter(trips, ::onTripAction, "driver")
                tripsRecyclerView.adapter = tripsAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AddTripActivity, "Erreur lors de la récupération des trajets: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Fonction pour gérer les actions sur un trajet
    private fun onTripAction(trip: Trip, action: String) {
        when (action) {
            "reserve" -> {
                // Ajouter la logique pour réserver un trajet
                Toast.makeText(this, "Réserver le trajet: ${trip.origin} -> ${trip.destination}", Toast.LENGTH_SHORT).show()
            }
            "edit" -> {
                // Ajouter la logique pour éditer un trajet
                Toast.makeText(this, "Modifier le trajet: ${trip.origin} -> ${trip.destination}", Toast.LENGTH_SHORT).show()
            }
            "delete" -> {
                // Ajouter la logique pour supprimer un trajet
                database.child("trips").child(trip.id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Trajet supprimé avec succès!", Toast.LENGTH_SHORT).show()
                        loadTrips(intent.getStringExtra("username") ?: "")
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Erreur lors de la suppression du trajet", Toast.LENGTH_SHORT).show()
                    }
            }
            "view_requests" -> {
                // Ajouter la logique pour voir les demandes de réservation
                Toast.makeText(this, "Voir les demandes pour: ${trip.origin} -> ${trip.destination}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        // Coordonnées pour Dakar
        val dakar = LatLng(14.6928, -17.4467) // Latitude et Longitude de Dakar

        // Configurer la caméra pour centrer sur Dakar avec un zoom adapté
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dakar, 12f))

        // Exemple d'ajout d'un marqueur pour Dakar
        googleMap.addMarker(
            MarkerOptions()
                .position(dakar)
                .title("Dakar, Sénégal")
        )
    }

    // Gérer les méthodes du cycle de vie de la carte
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
