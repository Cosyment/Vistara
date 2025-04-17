package com.vistara.aestheticwalls.ui.screens.edit

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * 自定义编辑图标
 */
object EditIcons {
    /**
     * 亮度图标
     */
    val Brightness: ImageVector
        get() {
            return ImageVector.Builder(
                name = "brightness",
                defaultWidth = 24.0.dp,
                defaultHeight = 24.0.dp,
                viewportWidth = 24.0f,
                viewportHeight = 24.0f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(12.0f, 7.0f)
                    curveTo(9.24f, 7.0f, 7.0f, 9.24f, 7.0f, 12.0f)
                    reflectiveCurveToRelative(2.24f, 5.0f, 5.0f, 5.0f)
                    reflectiveCurveToRelative(5.0f, -2.24f, 5.0f, -5.0f)
                    reflectiveCurveTo(14.76f, 7.0f, 12.0f, 7.0f)
                    close()
                    moveTo(12.0f, 2.0f)
                    lineTo(12.0f, 4.0f)
                    reflectiveCurveToRelative(0.0f, 2.0f, 0.0f, 0.0f)
                    moveTo(12.0f, 20.0f)
                    verticalLineToRelative(2.0f)
                    reflectiveCurveToRelative(0.0f, -2.0f, 0.0f, 0.0f)
                    moveTo(4.93f, 4.93f)
                    lineToRelative(1.41f, 1.41f)
                    reflectiveCurveToRelative(1.41f, 1.41f, 0.0f, 0.0f)
                    moveTo(17.66f, 17.66f)
                    lineToRelative(1.41f, 1.41f)
                    reflectiveCurveToRelative(-1.41f, -1.41f, 0.0f, 0.0f)
                    moveTo(2.0f, 12.0f)
                    horizontalLineToRelative(2.0f)
                    reflectiveCurveToRelative(2.0f, 0.0f, 0.0f, 0.0f)
                    moveTo(20.0f, 12.0f)
                    horizontalLineToRelative(2.0f)
                    reflectiveCurveToRelative(-2.0f, 0.0f, 0.0f, 0.0f)
                    moveTo(6.34f, 17.66f)
                    lineToRelative(-1.41f, 1.41f)
                    reflectiveCurveToRelative(1.41f, -1.41f, 0.0f, 0.0f)
                    moveTo(19.07f, 4.93f)
                    lineToRelative(-1.41f, 1.41f)
                    reflectiveCurveToRelative(-1.41f, -1.41f, 0.0f, 0.0f)
                }
            }.build()
        }

    /**
     * 对比度图标
     */
    val Contrast: ImageVector
        get() {
            return ImageVector.Builder(
                name = "contrast",
                defaultWidth = 24.0.dp,
                defaultHeight = 24.0.dp,
                viewportWidth = 24.0f,
                viewportHeight = 24.0f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(12.0f, 2.0f)
                    curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                    reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                    reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                    reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                    close()
                    moveTo(12.0f, 20.0f)
                    curveToRelative(-4.41f, 0.0f, -8.0f, -3.59f, -8.0f, -8.0f)
                    reflectiveCurveToRelative(3.59f, -8.0f, 8.0f, -8.0f)
                    verticalLineToRelative(16.0f)
                    close()
                }
            }.build()
        }

    /**
     * 饱和度图标
     */
    val Saturation: ImageVector
        get() {
            return ImageVector.Builder(
                name = "saturation",
                defaultWidth = 24.0.dp,
                defaultHeight = 24.0.dp,
                viewportWidth = 24.0f,
                viewportHeight = 24.0f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(12.0f, 2.0f)
                    curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                    reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                    reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                    reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                    close()
                    moveTo(12.0f, 20.0f)
                    curveToRelative(-4.41f, 0.0f, -8.0f, -3.59f, -8.0f, -8.0f)
                    reflectiveCurveToRelative(3.59f, -8.0f, 8.0f, -8.0f)
                    reflectiveCurveToRelative(8.0f, 3.59f, 8.0f, 8.0f)
                    reflectiveCurveTo(16.41f, 20.0f, 12.0f, 20.0f)
                    close()
                    moveTo(5.5f, 10.5f)
                    horizontalLineToRelative(13.0f)
                    verticalLineToRelative(3.0f)
                    horizontalLineToRelative(-13.0f)
                    close()
                }
            }.build()
        }

    /**
     * 滤镜图标
     */
    val Filter: ImageVector
        get() {
            return ImageVector.Builder(
                name = "filter",
                defaultWidth = 24.0.dp,
                defaultHeight = 24.0.dp,
                viewportWidth = 24.0f,
                viewportHeight = 24.0f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(19.0f, 3.0f)
                    horizontalLineTo(5.0f)
                    curveTo(3.9f, 3.0f, 3.0f, 3.9f, 3.0f, 5.0f)
                    verticalLineToRelative(14.0f)
                    curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                    horizontalLineToRelative(14.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    verticalLineTo(5.0f)
                    curveTo(21.0f, 3.9f, 20.1f, 3.0f, 19.0f, 3.0f)
                    close()
                    moveTo(19.0f, 19.0f)
                    horizontalLineTo(5.0f)
                    verticalLineTo(5.0f)
                    horizontalLineToRelative(14.0f)
                    verticalLineTo(19.0f)
                    close()
                    moveTo(13.0f, 17.0f)
                    horizontalLineTo(7.0f)
                    verticalLineTo(7.0f)
                    horizontalLineToRelative(6.0f)
                    verticalLineTo(17.0f)
                    close()
                    moveTo(17.0f, 15.0f)
                    horizontalLineTo(15.0f)
                    verticalLineTo(13.0f)
                    horizontalLineToRelative(2.0f)
                    verticalLineTo(15.0f)
                    close()
                    moveTo(17.0f, 11.0f)
                    horizontalLineTo(15.0f)
                    verticalLineTo(9.0f)
                    horizontalLineToRelative(2.0f)
                    verticalLineTo(11.0f)
                    close()
                }
            }.build()
        }
}
