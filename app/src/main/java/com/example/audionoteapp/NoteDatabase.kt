package com.example.audionoteapp
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

object NoteContract {
    object NoteEntry : BaseColumns {
        const val TABLE_NAME = "notes"
        const val COLUMN_NAME_TRANSCRIPTION = "transcription"
        const val COLUMN_NAME_TIMESTAMP = "timestamp"
    }
}

class NoteDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE ${NoteContract.NoteEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${NoteContract.NoteEntry.COLUMN_NAME_TRANSCRIPTION} TEXT," +
                    "${NoteContract.NoteEntry.COLUMN_NAME_TIMESTAMP} INTEGER)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Implement if needed for future database upgrades
    }

    companion object {
        const val DATABASE_NAME = "notes.db"
        const val DATABASE_VERSION = 1
    }
}
