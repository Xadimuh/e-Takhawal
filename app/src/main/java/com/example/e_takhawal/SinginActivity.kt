package com.example.e_takhawal


import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class SigninActivity : AppCompatActivity() {

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
                            Toast.makeText(this@SigninActivity, "Welcome, $username! Role: $role", Toast.LENGTH_SHORT).show()

                            // Redirection vers l'écran principal ou un autre écran
                            val intent = Intent(this@SigninActivity, MainActivity::class.java)
                            intent.putExtra("username", username)
                            intent.putExtra("role", role)
                            startActivity(intent)
                            finish()
                        } else {
                            // Mot de passe incorrect
                            Toast.makeText(this@SigninActivity, "Incorrect password", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // L'utilisateur n'existe pas
                        Toast.makeText(this@SigninActivity, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SigninActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
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
