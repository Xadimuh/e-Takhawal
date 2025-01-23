package com.example.e_takhawal



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReservationRequestsAdapter(
    private val requestsList: List<Reservation>,
    private val onRequestAction: (Reservation, String) -> Unit
) : RecyclerView.Adapter<ReservationRequestsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reservation_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reservation = requestsList[position]
        holder.passengerNameTextView.text = reservation.username
        holder.tripDetailsTextView.text = "Origine: ${reservation.origin}, Destination: ${reservation.destination}, Date: ${reservation.date}, Heure: ${reservation.time}"

        // Actions sur les boutons accepter et refuser
        holder.acceptButton.setOnClickListener {
            onRequestAction(reservation, "accept")
        }
        holder.rejectButton.setOnClickListener {
            onRequestAction(reservation, "reject")
        }
    }

    override fun getItemCount(): Int {
        return requestsList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val passengerNameTextView: TextView = itemView.findViewById(R.id.text_passenger_name)
        val tripDetailsTextView: TextView = itemView.findViewById(R.id.text_trip_details)
        val acceptButton: Button = itemView.findViewById(R.id.button_accept)
        val rejectButton: Button = itemView.findViewById(R.id.button_reject)
    }
}
