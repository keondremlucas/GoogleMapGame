package com.example.mapgame

import android.R.attr.x
import android.R.attr.y
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*


import java.util.*
import kotlin.math.sqrt


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,SensorEventListener
{
    private var scount = 0
    private var acount = 0

    private lateinit var mMap: GoogleMap
    private lateinit var geocoder: Geocoder


    private val TAG = "MainActivity"
    private val CHANNEL_ID = "com.example.notifications"
    private val dbHelper = ScoreDb(this)

    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null
    private var currentThreshold = 0.0
    private var previousValue = 0.0
    private var myMediaPlayer : MediaPlayer? = null




    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        registerSensor(mAccelerometer)
        dbHelper.deleteData()


        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.StatesGame) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }


    override fun onSensorChanged(event: SensorEvent?)
    {
        val sensor = event?.sensor ?: return
        when (sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {

                val x: Float = event.values[0]
                val y: Float = event.values[1]
                val z: Float = event.values[2]

                Log.d(TAG, "Accelerometer: ${event.values.joinToString(" ")}")

                val acc = sqrt((x * x + y * y + z * z).toDouble())
                currentThreshold = acc
                previousValue = currentThreshold
                val change = currentThreshold - previousValue
                if (change != 0.0) {
                    val myintent = Intent(this, MapsActivity::class.java)
                    startActivity(myintent)
                    previousValue = currentThreshold
                    currentThreshold = 0.0


                }

            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int)
    {

    }


    override fun onMapReady(googleMap: GoogleMap)
    {
            mMap = googleMap
            mMap.uiSettings.isZoomControlsEnabled = false
            genmap(mMap)

    }

    fun genmap(googleMap: GoogleMap)
    {
            val r = Random()
            val a = r.nextInt(47 - 32) + 32
            val b = r.nextInt(121 - 72) + 72

            val america = LatLng(a.toDouble(), b.toDouble() * -1)
            val boundsUSA = LatLngBounds(LatLng(19.5, 68.14712), LatLng(28.20453, 97.34466))
            geocoder = Geocoder(this)
            val addressList = geocoder.getFromLocation(a.toDouble(), (b.toDouble() * -1), 1)
            if (addressList != null && addressList[0] != null) {
                val address = addressList[0]
                if (address.adminArea != null) {
                    val state = address.adminArea
                    val country = address.countryName




                    Log.d(TAG, state)
                    Log.d(TAG, country)

                    val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("state", state)
                    editor.apply()


                    val markerOptions = MarkerOptions()
                        .position(america)
                        .title(address.adminArea)

                    googleMap.addMarker(markerOptions)
                    googleMap.moveCamera(CameraUpdateFactory.zoomTo(8.20f))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(america))

                    googleMap.uiSettings.isZoomControlsEnabled = false
                    googleMap.uiSettings.isZoomGesturesEnabled = false
                } else {
                    genmap(googleMap)
                }
            } else {
                genmap(googleMap)
            }
            val button = findViewById<Button>(R.id.button)
            val reset = findViewById<Button>(R.id.reset)
            val highscore = findViewById<Button>(R.id.button2)
            val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
            val state = sharedPreferences.getString("state", "")



            button.setOnClickListener()
            {

                val input = findViewById<EditText>(R.id.editText).text.toString()



                if (state == input) {
                    playSoundR()
                    val attempts = findViewById<TextView>(R.id.attempts)

                    val score = findViewById<TextView>(R.id.Score)
                    scount++
                    score.text = "Score: $scount"
                    Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
                    genmap(googleMap)
                    if(scount==3)
                    {
                        makeNotification()
                    }
                    acount=0
                    attempts.text = "Attempts: $acount"

                } else {
                    playSoundW()

                    val score = findViewById<TextView>(R.id.Score)
                    val attempts = findViewById<TextView>(R.id.attempts)
                    scount = 0
                    acount++
                    attempts.text = "Attempts: $acount"
                    score.text = "Score: $scount"
                    Toast.makeText(this, "Try Again!", Toast.LENGTH_SHORT).show()

                }
                dbHelper.insertData((acount).toString(), scount.toString())
                dbHelper.updateData((acount).toString(),scount.toString())
            }

            reset.setOnClickListener()
            {
                dbHelper.deleteData()
                val myintent = Intent(this, MapsActivity::class.java)
                startActivity(myintent)
            }

            highscore.setOnClickListener()
            {
                try {



                    val cursor = dbHelper.viewAllData
                    val buffer = StringBuffer()
                    while (cursor.moveToNext()) {
                        buffer.append("Guess:" + cursor.getInt(0) + "\n")
                        buffer.append("Attempts:" + cursor.getString(1) + "\n")
                        buffer.append("Score :" + cursor.getString(2) + "\n\n")
                    }
                    showDialog("Scores for this Session", buffer.toString())
                } catch (e: Exception) {
                    Log.e(TAG, "error: $e")
                }
            }


    }

    override fun onResume()
    {
        super.onResume()
        mSensorManager?.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)


    }


    override fun onPause()
    {
        super.onPause()
        mSensorManager?.unregisterListener(this)
    }

    private fun playSoundR()
    {
        if(myMediaPlayer != null)
        {

            myMediaPlayer?.release()
            myMediaPlayer = null
        }
        if (myMediaPlayer == null)
        {
            myMediaPlayer = MediaPlayer.create(this, R.raw.correct)
        }
        myMediaPlayer?.start()
    }

    private fun playSoundW()
    {
        if(myMediaPlayer != null)
        {

            myMediaPlayer?.release()
            myMediaPlayer = null
        }
        if (myMediaPlayer == null)
        {
            myMediaPlayer = MediaPlayer.create(this, R.raw.wrong)
        }
        myMediaPlayer?.start()

    }
    private fun makeNotification()
    {

        createNotificationChannel()
        val intent = Intent(this,MapsActivity::class.java)
        intent.putExtra("file",  "test")
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_notifications_24)
            .setContentTitle("You're good at this game!")
            .setContentText("You've scored over 3 points!!!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationId = Random().nextInt()
        NotificationManagerCompat.from(this).notify(notificationId, builder.build())

    }

    private fun createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val name = "My channel"
            val descriptionText = "My Default Priority Channel for Test"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
        }
    }
    private fun showDialog(title : String,Message : String)
    {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle(title)
        builder.setMessage(Message)
        builder.show()
    }

    override fun onDestroy()
    {
        dbHelper.close()
        super.onDestroy()
    }

    private fun registerSensor(sensor: Sensor?)
    {
            if (sensor != null)
            {
                mSensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
    }

}




