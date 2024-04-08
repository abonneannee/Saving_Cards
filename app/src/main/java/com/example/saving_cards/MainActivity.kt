package com.example.saving_cards

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlin.math.max

class MainActivity : AppCompatActivity() {
    var currentCardDisplayedIndex = 0
    lateinit var flashcardDatabase: FlashcardDatabase
    private var allFlashcards = mutableListOf<Flashcard>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        flashcardDatabase = FlashcardDatabase(this)
        flashcardDatabase.initFirstCard()
        allFlashcards = flashcardDatabase.getAllCards().toMutableList()
        val isShowingAnswers = findViewById<ImageView>(R.id.toggle123)
        val flashcard_question = findViewById<TextView>(R.id.flashcard_question)
        val flashcard_reponse = findViewById<TextView>(R.id.flashcard_reponse)
        val flashcard_reponse2 = findViewById<TextView>(R.id.flashcard_reponse2)
        val flashcard_reponse3 = findViewById<TextView>(R.id.flashcard_reponse3)
        val editButton = findViewById<ImageView>(R.id.edit_bouton)
        val NextButton = findViewById<ImageView>(R.id.next_button)
        val delete_bouton = findViewById<ImageView>(R.id.delete_bouton)



        NextButton.setOnClickListener {
            if (allFlashcards.isEmpty()) {
                return@setOnClickListener  // Il n'y a pas de cartes à afficher
            }

            currentCardDisplayedIndex++

            if (currentCardDisplayedIndex >= allFlashcards.size) {
                currentCardDisplayedIndex =
                    0  // Revenir à la première carte si nous avons atteint la fin
            }

            val (question, answer, wrongAnswer1, wrongAnswer2) = allFlashcards[currentCardDisplayedIndex]

            // Mettre à jour les TextViews avec la nouvelle carte
            flashcard_question.text = question
            flashcard_reponse.text = answer
            flashcard_reponse2.text = wrongAnswer1
            flashcard_reponse3.text = wrongAnswer2

        }


        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            val extras = data?.extras

            if (extras != null) { // Check that we have data returned
                val question = extras.getString("question")
                val answer = extras.getString("answer")
                val wrongAnswer1 = extras.getString("wrongAnswer1")
                val wrongAnswer2 = extras.getString("wrongAnswer2")

                // Log the value of the strings for easier debugging
                Log.i("MainActivity", "question: $question")
                Log.i("MainActivity", "answer: $answer")
                Log.i("MainActivity", "wrongAnswer1: $wrongAnswer1")
                Log.i("MainActivity", "wrongAnswer2: $wrongAnswer2")

                // Display newly created flashcard
                flashcard_question.text = question
                flashcard_reponse.text = answer
                flashcard_reponse2.text = wrongAnswer1
                flashcard_reponse3.text = wrongAnswer2

                // Save newly created flashcard to database
                if (question != null && answer != null && wrongAnswer1 != null && wrongAnswer2 != null) {
                    flashcardDatabase.insertCard(
                        Flashcard(
                            question,
                            answer,
                            wrongAnswer1,
                            wrongAnswer2
                        )
                    )
                    // Update set of flashcards to include new card
                    allFlashcards = flashcardDatabase.getAllCards().toMutableList()
                } else {
                    Log.e(
                        "TAG",
                        "Missing question or answer to input into database. Question is $question and answer is $answer and wrongAnswer1 is $wrongAnswer1 and wrongAnswer2 is $wrongAnswer2"
                    )
                }
            } else {
                Log.i("MainActivity", "Returned null data from AddCardActivity")
            }
        }

        delete_bouton.setOnClickListener {
            val currentQuestion = flashcard_question.text.toString()
            flashcardDatabase.deleteCard(currentQuestion)

            // Mettre à jour la liste des flashcards
            allFlashcards = flashcardDatabase.getAllCards().toMutableList()

            // Vérifier s'il reste des cartes
            if (allFlashcards.isNotEmpty()) {
                // Afficher la carte précédente (si disponible)
                currentCardDisplayedIndex = max(0, currentCardDisplayedIndex - 1)
                val (question, answer, wrongAnswer1, wrongAnswer2) = allFlashcards[currentCardDisplayedIndex]
                flashcard_question.text = question
                flashcard_reponse.text = answer
                flashcard_reponse2.text = wrongAnswer1
                flashcard_reponse3.text = wrongAnswer2
            } else {
                // S'il n'y a plus de cartes, afficher un état vide
                flashcard_question.text = ""
                flashcard_reponse.text = ""
                flashcard_reponse2.text = ""
                flashcard_reponse3.text = ""
            }
        }

        if (allFlashcards.size > 0) {
            flashcard_question.text = allFlashcards[0].question
            flashcard_reponse.text = allFlashcards[0].answer
            flashcard_reponse2.text = allFlashcards[0].wrongAnswer1
            flashcard_reponse3.text = allFlashcards[0].wrongAnswer2
        }


        editButton.setOnClickListener {
            val question = findViewById<TextView>(R.id.flashcard_question).text.toString()
            val answer = findViewById<TextView>(R.id.flashcard_reponse).text.toString()
            val wrongAnswer1 = flashcard_reponse2.text.toString()
            val wrongAnswer2 = flashcard_reponse3.text.toString()

            val intent = Intent(this, AddCard::class.java)
            intent.putExtra("question", question)
            intent.putExtra("answer", answer)
            intent.putExtra("wrongAnswer1", wrongAnswer1)
            intent.putExtra("wrongAnswer2", wrongAnswer2)
            resultLauncher.launch(intent)
        }

        // Lancer MainActivity en attente d'un résultat
        isShowingAnswers.setOnClickListener {
            val i = Intent(this, AddCard::class.java)
            resultLauncher.launch(i)

        }


    }
}
