package com.example.mapster

import android.content.res.XmlResourceParser
import com.mapbox.geojson.Point
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException


// Returns the list of points in the <trk> of the given XML.
//
// Can be empty, but cannot be null
fun getTrack(parser: XmlResourceParser): List<Point> {
    var result = emptyList<Point>()

    parser.require(XmlResourceParser.START_TAG, null, "gpx")
    while (parser.next() != XmlResourceParser.END_TAG) {
        if (parser.eventType != XmlResourceParser.START_TAG) {
            continue
        }

        if (parser.name == "trk") {
            result = readTrk(parser)
        } else {
            skip(parser)
        }
    }
    return result
}

private fun readTrk(parser: XmlResourceParser): List<Point> {
    val result = mutableListOf<Point>()

    parser.require(XmlResourceParser.START_TAG, null, "trk")
    while (parser.next() != XmlResourceParser.END_TAG) {
        if (parser.eventType != XmlResourceParser.START_TAG) {
            continue
        }

        if (parser.name == "trkseg") {
            result.addAll(readTrkSeg(parser))
        } else {
            skip(parser)
        }
    }

    return result
}

private fun readTrkSeg(parser: XmlResourceParser): List<Point> {
    val result = mutableListOf<Point>()

    parser.require(XmlResourceParser.START_TAG, null, "trkseg")
    while (parser.next() != XmlResourceParser.END_TAG) {
        if (parser.eventType != XmlResourceParser.START_TAG) {
            continue
        }

        if (parser.name == "trkpt") {
            result.add(readTrkPt(parser))
        } else {
            skip(parser)
        }
    }

    return result
}

private fun readTrkPt(parser: XmlResourceParser): Point {
    parser.require(XmlResourceParser.START_TAG, null, "trkpt")

    val lat = parser.getAttributeFloatValue(null, "lat", 0.0f)
    val lon = parser.getAttributeFloatValue(null, "lon", 0.0f)

    while (parser.next() != XmlResourceParser.END_TAG || parser.name != "trkpt");

    return Point.fromLngLat(lon.toDouble(), lat.toDouble())
}

@Throws(XmlPullParserException::class, IOException::class)
private fun skip(parser: XmlResourceParser) {
    if (parser.eventType != XmlResourceParser.START_TAG) {
        throw IllegalStateException()
    }
    var depth = 1
    while (depth != 0) {
        when (parser.next()) {
            XmlResourceParser.END_TAG -> depth--
            XmlResourceParser.START_TAG -> depth++
        }
    }
}
