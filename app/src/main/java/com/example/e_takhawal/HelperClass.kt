package com.example.e_takhawal



class HelperClass {
    // Getters et Setters
    var name: String? = null
    var email: String? = null
    var username: String? = null
    var password: String? = null
    var role: String? = null // Nouveau champ pour le rôle

    // Constructeur avec tous les paramètres
    constructor(
        name: String?,
        email: String?,
        username: String?,
        password: String?,
        role: String?
    ) {
        this.name = name
        this.email = email
        this.username = username
        this.password = password
        this.role = role
    }

    // Constructeur vide (obligatoire pour Firebase)
    constructor()
}
