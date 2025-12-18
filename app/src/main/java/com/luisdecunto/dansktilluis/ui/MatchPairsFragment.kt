package com.luisdecunto.dansktilluis.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.luisdecunto.dansktilluis.R
import com.luisdecunto.dansktilluis.databinding.FragmentMatchPairsBinding
import com.luisdecunto.dansktilluis.models.MatchPairsExercise

class MatchPairsFragment : Fragment() {

    private var _binding: FragmentMatchPairsBinding? = null
    private val binding get() = _binding!!

    private lateinit var exercise: MatchPairsExercise
    private var onAnswerSubmitted: ((Boolean) -> Unit)? = null

    private var selectedLeftIndex: Int? = null
    private var selectedRightIndex: Int? = null
    private val userPairs = mutableMapOf<Int, Int>()

    private val leftButtons = mutableListOf<Button>()
    private val rightButtons = mutableListOf<Button>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatchPairsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.questionTextView.text = exercise.question

        // Create buttons for left column
        exercise.leftItems.forEachIndexed { index, item ->
            val button = createPairButton(item, index, isLeft = true)
            leftButtons.add(button)
            binding.leftColumn.addView(button)
        }

        // Create buttons for right column
        exercise.rightItems.forEachIndexed { index, item ->
            val button = createPairButton(item, index, isLeft = false)
            rightButtons.add(button)
            binding.rightColumn.addView(button)
        }

        binding.resetButton.setOnClickListener {
            resetSelections()
        }

        binding.submitButton.setOnClickListener {
            if (userPairs.size == exercise.leftItems.size) {
                val answerString = userPairs.entries.joinToString(",") { "{${it.key}:${it.value}}" }
                val isCorrect = exercise.checkAnswer(answerString)

                showFeedback(isCorrect)
                exercise.isCompleted = isCorrect
                exercise.userAnswer = answerString

                // Disable interaction after submission
                disableAllButtons()
                binding.submitButton.isEnabled = false
                binding.resetButton.isEnabled = false

                onAnswerSubmitted?.invoke(isCorrect)
            }
        }
    }

    private fun createPairButton(text: String, index: Int, isLeft: Boolean): Button {
        return Button(requireContext()).apply {
            this.text = text
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }

            setOnClickListener {
                if (isLeft) {
                    handleLeftButtonClick(index)
                } else {
                    handleRightButtonClick(index)
                }
            }
        }
    }

    private fun handleLeftButtonClick(index: Int) {
        if (userPairs.containsKey(index)) return // Already paired

        selectedLeftIndex = index
        updateButtonStates()

        if (selectedRightIndex != null) {
            createPair(selectedLeftIndex!!, selectedRightIndex!!)
        } else {
            binding.instructionTextView.text = getString(R.string.select_second_item)
        }
    }

    private fun handleRightButtonClick(index: Int) {
        if (userPairs.containsValue(index)) return // Already paired

        selectedRightIndex = index
        updateButtonStates()

        if (selectedLeftIndex != null) {
            createPair(selectedLeftIndex!!, selectedRightIndex!!)
        } else {
            binding.instructionTextView.text = getString(R.string.select_first_item)
        }
    }

    private fun createPair(leftIndex: Int, rightIndex: Int) {
        userPairs[leftIndex] = rightIndex
        selectedLeftIndex = null
        selectedRightIndex = null
        binding.instructionTextView.text = getString(R.string.select_first_item)
        updateButtonStates()
    }

    private fun updateButtonStates() {
        leftButtons.forEachIndexed { index, button ->
            when {
                userPairs.containsKey(index) -> {
                    button.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.correct_green)
                    )
                }
                selectedLeftIndex == index -> {
                    button.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.selected_blue)
                    )
                }
                else -> {
                    button.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.light_gray)
                    )
                }
            }
        }

        rightButtons.forEachIndexed { index, button ->
            when {
                userPairs.containsValue(index) -> {
                    button.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.correct_green)
                    )
                }
                selectedRightIndex == index -> {
                    button.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.selected_blue)
                    )
                }
                else -> {
                    button.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.light_gray)
                    )
                }
            }
        }
    }

    private fun resetSelections() {
        userPairs.clear()
        selectedLeftIndex = null
        selectedRightIndex = null
        binding.instructionTextView.text = getString(R.string.select_first_item)
        updateButtonStates()
    }

    private fun disableAllButtons() {
        leftButtons.forEach { it.isEnabled = false }
        rightButtons.forEach { it.isEnabled = false }
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
            exercise: MatchPairsExercise,
            onAnswerSubmitted: (Boolean) -> Unit
        ): MatchPairsFragment {
            return MatchPairsFragment().apply {
                this.exercise = exercise
                this.onAnswerSubmitted = onAnswerSubmitted
            }
        }
    }
}
