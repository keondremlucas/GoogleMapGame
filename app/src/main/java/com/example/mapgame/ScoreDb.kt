package com.example.mapgame
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ScoreDb(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION)
{

        private var ID = 1
        companion object
        {
            val DATABASE_NAME = "scores.db"
            const val DATABASE_VERSION = 1
            val TABLE_NAME = "score_table"
            val _ID = "id" // _(underscore indicates primary key), it is a convention
            val TRIES = "Tries"
            val SCORE = "Score"
        }

        override fun onCreate(db: SQLiteDatabase?)
        {

            val SQL_CREATE_TABLE =
                "CREATE TABLE ${TABLE_NAME} (" +
                        "$_ID INTEGER PRIMARY KEY," +
                        "$TRIES," +
                        "$SCORE)"

            db?.execSQL(SQL_CREATE_TABLE)
        }


        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int)
        {
            val SQL_DELETE_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
            db?.execSQL(SQL_DELETE_TABLE)
            onCreate(db)
        }

        fun insertData(tries: String, score: String)
        {
            val db = this.writableDatabase
            if(tries == "0")
            {tries == "1"}
            val contentValues = ContentValues()
            contentValues.put(TRIES , tries)
            contentValues.put(SCORE, score)
            db.insert(TABLE_NAME, null, contentValues)

        }

        fun updateData(tries: String, score: String): Boolean
        {


            val db = this.writableDatabase
            val contentValues = ContentValues()

            contentValues.put(_ID, ID)
            ID++
            contentValues.put(TRIES ,tries)
            contentValues.put(SCORE, score)
            db.update(TABLE_NAME, contentValues, "ID = ?", arrayOf(ID.toString()))
            return true
        }

        fun deleteData(): Int
        {

            val db = this.writableDatabase
            return db.delete(TABLE_NAME,null, null)
        }
        val viewAllData : Cursor
            get()
            {
                // Gets the data repository in write mode
                val db = this.writableDatabase
                // Cursor iterates through one row at a time in the results
                return db.rawQuery("SELECT * FROM " + TABLE_NAME, null)
            }
    }

