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
## 关于表盘的的简单说明

1、 表盘使用自定义View完成，通过绘制文字表盘刻度等实现无论如何旋转文字都是正的，大体思路如下；此View是使用最笨的
方式去实现的，还有很多需要完善的地方
```
canvas.save()           //保存画布，先旋转和方向无关的东西
canvas.rotate(mDirection, (width / 2).toFloat(), (height / 2).toFloat())
drawBackGroundCircle()
drawScaleLine()
canvas.restore()        //重置画布

canvas.save()
drawDirectionText()     //重置画布后通过传感器角度来确定文字的位置，以达到文字问正的情况
drawAngleText()
canvas.rotate(mDirection, (width / 2).toFloat(), (height / 2).toFloat())
canvas.restore()
drawNHand()             //最后画不需要动的指针
```

## 简易指南针预览如下

![image](https://github.com/leiyun1993/EasyCompass/raw/master/screenshot/1.jpg)
![image](https://github.com/leiyun1993/EasyCompass/raw/master/screenshot/2.jpg)

## 相关链接

1、感谢[MIUI指南针社区开源版](https://github.com/MiCode/Compass)

2、UI没啥灵感于是参考了[MiClockView](https://github.com/MonkeyMushroom/MiClockView),非常感谢

3、Google 2017IO大会指定Android开发语言[Kotlin](https://github.com/JetBrains/kotlin)

4、Anko是一个使开发Android应用更简单更快捷的库[Anko](https://github.com/Kotlin/anko)