package com.example.e_takhawal

import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class EditTripActivity : AppCompatActivity() {

    private lateinit var tripName: EditText
    private lateinit var tripDestination: EditText
    private lateinit var tripDate: EditText
    private lateinit var tripTime: EditText
    private lateinit var tripSeats: EditText
    private lateinit var saveButton: Button

    private lateinit var tripId: String
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_trip)

        // Initialisation des vues
        tripName = findViewById(R.id.edit_trip_name)
        tripDestination = findViewById(R.id.edit_trip_destination)
        tripDate = findViewById(R.id.edit_trip_date)
        tripTime = findViewById(R.id.edit_trip_time)
        tripSeats = findViewById(R.id.edit_trip_seats)
        saveButton = findViewById(R.id.edit_trip_save_button)

        database = FirebaseDatabase.getInstance()

        // Récupérer les données du trajet à modifier
        tripId = intent.getStringExtra("tripId") ?: ""
        tripName.setText(intent.getStringExtra("tripName"))
        tripDestination.setText(intent.getStringExtra("tripDestination"))
        tripDate.setText(intent.getStringExtra("tripDate"))
        tripTime.setText(intent.getStringExtra("tripTime"))
        tripSeats.setText(intent.getStringExtra("tripSeats"))

        saveButton.setOnClickListener {
            val name = tripName.text.toString().trim()
            val destination = tripDestination.text.toString().trim()
            val date = tripDate.text.toString().trim()
            val time = tripTime.text.toString().trim()
            val seats = tripSeats.text.toString().trim()

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(destination) || TextUtils.isEmpty(date) || TextUtils.isEmpty(time) || TextUtils.isEmpty(seats)) {
                Toast.makeText(this, "Tous les champs sont requis", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tripData = mapOf(
                "name" to name,
                "destination" to destination,
                "date" to date,
                "time" to time,
                "seats" to seats.toInt()
            )

            // Mettre à jour les données dans Firebase
            database.reference.child("trips").child(tripId)
                .updateChildren(tripData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Trajet mis à jour avec succès", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Échec de la mise à jour : ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
