package com.androidbox.lightsensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private val maxValue = 200f
//    private val minValue = 0f

    private var minBrightness = 0f
    private var maxBrightness = 100f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get an instance of the sensor service, and use that to get an instance of
        // a particular sensor.
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT).apply {
            lightBarView.max = maximumRange.toInt()
        }

        setSeekBars()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { e ->
            if (e.sensor.type == Sensor.TYPE_LIGHT) {
                val luminosity = e.values[0]
                val conditionValue = getConditionValue(luminosity)

                helloTextView.text = "Illuminance value: $luminosity"
                lightBarView.progress = luminosity.toInt()

                conditionTextView.text = "Condition: ${getConditionName(luminosity)}"
                conditionBar.progress = conditionValue.toInt()
                currentSeekBar.progress = conditionValue.toInt()
            }
        }
    }

    /// https://en.wikipedia.org/wiki/Lux
    private fun getConditionName(luminosity: Float): String {
        return when (luminosity) {
            in 0f..0.001f -> "Moonless, overcast night sky"
            in 0.002f..0.04f -> "Moonless clear night sky with"
            in 0.05f..0.3f -> "Full moon on a clear night"
            in 0.4f..19f -> "Dark limit of civil twilight under a clear sky"
            in 20f..50f -> "Public areas with dark surroundings"
            in 51f..79f -> "Family living room lights"
            in 80f..99f -> "Office building hallway/toilet lighting"
            in 100f..149f -> "Very dark overcast day"
            in 150f..319f -> "Train station platforms"
            in 320f..500f -> "Office lighting"
            in 501f..1000f -> "Overcast day; typical TV studio lighting"
            in 10000f..25000f -> "Full daylight"
            else -> "Direct sunlight"
        }
    }

    private fun getConditionValue(luminosity: Float): Float {
        if (luminosity > maxValue) return 100f

        return ((luminosity * 100) / maxValue)
    }

    override fun onResume() {
        // Register a listener for the sensor.
        super.onResume()
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun setSeekBars() {
        minSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                txtMinValue.text = "$progress%"
                minBrightness = progress.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        maxSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                txtMaxValue.text = "$progress%"
                maxBrightness = progress.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        currentSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                txtCurrentValue.text = "$progress%"
                changeBrightness(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun changeBrightness(value: Float) {
        val total = (value * (maxBrightness - minBrightness)) / 100

        window.attributes = window.attributes.apply {
            screenBrightness = ((value * total) / 100) + minBrightness
        }
    }
}
