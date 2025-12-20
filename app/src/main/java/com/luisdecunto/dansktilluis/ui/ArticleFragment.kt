package com.luisdecunto.dansktilluis.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.luisdecunto.dansktilluis.R
import com.luisdecunto.dansktilluis.databinding.FragmentArticleBinding
import com.luisdecunto.dansktilluis.models.ArticleExercise
import com.luisdecunto.dansktilluis.models.ArticleSubExercise

class ArticleFragment : Fragment() {

    private var _binding: FragmentArticleBinding? = null
    private val binding get() = _binding!!

    private lateinit var exercise: ArticleExercise
    private var onAnswerSubmitted: ((Boolean) -> Unit)? = null
    private val userAnswers = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArticleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup article drawer
        binding.articleTitleTextView.text = exercise.textTitle
        binding.drawerArticleTitleTextView.text = exercise.textTitle
        binding.articleContentTextView.text = exercise.textContent

        // Show pompadour if available
        if (!exercise.textPompadour.isNullOrEmpty()) {
            binding.articlePompadourTextView.text = exercise.textPompadour
            binding.articlePompadourTextView.visibility = android.view.View.VISIBLE
            binding.drawerArticlePompadourTextView.text = exercise.textPompadour
            binding.drawerArticlePompadourTextView.visibility = android.view.View.VISIBLE
        } else {
            binding.articlePompadourTextView.visibility = android.view.View.GONE
            binding.drawerArticlePompadourTextView.visibility = android.view.View.GONE
        }

        // Initialize user answers list
        userAnswers.clear()
        repeat(exercise.subExercises.size) { userAnswers.add("") }

        // Create UI for each sub-exercise
        exercise.subExercises.forEachIndexed { index, subExercise ->
            when (subExercise) {
                is ArticleSubExercise.MultipleChoice -> {
                    createMultipleChoiceQuestion(index, subExercise)
                }
                is ArticleSubExercise.OpenEnded -> {
                    createOpenEndedQuestion(index, subExercise)
                }
            }
        }

        // Setup submit button
        binding.submitButton.setOnClickListener {
            submitAnswers()
        }

        // Allow clicking on hint to open drawer
        binding.swipeHintTextView.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }
    }

    private fun createMultipleChoiceQuestion(index: Int, subExercise: ArticleSubExercise.MultipleChoice) {
        val questionCard = MaterialCardView(requireContext()).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24)
            }
            cardElevation = 4f
            radius = 8f
            setCardBackgroundColor(Color.WHITE)
        }

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        // Question text
        val questionText = TextView(requireContext()).apply {
            text = "${index + 1}. ${subExercise.question}"
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 16)
        }
        container.addView(questionText)

        // Options
        val optionsContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
        }

        subExercise.options.forEachIndexed { optionIndex, option ->
            val optionCard = MaterialCardView(requireContext()).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 12)
                }
                cardElevation = 2f
                radius = 6f
                setCardBackgroundColor(Color.WHITE)
                strokeWidth = 2
                strokeColor = ContextCompat.getColor(requireContext(), R.color.light_gray)
            }

            val optionText = TextView(requireContext()).apply {
                text = option
                textSize = 14f
                setPadding(24, 24, 24, 24)
                setTextColor(Color.BLACK)
            }

            optionCard.addView(optionText)

            optionCard.setOnClickListener {
                // Deselect all options for this question
                for (i in 0 until optionsContainer.childCount) {
                    val card = optionsContainer.getChildAt(i) as MaterialCardView
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

                userAnswers[index] = optionIndex.toString()
            }

            optionsContainer.addView(optionCard)
        }

        container.addView(optionsContainer)
        questionCard.addView(container)
        binding.questionsContainer.addView(questionCard)
    }

    private fun createOpenEndedQuestion(index: Int, subExercise: ArticleSubExercise.OpenEnded) {
        val questionCard = MaterialCardView(requireContext()).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24)
            }
            cardElevation = 4f
            radius = 8f
            setCardBackgroundColor(Color.WHITE)
        }

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        // Question text
        val questionText = TextView(requireContext()).apply {
            text = "${index + 1}. ${subExercise.question}"
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 16)
        }
        container.addView(questionText)

        // Answer input
        val answerInput = EditText(requireContext()).apply {
            hint = "Type your answer..."
            textSize = 14f
            setPadding(16, 16, 16, 16)
            setBackgroundResource(android.R.drawable.edit_text)
        }

        answerInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                userAnswers[index] = s.toString()
            }
        })

        container.addView(answerInput)
        questionCard.addView(container)
        binding.questionsContainer.addView(questionCard)
    }

    private fun submitAnswers() {
        val answerString = userAnswers.joinToString("|")
        val isCorrect = exercise.checkAnswer(answerString)

        showFeedback(isCorrect)
        exercise.isCompleted = isCorrect
        exercise.userAnswer = answerString

        // Disable all interactions
        binding.submitButton.isEnabled = false
        disableAllInputs()

        onAnswerSubmitted?.invoke(isCorrect)
    }

    private fun disableAllInputs() {
        for (i in 0 until binding.questionsContainer.childCount) {
            val questionCard = binding.questionsContainer.getChildAt(i)
            questionCard.isEnabled = false
            disableViewGroup(questionCard as ViewGroup)
        }
    }

    private fun disableViewGroup(viewGroup: ViewGroup) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            child.isEnabled = false
            if (child is ViewGroup) {
                disableViewGroup(child)
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
            exercise: ArticleExercise,
            onAnswerSubmitted: (Boolean) -> Unit
        ): ArticleFragment {
            return ArticleFragment().apply {
                this.exercise = exercise
                this.onAnswerSubmitted = onAnswerSubmitted
            }
        }
    }
}
