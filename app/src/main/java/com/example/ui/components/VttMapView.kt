package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.VTTBlueDark
import java.util.Locale

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VttMapView(
    pickupLat: Double = 23.2599,
    pickupLng: Double = 77.4126,
    dropLat: Double = 22.7196,
    dropLng: Double = 75.8577,
    driverLat: Double? = null,
    driverLng: Double? = null,
    showRoute: Boolean = true,
    driverName: String? = null,
    etaMins: Int? = null,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isMapLoaded by remember { mutableStateOf(false) }

    fun fmt(valNum: Double?): String {
        return if (valNum != null && !valNum.isNaN()) {
            String.format(Locale.US, "%.6f", valNum)
        } else {
            "null"
        }
    }

    // Leaflet + OpenStreetMap + OSRM HTML Page
    val leafletHtml = remember {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" crossorigin="" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" crossorigin=""></script>
            <style>
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                }
                html, body {
                    height: 100%;
                    width: 100%;
                    background-color: #cbd5e1;
                    overflow: hidden;
                }
                #map {
                    height: 100%;
                    width: 100%;
                    min-height: 280px;
                    background-color: #cbd5e1;
                    position: absolute;
                    top: 0;
                    bottom: 0;
                    left: 0;
                    right: 0;
                }
                .leaflet-control-attribution {
                    font-size: 9px !important;
                    background: rgba(255,255,255,0.85) !important;
                    padding: 2px 6px !important;
                    border-radius: 4px;
                }
                .custom-div-icon {
                    background: none;
                    border: none;
                }
                .marker-pin-pickup {
                    background: #10B981;
                    width: 30px;
                    height: 30px;
                    border-radius: 50% 50% 50% 0;
                    transform: rotate(-45deg);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    box-shadow: 0 4px 8px rgba(0,0,0,0.35);
                    border: 2px solid #ffffff;
                }
                .marker-pin-drop {
                    background: #EF4444;
                    width: 30px;
                    height: 30px;
                    border-radius: 50% 50% 50% 0;
                    transform: rotate(-45deg);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    box-shadow: 0 4px 8px rgba(0,0,0,0.35);
                    border: 2px solid #ffffff;
                }
                .marker-pin-driver {
                    background: #1E40AF;
                    width: 34px;
                    height: 34px;
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    box-shadow: 0 4px 12px rgba(30,64,175,0.5);
                    border: 3px solid #ffffff;
                    font-size: 16px;
                }
                .pin-inner {
                    transform: rotate(45deg);
                    color: white;
                    font-weight: bold;
                    font-size: 12px;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = null;
                var pickupMarker = null;
                var dropMarker = null;
                var driverMarker = null;
                var routePolyline = null;

                function initMap(pLat, pLng, dLat, dLng, drvLat, drvLng) {
                    try {
                        if (map) {
                            map.remove();
                            map = null;
                        }

                        map = L.map('map', {
                            zoomControl: false,
                            attributionControl: true
                        }).setView([pLat, pLng], 13);

                        // CartoDB Voyager / Positron tile provider (reliable, non-blocking OpenStreetMap tiles)
                        var tileUrl = 'https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png';
                        var tileLayer = L.tileLayer(tileUrl, {
                            maxZoom: 19,
                            subdomains: 'abcd',
                            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> &copy; <a href="https://carto.com/">CARTO</a>'
                        });

                        tileLayer.on('tileerror', function(error, tile) {
                            console.warn('CartoDB tile error, retrying standard OSM...');
                            L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                                maxZoom: 19,
                                attribution: '&copy; OpenStreetMap contributors'
                            }).addTo(map);
                        });

                        tileLayer.addTo(map);

                        L.control.zoom({ position: 'bottomright' }).addTo(map);

                        setTimeout(function() {
                            if (map) map.invalidateSize();
                        }, 200);
                        setTimeout(function() {
                            if (map) map.invalidateSize();
                        }, 600);

                        updateMapLocations(pLat, pLng, dLat, dLng, drvLat, drvLng);
                    } catch (err) {
                        console.error("Leaflet init err:", err);
                    }
                }

                function updateMapLocations(pLat, pLng, dLat, dLng, drvLat, drvLng) {
                    try {
                        if (!map) return;

                        var pickupIcon = L.divIcon({
                            className: 'custom-div-icon',
                            html: '<div class="marker-pin-pickup"><div class="pin-inner">P</div></div>',
                            iconSize: [30, 42],
                            iconAnchor: [15, 42]
                        });
                        if (pickupMarker) {
                            pickupMarker.setLatLng([pLat, pLng]);
                        } else {
                            pickupMarker = L.marker([pLat, pLng], { icon: pickupIcon }).addTo(map)
                                .bindPopup('<b>Pickup Location</b>');
                        }

                        var dropIcon = L.divIcon({
                            className: 'custom-div-icon',
                            html: '<div class="marker-pin-drop"><div class="pin-inner">D</div></div>',
                            iconSize: [30, 42],
                            iconAnchor: [15, 42]
                        });
                        if (dropMarker) {
                            dropMarker.setLatLng([dLat, dLng]);
                        } else {
                            dropMarker = L.marker([dLat, dLng], { icon: dropIcon }).addTo(map)
                                .bindPopup('<b>Drop Location</b>');
                        }

                        if (drvLat && drvLng && !isNaN(drvLat) && !isNaN(drvLng)) {
                            var driverIcon = L.divIcon({
                                className: 'custom-div-icon',
                                html: '<div class="marker-pin-driver">🚗</div>',
                                iconSize: [34, 34],
                                iconAnchor: [17, 17]
                            });
                            if (driverMarker) {
                                driverMarker.setLatLng([drvLat, drvLng]);
                            } else {
                                driverMarker = L.marker([drvLat, drvLng], { icon: driverIcon }).addTo(map)
                                    .bindPopup('<b>VTT Cab Driver (Live GPS)</b>');
                            }
                        }

                        fetchRoute(pLat, pLng, dLat, dLng);

                        var points = [[pLat, pLng], [dLat, dLng]];
                        if (drvLat && drvLng && !isNaN(drvLat) && !isNaN(drvLng)) {
                            points.push([drvLat, drvLng]);
                        }
                        map.fitBounds(L.latLngBounds(points), { padding: [40, 40] });
                        map.invalidateSize();
                    } catch (err) {
                        console.error("Update map err:", err);
                    }
                }

                function fetchRoute(pLat, pLng, dLat, dLng) {
                    var osrmUrl = 'https://router.project-osrm.org/route/v1/driving/' + pLng + ',' + pLat + ';' + dLng + ',' + dLat + '?overview=full&geometries=geojson';
                    
                    fetch(osrmUrl)
                        .then(function(res) { return res.json(); })
                        .then(function(data) {
                            if (data.routes && data.routes.length > 0) {
                                var route = data.routes[0];
                                var coords = route.geometry.coordinates.map(function(c) {
                                    return [c[1], c[0]];
                                });

                                if (routePolyline) {
                                    map.removeLayer(routePolyline);
                                }

                                routePolyline = L.polyline(coords, {
                                    color: '#2563EB',
                                    weight: 5,
                                    opacity: 0.85,
                                    lineJoin: 'round'
                                }).addTo(map);
                            } else {
                                drawFallbackLine(pLat, pLng, dLat, dLng);
                            }
                        })
                        .catch(function(err) {
                            drawFallbackLine(pLat, pLng, dLat, dLng);
                        });
                }

                function drawFallbackLine(pLat, pLng, dLat, dLng) {
                    if (routePolyline) {
                        map.removeLayer(routePolyline);
                    }
                    routePolyline = L.polyline([[pLat, pLng], [dLat, dLng]], {
                        color: '#2563EB',
                        weight: 4,
                        dashArray: '8, 8',
                        opacity: 0.8
                    }).addTo(map);
                }
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    LaunchedEffect(pickupLat, pickupLng, dropLat, dropLng, driverLat, driverLng, isMapLoaded) {
        if (isMapLoaded && webViewInstance != null) {
            val js = "javascript:updateMapLocations(${fmt(pickupLat)}, ${fmt(pickupLng)}, ${fmt(dropLat)}, ${fmt(dropLng)}, ${fmt(driverLat)}, ${fmt(driverLng)});"
            webViewInstance?.evaluateJavascript(js, null)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        @Suppress("DEPRECATION")
                        databaseEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        allowFileAccess = true
                        allowContentAccess = true
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 VTTCabs/1.0"
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isMapLoaded = true
                            val initJs = "javascript:initMap(${fmt(pickupLat)}, ${fmt(pickupLng)}, ${fmt(dropLat)}, ${fmt(dropLng)}, ${fmt(driverLat)}, ${fmt(driverLng)});"
                            view?.evaluateJavascript(initJs, null)
                        }
                    }
                    loadDataWithBaseURL("https://openstreetmap.org", leafletHtml, "text/html", "UTF-8", null)
                    webViewInstance = this
                }
            },
            update = { webView ->
                webViewInstance = webView
            }
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            shape = RoundedCornerShape(20.dp),
            color = VTTBlueDark.copy(alpha = 0.9f)
        ) {
            Text(
                text = "🗺️ OpenStreetMap + OSRM Live Route",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}
