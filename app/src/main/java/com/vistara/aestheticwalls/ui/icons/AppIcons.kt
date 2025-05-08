package com.vistara.aestheticwalls.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * 应用自定义图标
 */
object AppIcons {
    /**
     * 设置图标 - 更现代的齿轮设计
     */
    val Settings: ImageVector
        get() {
            return ImageVector.Builder(
                name = "settings",
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
                    moveTo(19.5f, 12.0f)
                    curveToRelative(0.0f, -0.32f, -0.03f, -0.63f, -0.08f, -0.93f)
                    lineToRelative(2.0f, -1.56f)
                    curveToRelative(0.18f, -0.14f, 0.23f, -0.39f, 0.11f, -0.59f)
                    lineToRelative(-1.9f, -3.29f)
                    curveToRelative(-0.12f, -0.2f, -0.36f, -0.29f, -0.58f, -0.21f)
                    lineToRelative(-2.36f, 0.95f)
                    curveToRelative(-0.49f, -0.38f, -1.03f, -0.69f, -1.62f, -0.92f)
                    lineToRelative(-0.36f, -2.51f)
                    curveToRelative(-0.04f, -0.24f, -0.24f, -0.41f, -0.48f, -0.41f)
                    horizontalLineToRelative(-3.8f)
                    curveToRelative(-0.24f, 0.0f, -0.44f, 0.17f, -0.48f, 0.41f)
                    lineToRelative(-0.36f, 2.51f)
                    curveToRelative(-0.59f, 0.23f, -1.13f, 0.54f, -1.62f, 0.92f)
                    lineToRelative(-2.36f, -0.95f)
                    curveToRelative(-0.22f, -0.08f, -0.46f, 0.01f, -0.58f, 0.21f)
                    lineToRelative(-1.9f, 3.29f)
                    curveToRelative(-0.12f, 0.2f, -0.07f, 0.45f, 0.11f, 0.59f)
                    lineToRelative(2.0f, 1.56f)
                    curveToRelative(-0.05f, 0.3f, -0.08f, 0.61f, -0.08f, 0.93f)
                    reflectiveCurveToRelative(0.03f, 0.63f, 0.08f, 0.93f)
                    lineToRelative(-2.0f, 1.56f)
                    curveToRelative(-0.18f, 0.14f, -0.23f, 0.39f, -0.11f, 0.59f)
                    lineToRelative(1.9f, 3.29f)
                    curveToRelative(0.12f, 0.2f, 0.36f, 0.29f, 0.58f, 0.21f)
                    lineToRelative(2.36f, -0.95f)
                    curveToRelative(0.49f, 0.38f, 1.03f, 0.69f, 1.62f, 0.92f)
                    lineToRelative(0.36f, 2.51f)
                    curveToRelative(0.04f, 0.24f, 0.24f, 0.41f, 0.48f, 0.41f)
                    horizontalLineToRelative(3.8f)
                    curveToRelative(0.24f, 0.0f, 0.44f, -0.17f, 0.48f, -0.41f)
                    lineToRelative(0.36f, -2.51f)
                    curveToRelative(0.59f, -0.23f, 1.13f, -0.54f, 1.62f, -0.92f)
                    lineToRelative(2.36f, 0.95f)
                    curveToRelative(0.22f, 0.08f, 0.46f, -0.01f, 0.58f, -0.21f)
                    lineToRelative(1.9f, -3.29f)
                    curveToRelative(0.12f, -0.2f, 0.07f, -0.45f, -0.11f, -0.59f)
                    lineToRelative(-2.0f, -1.56f)
                    curveTo(19.47f, 12.63f, 19.5f, 12.32f, 19.5f, 12.0f)
                    close()
                    moveTo(12.0f, 15.5f)
                    curveToRelative(-1.93f, 0.0f, -3.5f, -1.57f, -3.5f, -3.5f)
                    reflectiveCurveToRelative(1.57f, -3.5f, 3.5f, -3.5f)
                    reflectiveCurveToRelative(3.5f, 1.57f, 3.5f, 3.5f)
                    reflectiveCurveTo(13.93f, 15.5f, 12.0f, 15.5f)
                    close()
                }
            }.build()
        }

    /**
     * 通知图标 - 更现代的铃铛设计
     */
    val Notifications: ImageVector
        get() {
            return ImageVector.Builder(
                name = "notifications",
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
                    moveTo(12.0f, 22.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    horizontalLineToRelative(-4.0f)
                    curveTo(10.0f, 21.1f, 10.9f, 22.0f, 12.0f, 22.0f)
                    close()
                    moveTo(18.0f, 16.0f)
                    verticalLineToRelative(-5.0f)
                    curveToRelative(0.0f, -3.07f, -1.63f, -5.64f, -4.5f, -6.32f)
                    verticalLineTo(4.0f)
                    curveToRelative(0.0f, -0.83f, -0.67f, -1.5f, -1.5f, -1.5f)
                    reflectiveCurveToRelative(-1.5f, 0.67f, -1.5f, 1.5f)
                    verticalLineToRelative(0.68f)
                    curveTo(7.64f, 5.36f, 6.0f, 7.92f, 6.0f, 11.0f)
                    verticalLineToRelative(5.0f)
                    lineToRelative(-2.0f, 2.0f)
                    verticalLineToRelative(1.0f)
                    horizontalLineToRelative(16.0f)
                    verticalLineToRelative(-1.0f)
                    lineToRelative(-2.0f, -2.0f)
                    close()
                    moveTo(16.0f, 17.0f)
                    horizontalLineTo(8.0f)
                    verticalLineToRelative(-6.0f)
                    curveToRelative(0.0f, -2.48f, 1.51f, -4.5f, 4.0f, -4.5f)
                    reflectiveCurveToRelative(4.0f, 2.02f, 4.0f, 4.5f)
                    verticalLineTo(17.0f)
                    close()
                }
            }.build()
        }

    /**
     * 信息图标 - 更现代的信息设计
     */
    val Info: ImageVector
        get() {
            return ImageVector.Builder(
                name = "info",
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
                    moveTo(12.0f, 17.0f)
                    curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                    verticalLineToRelative(-4.0f)
                    curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                    reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                    verticalLineToRelative(4.0f)
                    curveTo(13.0f, 16.55f, 12.55f, 17.0f, 12.0f, 17.0f)
                    close()
                    moveTo(13.0f, 9.0f)
                    horizontalLineToRelative(-2.0f)
                    verticalLineTo(7.0f)
                    horizontalLineToRelative(2.0f)
                    verticalLineTo(9.0f)
                    close()
                }
            }.build()
        }

    /**
     * 收藏图标 - 更现代的星星设计
     */
    val Star: ImageVector
        get() {
            return ImageVector.Builder(
                name = "star",
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
                    moveTo(12.0f, 17.27f)
                    lineTo(18.18f, 21.0f)
                    lineToRelative(-1.64f, -7.03f)
                    lineTo(22.0f, 9.24f)
                    lineToRelative(-7.19f, -0.61f)
                    lineTo(12.0f, 2.0f)
                    lineTo(9.19f, 8.63f)
                    lineTo(2.0f, 9.24f)
                    lineToRelative(5.46f, 4.73f)
                    lineTo(5.82f, 21.0f)
                    lineTo(12.0f, 17.27f)
                    close()
                }
            }.build()
        }

    /**
     * 删除图标 - 更现代的垃圾桶设计
     */
    val Delete: ImageVector
        get() {
            return ImageVector.Builder(
                name = "delete",
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
                    moveTo(16.0f, 9.0f)
                    verticalLineToRelative(10.0f)
                    horizontalLineTo(8.0f)
                    verticalLineTo(9.0f)
                    horizontalLineToRelative(8.0f)
                    moveToRelative(2.0f, -2.0f)
                    horizontalLineTo(6.0f)
                    verticalLineToRelative(14.0f)
                    curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                    horizontalLineToRelative(8.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    verticalLineTo(7.0f)
                    close()
                    moveTo(15.5f, 4.0f)
                    horizontalLineTo(18.0f)
                    verticalLineTo(2.0f)
                    horizontalLineTo(6.0f)
                    verticalLineTo(4.0f)
                    horizontalLineTo(8.5f)
                    lineToRelative(1.0f, 1.0f)
                    horizontalLineToRelative(5.0f)
                    lineToRelative(1.0f, -1.0f)
                    close()
                }
            }.build()
        }

    /**
     * 退出图标 - 更现代的退出设计
     */
    val ExitToApp: ImageVector
        get() {
            return ImageVector.Builder(
                name = "exit_to_app",
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
                    moveTo(10.09f, 15.59f)
                    lineTo(11.5f, 17.0f)
                    lineTo(16.5f, 12.0f)
                    lineTo(11.5f, 7.0f)
                    lineTo(10.09f, 8.41f)
                    lineTo(12.67f, 11.0f)
                    lineTo(3.0f, 11.0f)
                    lineTo(3.0f, 13.0f)
                    lineTo(12.67f, 13.0f)
                    lineTo(10.09f, 15.59f)
                    close()
                    moveTo(19.0f, 3.0f)
                    lineTo(5.0f, 3.0f)
                    curveToRelative(-1.11f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                    verticalLineToRelative(4.0f)
                    horizontalLineToRelative(2.0f)
                    verticalLineTo(5.0f)
                    horizontalLineToRelative(14.0f)
                    verticalLineToRelative(14.0f)
                    horizontalLineTo(5.0f)
                    verticalLineToRelative(-4.0f)
                    horizontalLineTo(3.0f)
                    verticalLineToRelative(4.0f)
                    curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
                    horizontalLineToRelative(14.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    verticalLineTo(5.0f)
                    curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                    close()
                }
            }.build()
        }

    /**
     * 构建工具图标 - 更现代的扳手设计
     */
    val Build: ImageVector
        get() {
            return ImageVector.Builder(
                name = "build",
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
                    moveTo(22.7f, 19.0f)
                    lineToRelative(-9.1f, -9.1f)
                    curveToRelative(0.9f, -2.3f, 0.4f, -5.0f, -1.5f, -6.9f)
                    curveToRelative(-2.0f, -2.0f, -5.0f, -2.4f, -7.4f, -1.3f)
                    lineTo(9.0f, 6.0f)
                    lineTo(6.0f, 9.0f)
                    lineToRelative(-4.3f, 4.3f)
                    curveToRelative(-1.2f, 2.4f, -0.7f, 5.4f, 1.3f, 7.4f)
                    curveToRelative(1.9f, 1.9f, 4.6f, 2.4f, 6.9f, 1.5f)
                    lineToRelative(9.1f, 9.1f)
                    curveToRelative(0.4f, 0.4f, 1.0f, 0.4f, 1.4f, 0.0f)
                    lineToRelative(2.3f, -2.3f)
                    curveTo(23.1f, 20.0f, 23.1f, 19.3f, 22.7f, 19.0f)
                    close()
                    moveTo(7.0f, 18.0f)
                    curveToRelative(-1.7f, 0.0f, -3.0f, -1.3f, -3.0f, -3.0f)
                    reflectiveCurveToRelative(1.3f, -3.0f, 3.0f, -3.0f)
                    reflectiveCurveToRelative(3.0f, 1.3f, 3.0f, 3.0f)
                    reflectiveCurveTo(8.7f, 18.0f, 7.0f, 18.0f)
                    close()
                }
            }.build()
        }

    /**
     * 喜爱图标 - 更现代的心形设计
     */
    val Favorite: ImageVector
        get() {
            return ImageVector.Builder(
                name = "favorite",
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
                    moveTo(12.0f, 21.35f)
                    lineToRelative(-1.45f, -1.32f)
                    curveTo(5.4f, 15.36f, 2.0f, 12.28f, 2.0f, 8.5f)
                    curveTo(2.0f, 5.42f, 4.42f, 3.0f, 7.5f, 3.0f)
                    curveToRelative(1.74f, 0.0f, 3.41f, 0.81f, 4.5f, 2.09f)
                    curveTo(13.09f, 3.81f, 14.76f, 3.0f, 16.5f, 3.0f)
                    curveTo(19.58f, 3.0f, 22.0f, 5.42f, 22.0f, 8.5f)
                    curveToRelative(0.0f, 3.78f, -3.4f, 6.86f, -8.55f, 11.54f)
                    lineTo(12.0f, 21.35f)
                    close()
                }
            }.build()
        }

    /**
     * 刷新图标 - 更现代的刷新设计
     */
    val Refresh: ImageVector
        get() {
            return ImageVector.Builder(
                name = "refresh",
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
                    moveTo(17.65f, 6.35f)
                    curveTo(16.2f, 4.9f, 14.21f, 4.0f, 12.0f, 4.0f)
                    curveToRelative(-4.42f, 0.0f, -7.99f, 3.58f, -7.99f, 8.0f)
                    reflectiveCurveToRelative(3.57f, 8.0f, 7.99f, 8.0f)
                    curveToRelative(3.73f, 0.0f, 6.84f, -2.55f, 7.73f, -6.0f)
                    horizontalLineToRelative(-2.08f)
                    curveToRelative(-0.82f, 2.33f, -3.04f, 4.0f, -5.65f, 4.0f)
                    curveToRelative(-3.31f, 0.0f, -6.0f, -2.69f, -6.0f, -6.0f)
                    reflectiveCurveToRelative(2.69f, -6.0f, 6.0f, -6.0f)
                    curveToRelative(1.66f, 0.0f, 3.14f, 0.69f, 4.22f, 1.78f)
                    lineTo(13.0f, 11.0f)
                    horizontalLineToRelative(7.0f)
                    verticalLineTo(4.0f)
                    lineToRelative(-2.35f, 2.35f)
                    close()
                }
            }.build()
        }

    /**
     * 人物图标 - 更现代的人物设计
     */
    val Person: ImageVector
        get() {
            return ImageVector.Builder(
                name = "person",
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
                    moveTo(12.0f, 12.0f)
                    curveToRelative(2.21f, 0.0f, 4.0f, -1.79f, 4.0f, -4.0f)
                    reflectiveCurveToRelative(-1.79f, -4.0f, -4.0f, -4.0f)
                    reflectiveCurveToRelative(-4.0f, 1.79f, -4.0f, 4.0f)
                    reflectiveCurveTo(9.79f, 12.0f, 12.0f, 12.0f)
                    close()
                    moveTo(12.0f, 14.0f)
                    curveToRelative(-2.67f, 0.0f, -8.0f, 1.34f, -8.0f, 4.0f)
                    verticalLineToRelative(2.0f)
                    horizontalLineToRelative(16.0f)
                    verticalLineToRelative(-2.0f)
                    curveTo(20.0f, 15.34f, 14.67f, 14.0f, 12.0f, 14.0f)
                    close()
                }
            }.build()
        }

    /**
     * 下载图标 - 更现代的下载设计
     */
    val Download: ImageVector
        get() {
            return ImageVector.Builder(
                name = "download",
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
                    moveTo(5.0f, 20.0f)
                    horizontalLineToRelative(14.0f)
                    verticalLineToRelative(-2.0f)
                    horizontalLineTo(5.0f)
                    verticalLineTo(20.0f)
                    close()
                    moveTo(19.0f, 9.0f)
                    horizontalLineToRelative(-4.0f)
                    verticalLineTo(3.0f)
                    horizontalLineTo(9.0f)
                    verticalLineToRelative(6.0f)
                    horizontalLineTo(5.0f)
                    lineToRelative(7.0f, 7.0f)
                    lineToRelative(7.0f, -7.0f)
                    close()
                }
            }.build()
        }

    /**
     * 暗黑模式图标 - 更现代的暗黑模式设计
     */
    val DarkMode: ImageVector
        get() {
            return ImageVector.Builder(
                name = "dark_mode",
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
                    moveTo(12.0f, 3.0f)
                    curveToRelative(-4.97f, 0.0f, -9.0f, 4.03f, -9.0f, 9.0f)
                    reflectiveCurveToRelative(4.03f, 9.0f, 9.0f, 9.0f)
                    reflectiveCurveToRelative(9.0f, -4.03f, 9.0f, -9.0f)
                    curveToRelative(0.0f, -0.46f, -0.04f, -0.92f, -0.1f, -1.36f)
                    curveToRelative(-0.98f, 1.37f, -2.58f, 2.26f, -4.4f, 2.26f)
                    curveToRelative(-2.98f, 0.0f, -5.4f, -2.42f, -5.4f, -5.4f)
                    curveToRelative(0.0f, -1.81f, 0.89f, -3.42f, 2.26f, -4.4f)
                    curveTo(12.92f, 3.04f, 12.46f, 3.0f, 12.0f, 3.0f)
                    close()
                }
            }.build()
        }

    /**
     * 语言图标 - 更现代的语言设计
     */
    val Language: ImageVector
        get() {
            return ImageVector.Builder(
                name = "language",
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
                    moveTo(12.87f, 15.07f)
                    lineToRelative(-2.54f, -2.51f)
                    lineToRelative(0.03f, -0.03f)
                    curveToRelative(1.74f, -1.94f, 2.98f, -4.17f, 3.71f, -6.53f)
                    horizontalLineTo(17.0f)
                    verticalLineTo(4.0f)
                    horizontalLineTo(10.0f)
                    verticalLineTo(2.0f)
                    horizontalLineTo(8.0f)
                    verticalLineToRelative(2.0f)
                    horizontalLineTo(1.0f)
                    verticalLineToRelative(2.0f)
                    horizontalLineToRelative(11.17f)
                    curveTo(11.5f, 7.92f, 10.44f, 9.75f, 9.0f, 11.35f)
                    curveTo(8.07f, 10.32f, 7.3f, 9.19f, 6.69f, 8.0f)
                    horizontalLineTo(4.69f)
                    curveToRelative(0.76f, 1.61f, 1.79f, 3.11f, 3.09f, 4.45f)
                    lineToRelative(-4.09f, 4.05f)
                    lineToRelative(1.41f, 1.41f)
                    lineToRelative(4.0f, -4.0f)
                    lineToRelative(2.78f, 2.78f)
                    lineToRelative(1.0f, -1.62f)
                    close()
                    moveTo(19.0f, 12.0f)
                    horizontalLineToRelative(-2.0f)
                    verticalLineToRelative(2.0f)
                    horizontalLineTo(15.0f)
                    verticalLineToRelative(2.0f)
                    horizontalLineToRelative(2.0f)
                    verticalLineToRelative(2.0f)
                    horizontalLineToRelative(2.0f)
                    verticalLineToRelative(-2.0f)
                    horizontalLineToRelative(2.0f)
                    verticalLineToRelative(-2.0f)
                    horizontalLineToRelative(-2.0f)
                    verticalLineToRelative(-2.0f)
                    close()
                }
            }.build()
        }

    /**
     * 调色板图标 - 更现代的调色板设计
     */
    val Palette: ImageVector
        get() {
            return ImageVector.Builder(
                name = "palette",
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
                    curveTo(6.49f, 2.0f, 2.0f, 6.49f, 2.0f, 12.0f)
                    reflectiveCurveToRelative(4.49f, 10.0f, 10.0f, 10.0f)
                    curveToRelative(1.38f, 0.0f, 2.5f, -1.12f, 2.5f, -2.5f)
                    curveToRelative(0.0f, -0.61f, -0.23f, -1.2f, -0.64f, -1.67f)
                    curveToRelative(-0.08f, -0.1f, -0.13f, -0.21f, -0.13f, -0.33f)
                    curveToRelative(0.0f, -0.28f, 0.22f, -0.5f, 0.5f, -0.5f)
                    horizontalLineTo(16.0f)
                    curveToRelative(2.21f, 0.0f, 4.0f, -1.79f, 4.0f, -4.0f)
                    curveToRelative(0.0f, -4.42f, -4.03f, -8.0f, -8.0f, -8.0f)
                    close()
                    moveTo(7.5f, 11.0f)
                    curveTo(6.67f, 11.0f, 6.0f, 10.33f, 6.0f, 9.5f)
                    reflectiveCurveTo(6.67f, 8.0f, 7.5f, 8.0f)
                    reflectiveCurveTo(9.0f, 8.67f, 9.0f, 9.5f)
                    reflectiveCurveTo(8.33f, 11.0f, 7.5f, 11.0f)
                    close()
                    moveTo(9.5f, 7.0f)
                    curveTo(8.67f, 7.0f, 8.0f, 6.33f, 8.0f, 5.5f)
                    reflectiveCurveTo(8.67f, 4.0f, 9.5f, 4.0f)
                    reflectiveCurveTo(11.0f, 4.67f, 11.0f, 5.5f)
                    reflectiveCurveTo(10.33f, 7.0f, 9.5f, 7.0f)
                    close()
                    moveTo(14.5f, 7.0f)
                    curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                    reflectiveCurveTo(13.67f, 4.0f, 14.5f, 4.0f)
                    reflectiveCurveTo(16.0f, 4.67f, 16.0f, 5.5f)
                    reflectiveCurveTo(15.33f, 7.0f, 14.5f, 7.0f)
                    close()
                    moveTo(16.5f, 11.0f)
                    curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                    reflectiveCurveTo(15.67f, 8.0f, 16.5f, 8.0f)
                    reflectiveCurveTo(18.0f, 8.67f, 18.0f, 9.5f)
                    reflectiveCurveTo(17.33f, 11.0f, 16.5f, 11.0f)
                    close()
                }
            }.build()
        }

    /**
     * 高清图标 - 更现代的高清设计
     */
    val HighQuality: ImageVector
        get() {
            return ImageVector.Builder(
                name = "high_quality",
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
                    moveTo(19.0f, 4.0f)
                    horizontalLineTo(5.0f)
                    curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                    verticalLineToRelative(12.0f)
                    curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                    horizontalLineToRelative(14.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    verticalLineTo(6.0f)
                    curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                    close()
                    moveTo(9.5f, 16.0f)
                    horizontalLineTo(8.0f)
                    verticalLineTo(8.0f)
                    horizontalLineToRelative(1.5f)
                    verticalLineTo(16.0f)
                    close()
                    moveTo(12.0f, 16.0f)
                    horizontalLineToRelative(-1.5f)
                    verticalLineToRelative(-3.0f)
                    horizontalLineTo(9.0f)
                    verticalLineToRelative(-1.5f)
                    horizontalLineToRelative(1.5f)
                    verticalLineTo(8.0f)
                    horizontalLineTo(12.0f)
                    verticalLineToRelative(1.5f)
                    horizontalLineToRelative(1.5f)
                    verticalLineTo(12.0f)
                    horizontalLineTo(12.0f)
                    verticalLineTo(16.0f)
                    close()
                    moveTo(16.0f, 11.5f)
                    horizontalLineToRelative(1.5f)
                    verticalLineTo(13.0f)
                    horizontalLineTo(16.0f)
                    verticalLineToRelative(1.5f)
                    horizontalLineToRelative(1.5f)
                    verticalLineTo(16.0f)
                    horizontalLineTo(16.0f)
                    verticalLineTo(16.0f)
                    horizontalLineToRelative(-3.0f)
                    verticalLineTo(8.0f)
                    horizontalLineToRelative(3.0f)
                    verticalLineToRelative(1.5f)
                    horizontalLineTo(16.0f)
                    verticalLineTo(11.5f)
                    close()
                }
            }.build()
        }

    /**
     * 版本图标 - 更现代的版本设计
     */
    val Version: ImageVector
        get() {
            return ImageVector.Builder(
                name = "version",
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
                    moveTo(20.0f, 4.0f)
                    horizontalLineTo(4.0f)
                    curveToRelative(-1.11f, 0.0f, -1.99f, 0.89f, -1.99f, 2.0f)
                    lineTo(2.0f, 18.0f)
                    curveToRelative(0.0f, 1.11f, 0.89f, 2.0f, 2.0f, 2.0f)
                    horizontalLineToRelative(16.0f)
                    curveToRelative(1.11f, 0.0f, 2.0f, -0.89f, 2.0f, -2.0f)
                    verticalLineTo(6.0f)
                    curveToRelative(0.0f, -1.11f, -0.89f, -2.0f, -2.0f, -2.0f)
                    close()
                    moveTo(9.0f, 11.5f)
                    curveToRelative(0.0f, 0.83f, -0.67f, 1.5f, -1.5f, 1.5f)
                    reflectiveCurveTo(6.0f, 12.33f, 6.0f, 11.5f)
                    verticalLineTo(10.0f)
                    horizontalLineToRelative(1.0f)
                    verticalLineToRelative(1.5f)
                    curveToRelative(0.0f, 0.28f, 0.22f, 0.5f, 0.5f, 0.5f)
                    reflectiveCurveToRelative(0.5f, -0.22f, 0.5f, -0.5f)
                    verticalLineTo(10.0f)
                    horizontalLineToRelative(1.0f)
                    verticalLineTo(11.5f)
                    close()
                    moveTo(13.0f, 11.5f)
                    curveToRelative(0.0f, 0.83f, -0.67f, 1.5f, -1.5f, 1.5f)
                    reflectiveCurveToRelative(-1.5f, -0.67f, -1.5f, -1.5f)
                    verticalLineTo(10.0f)
                    horizontalLineToRelative(1.0f)
                    verticalLineToRelative(1.5f)
                    curveToRelative(0.0f, 0.28f, 0.22f, 0.5f, 0.5f, 0.5f)
                    reflectiveCurveToRelative(0.5f, -0.22f, 0.5f, -0.5f)
                    verticalLineTo(10.0f)
                    horizontalLineToRelative(1.0f)
                    verticalLineTo(11.5f)
                    close()
                    moveTo(17.0f, 11.5f)
                    curveToRelative(0.0f, 0.83f, -0.67f, 1.5f, -1.5f, 1.5f)
                    reflectiveCurveToRelative(-1.5f, -0.67f, -1.5f, -1.5f)
                    verticalLineTo(10.0f)
                    horizontalLineToRelative(1.0f)
                    verticalLineToRelative(1.5f)
                    curveToRelative(0.0f, 0.28f, 0.22f, 0.5f, 0.5f, 0.5f)
                    reflectiveCurveToRelative(0.5f, -0.22f, 0.5f, -0.5f)
                    verticalLineTo(10.0f)
                    horizontalLineToRelative(1.0f)
                    verticalLineTo(11.5f)
                    close()
                }
            }.build()
        }

    /**
     * 自动更换图标 - 更现代的自动更换设计
     */
    val AutoChange: ImageVector
        get() {
            return ImageVector.Builder(
                name = "auto_change",
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
                    moveTo(16.59f, 7.58f)
                    lineTo(10.0f, 14.17f)
                    lineToRelative(-2.59f, -2.58f)
                    lineTo(6.0f, 13.0f)
                    lineToRelative(4.0f, 4.0f)
                    lineToRelative(8.0f, -8.0f)
                    close()
                }
            }.build()
        }

    /**
     * 频率图标 - 更现代的频率设计
     */
    val Frequency: ImageVector
        get() {
            return ImageVector.Builder(
                name = "frequency",
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
                    moveTo(11.99f, 2.0f)
                    curveTo(6.47f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                    reflectiveCurveToRelative(4.47f, 10.0f, 9.99f, 10.0f)
                    curveTo(17.52f, 22.0f, 22.0f, 17.52f, 22.0f, 12.0f)
                    reflectiveCurveTo(17.52f, 2.0f, 11.99f, 2.0f)
                    close()
                    moveTo(12.0f, 20.0f)
                    curveToRelative(-4.42f, 0.0f, -8.0f, -3.58f, -8.0f, -8.0f)
                    reflectiveCurveToRelative(3.58f, -8.0f, 8.0f, -8.0f)
                    reflectiveCurveToRelative(8.0f, 3.58f, 8.0f, 8.0f)
                    reflectiveCurveTo(16.42f, 20.0f, 12.0f, 20.0f)
                    close()
                    moveTo(12.5f, 7.0f)
                    horizontalLineTo(11.0f)
                    verticalLineToRelative(6.0f)
                    lineToRelative(5.25f, 3.15f)
                    lineToRelative(0.75f, -1.23f)
                    lineToRelative(-4.5f, -2.67f)
                    close()
                }
            }.build()
        }

    /**
     * WiFi图标 - 更现代的WiFi设计
     */
    val Wifi: ImageVector
        get() {
            return ImageVector.Builder(
                name = "wifi",
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
                    moveTo(1.0f, 9.0f)
                    lineToRelative(2.0f, 2.0f)
                    curveToRelative(4.97f, -4.97f, 13.03f, -4.97f, 18.0f, 0.0f)
                    lineToRelative(2.0f, -2.0f)
                    curveTo(16.93f, 2.93f, 7.08f, 2.93f, 1.0f, 9.0f)
                    close()
                    moveTo(9.0f, 17.0f)
                    lineToRelative(3.0f, 3.0f)
                    lineToRelative(3.0f, -3.0f)
                    curveToRelative(-1.65f, -1.66f, -4.34f, -1.66f, -6.0f, 0.0f)
                    close()
                    moveTo(5.0f, 13.0f)
                    lineToRelative(2.0f, 2.0f)
                    curveToRelative(2.76f, -2.76f, 7.24f, -2.76f, 10.0f, 0.0f)
                    lineToRelative(2.0f, -2.0f)
                    curveTo(15.14f, 9.14f, 8.87f, 9.14f, 5.0f, 13.0f)
                    close()
                }
            }.build()
        }

    /**
     * 壁纸目标图标 - 更现代的壁纸目标设计
     */
    val WallpaperTarget: ImageVector
        get() {
            return ImageVector.Builder(
                name = "wallpaper_target",
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
                    moveTo(4.0f, 4.0f)
                    horizontalLineToRelative(7.0f)
                    verticalLineTo(2.0f)
                    horizontalLineTo(4.0f)
                    curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                    verticalLineToRelative(7.0f)
                    horizontalLineToRelative(2.0f)
                    verticalLineTo(4.0f)
                    close()
                    moveTo(10.0f, 13.0f)
                    lineToRelative(-4.0f, 4.0f)
                    verticalLineToRelative(3.0f)
                    horizontalLineToRelative(14.0f)
                    verticalLineToRelative(-3.0f)
                    lineToRelative(-4.0f, -4.0f)
                    horizontalLineTo(10.0f)
                    close()
                    moveTo(17.0f, 4.0f)
                    verticalLineToRelative(7.0f)
                    horizontalLineToRelative(2.0f)
                    verticalLineTo(4.0f)
                    curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                    horizontalLineToRelative(-7.0f)
                    verticalLineToRelative(2.0f)
                    horizontalLineToRelative(7.0f)
                    close()
                }
            }.build()
        }

    /**
     * 皇冠图标 - 高级用户标识
     */
    val Crown: ImageVector
        get() {
            return ImageVector.Builder(
                name = "crown",
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
                    // 皇冠底部
                    moveTo(5.0f, 16.0f)
                    verticalLineTo(19.0f)
                    horizontalLineTo(19.0f)
                    verticalLineTo(16.0f)
                    horizontalLineTo(5.0f)
                    close()

                    // 皇冠主体
                    moveTo(12.0f, 3.0f)
                    lineTo(2.0f, 9.0f)
                    lineTo(5.0f, 16.0f)
                    lineTo(19.0f, 16.0f)
                    lineTo(22.0f, 9.0f)
                    lineTo(12.0f, 3.0f)
                    close()

                    // 左侧宝石
                    moveTo(6.0f, 13.0f)
                    arcTo(1.0f, 1.0f, 0.0f, false, true, 4.0f, 12.0f)
                    arcTo(1.0f, 1.0f, 0.0f, false, true, 6.0f, 11.0f)
                    arcTo(1.0f, 1.0f, 0.0f, false, true, 8.0f, 12.0f)
                    arcTo(1.0f, 1.0f, 0.0f, false, true, 6.0f, 13.0f)
                    close()

                    // 中间宝石
                    moveTo(12.0f, 13.0f)
                    arcTo(1.0f, 1.0f, 0.0f, false, true, 10.0f, 12.0f)
                    arcTo(1.0f, 1.0f, 0.0f, false, true, 12.0f, 11.0f)
                    arcTo(1.0f, 1.0f, 0.0f, false, true, 14.0f, 12.0f)
                    arcTo(1.0f, 1.0f, 0.0f, false, true, 12.0f, 13.0f)
                    close()

                    // 右侧宝石
                    moveTo(18.0f, 13.0f)
                    arcTo(1.0f, 1.0f, 0.0f, false, true, 16.0f, 12.0f)
                    arcTo(1.0f, 1.0f, 0.0f, false, true, 18.0f, 11.0f)
                    arcTo(1.0f, 1.0f, 0.0f, false, true, 20.0f, 12.0f)
                    arcTo(1.0f, 1.0f, 0.0f, false, true, 18.0f, 13.0f)
                    close()
                }
            }.build()
        }

    /**
     * 历史记录图标
     */
    val History: ImageVector
        get() {
            return ImageVector.Builder(
                name = "history",
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
                    // 外圈
                    moveTo(13.0f, 3.0f)
                    arcTo(9.0f, 9.0f, 0.0f, false, false, 4.0f, 12.0f)
                    horizontalLineTo(1.0f)
                    lineTo(4.89f, 15.89f)
                    lineTo(4.96f, 16.03f)
                    lineTo(9.0f, 12.0f)
                    horizontalLineTo(6.0f)
                    arcTo(7.0f, 7.0f, 0.0f, false, true, 13.0f, 5.0f)
                    arcTo(7.0f, 7.0f, 0.0f, false, true, 20.0f, 12.0f)
                    arcTo(7.0f, 7.0f, 0.0f, false, true, 13.0f, 19.0f)
                    arcTo(7.0f, 7.0f, 0.0f, false, true, 7.21f, 15.5f)
                    lineTo(5.79f, 16.92f)
                    arcTo(9.0f, 9.0f, 0.0f, false, false, 13.0f, 21.0f)
                    arcTo(9.0f, 9.0f, 0.0f, false, false, 22.0f, 12.0f)
                    arcTo(9.0f, 9.0f, 0.0f, false, false, 13.0f, 3.0f)
                    close()

                    // 时钟指针
                    moveTo(12.5f, 8.0f)
                    horizontalLineTo(11.0f)
                    verticalLineTo(14.0f)
                    lineTo(15.75f, 16.85f)
                    lineTo(16.5f, 15.62f)
                    lineTo(12.5f, 13.25f)
                    verticalLineTo(8.0f)
                    close()
                }
            }.build()
        }

    /**
     * 钻石图标 - 更现代的钻石设计
     */
    val Diamond: ImageVector
        get() {
            return ImageVector.Builder(
                name = "diamond",
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
                    // 钻石主体
                    moveTo(12.0f, 3.0f)
                    lineTo(2.0f, 9.0f)
                    lineTo(12.0f, 21.0f)
                    lineTo(22.0f, 9.0f)
                    lineTo(12.0f, 3.0f)
                    close()

                    // 钻石内部切面
                    moveTo(12.0f, 7.0f)
                    lineTo(6.0f, 9.0f)
                    lineTo(12.0f, 17.0f)
                    lineTo(18.0f, 9.0f)
                    lineTo(12.0f, 7.0f)
                    close()
                }
            }.build()
        }

    /**
     * 无限下载图标 - 适用于无限下载高清壁纸功能
     */
    val UnlimitedDownload: ImageVector
        get() {
            return ImageVector.Builder(
                name = "unlimited_download",
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
                    // 下载箭头
                    moveTo(12.0f, 16.0f)
                    lineTo(16.0f, 12.0f)
                    horizontalLineTo(13.0f)
                    verticalLineTo(4.0f)
                    horizontalLineTo(11.0f)
                    verticalLineTo(12.0f)
                    horizontalLineTo(8.0f)
                    lineTo(12.0f, 16.0f)
                    close()

                    // 底部线条
                    moveTo(5.0f, 20.0f)
                    horizontalLineTo(19.0f)
                    verticalLineTo(18.0f)
                    horizontalLineTo(5.0f)
                    verticalLineTo(20.0f)
                    close()

                    // 无限符号
                    moveTo(7.0f, 6.0f)
                    curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                    reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                    reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                    reflectiveCurveTo(8.1f, 6.0f, 7.0f, 6.0f)
                    close()

                    moveTo(17.0f, 6.0f)
                    curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                    reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                    reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                    reflectiveCurveTo(18.1f, 6.0f, 17.0f, 6.0f)
                    close()

                    moveTo(7.0f, 9.0f)
                    curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                    reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                    reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                    reflectiveCurveTo(7.55f, 9.0f, 7.0f, 9.0f)
                    close()

                    moveTo(17.0f, 9.0f)
                    curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                    reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                    reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                    reflectiveCurveTo(17.55f, 9.0f, 17.0f, 9.0f)
                    close()

                    moveTo(12.0f, 8.5f)
                    curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                    horizontalLineTo(9.5f)
                    verticalLineTo(8.0f)
                    horizontalLineTo(10.0f)
                    curveToRelative(0.28f, 0.0f, 0.5f, 0.22f, 0.5f, 0.5f)
                    reflectiveCurveToRelative(-0.22f, 0.5f, -0.5f, 0.5f)
                    horizontalLineTo(9.0f)
                    curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                    reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                    horizontalLineTo(10.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                    reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                    horizontalLineTo(9.5f)
                    verticalLineTo(10.0f)
                    horizontalLineTo(10.0f)
                    curveToRelative(0.28f, 0.0f, 0.5f, -0.22f, 0.5f, -0.5f)
                    close()

                    moveTo(14.0f, 8.5f)
                    curveToRelative(0.0f, -0.28f, 0.22f, -0.5f, 0.5f, -0.5f)
                    horizontalLineTo(15.0f)
                    curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                    reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                    horizontalLineTo(14.5f)
                    verticalLineTo(9.0f)
                    horizontalLineTo(15.0f)
                    curveToRelative(0.28f, 0.0f, 0.5f, -0.22f, 0.5f, -0.5f)
                    reflectiveCurveToRelative(-0.22f, -0.5f, -0.5f, -0.5f)
                    horizontalLineTo(14.5f)
                    curveToRelative(-0.28f, 0.0f, -0.5f, 0.22f, -0.5f, 0.5f)
                    reflectiveCurveToRelative(0.22f, 0.5f, 0.5f, 0.5f)
                    horizontalLineTo(15.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                    reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                    horizontalLineTo(14.5f)
                    verticalLineTo(12.0f)
                    horizontalLineTo(15.0f)
                    curveToRelative(0.28f, 0.0f, 0.5f, -0.22f, 0.5f, -0.5f)
                    reflectiveCurveToRelative(-0.22f, -0.5f, -0.5f, -0.5f)
                    horizontalLineTo(14.5f)
                    curveToRelative(-0.28f, 0.0f, -0.5f, 0.22f, -0.5f, 0.5f)
                    reflectiveCurveTo(14.22f, 12.0f, 14.5f, 12.0f)
                    close()
                }
            }.build()
        }

    /**
     * 特效动态壁纸图标 - 适用于特效动态壁纸功能
     */
    val SpecialEffectWallpaper: ImageVector
        get() {
            return ImageVector.Builder(
                name = "special_effect_wallpaper",
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
                    // 手机外框
                    moveTo(17.0f, 1.01f)
                    lineTo(7.0f, 1.0f)
                    curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                    verticalLineToRelative(18.0f)
                    curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                    horizontalLineToRelative(10.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    verticalLineTo(3.0f)
                    curveToRelative(0.0f, -1.1f, -0.9f, -1.99f, -2.0f, -1.99f)
                    close()
                    moveTo(17.0f, 19.0f)
                    horizontalLineTo(7.0f)
                    verticalLineTo(5.0f)
                    horizontalLineToRelative(10.0f)
                    verticalLineTo(19.0f)
                    close()

                    // 动态效果波浪
                    moveTo(8.0f, 6.0f)
                    curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                    reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                    reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                    reflectiveCurveTo(8.0f, 4.9f, 8.0f, 6.0f)
                    close()

                    moveTo(16.0f, 12.0f)
                    curveToRelative(0.0f, 1.1f, -0.9f, 2.0f, -2.0f, 2.0f)
                    reflectiveCurveToRelative(-2.0f, -0.9f, -2.0f, -2.0f)
                    reflectiveCurveToRelative(0.9f, -2.0f, 2.0f, -2.0f)
                    reflectiveCurveTo(16.0f, 10.9f, 16.0f, 12.0f)
                    close()

                    moveTo(8.0f, 18.0f)
                    curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
                    reflectiveCurveToRelative(2.0f, 0.9f, 2.0f, 2.0f)
                    reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                    reflectiveCurveTo(8.0f, 19.1f, 8.0f, 18.0f)
                    close()

                    // 星星特效
                    moveTo(18.0f, 7.0f)
                    lineTo(19.0f, 5.0f)
                    lineTo(20.0f, 7.0f)
                    lineTo(22.0f, 8.0f)
                    lineTo(20.0f, 9.0f)
                    lineTo(19.0f, 11.0f)
                    lineTo(18.0f, 9.0f)
                    lineTo(16.0f, 8.0f)
                    lineTo(18.0f, 7.0f)
                    close()

                    moveTo(4.0f, 14.0f)
                    lineTo(5.0f, 12.0f)
                    lineTo(6.0f, 14.0f)
                    lineTo(8.0f, 15.0f)
                    lineTo(6.0f, 16.0f)
                    lineTo(5.0f, 18.0f)
                    lineTo(4.0f, 16.0f)
                    lineTo(2.0f, 15.0f)
                    lineTo(4.0f, 14.0f)
                    close()
                }
            }.build()
        }

    /**
     * 专属高级壁纸图标 - 适用于专属高级壁纸功能
     */
    val ExclusiveWallpaper: ImageVector
        get() {
            return ImageVector.Builder(
                name = "exclusive_wallpaper",
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
                    // 图片框
                    moveTo(21.0f, 3.0f)
                    horizontalLineTo(3.0f)
                    curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                    verticalLineToRelative(14.0f)
                    curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                    horizontalLineToRelative(18.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    verticalLineTo(5.0f)
                    curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                    close()
                    moveTo(21.0f, 19.0f)
                    horizontalLineTo(3.0f)
                    verticalLineTo(5.0f)
                    horizontalLineToRelative(18.0f)
                    verticalLineTo(19.0f)
                    close()

                    // 皇冠标志
                    moveTo(12.0f, 6.0f)
                    lineTo(9.0f, 9.0f)
                    horizontalLineTo(15.0f)
                    lineTo(12.0f, 6.0f)
                    close()

                    moveTo(8.0f, 10.0f)
                    lineTo(6.0f, 8.0f)
                    lineTo(4.0f, 10.0f)
                    verticalLineTo(12.0f)
                    horizontalLineTo(8.0f)
                    verticalLineTo(10.0f)
                    close()

                    moveTo(20.0f, 10.0f)
                    lineTo(18.0f, 8.0f)
                    lineTo(16.0f, 10.0f)
                    verticalLineTo(12.0f)
                    horizontalLineTo(20.0f)
                    verticalLineTo(10.0f)
                    close()

                    moveTo(4.0f, 15.0f)
                    horizontalLineTo(20.0f)
                    verticalLineTo(13.0f)
                    horizontalLineTo(4.0f)
                    verticalLineTo(15.0f)
                    close()

                    moveTo(8.0f, 18.0f)
                    horizontalLineTo(16.0f)
                    verticalLineTo(16.0f)
                    horizontalLineTo(8.0f)
                    verticalLineTo(18.0f)
                    close()
                }
            }.build()
        }

    /**
     * 高级编辑功能图标 - 适用于高级编辑功能
     */
    val AdvancedEditing: ImageVector
        get() {
            return ImageVector.Builder(
                name = "advanced_editing",
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
                    // 画笔
                    moveTo(3.0f, 17.25f)
                    verticalLineTo(21.0f)
                    horizontalLineTo(6.75f)
                    lineTo(17.81f, 9.94f)
                    lineTo(14.06f, 6.19f)
                    lineTo(3.0f, 17.25f)
                    close()

                    // 调色板
                    moveTo(20.71f, 7.04f)
                    curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                    lineTo(18.37f, 3.29f)
                    curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                    lineTo(15.0f, 5.25f)
                    lineTo(18.75f, 9.0f)
                    lineTo(20.71f, 7.04f)
                    close()

                    // 滑块控制
                    moveTo(3.0f, 10.0f)
                    horizontalLineTo(12.0f)
                    verticalLineTo(12.0f)
                    horizontalLineTo(3.0f)
                    verticalLineTo(10.0f)
                    close()

                    moveTo(5.0f, 6.0f)
                    horizontalLineTo(9.0f)
                    verticalLineTo(8.0f)
                    horizontalLineTo(5.0f)
                    verticalLineTo(6.0f)
                    close()

                    moveTo(7.0f, 2.0f)
                    horizontalLineTo(11.0f)
                    verticalLineTo(4.0f)
                    horizontalLineTo(7.0f)
                    verticalLineTo(2.0f)
                    close()
                }
            }.build()
        }

    /**
     * 自动更换壁纸图标 - 适用于自动更换壁纸功能
     */
    val AutoWallpaperChange: ImageVector
        get() {
            return ImageVector.Builder(
                name = "auto_wallpaper_change",
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
                    // 时钟
                    moveTo(12.0f, 2.0f)
                    curveToRelative(-5.52f, 0.0f, -10.0f, 4.48f, -10.0f, 10.0f)
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

                    // 时钟指针
                    moveTo(12.5f, 7.0f)
                    horizontalLineTo(11.0f)
                    verticalLineToRelative(6.0f)
                    lineToRelative(5.25f, 3.15f)
                    lineToRelative(0.75f, -1.23f)
                    lineToRelative(-4.5f, -2.67f)
                    verticalLineTo(7.0f)
                    close()

                    // 循环箭头
                    moveTo(17.65f, 6.35f)
                    curveToRelative(-1.45f, -1.45f, -3.44f, -2.35f, -5.65f, -2.35f)
                    verticalLineTo(2.0f)
                    lineTo(8.0f, 5.0f)
                    lineToRelative(4.0f, 3.0f)
                    verticalLineTo(6.0f)
                    curveToRelative(1.21f, 0.0f, 2.31f, 0.49f, 3.12f, 1.29f)
                    curveToRelative(0.81f, 0.81f, 1.29f, 1.91f, 1.29f, 3.12f)
                    curveToRelative(0.0f, 2.42f, -1.95f, 4.38f, -4.38f, 4.38f)
                    curveToRelative(-2.42f, 0.0f, -4.38f, -1.95f, -4.38f, -4.38f)
                    horizontalLineTo(6.0f)
                    curveToRelative(0.0f, 3.31f, 2.69f, 6.0f, 6.0f, 6.0f)
                    reflectiveCurveToRelative(6.0f, -2.69f, 6.0f, -6.0f)
                    curveToRelative(0.0f, -1.65f, -0.67f, -3.15f, -1.75f, -4.24f)
                    close()
                }
            }.build()
        }

    /**
     * 壁纸预览图标 - 更现代的壁纸预览设计
     */
    val WallpaperPreview: ImageVector
        get() {
            return ImageVector.Builder(
                name = "wallpaper_preview",
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
                    // 外框
                    moveTo(21.0f, 3.0f)
                    horizontalLineTo(3.0f)
                    curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                    verticalLineToRelative(14.0f)
                    curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                    horizontalLineToRelative(18.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    verticalLineTo(5.0f)
                    curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                    close()

                    // 内框
                    moveTo(21.0f, 19.0f)
                    horizontalLineTo(3.0f)
                    verticalLineTo(5.0f)
                    horizontalLineToRelative(18.0f)
                    verticalLineTo(19.0f)
                    close()

                    // 山
                    moveTo(6.0f, 17.0f)
                    horizontalLineTo(18.0f)
                    lineTo(15.0f, 13.0f)
                    lineTo(12.0f, 16.0f)
                    lineTo(9.0f, 13.0f)
                    lineTo(6.0f, 17.0f)
                    close()

                    // 太阳
                    moveTo(8.0f, 10.0f)
                    arcTo(2.0f, 2.0f, 0.0f, false, true, 6.0f, 8.0f)
                    arcTo(2.0f, 2.0f, 0.0f, false, true, 8.0f, 6.0f)
                    arcTo(2.0f, 2.0f, 0.0f, false, true, 10.0f, 8.0f)
                    arcTo(2.0f, 2.0f, 0.0f, false, true, 8.0f, 10.0f)
                    close()

                    // 眼睛
                    moveTo(12.0f, 8.5f)
                    curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
                    reflectiveCurveToRelative(1.34f, 3.0f, 3.0f, 3.0f)
                    reflectiveCurveToRelative(3.0f, -1.34f, 3.0f, -3.0f)
                    reflectiveCurveTo(13.66f, 8.5f, 12.0f, 8.5f)
                    close()

                    // 眼睛瞳孔
                    moveTo(12.0f, 13.0f)
                    curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                    reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                    reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                    reflectiveCurveTo(12.83f, 13.0f, 12.0f, 13.0f)
                    close()
                }
            }.build()
        }
}
