package com.leiyun.easycompass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import com.tencent.map.geolocation.TencentLocation
import com.tencent.map.geolocation.TencentLocationListener
import com.tencent.map.geolocation.TencentLocationManager
import com.tencent.map.geolocation.TencentLocationRequest
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.Permission
import com.yanzhenjie.permission.PermissionListener
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.imageResource


class MainActivity : AppCompatActivity(), Runnable, TencentLocationListener {


    private var mTargetDirection: Float = 0.0f
    private var mStopDrawing: Boolean = false
    private var accelerometerValues = FloatArray(3)
    private var magneticFieldValues = FloatArray(3)
    private lateinit var mSensorManager: SensorManager
    private lateinit var mOrientationSensor: Sensor
    private lateinit var mMagneticSensor: Sensor
    private lateinit var mOrientationListener: MySensorEventListener
    private lateinit var mMagneticListener: MySensorEventListener
    private val mHandler = Handler()

    override fun run() {
        if (mDrawPointer != null && !mStopDrawing) {
            mDrawPointer.updateDirection(360 - mTargetDirection)
            calculateOrientation()
            mHandler.postDelayed(this, 20)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initResources()
        initServices()
        requestPermissions()
    }

    override fun onResume() {
        super.onResume()
        mOrientationListener = MySensorEventListener()
        mMagneticListener = MySensorEventListener()
        mSensorManager.registerListener(mOrientationListener, mOrientationSensor, Sensor.TYPE_ACCELEROMETER)
        mSensorManager.registerListener(mMagneticListener, mMagneticSensor, Sensor.TYPE_MAGNETIC_FIELD)
        mStopDrawing = false
        mHandler.postDelayed(this, 20)
    }

    override fun onPause() {
        super.onPause()
        mStopDrawing = true
        mSensorManager.unregisterListener(mOrientationListener)
        mSensorManager.unregisterListener(mMagneticListener)
    }

    fun requestPermissions() {
        AndPermission.with(this)
                .requestCode(0x01)
                .permission(Permission.LOCATION)
                .rationale { _, rationale ->
                    AndPermission.rationaleDialog(this, rationale).show()
                }
                .callback(object : PermissionListener {
                    override fun onSucceed(requestCode: Int, grantPermissions: MutableList<String>) {
                        if (requestCode == 0x01) {
                            startLocation()
                        }
                    }

                    override fun onFailed(requestCode: Int, deniedPermissions: MutableList<String>) {
                        if (requestCode == 0x01) {

                        }
                    }

                }).start()
    }

    fun startLocation() {
        val request = TencentLocationRequest.create()
        val error = TencentLocationManager.getInstance(this).requestLocationUpdates(request, this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    override fun onStatusUpdate(p0: String?, p1: Int, p2: String?) {

    }

    override fun onLocationChanged(location: TencentLocation, error: Int, p2: String?) {
        if (error == TencentLocation.ERROR_OK) {
            val latStr = location.latitude.toString()
            val lngStr = location.longitude.toString()
            val addressStr = location.address
            lat.text = latStr
            lng.text = lngStr
            if (addressStr == null || addressStr.isEmpty()) {
                address.text = "请检查网络是否通畅"
            } else {
                address.text = addressStr
            }
        } else {

        }
    }

    fun initResources() {
        mStopDrawing = true
    }

    fun initServices() {
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) //加速度传感器
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)   //地磁场传感器
    }

    fun calculateOrientation() {
        val values = FloatArray(3)
        val RValues = FloatArray(9)
        SensorManager.getRotationMatrix(RValues, null, accelerometerValues, magneticFieldValues)
        SensorManager.getOrientation(RValues, values)
        values[0] = Math.toDegrees(values[0].toDouble()).toFloat()


        val lp: ViewGroup.LayoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mDirectionLayout.removeAllViews()
        mAngleLayout.removeAllViews()
        var east: ImageView? = null
        var west: ImageView? = null
        var south: ImageView? = null
        var north: ImageView? = null
        val direction = values[0]

        if (direction >= 22.5f && direction < 157.5f) {
            //east
            east = ImageView(this)
            east.imageResource = R.mipmap.e_cn
            east.layoutParams = lp
        } else if (direction > -157.5f && direction < -22.5f) {
            //west
            west = ImageView(this)
            west.imageResource = R.mipmap.w_cn
            west.layoutParams = lp
        }
        if (direction > 122.5f || direction < -122.5f) {
            // south
            south = ImageView(this)
            south.imageResource = R.mipmap.s_cn
            south.layoutParams = lp
        } else if (direction < 67.5f && direction > -67.5f) {
            // north
            north = ImageView(this)
            north.imageResource = R.mipmap.n_cn
            north.layoutParams = lp
        }
        if (east != null) {
            mDirectionLayout.addView(east)
        }
        if (west != null) {
            mDirectionLayout.addView(west)
        }
        if (south != null) {
            mDirectionLayout.addView(south)
        }
        if (north != null) {
            mDirectionLayout.addView(north)
        }
        mTargetDirection = direction
        var direction2 = normalizeDegree(direction).toInt()
        var show = false
        if (direction2 >= 100) {
            mAngleLayout.addView(getNumberImage(direction2 / 100))
            direction2 %= 100
            show = true
        }
        if (direction2 >= 10 || show) {
            mAngleLayout.addView(getNumberImage(direction2 / 10))
            direction2 %= 10
        }
        mAngleLayout.addView(getNumberImage(direction2))

        val degreeImageView = ImageView(this)
        degreeImageView.imageResource = R.mipmap.degree
        degreeImageView.layoutParams = lp
        mAngleLayout.addView(degreeImageView)

        Log.d("mainActivity", direction.toString())
    }

    private fun getNumberImage(number: Int): ImageView {
        val image = ImageView(this)
        val lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        when (number) {
            0 -> image.setImageResource(R.mipmap.number_0)
            1 -> image.setImageResource(R.mipmap.number_1)
            2 -> image.setImageResource(R.mipmap.number_2)
            3 -> image.setImageResource(R.mipmap.number_3)
            4 -> image.setImageResource(R.mipmap.number_4)
            5 -> image.setImageResource(R.mipmap.number_5)
            6 -> image.setImageResource(R.mipmap.number_6)
            7 -> image.setImageResource(R.mipmap.number_7)
            8 -> image.setImageResource(R.mipmap.number_8)
            9 -> image.setImageResource(R.mipmap.number_9)
        }
        image.layoutParams = lp
        return image
    }

    private fun normalizeDegree(degree: Float): Float {
        return (degree + 720) % 360
    }


    inner class MySensorEventListener : SensorEventListener2 {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }

        override fun onFlushCompleted(sensor: Sensor?) {

        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = event.values
            }
            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticFieldValues = event.values
            }
        }

    }
}
