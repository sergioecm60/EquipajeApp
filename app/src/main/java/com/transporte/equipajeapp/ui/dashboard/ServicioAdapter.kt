package com.transporte.equipajeapp.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.transporte.equipajeapp.databinding.ItemServicioBinding
import com.transporte.equipajeapp.domain.model.Servicio

class ServicioAdapter(
    private val onItemClick: (Servicio) -> Unit
) : ListAdapter<Servicio, ServicioAdapter.ServicioViewHolder>(ServicioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioViewHolder {
        val binding = ItemServicioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ServicioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServicioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ServicioViewHolder(
        private val binding: ItemServicioBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(servicio: Servicio) {
            binding.tvOrigenDestino.text = "${servicio.origen} - ${servicio.destino}"
            binding.tvFecha.text = servicio.fecha ?: ""
            binding.tvHora.text = "${servicio.horaSalida} - ${servicio.horaLlegada}"
            binding.tvEmpresa.text = "Empresa: ${servicio.empresa}"
        }
    }

    private class ServicioDiffCallback : DiffUtil.ItemCallback<Servicio>() {
        override fun areItemsTheSame(oldItem: Servicio, newItem: Servicio): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Servicio, newItem: Servicio): Boolean {
            return oldItem == newItem
        }
    }
}
