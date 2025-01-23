package com.example.e_takhawal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ReservationsAdapter(private val reservationsList: List<Reservation>) :
    RecyclerView.Adapter<ReservationsAdapter.ReservationsViewHolder>() {

    class ReservationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tripDetails: TextView = itemView.findViewById(R.id.text_trip_details)
        val status: TextView = itemView.findViewById(R.id.text_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reservation, parent, false)
        return ReservationsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReservationsViewHolder, position: Int) {
        val reservation = reservationsList[position]
        holder.tripDetails.text =
            "From: ${reservation.origin}, To: ${reservation.destination}, Date: ${reservation.date} ${reservation.time}"
        holder.status.text = "Status: ${reservation.status}"
    }

    override fun getItemCount(): Int = reservationsList.size
}
