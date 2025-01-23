package com.example.e_takhawal

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.*

class ProfileActivity : BaseDriverActivity() { // Ou BasePassengerActivity selon les rôles

    private lateinit var database: DatabaseReference
    private lateinit var nameTextView: TextView
    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var roleTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialisation des vues
        nameTextView = findViewById(R.id.profile_name)
        usernameTextView = findViewById(R.id.profile_username)
        emailTextView = findViewById(R.id.profile_email)
        roleTextView = findViewById(R.id.profile_role)

        database = FirebaseDatabase.getInstance().reference

        // Charger les informations de l'utilisateur
        loadUserInfo()
    }

    private fun loadUserInfo() {
        // Récupérer le username transmis
        val currentUsername = username

        // Chercher l'utilisateur dans la base de données
        database.child("users").orderByChild("username").equalTo(currentUsername)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val name = userSnapshot.child("name").getValue(String::class.java)
                            val email = userSnapshot.child("email").getValue(String::class.java)
                            val role = userSnapshot.child("role").getValue(String::class.java)
                            val username = userSnapshot.child("username").getValue(String::class.java)

                            // Afficher les données récupérées
                            nameTextView.text = name ?: "Non défini"
                            emailTextView.text = email ?: "Non défini"
                            roleTextView.text = role ?: "Non défini"
                            usernameTextView.text = username ?: "Non défini"
                        }
                    } else {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Utilisateur non trouvé.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Erreur lors de la récupération des données.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
