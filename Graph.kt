package com.example.composekotlingraph.Graph

import android.graphics.PointF
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.composekotlingraph.data.GraphData
import com.example.composekotlingraph.data.GraphDataData
import com.example.composekotlingraph.data.GraphDataDouble
import com.example.composekotlingraph.data.bracketInSeconds
import com.example.composekotlingraph.data.numberEntries
import com.example.composekotlingraph.ui.theme.ComposeKotlinGraphTheme
import kotlin.math.roundToInt

@Composable
fun BoxForGraph(listData: List<GraphData>){
    Box(Modifier.size(width = 400.dp, height = 100.dp)){
        Graph(
            listData = listData,
            modifier = Modifier.padding(16.dp)
        )
    }
}
@Composable
fun BoxForGraphDouble(listData: List<GraphDataDouble>){
    Box(Modifier.size(width = 400.dp, height = 100.dp)){
        GraphDouble(
            listData = listData,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun Graph(
    listData: List<GraphData>,
    modifier: Modifier = Modifier,
    waveLineColors: List<Color> = ComposeKotlinGraphTheme.extraColors.heartWave,
    pathBackground: Color = ComposeKotlinGraphTheme.extraColors.heartWaveBackground
) {
    if (waveLineColors.size < 2) {
        throw IllegalArgumentException("waveLineColors requires 2+ colors; $waveLineColors")
    }
    Box(
        modifier
            .fillMaxSize()
            .drawWithCache {
                val paths = generateSmoothPath(listData, size)
                val lineBrush = Brush.verticalGradient(waveLineColors)
                onDrawBehind {
                    drawPath(
                        paths.second,
                        pathBackground,
                        style = Fill
                    )
                    drawPath(
                        paths.first,
                        lineBrush,
                        style = Stroke(2.dp.toPx())
                    )
                }
            }
    )
}

@Composable
fun GraphDouble(
    listData: List<GraphDataDouble>,
    modifier: Modifier = Modifier,
    waveLineColors: List<Color> = ComposeKotlinGraphTheme.extraColors.heartWave,
) {
    if (waveLineColors.size < 2) {
        throw IllegalArgumentException("waveLineColors requires 2+ colors; $waveLineColors")
    }
    Box(
        modifier
            .fillMaxSize(1F)
            .drawWithCache {
                val paths = generatePathForDataPoints(listData, size)
                val lineBrush = Brush.verticalGradient(waveLineColors)
                onDrawBehind {
                    drawPath(
                        paths,
                        lineBrush,
                        style = Stroke(2.dp.toPx()),
                    )


                }
            }
    )
}

fun generatePathForDataPoints(dataPoints: List<GraphDataDouble>, size: Size): Path {
    val path = Path()

    // Calculate scaling factors
    val totalSeconds = dataPoints.size
    Log.d("TotalSeconds", totalSeconds.toString())
    val widthPerSecond = size.width / totalSeconds
    Log.d("widthPerSecond", "Size is: ${size.width} and Width Per s is: ${widthPerSecond.toString()}")
    val maxValue = dataPoints.maxBy { it.y }.y
    val minValue = dataPoints.minBy { it.y }.y
    Log.d("YBounds", "Max Y: $maxValue, Min Y: $minValue")
    val graphTop = maxValue+1//((maxValue + 5) / 10f).roundToInt() * 10
    val graphBottom = minValue -1//(minValue / 10f).toInt() * 10
    Log.d("GraphBounds", "Top Y: $graphTop, Bottom Y: $graphBottom")
    val range = graphTop - graphBottom
    val heightPxPerAmount = size.height / range.toFloat()

    // Iterate through data points and add them to the path
    dataPoints.forEachIndexed { index, point ->
        //Log.d("xPlot", "point.x is: ${point.x}, Final: ${point.x * widthPerSecond} ")
        val x = point.x * widthPerSecond

        val y = size.height - (point.y - graphBottom).toFloat() * heightPxPerAmount

        if (index == 0) {
            path.moveTo(x.toFloat(), y)
        } else {
            path.lineTo(x.toFloat(), y)
        }
    }

    return path
}

sealed class DataPoint {
    object NoMeasurement : DataPoint()
    data class Measurement(
        val averageMeasurementTime: Int,
        val minHeartRate: Int,
        val maxHeartRate: Int,
        val averageHeartRate: Int,
    ) : DataPoint()
}

fun generateSmoothPath(data: List<GraphData>, size: Size): Pair<Path, Path> {
    val path = Path()
    val variancePath = Path()

    val totalSeconds = 60 * 60 * 24
    val widthPerSecond = size.width / totalSeconds
    val maxValue = data.maxBy { it.amount }.amount
    val minValue = data.minBy { it.amount }.amount
    val graphTop = ((maxValue + 5) / 10f).roundToInt() * 10
    val graphBottom = (minValue / 10f).toInt() * 10
    val range = graphTop - graphBottom
    val heightPxPerAmount = size.height / range.toFloat()

    var previousX = 0f
    var previousY = size.height
    var previousMaxX = 0f
    var previousMaxY = size.height

    val groupedMeasurements = (0..numberEntries).map { bracketStart ->
        GraphDataData.filter {
            (bracketStart * bracketInSeconds..(bracketStart + 1) * bracketInSeconds)
                .contains(it.date.toSecondOfDay())
        }
    }.map { heartRates ->
        if (heartRates.isEmpty()) DataPoint.NoMeasurement else
            DataPoint.Measurement(
                averageMeasurementTime = heartRates.map { it.date.toSecondOfDay() }.average()
                    .roundToInt(),
                minHeartRate = heartRates.minBy { it.amount }.amount,
                maxHeartRate = heartRates.maxBy { it.amount }.amount,
                averageHeartRate = heartRates.map { it.amount }.average().roundToInt()
            )
    }

    groupedMeasurements.forEachIndexed { i, dataPoint ->
        if (i == 0 && dataPoint is DataPoint.Measurement) {
            path.moveTo(
                0f,
                size.height - (dataPoint.averageHeartRate - graphBottom).toFloat() *
                        heightPxPerAmount
            )
            variancePath.moveTo(
                0f,
                size.height - (dataPoint.maxHeartRate - graphBottom).toFloat() *
                        heightPxPerAmount
            )
        }

        if (dataPoint is DataPoint.Measurement) {
            val x = dataPoint.averageMeasurementTime * widthPerSecond
            val y = size.height - (dataPoint.averageHeartRate - graphBottom).toFloat() *
                    heightPxPerAmount

            // to do smooth curve graph - we use cubicTo, uncomment section below for non-curve
            val controlPoint1 = PointF((x + previousX) / 2f, previousY)
            val controlPoint2 = PointF((x + previousX) / 2f, y)
            path.cubicTo(
                controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y,
                x, y
            )
            previousX = x
            previousY = y

            val maxX = dataPoint.averageMeasurementTime * widthPerSecond
            val maxY = size.height - (dataPoint.maxHeartRate - graphBottom).toFloat() *
                    heightPxPerAmount
            val maxControlPoint1 = PointF((maxX + previousMaxX) / 2f, previousMaxY)
            val maxControlPoint2 = PointF((maxX + previousMaxX) / 2f, maxY)
            variancePath.cubicTo(
                maxControlPoint1.x, maxControlPoint1.y, maxControlPoint2.x, maxControlPoint2.y,
                maxX, maxY
            )

            previousMaxX = maxX
            previousMaxY = maxY
        }
    }

    var previousMinX = size.width
    var previousMinY = size.height

    groupedMeasurements.reversed().forEachIndexed { index, dataPoint ->
        val i = 47 - index
        if (i == 47 && dataPoint is DataPoint.Measurement) {
            variancePath.moveTo(
                size.width,
                size.height - (dataPoint.minHeartRate - graphBottom).toFloat() *
                        heightPxPerAmount
            )
        }

        if (dataPoint is DataPoint.Measurement) {
            val minX = dataPoint.averageMeasurementTime * widthPerSecond
            val minY = size.height - (dataPoint.minHeartRate - graphBottom).toFloat() *
                    heightPxPerAmount
            val minControlPoint1 = PointF((minX + previousMinX) / 2f, previousMinY)
            val minControlPoint2 = PointF((minX + previousMinX) / 2f, minY)
            variancePath.cubicTo(
                minControlPoint1.x, minControlPoint1.y, minControlPoint2.x, minControlPoint2.y,
                minX, minY
            )

            previousMinX = minX
            previousMinY = minY
        }
    }
    return path to variancePath
}

val HighlightColor = Color.White.copy(alpha = 0.7f)