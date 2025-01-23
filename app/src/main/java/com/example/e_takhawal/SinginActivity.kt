package com.example.e_takhawal

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class SinginActivity : AppCompatActivity() {

    private lateinit var loginUsername: EditText
    private lateinit var loginPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var signupRedirectText: TextView
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singin)

        // Liaison avec les vues XML
        loginUsername = findViewById(R.id.login_username)
        loginPassword = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.login_button)
        signupRedirectText = findViewById(R.id.signupRedirectText)

        // Configuration du clic sur le bouton de connexion
        loginButton.setOnClickListener {
            val username = loginUsername.text.toString().trim()
            val password = loginPassword.text.toString().trim()

            // Validation des champs
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Connexion avec Firebase
            databaseReference = FirebaseDatabase.getInstance().getReference("users")
            databaseReference.child(username).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Récupérer l'utilisateur depuis la base de données
                        val dbPassword = snapshot.child("password").value.toString()

                        if (dbPassword == password) {
                            // Connexion réussie
                            val role = snapshot.child("role").value.toString()

                            // Enregistrer l'utilisateur dans SharedPreferences
                            val sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("current_user", username)  // Stocker le nom d'utilisateur
                            editor.apply()

                            // Redirection vers l'écran en fonction du rôle
                            when (role) {
                                "only driver", "mainly driver" -> {
                                    val intent = Intent(this@SinginActivity, DriverActivity::class.java)
                                    intent.putExtra("username", username)
                                    startActivity(intent)
                                }
                                "only passenger", "mainly passenger" -> {
                                    val intent = Intent(this@SinginActivity, PassengerActivity::class.java)
                                    intent.putExtra("username", username)
                                    startActivity(intent)
                                }
                                else -> {
                                    Toast.makeText(this@SinginActivity, "Invalid role: $role", Toast.LENGTH_SHORT).show()
                                }
                            }
                            finish() // Terminer cette activité
                        } else {
                            // Mot de passe incorrect
                            Toast.makeText(this@SinginActivity, "Incorrect password", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // L'utilisateur n'existe pas
                        Toast.makeText(this@SinginActivity, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SinginActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Redirection vers la page d'inscription
        signupRedirectText.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}
