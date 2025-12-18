package com.luisdecunto.dansktilluis.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.luisdecunto.dansktilluis.R
import com.luisdecunto.dansktilluis.databinding.FragmentMultipleChoiceBinding
import com.luisdecunto.dansktilluis.models.MultipleChoiceExercise

class MultipleChoiceFragment : Fragment() {

    private var _binding: FragmentMultipleChoiceBinding? = null
    private val binding get() = _binding!!

    private lateinit var exercise: MultipleChoiceExercise
    private var onAnswerSubmitted: ((Boolean) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMultipleChoiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.questionTextView.text = exercise.question

        // Create radio buttons for each option
        exercise.options.forEachIndexed { index, option ->
            val radioButton = RadioButton(requireContext()).apply {
                id = View.generateViewId()
                text = option
                textSize = 16f
                setPadding(16, 16, 16, 16)
            }
            binding.optionsRadioGroup.addView(radioButton)
        }

        binding.submitButton.setOnClickListener {
            val selectedId = binding.optionsRadioGroup.checkedRadioButtonId
            if (selectedId != -1) {
                val selectedIndex = binding.optionsRadioGroup.indexOfChild(
                    binding.optionsRadioGroup.findViewById(selectedId)
                )
                val isCorrect = exercise.checkAnswer(selectedIndex.toString())

                showFeedback(isCorrect)
                exercise.isCompleted = isCorrect
                exercise.userAnswer = selectedIndex.toString()

                // Disable interaction after submission
                for (i in 0 until binding.optionsRadioGroup.childCount) {
                    binding.optionsRadioGroup.getChildAt(i).isEnabled = false
                }
                binding.submitButton.isEnabled = false

                onAnswerSubmitted?.invoke(isCorrect)
            }
        }
    }

    private fun showFeedback(isCorrect: Boolean) {
        binding.feedbackTextView.visibility = View.VISIBLE
        if (isCorrect) {
            binding.feedbackTextView.text = getString(R.string.correct)
            binding.feedbackTextView.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.correct_green)
            )
        } else {
            binding.feedbackTextView.text = getString(R.string.incorrect)
            binding.feedbackTextView.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.incorrect_red)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            exercise: MultipleChoiceExercise,
            onAnswerSubmitted: (Boolean) -> Unit
        ): MultipleChoiceFragment {
            return MultipleChoiceFragment().apply {
                this.exercise = exercise
                this.onAnswerSubmitted = onAnswerSubmitted
            }
        }
    }
}
