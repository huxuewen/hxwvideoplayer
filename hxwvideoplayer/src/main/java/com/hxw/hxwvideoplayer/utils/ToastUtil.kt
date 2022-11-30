package com.hxw.hxwvideoplayer.utils

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.hxw.hxwvideoplayer.HxwVideoPlayerManager
import com.hxw.hxwvideoplayer.R
import com.hxw.hxwvideoplayer.databinding.ComponentToastBinding
import java.util.*

/**
 * @author xuewen hu
 * @date 2021/9/4 10:11
 */
object ToastUtil {
	private val toastList: MutableList<Toast> = ArrayList()

	@JvmStatic
	fun shortShow(msg: String?) {
		cancelAllToast()
		val toast = Toast.makeText(HxwVideoPlayerManager.applicationContext, msg, Toast.LENGTH_SHORT)
		toast.show()
		toastList.add(toast)
	}

	@JvmStatic
	fun longShow(msg: String?) {
		cancelAllToast()
		val toast = Toast.makeText(HxwVideoPlayerManager.applicationContext, msg, Toast.LENGTH_LONG)
		toast.show()
		toastList.add(toast)
	}

	@JvmStatic
	fun shortShow(resId: Int) {
		cancelAllToast()
		val toast = Toast.makeText(HxwVideoPlayerManager.applicationContext, resId, Toast.LENGTH_SHORT)
		toast.show()
		toastList.add(toast)
	}

    @JvmStatic
    fun shortShow(first :String, second:String) {
        val binding = ComponentToastBinding.inflate(LayoutInflater.from(HxwVideoPlayerManager.applicationContext))
        cancelAllToast()
        val toast = Toast.makeText(HxwVideoPlayerManager.applicationContext, "", Toast.LENGTH_SHORT)
        toast.view = binding.root
        binding.toast1.text = first
        binding.toast2.text = second
        binding.toast2.setTextColor(AndroidInfoUtil.getColor(R.color.orange))
        toast.show()
        toastList.add(toast)
    }

    @JvmStatic
    fun shortShow(context: Context,first :String, second:String, third: String) {
        val binding = ComponentToastBinding.inflate(LayoutInflater.from(context))
        cancelAllToast()
        val toast = Toast.makeText(HxwVideoPlayerManager.applicationContext, "", Toast.LENGTH_SHORT)
        toast.view = binding.root
        binding.toast1.text = first
        binding.toast2.text = second
        binding.toast2.setTextColor(AndroidInfoUtil.getColor(R.color.orange))
        binding.toast3.text = third
        toast.show()
        toastList.add(toast)
    }

	@JvmStatic
	fun longShow(resId: Int) {
		cancelAllToast()
		val toast = Toast.makeText(HxwVideoPlayerManager.applicationContext, resId, Toast.LENGTH_LONG)
		toast.show()
		toastList.add(toast)
	}

	@JvmStatic
	fun cancelAllToast() {
		if (toastList.isNotEmpty()) {
			for (toast in toastList) {
				toast.cancel()
			}
			toastList.clear()
		}
	}
}