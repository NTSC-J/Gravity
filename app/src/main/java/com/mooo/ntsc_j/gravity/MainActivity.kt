package com.mooo.ntsc_j.gravity

import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.math.sqrt
import com.jjoe64.graphview.series.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity(), SensorEventListener {
    private val recDir = File(Environment.getExternalStorageDirectory().path, "Gravity")
    private var series = LineGraphSeries<DataPoint>()
    private var startTimeInMillis: Long = 0
    private lateinit var recStream: FileOutputStream

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val manager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)

        recButton.setOnClickListener(recButton)
        recButton.onStartRec = {
            val fileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.JAPAN).format(Date()) + ".csv"
            recDir.mkdirs()
            recStream = FileOutputStream(File(recDir, fileName), true)
            recStream.write("Time,v0,v1,v2\r\n".toByteArray())

            Toast.makeText(this, "saving CSV to ${recDir.toString()}/$fileName", Toast.LENGTH_LONG).show()

            series.resetData(arrayOf())
            startTimeInMillis = Calendar.getInstance().timeInMillis
            manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)
        }
        recButton.onStopRec = {
            manager.unregisterListener(this, accelerometer)
            recStream.close()
        }

        graph.addSeries(series)
        graph.viewport.isXAxisBoundsManual = true
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d("gravity", "accuracy changed")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event!!.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val vs = event.values.clone()
                assert(vs.size == 3)
                val v = sqrt(vs[0] * vs[0] + vs[1] * vs[1] + vs[2] * vs[2]).toDouble()
                accView.text = v.toString()
                val time = (Calendar.getInstance().timeInMillis - startTimeInMillis) * 0.001
                recStream.write("$time,${vs[0]},${vs[1]},${vs[2]},$v\r\n".toByteArray())
                series.appendData(DataPoint(time, v), true, 5000)
                graph.viewport.setMinX(0.0)
            }
        }
    }
}
