package com.example.audionoteapp
import NoteRepository
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*
import android.Manifest


class MainActivity : AppCompatActivity() {

    private val RECORD_AUDIO_PERMISSION = 1
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var transcriptionEditText: EditText
    private lateinit var noteRepository: NoteRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startRecordingButton: Button = findViewById(R.id.buttonStartRecording)
        val stopRecordingButton: Button = findViewById(R.id.buttonStopRecording)

        transcriptionEditText = findViewById(R.id.editTextTranscription)
        val saveNoteButton: Button = findViewById(R.id.buttonSaveNote)
        val newNoteButton: Button = findViewById(R.id.buttonNewNote)
        val notesContainer: LinearLayout = findViewById(R.id.notesContainer)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {}
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    val transcription = matches[0]
                    transcriptionEditText.setText(transcription)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        noteRepository = NoteRepository(this)

        startRecordingButton.setOnClickListener {
            if (checkAudioPermission()) {
                startRecordingButton.visibility = View.GONE
                stopRecordingButton.visibility = View.VISIBLE
                startSpeechRecognition()
            }
        }

        stopRecordingButton.setOnClickListener {
            stopRecordingButton.visibility = View.GONE
            startRecordingButton.visibility = View.VISIBLE
            stopSpeechRecognition()
        }

        saveNoteButton.setOnClickListener {
            val transcription = transcriptionEditText.text.toString()
            if (transcription.isNotEmpty()) {
                saveNoteToDatabase(transcription)
                loadNotes()
                transcriptionEditText.setText("")
            }
        }

        newNoteButton.setOnClickListener {
            transcriptionEditText.setText("")
        }

        loadNotes()
    }

    private fun saveNoteToDatabase(transcription: String) {
        val timestamp = System.currentTimeMillis()
        val noteId = noteRepository.insertNote(transcription, timestamp)
        if (noteId != -1L) {
            // Successfully saved to database
        }
    }

    private fun loadNotes() {
        val notes = noteRepository.getAllNotes()
        displayNotes(notes)
    }
    private fun checkAudioPermission(): Boolean {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION
            )
            return false
        }
        return true
    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        speechRecognizer.startListening(intent)
    }
    private fun stopSpeechRecognition() {
        speechRecognizer.stopListening()
    }

    private fun deleteNoteById(noteId: Long) {
        noteRepository.deleteNoteById(noteId)
        loadNotes()
    }

    private fun updateNoteById(noteId: Long, editedTranscription: String) {
        noteRepository.updateNoteById(noteId, editedTranscription)
        loadNotes()
    }


    private fun displayNotes(notes: List<Note>) {
        val notesContainer: LinearLayout = findViewById(R.id.notesContainer)
        notesContainer.removeAllViews()

        for (note in notes) {
            val noteView = layoutInflater.inflate(R.layout.note_item, null)
            val noteTextView: TextView = noteView.findViewById(R.id.textViewNote)
            val editNoteButton: Button = noteView.findViewById(R.id.buttonEditNote)
            val deleteNoteButton: Button = noteView.findViewById(R.id.buttonDeleteNote)

            noteTextView.text = note.transcription

            editNoteButton.setOnClickListener {
                showEditNoteDialog(note)
            }

            deleteNoteButton.setOnClickListener {
                noteRepository.deleteNoteById(note.id)
                loadNotes()
            }

            notesContainer.addView(noteView)
        }
    }

    private fun showEditNoteDialog(note: Note) {
        val editNoteDialog = AlertDialog.Builder(this)
        val editNoteView = layoutInflater.inflate(R.layout.edit_note_dialog, null)
        val editNoteEditText: EditText = editNoteView.findViewById(R.id.editTextEditNote)
        editNoteEditText.setText(note.transcription)

        editNoteDialog.setView(editNoteView)
        editNoteDialog.setPositiveButton("Save") { _, _ ->
            val editedTranscription = editNoteEditText.text.toString()
            if (editedTranscription.isNotEmpty()) {
                noteRepository.updateNoteById(note.id, editedTranscription)
                loadNotes()
            }
        }
        editNoteDialog.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        editNoteDialog.show()
    }

}
