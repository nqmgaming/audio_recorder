package com.nqmgaming.audiorecorder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener


class WaveformView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint().apply { color = Color.rgb(244, 81, 30) }
    private val amplitudes = ArrayList<Float>()
    private val spikes = ArrayList<RectF>()
    private var animator: ValueAnimator? = null
    private val radius = 6f
    private val w = 9f
    private val d = 6f
    private val sh = 800f
    private val maxSpikes = (resources.displayMetrics.widthPixels / (w + d)).toInt()

    private val minAmplitude = 100f
    private val maxAmplitude = 1000f
    private val minHeight = 10f
    private val maxHeight = 600f
    private val animationDuration = 500L

    fun addAmplitude(amp: Float) {
        val filteredAmp = applyNoiseReductionFilter(amp)
        val lastAmplitude = if (amplitudes.isEmpty()) 0f else amplitudes.last()
        val interpolatedAmplitude = interpolate(lastAmplitude, filteredAmp, 0.05f)

        val heightPercentage =
            (interpolatedAmplitude - minAmplitude) / (maxAmplitude - minAmplitude)
        val targetHeight = minHeight + (maxHeight - minHeight) * heightPercentage

        amplitudes.add(targetHeight.coerceIn(minHeight, maxHeight))
        spikes.clear()
        amplitudes.takeLast(maxSpikes).forEachIndexed { i, amplitude ->
            val left = i * (w + d)
            val top = sh / 2 - amplitude / 2
            spikes.add(RectF(left, top, left + w, top + amplitude))
        }

        invalidate()

    }

    private fun applyNoiseReductionFilter(amp: Float): Float {
        // This is a placeholder for your noise reduction algorithm.
        // Replace this with your actual implementation.
        return amp
    }

    fun clear() = ArrayList(amplitudes).also {
        amplitudes.clear()
        spikes.clear()
        invalidate()
    }

    fun interpolate(a: Float, b: Float, proportion: Float): Float {
        return (a + ((b - a) * proportion))
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        spikes.forEach { canvas.drawRoundRect(it, radius, radius, paint) }
    }
}