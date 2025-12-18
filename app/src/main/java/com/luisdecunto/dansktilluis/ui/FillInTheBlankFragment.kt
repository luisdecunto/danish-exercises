package com.luisdecunto.dansktilluis.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.luisdecunto.dansktilluis.R
import com.luisdecunto.dansktilluis.databinding.FragmentFillInBlankBinding
import com.luisdecunto.dansktilluis.models.FillInTheBlankExercise

class FillInTheBlankFragment : Fragment() {

    private var _binding: FragmentFillInBlankBinding? = null
    private val binding get() = _binding!!

    private lateinit var exercise: FillInTheBlankExercise
    private var onAnswerSubmitted: ((Boolean) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFillInBlankBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.questionTextView.text = exercise.question

        // Show hint if available
        exercise.hint?.let {
            binding.hintTextView.visibility = View.VISIBLE
            binding.hintTextView.text = "Hint: $it"
        }

        // Handle Enter key press
        binding.answerEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_GO ||
                (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                submitAnswer()
                true
            } else {
                false
            }
        }

        binding.submitButton.setOnClickListener {
            submitAnswer()
        }
    }

    private fun submitAnswer() {
        val userAnswer = binding.answerEditText.text.toString()
        if (userAnswer.isNotBlank()) {
            val isCorrect = exercise.checkAnswer(userAnswer)

            showFeedback(isCorrect)
            exercise.isCompleted = isCorrect
            exercise.userAnswer = userAnswer

            // Disable interaction after submission
            binding.answerEditText.isEnabled = false
            binding.submitButton.isEnabled = false

            onAnswerSubmitted?.invoke(isCorrect)
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
            exercise: FillInTheBlankExercise,
            onAnswerSubmitted: (Boolean) -> Unit
        ): FillInTheBlankFragment {
            return FillInTheBlankFragment().apply {
                this.exercise = exercise
                this.onAnswerSubmitted = onAnswerSubmitted
            }
        }
    }
}
