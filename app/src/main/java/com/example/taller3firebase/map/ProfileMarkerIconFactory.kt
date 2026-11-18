package com.example.taller3firebase.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import kotlin.math.max

object ProfileMarkerIconFactory {
    fun create(
        context: Context,
        photo: Bitmap?,
        initial: String,
        isCurrentUser: Boolean,
        currentUserColor: Int,
        otherUserColor: Int,
        backgroundColor: Int,
        textColor: Int,
    ): Drawable {
        val density = context.resources.displayMetrics.density
        val size = ((if (isCurrentUser) 64 else 54) * density).toInt()
        val border = ((if (isCurrentUser) 4 else 3) * density)
        val center = size / 2f
        val radius = center
        val innerRadius = radius - border
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val borderColor = if (isCurrentUser) currentUserColor else otherUserColor
        canvas.drawCircle(center, center, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = borderColor })
        canvas.drawCircle(center, center, innerRadius, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = backgroundColor })

        if (photo != null && photo.width > 0 && photo.height > 0) {
            val diameter = innerRadius * 2
            val scale = max(diameter / photo.width, diameter / photo.height)
            val shaderMatrix = Matrix().apply {
                setScale(scale, scale)
                postTranslate(
                    center - photo.width * scale / 2f,
                    center - photo.height * scale / 2f,
                )
            }
            val shader = BitmapShader(photo, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply {
                setLocalMatrix(shaderMatrix)
            }
            canvas.drawCircle(
                center,
                center,
                innerRadius - density,
                Paint(Paint.ANTI_ALIAS_FLAG).apply { this.shader = shader },
            )
        } else {
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor
                textAlign = Paint.Align.CENTER
                textSize = size * 0.38f
                typeface = Typeface.DEFAULT_BOLD
            }
            val baseline = center - (textPaint.ascent() + textPaint.descent()) / 2f
            canvas.drawText(initial.ifBlank { "?" }, center, baseline, textPaint)
        }

        return BitmapDrawable(context.resources, output).apply {
            setTargetDensity(context.resources.displayMetrics)
        }
    }
}
