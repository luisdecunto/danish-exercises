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

    private var selectedLeftButton: Button? = null
    private var selectedRightButton: Button? = null
    private val userPairs = mutableMapOf<Button, Button>()

    private val leftButtons = mutableListOf<Button>()
    private val rightButtons = mutableListOf<Button>()
    private val leftIndexMap = mutableMapOf<Button, Int>()
    private val rightIndexMap = mutableMapOf<Button, Int>()

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

        // Setup collapsible text if available
        setupCollapsibleText()

        binding.questionTextView.text = exercise.question

        // Shuffle both columns with indices
        val shuffledLeftWithIndex = exercise.leftItems.mapIndexed { index, item -> index to item }.shuffled()
        val shuffledRightWithIndex = exercise.rightItems.mapIndexed { index, item -> index to item }.shuffled()

        // Create buttons for left column
        shuffledLeftWithIndex.forEach { (originalIndex, item) ->
            val button = createPairButton(item, isLeft = true)
            leftButtons.add(button)
            leftIndexMap[button] = originalIndex
            binding.leftColumn.addView(button)
        }

        // Create buttons for right column
        shuffledRightWithIndex.forEach { (originalIndex, item) ->
            val button = createPairButton(item, isLeft = false)
            rightButtons.add(button)
            rightIndexMap[button] = originalIndex
            binding.rightColumn.addView(button)
        }

        binding.resetButton.setOnClickListener {
            resetSelections()
        }

        binding.submitButton.setOnClickListener {
            if (userPairs.size == leftButtons.size) {
                val answerPairs = mutableMapOf<Int, Int>()
                userPairs.forEach { (leftBtn, rightBtn) ->
                    val leftIdx = leftIndexMap[leftBtn]!!
                    val rightIdx = rightIndexMap[rightBtn]!!
                    answerPairs[leftIdx] = rightIdx
                }

                val answerString = answerPairs.entries.joinToString(",") { "{${it.key}:${it.value}}" }
                val isCorrect = exercise.checkAnswer(answerString)

                showFeedback(isCorrect)
                exercise.isCompleted = isCorrect
                exercise.userAnswer = answerString

                disableAllButtons()
                binding.submitButton.isEnabled = false
                binding.resetButton.isEnabled = false

                onAnswerSubmitted?.invoke(isCorrect)
            }
        }
    }

    private fun createPairButton(text: String, isLeft: Boolean): Button {
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
                    if (userPairs.containsKey(this)) return@setOnClickListener
                    handleLeftButtonClick(this)
                } else {
                    if (userPairs.containsValue(this)) return@setOnClickListener
                    handleRightButtonClick(this)
                }
            }
        }
    }

    private fun handleLeftButtonClick(button: Button) {
        if (userPairs.containsKey(button)) return

        selectedLeftButton = button
        updateButtonStates()

        if (selectedRightButton != null) {
            createPair(selectedLeftButton!!, selectedRightButton!!)
        } else {
            binding.instructionTextView.text = getString(R.string.select_second_item)
        }
    }

    private fun handleRightButtonClick(button: Button) {
        if (userPairs.containsValue(button)) return

        selectedRightButton = button
        updateButtonStates()

        if (selectedLeftButton != null) {
            createPair(selectedLeftButton!!, selectedRightButton!!)
        } else {
            binding.instructionTextView.text = getString(R.string.select_first_item)
        }
    }

    private fun createPair(leftBtn: Button, rightBtn: Button) {
        userPairs[leftBtn] = rightBtn
        selectedLeftButton = null
        selectedRightButton = null
        binding.instructionTextView.text = getString(R.string.select_first_item)
        updateButtonStates()
    }

    private fun updateButtonStates() {
        leftButtons.forEach { button ->
            when {
                userPairs.containsKey(button) -> {
                    button.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.correct_green)
                    )
                }
                selectedLeftButton == button -> {
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

        rightButtons.forEach { button ->
            when {
                userPairs.containsValue(button) -> {
                    button.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.correct_green)
                    )
                }
                selectedRightButton == button -> {
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
        selectedLeftButton = null
        selectedRightButton = null
        binding.instructionTextView.text = getString(R.string.select_first_item)
        updateButtonStates()
    }

    private fun disableAllButtons() {
        leftButtons.forEach { it.isEnabled = false }
        rightButtons.forEach { it.isEnabled = false }
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
