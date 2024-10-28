package com.example.composekotlingraph

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.composekotlingraph.Graph.BoxForGraph
import com.example.composekotlingraph.Graph.BoxForGraphDouble
import com.example.composekotlingraph.data.GraphData
import com.example.composekotlingraph.data.GraphDataData
import com.example.composekotlingraph.data.GraphDataDouble
import com.example.composekotlingraph.ui.theme.ComposeKotlinGraphTheme
import java.lang.Math.random
import kotlin.math.sin


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeKotlinGraphTheme {
                Column {
                    Text("This is a Sample of a Graph")
                    MyGraph(GraphDataData)
                    Text("This is a Sin wave")
                    val dataPoints: List<GraphDataDouble> =
                        generateSinDataPoints(100, 2.0, 1.0) // Your generated data points
                    Log.d("data points", "there are ${dataPoints.size} in the list")
                    MyGraphDouble(dataPoints)
                    Text("this is Random data")
                    val randomPoints: List<GraphDataDouble> = makeRandomPoints()
                    MyGraphDouble(randomPoints)
                    Text("End of Example")
                }
            }
        }
    }
}

fun makeRandomPoints(): List<GraphDataDouble>{
    val dataPoints = mutableListOf<GraphDataDouble>()
    var i = 0
    repeat(100){
        val x = i.toDouble()
        val y = random()*10
        dataPoints.add(GraphDataDouble(x, y))
        i++
    }
    return dataPoints
}

fun generateSinDataPoints(numPoints: Int, amplitude: Double, period: Double): List<GraphDataDouble> {
    val dataPoints = mutableListOf<GraphDataDouble>()
    val step = (2 * Math.PI) / numPoints

    for (i in 0 until numPoints) {
        //decimal x
        //val x = i * step
        val x = i.toDouble()
        val y = amplitude * sin(x * period)
        //Log.d("points", "X: $x, Y: $y")
        dataPoints.add(GraphDataDouble(x, y))
    }

    return dataPoints
}

@Composable
fun MyGraph(listData: List<GraphData>) {
   BoxForGraph( listData )
}

@Composable
fun MyGraphDouble(listData: List<GraphDataDouble>){
    BoxForGraphDouble(listData)
}

@Preview(showBackground = true)
@Composable
fun MyGraphPreview() {
    ComposeKotlinGraphTheme {
        MyGraph(GraphDataData)
    }
}

@Preview(showBackground = true)
@Composable
fun MyGraphDoublePreview(){
    ComposeKotlinGraphTheme{
        val dataPoints: List<GraphDataDouble> =
            generateSinDataPoints(100, 1.0, 1.0) // Your generated data points
        Log.d("data points", "there are ${dataPoints.size} in the list")
        MyGraphDouble(dataPoints)
    }
}