package com.example.e_takhawal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TripsAdapter(
    private val trips: List<Trip>,
    private val onTripAction: (Trip, String) -> Unit, // Fonction pour gérer les actions
    private val userRole: String // Rôle de l'utilisateur (driver ou passenger)
) : RecyclerView.Adapter<TripsAdapter.TripViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        // Gonfler le layout pour chaque élément de la liste
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trip, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = trips[position]
        holder.bind(trip)

        // Gérer la visibilité des boutons en fonction du rôle
        if (userRole == "passenger") {
            holder.buttonReserve.visibility = View.VISIBLE
            holder.buttonReserve.setOnClickListener {
                onTripAction(trip, "reserve") // Appeler l'action "reserve" pour le passager
            }

            // Cacher les autres boutons pour les passagers
            holder.buttonEdit.visibility = View.GONE
            holder.buttonDelete.visibility = View.GONE
            holder.buttonViewRequests.visibility = View.GONE
        } else if (userRole == "driver") {
            holder.buttonReserve.visibility = View.GONE // Cacher le bouton "Réserver" pour le conducteur

            // Afficher et gérer les boutons pour le conducteur
            holder.buttonEdit.visibility = View.VISIBLE
            holder.buttonDelete.visibility = View.VISIBLE
            holder.buttonViewRequests.visibility = View.VISIBLE

            // Actions pour le conducteur
            holder.buttonViewRequests.setOnClickListener {
                onTripAction(trip, "view_requests") // Voir les demandes pour le conducteur
            }

            holder.buttonEdit.setOnClickListener {
                onTripAction(trip, "edit") // Modifier le trajet
            }

            holder.buttonDelete.setOnClickListener {
                onTripAction(trip, "delete") // Supprimer le trajet
            }
        }
    }

    override fun getItemCount(): Int {
        return trips.size
    }

    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val buttonReserve: Button = itemView.findViewById(R.id.button_reserve)
        val buttonEdit: Button = itemView.findViewById(R.id.button_edit)
        val buttonDelete: Button = itemView.findViewById(R.id.button_delete)
        val buttonViewRequests: Button = itemView.findViewById(R.id.button_view_requests)

        // Fonction pour lier les données du trajet à l'interface utilisateur
        fun bind(trip: Trip) {
            itemView.findViewById<TextView>(R.id.text_source).text = "Depart: ${trip.origin}"
            itemView.findViewById<TextView>(R.id.text_destination).text = "Arrivee: ${trip.destination}"
            itemView.findViewById<TextView>(R.id.text_date).text = trip.date
            itemView.findViewById<TextView>(R.id.text_time).text = trip.time
            itemView.findViewById<TextView>(R.id.text_available_seats).text = trip.availableSeats.toString() // Convertir en String
            itemView.findViewById<TextView>(R.id.text_car_info).text = "Plaque: ${trip.carInfo}"
        }
    }

}
