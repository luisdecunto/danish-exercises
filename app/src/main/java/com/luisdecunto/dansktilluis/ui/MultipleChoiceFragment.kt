package com.luisdecunto.dansktilluis.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.luisdecunto.dansktilluis.R
import com.luisdecunto.dansktilluis.databinding.FragmentMultipleChoiceBinding
import com.luisdecunto.dansktilluis.models.MultipleChoiceExercise

class MultipleChoiceFragment : Fragment() {

    private var _binding: FragmentMultipleChoiceBinding? = null
    private val binding get() = _binding!!

    private lateinit var exercise: MultipleChoiceExercise
    private var onAnswerSubmitted: ((Boolean) -> Unit)? = null
    private var selectedOptionIndex: Int? = null
    private val shuffledIndices = mutableListOf<Int>()

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

        // Setup collapsible text if available
        setupCollapsibleText()

        binding.questionTextView.text = exercise.question

        // Randomize options
        shuffledIndices.clear()
        shuffledIndices.addAll(exercise.options.indices.shuffled())

        // Create card buttons for each option
        shuffledIndices.forEachIndexed { displayIndex, originalIndex ->
            val option = exercise.options[originalIndex]

            val optionCard = MaterialCardView(requireContext()).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
                cardElevation = 4f
                radius = 8f
                setCardBackgroundColor(Color.WHITE)
                strokeWidth = 2
                strokeColor = ContextCompat.getColor(requireContext(), R.color.light_gray)
            }

            val textView = TextView(requireContext()).apply {
                text = option
                textSize = 16f
                setPadding(32, 32, 32, 32)
                setTextColor(Color.BLACK)
            }

            optionCard.addView(textView)

            optionCard.setOnClickListener {
                // Deselect all options
                for (i in 0 until binding.optionsContainer.childCount) {
                    val card = binding.optionsContainer.getChildAt(i) as MaterialCardView
                    card.strokeColor = ContextCompat.getColor(requireContext(), R.color.light_gray)
                    card.strokeWidth = 2
                    card.setCardBackgroundColor(Color.WHITE)
                }

                // Select this option
                optionCard.strokeColor = ContextCompat.getColor(requireContext(), R.color.selected_blue)
                optionCard.strokeWidth = 4
                optionCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.selected_blue).let {
                    Color.argb(30, Color.red(it), Color.green(it), Color.blue(it))
                })
                selectedOptionIndex = originalIndex
            }

            binding.optionsContainer.addView(optionCard)
        }

        binding.submitButton.setOnClickListener {
            if (selectedOptionIndex != null) {
                val isCorrect = exercise.checkAnswer(selectedOptionIndex.toString())

                showFeedback(isCorrect)
                exercise.isCompleted = isCorrect
                exercise.userAnswer = selectedOptionIndex.toString()

                // Disable interaction after submission
                binding.optionsContainer.children.forEach { it.isEnabled = false }
                binding.submitButton.isEnabled = false

                onAnswerSubmitted?.invoke(isCorrect)
            }
        }
    }

    private fun setupCollapsibleText() {
        if (exercise.textContent != null) {
            binding.collapsibleText.root.visibility = View.VISIBLE
            binding.collapsibleText.textContentTextView.text = exercise.textContent

            var isExpanded = false
            binding.collapsibleText.textHeader.setOnClickListener {
                isExpanded = !isExpanded
                binding.collapsibleText.textContentTextView.visibility =
                    if (isExpanded) View.VISIBLE else View.GONE
                binding.collapsibleText.expandIcon.setImageResource(
                    if (isExpanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more
                )
            }
        } else {
            binding.collapsibleText.root.visibility = View.GONE
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
