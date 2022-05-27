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

    forChild(parser, "gpx", "trk") { result = readTrk(parser) }

    return result
}

private fun readTrk(parser: XmlResourceParser): List<Point> {
    val result = mutableListOf<Point>()

    forChild(parser, "trk", "trkseg") { result.addAll(readTrkSeg(parser)) }

    return result
}

private fun readTrkSeg(parser: XmlResourceParser): List<Point> {
    val result = mutableListOf<Point>()

    forChild(parser, "trkseg", "trkpt") { result.add(readTrkPt(parser)) }

    return result
}

private fun readTrkPt(parser: XmlResourceParser): Point {
    parser.require(XmlResourceParser.START_TAG, null, "trkpt")

    val lat = parser.getAttributeFloatValue(null, "lat", 0.0f)
    val lon = parser.getAttributeFloatValue(null, "lon", 0.0f)

    while (parser.next() != XmlResourceParser.END_TAG || parser.name != "trkpt");

    return Point.fromLngLat(lon.toDouble(), lat.toDouble())
}

/**
 * Processes a child tag.
 *
 * Asserts that the parser has reached the specified {@code parentTagName} and then applies the
 * given {@code processor} to each of its children matching {@code childTagName}.
 */
private fun forChild(
    parser: XmlResourceParser,
    parentTagName: String,
    childTagName: String,
    processor: () -> Unit
) {
    parser.require(XmlResourceParser.START_TAG, null, parentTagName)
    while (parser.next() != XmlResourceParser.END_TAG) {
        if (parser.eventType != XmlResourceParser.START_TAG) {
            continue
        }

        if (parser.name == childTagName) {
            processor()
        } else {
            skip(parser)
        }
    }
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
