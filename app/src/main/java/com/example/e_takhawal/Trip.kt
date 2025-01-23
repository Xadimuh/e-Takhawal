package com.example.e_takhawal


data class Trip(
    val id: String = "",
    val username: String = "", // Nouveau champ pour associer le conducteur

    val origin: String = "",
    val destination: String = "",
    val date: String = "",
    val time: String = "",
    val availableSeats: Int = 0,
    val carInfo: String = "" // Nouveau champ pour les informations sur la voiture
)