package melo.vlademir.seminario

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "tracker.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_NAME = "tracker"
        private const val ID = "_id"
        private const val TIME = "time"
        private const val DISTANCE = "distance"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableSQL = """
            CREATE TABLE $TABLE_NAME (
                $ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $TIME TEXT NOT NULL,
                $DISTANCE REAL NOT NULL
            )
        """
        db?.execSQL(createTableSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addRun(time: String, distance: Double) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(TIME, time)
            put(DISTANCE, distance)
        }
        db.insert(TABLE_NAME, null, values)
    }

    fun getRuns(): Cursor? {
        val db = readableDatabase
        return db.query(
            TABLE_NAME,
            arrayOf(ID, TIME, DISTANCE),
            null, null, null, null, null
        )
    }

    fun deleteRun(id: Long) {
        val db = writableDatabase
        db.delete(TABLE_NAME, "$ID = ?", arrayOf(id.toString()))
    }
}
