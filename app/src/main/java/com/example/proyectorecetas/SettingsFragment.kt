package com.example.proyectorecetas

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectorecetas.databinding.FragmentSettingsBinding
import com.example.proyectorecetas.databinding.SettingItemBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        // Lista de ajustes simplificada (sin modos daltónicos)
        val settingsList = listOf(
            SettingItem("Tamaño de texto", "font_size", listOf(
                SettingOption("Pequeño", "small"),
                SettingOption("Mediano", "medium"),
                SettingOption("Grande", "large"),
                SettingOption("Muy grande", "xlarge")
            )),
            SettingItem("Alto contraste", "high_contrast")
        )

        binding.settingsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.settingsRecycler.adapter = SettingsAdapter(settingsList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class SettingsAdapter(private val items: List<SettingItem>) :
        RecyclerView.Adapter<SettingsAdapter.SettingViewHolder>() {

        inner class SettingViewHolder(val itemBinding: SettingItemBinding) :
            RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingViewHolder {
            val binding = SettingItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return SettingViewHolder(binding)
        }

        override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
            val item = items[position]
            holder.itemBinding.settingTitle.text = item.title

            if (item.options.isNotEmpty()) {
                holder.itemBinding.settingValue.visibility = View.VISIBLE
                holder.itemBinding.settingSwitch.visibility = View.GONE

                val currentValue = prefs.getString(item.key, item.options.first().value) ?: ""
                val currentOption = item.options.find { it.value == currentValue }
                holder.itemBinding.settingValue.text = currentOption?.label ?: ""

                holder.itemBinding.root.setOnClickListener {
                    showOptionsDialog(item)
                }
            } else {
                holder.itemBinding.settingValue.visibility = View.GONE
                holder.itemBinding.settingSwitch.visibility = View.VISIBLE

                // Resetear el listener para evitar múltiples llamadas
                holder.itemBinding.settingSwitch.setOnCheckedChangeListener(null)
                holder.itemBinding.settingSwitch.isChecked = prefs.getBoolean(item.key, false)

                holder.itemBinding.settingSwitch.setOnCheckedChangeListener { _, isChecked ->
                    prefs.edit().putBoolean(item.key, isChecked).apply()
                    requireActivity().recreate()
                }
            }
        }

        override fun getItemCount() = items.size

        private fun showOptionsDialog(item: SettingItem) {
            val options = item.options.map { it.label }.toTypedArray()
            var selectedIndex = item.options.indexOfFirst {
                it.value == prefs.getString(item.key, item.options.first().value)
            }

            AlertDialog.Builder(requireContext())
                .setTitle(item.title)
                .setSingleChoiceItems(options, selectedIndex) { _, which ->
                    selectedIndex = which
                }
                .setPositiveButton("Aceptar") { _, _ ->
                    val selectedValue = item.options[selectedIndex].value
                    prefs.edit().putString(item.key, selectedValue).apply()
                    notifyDataSetChanged()
                    requireActivity().recreate()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    data class SettingItem(
        val title: String,
        val key: String,
        val options: List<SettingOption> = emptyList()
    )

    data class SettingOption(val label: String, val value: String)
}