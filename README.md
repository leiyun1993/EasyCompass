# EasyCompass
### 使用kotlin开发的一个简易指南针

## 关于传感器的简单说明

1、初始化
```
fun initServices() {
    mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) //加速度传感器
    mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)   //地磁场传感器
}
```
2、设置监听和取消监听
```
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
```
3、判断方向
```
if (direction >= 22.5f && direction < 157.5f) {
    //east
} else if (direction > -157.5f && direction < -22.5f) {
    //west
}
if (direction > 122.5f || direction < -122.5f) {
    // south
} else if (direction < 67.5f && direction > -67.5f) {
    // north
}
```
