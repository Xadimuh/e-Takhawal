package com.example.e_takhawal

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.text.TextUtils
import android.content.Intent
import com.example.e_takhawal.HelperClass
import com.example.e_takhawal.R
import com.example.e_takhawal.SinginActivity

class SignupActivity : AppCompatActivity() {

    private lateinit var signupName: EditText
    private lateinit var signupUsername: EditText
    private lateinit var signupEmail: EditText
    private lateinit var signupPassword: EditText
    private lateinit var signupRole: Spinner
    private lateinit var loginRedirectText: TextView
    private lateinit var signupButton: Button
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singup)

        // Liaison avec les vues XML
        signupName = findViewById(R.id.signup_name)
        signupEmail = findViewById(R.id.signup_email)
        signupUsername = findViewById(R.id.signup_username)
        signupPassword = findViewById(R.id.signup_password)
        signupRole = findViewById(R.id.signup_role)  // Spinner pour les rôles
        loginRedirectText = findViewById(R.id.loginRedirectText)
        signupButton = findViewById(R.id.signup_button)

        // Initialiser le Spinner avec une liste de rôles
        val roles = listOf("only driver", "only passenger", "mainly driver", "mainly passenger")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        signupRole.adapter = adapter

        signupButton.setOnClickListener {
            val name = signupName.text.toString().trim()
            val email = signupEmail.text.toString().trim()
            val username = signupUsername.text.toString().trim()
            val password = signupPassword.text.toString().trim()
            val role = signupRole.selectedItem.toString().trim()  // Obtenir le rôle sélectionné

            // Vérification des champs vides
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(username) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(role)) {
                Toast.makeText(this, "Tous les champs sont obligatoires !", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Vérification de la validité du rôle
            if (!listOf("only driver", "only passenger", "mainly driver", "mainly passenger")
                    .contains(role.lowercase())) {
                Toast.makeText(
                    this,
                    "Rôle invalide ! Utilisez 'only driver', 'only passenger', 'mainly driver' ou 'mainly passenger'.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // Initialisation de Firebase
            database = FirebaseDatabase.getInstance()
            reference = database.getReference("users")

            // Création de l'objet utilisateur
            val helperClass = HelperClass(name, email, username, password, role)

            // Enregistrement dans Firebase
            reference.child(username).setValue(helperClass)
                .addOnSuccessListener {
                    Toast.makeText(this, "Vous vous êtes inscrit avec succès !", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SinginActivity::class.java)
                    startActivity(intent)
                    finish() // Empêcher un retour à l'écran d'inscription
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Échec de l'inscription: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Redirection vers la page de connexion
        loginRedirectText.setOnClickListener {
            val intent = Intent(this, SinginActivity::class.java)
            startActivity(intent)
        }
    }
}
