import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import com.example.audionoteapp.Note
import com.example.audionoteapp.NoteContract
import com.example.audionoteapp.NoteDatabaseHelper

class NoteRepository(context: Context) {

    private val databaseHelper: NoteDatabaseHelper = NoteDatabaseHelper(context)

    fun insertNote(transcription: String, timestamp: Long): Long {
        val db = databaseHelper.writableDatabase

        val values = ContentValues().apply {
            put(NoteContract.NoteEntry.COLUMN_NAME_TRANSCRIPTION, transcription)
            put(NoteContract.NoteEntry.COLUMN_NAME_TIMESTAMP, timestamp)
        }

        return db.insert(NoteContract.NoteEntry.TABLE_NAME, null, values)
    }

    fun getAllNotes(): List<Note> {
        val db = databaseHelper.readableDatabase

        val projection = arrayOf(
            BaseColumns._ID,
            NoteContract.NoteEntry.COLUMN_NAME_TRANSCRIPTION,
            NoteContract.NoteEntry.COLUMN_NAME_TIMESTAMP
        )

        val cursor = db.query(
            NoteContract.NoteEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            "${NoteContract.NoteEntry.COLUMN_NAME_TIMESTAMP} DESC"
        )

        return parseNotes(cursor)
    }

    private fun parseNotes(cursor: Cursor): List<Note> {
        val notes = mutableListOf<Note>()
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val transcription =
                    getString(getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_NAME_TRANSCRIPTION))
                val timestamp =
                    getLong(getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_NAME_TIMESTAMP))

                notes.add(Note(id, transcription, timestamp))
            }
        }
        return notes
    }

    fun deleteNoteById(noteId: Long) {
        val db = databaseHelper.writableDatabase
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(noteId.toString())
        db.delete(NoteContract.NoteEntry.TABLE_NAME, selection, selectionArgs)
    }

    fun updateNoteById(noteId: Long, transcription: String) {
        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put(NoteContract.NoteEntry.COLUMN_NAME_TRANSCRIPTION, transcription)
        }
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(noteId.toString())
        db.update(NoteContract.NoteEntry.TABLE_NAME, values, selection, selectionArgs)
    }
}
