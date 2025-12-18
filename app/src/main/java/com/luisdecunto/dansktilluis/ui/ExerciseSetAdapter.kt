package com.luisdecunto.dansktilluis.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.luisdecunto.dansktilluis.R
import com.luisdecunto.dansktilluis.databinding.ItemExerciseSetBinding
import com.luisdecunto.dansktilluis.models.ExerciseSet

class ExerciseSetAdapter(
    private val exerciseSets: List<ExerciseSet>,
    private val onSetClicked: (ExerciseSet) -> Unit
) : RecyclerView.Adapter<ExerciseSetAdapter.ExerciseSetViewHolder>() {

    inner class ExerciseSetViewHolder(
        private val binding: ItemExerciseSetBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(exerciseSet: ExerciseSet) {
            binding.titleTextView.text = exerciseSet.title
            binding.descriptionTextView.text = exerciseSet.description

            val progress = exerciseSet.getProgress()
            binding.progressBar.progress = progress

            val completed = exerciseSet.getCompletedExercises()
            val total = exerciseSet.getTotalExercises()
            binding.progressTextView.text = "$completed/$total"

            if (completed > 0) {
                binding.startButton.text = itemView.context.getString(R.string.continue_exercise)
            } else {
                binding.startButton.text = itemView.context.getString(R.string.start)
            }

            binding.startButton.setOnClickListener {
                onSetClicked(exerciseSet)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseSetViewHolder {
        val binding = ItemExerciseSetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExerciseSetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseSetViewHolder, position: Int) {
        holder.bind(exerciseSets[position])
    }

    override fun getItemCount(): Int = exerciseSets.size
}
