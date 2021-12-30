package com.zyk.wifiautoproxy

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.annotation.TargetApi
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo


/**
 * Created by zhangyakun on 2020/12/30.
 */
abstract class BaseAccessibilityService : AccessibilityService() {
    companion object {
        /**
         * Check当前辅助服务是否启用
         *
         * @param serviceName serviceName
         * @return 是否启用
         */
        fun checkAccessibilityEnabled(context: Context, serviceName: String): Boolean {
            val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as
                AccessibilityManager
            val accessibilityServices =
                accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
            for (info in accessibilityServices) {
                if (info.resolveInfo?.serviceInfo?.name == serviceName) {
                    return true
                }
            }
            return false
        }

        /**
         * 前往开启辅助服务界面
         */
        fun goAccess(context: Context) {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }

        fun sleep(timeMillis: Long = 300) {
            try {
                Thread.sleep(timeMillis)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 模拟返回操作
     */
    fun performBackClick() {
        try {
            sleep(500)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    /**
     * 模拟home 键操作
     */
    fun performHomeClick() {
        try {
            sleep(500)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    /**
     * 模拟下滑操作
     */
    fun performScrollBackward() {
        try {
            sleep(500)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        performGlobalAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
    }

    /**
     * 模拟上滑操作
     */
    fun performScrollForward() {
        try {
            sleep(500)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        performGlobalAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
    }

    /**
     * 查找对应文本的View
     *
     * @param text text
     * @return View
     */
    @JvmOverloads
    fun findViewByText(
        text: String?,
        clickable: Boolean = false
    ): AccessibilityNodeInfo? {
        val accessibilityNodeInfo = rootInActiveWindow ?: return null
        val nodeInfoList =
            accessibilityNodeInfo.findAccessibilityNodeInfosByText(text)
        if (nodeInfoList != null && nodeInfoList.isNotEmpty()) {
            for (nodeInfo in nodeInfoList) {
                if (nodeInfo != null && nodeInfo.isClickable == clickable) {
                    return nodeInfo
                }
            }
        }
        return null
    }

    /**
     * 查找对应ID的View
     *
     * @param id id
     * @return View
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun findViewByID(id: String?): AccessibilityNodeInfo? {
        val accessibilityNodeInfo = rootInActiveWindow ?: return null
        val nodeInfoList =
            accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id)
        if (nodeInfoList != null && nodeInfoList.isNotEmpty()) {
            for (nodeInfo in nodeInfoList) {
                if (nodeInfo != null) {
                    return nodeInfo
                }
            }
        }
        return null
    }

    fun clickViewByText(text: String?) {
        val accessibilityNodeInfo = rootInActiveWindow ?: return
        val nodeInfoList =
            accessibilityNodeInfo.findAccessibilityNodeInfosByText(text)
        if (nodeInfoList != null && nodeInfoList.isNotEmpty()) {
            for (nodeInfo in nodeInfoList) {
                if (nodeInfo != null) {
                    performViewClick(nodeInfo)
                    break
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun clickViewByID(id: String?) {
        val accessibilityNodeInfo = rootInActiveWindow ?: return
        val nodeInfoList =
            accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id)
        if (nodeInfoList != null && nodeInfoList.isNotEmpty()) {
            for (nodeInfo in nodeInfoList) {
                if (nodeInfo != null) {
                    performViewClick(nodeInfo)
                    break
                }
            }
        }
    }

    /**
     * 模拟点击事件
     *
     * @param nodeInfo nodeInfo
     */
    fun performViewClick(nodeInfo: AccessibilityNodeInfo?) {
        var nodeInfo: AccessibilityNodeInfo? = nodeInfo ?: return
        while (nodeInfo != null) {
            if (nodeInfo.isClickable) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                break
            }
            nodeInfo = nodeInfo.parent
        }
    }

    fun pointClick(point: Point) {
        //只有7.0才可以用
        val builder = GestureDescription.Builder()
        val path = Path()
        path.moveTo(point.x.toFloat(), point.y.toFloat())
        path.lineTo(point.x.toFloat(), point.y.toFloat())
        /**
         * 参数path：笔画路径
         * 参数startTime：时间 (以毫秒为单位)，从手势开始到开始笔划的时间，非负数
         * 参数duration：笔划经过路径的持续时间(以毫秒为单位)，非负数
         */
        builder.addStroke(StrokeDescription(path, 1, 1))
        val build = builder.build()
        /**
         * 参数GestureDescription：翻译过来就是手势的描述，如果要实现模拟，首先要描述你的腰模拟的手势嘛
         * 参数GestureResultCallback：翻译过来就是手势的回调，手势模拟执行以后回调结果
         * 参数handler：大部分情况我们不用的话传空就可以了
         * 一般我们关注GestureDescription这个参数就够了，下边就重点介绍一下这个参数
         */
        dispatchGesture(build, object : GestureResultCallback() {
            override fun onCancelled(gestureDescription: GestureDescription) {
                super.onCancelled(gestureDescription)
            }

            override fun onCompleted(gestureDescription: GestureDescription) {
                super.onCompleted(gestureDescription)
            }
        }, null)
    }

    /**
     * 模拟输入
     *
     * @param nodeInfo nodeInfo
     * @param text     text
     */
    fun inputText(nodeInfo: AccessibilityNodeInfo?, text: String?) {
        if (nodeInfo == null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val arguments = Bundle()
            arguments.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
            )
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            val clipboard =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", text)
            clipboard.setPrimaryClip(clip)
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE)
        }
    }

    override fun onInterrupt() {}
}