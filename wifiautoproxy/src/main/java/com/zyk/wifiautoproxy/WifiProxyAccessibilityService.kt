package com.zyk.wifiautoproxy

import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.view.Gravity
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.annotation.RequiresApi

/**
 * ~/Library/Android/sdk/tools/bin/uiautomatorviewer
 * Created by zhangyakun on 2020/12/30.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class WifiProxyAccessibilityService : BaseAccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (WifiConfig.asOpen && event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event.packageName == "com.android.settings") {
            val className = event.className
            if (className == "com.android.settings.Settings\$WifiSettingsActivity") {
                // wlan页面
                clickViewByID("com.android.settings:id/preference_detail")
                // 详情页面
                sleep()
//                clickViewByText("高级设置")
                // 高级设置页面
//                sleep()
                val spinner = findViewByText("代理")?.parent?.getChild(0)
//                performViewClick(spinner)
                val rect = Rect()
                spinner?.getBoundsInScreen(rect)
                pointClick(Point(rect.right - 50, rect.bottom - 50))
//                clickViewByID("com.android.settings:id/icon1")
                sleep(1000)
                if (WifiConfig.open) {
                    val manual = findViewByText("手动", true)
                    performViewClick(manual)
                    sleep(1000)
                    val hostInfo = findViewByID("com.android.settings:id/proxy_hostname")
                    inputText(hostInfo, WifiConfig.host)
                    val portInfo = findViewByID("com.android.settings:id/proxy_port")
                    inputText(portInfo, WifiConfig.port)
                    val yes = findViewByText("确定", true)
                    performViewClick(yes)
                    Toast.makeText(
                        this,
                        "已经打开${WifiConfig.proxyType}代理!\n ${WifiConfig.host}:${WifiConfig.port}",
                        Toast.LENGTH_LONG
                    ).apply { setGravity(Gravity.CENTER, 0, 0) }.show()
                    WifiConfig.asOpen = false
                } else {
                    val none = findViewByText("无", true)
                    performViewClick(none)
                    val yes = findViewByText("确定", true)
                    performViewClick(yes)

                    Toast.makeText(this, "已经关闭代理", Toast.LENGTH_LONG)
                        .apply { setGravity(Gravity.CENTER, 0, 0) }.show()
                    WifiConfig.asOpen = false
                }
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
        }
    }
}