// ========================================
// CONFIGURATION
// ========================================

const API_CONFIG = {
    NOMINATIM_URL: 'https://nominatim.openstreetmap.org/search',
    ROUTING_URL: 'http://localhost:8090/MyService/Itinerary',
    OSRM_BASE_URL: 'https://router.project-osrm.org/route/v1',
    ACTIVEMQ_WS_URL: 'ws://127.0.0.1:61614',   // ← FIXED: Added /stomp
    ACTIVEMQ_TOPIC: '/topic/stationAlerts',
    MAP_CENTER: [45.764043, 4.835659],
    MAP_ZOOM: 13
};

let map, currentRouteLayer, markersLayer;
let searchTimeout;
let selectedOriginIndex = -1;
let selectedDestinationIndex = -1;
let stompClient = null;
let currentPickupStation = null;
let currentCity = null;

// STEP 2: Replace the connectWebSocket function (lines 28-61)
function connectWebSocket() {
    console.log('[ActiveMQ] Attempting connection to:', API_CONFIG.ACTIVEMQ_WS_URL);
    
    try {
        const ws = new WebSocket(API_CONFIG.ACTIVEMQ_WS_URL, ['stomp']);
        stompClient = Stomp.over(ws);
        
        // Disable debug to reduce console spam
        stompClient.debug = null;
        
        // Set heartbeat to keep connection alive
        stompClient.heartbeat.outgoing = 20000; // Send heartbeat every 20 seconds
        stompClient.heartbeat.incoming = 20000; // Expect heartbeat every 20 seconds
        
        stompClient.connect(
            {},  // Empty headers (no auth needed if default config)
            function (frame) {
                console.log('[ActiveMQ] ✓ Connected successfully!');
                updateWSStatus('connected');
                
                stompClient.subscribe(API_CONFIG.ACTIVEMQ_TOPIC, function (message) {
                    console.log('[ActiveMQ] 📨 Message received:', message.body);
                    try {
                        const alert = JSON.parse(message.body);
                        showStationAlert(
                            `Station Alert: ${alert.Message}`,
                            `Station: ${alert.StationName}\nCity: ${alert.City}\nBikes: ${alert.AvailableBikes}\nStands: ${alert.AvailableStands}`
                        );
                    } catch (error) {
                        console.error('[ActiveMQ] Error parsing message:', error);
                    }
                });
                
                console.log('[ActiveMQ] ✓ Subscribed to:', API_CONFIG.ACTIVEMQ_TOPIC);
            },
            function (error) {
                console.error('[ActiveMQ] ✗ Connection failed:', error);
                updateWSStatus('disconnected');
                
                // Retry after 5 seconds
                setTimeout(connectWebSocket, 5000);
            }
        );
        
        // Handle WebSocket close
        ws.onclose = function(event) {
            console.log('[ActiveMQ] WebSocket closed. Code:', event.code);
            updateWSStatus('disconnected');
            setTimeout(connectWebSocket, 5000);
        };
        
    } catch (error) {
        console.error('[ActiveMQ] ✗ Exception:', error);
        updateWSStatus('disconnected');
        setTimeout(connectWebSocket, 5000);
    }
}
// Keep these functions as they are (lines 63-84)
function updateWSStatus(status) {
    const el = document.getElementById('ws-status');
    if (!el) return;
    el.className = `ws-status ${status}`;
    el.querySelector('span').textContent = status === 'connected' ? 'Live Updates' : 'Disconnected';
}

function showStationAlert(title, message) {
    console.log('[Alert] Showing popup');
    document.getElementById('alert-title').textContent = title;
    document.getElementById('alert-message').textContent = message;
    document.getElementById('station-alert-popup').classList.add('show');
    setTimeout(() => closeStationAlert(), 30000);
}

function closeStationAlert() {
    document.getElementById('station-alert-popup').classList.remove('show');
}

function handleRecalculate() {
    console.log('[Recalculate] Recalculating route...');
    
    
    closeStationAlert();
    

    calculateRoute();
}









// ========================================
// FORCE STATION
// ========================================
async function forceStationUnavailable() {
    // Get values from manual inputs OR current pickup station
    const stationId = document.getElementById('test-station-id')?.value ||
        (currentPickupStation ? currentPickupStation.id : null);
    const city = document.getElementById('test-city')?.value ||
        (currentPickupStation ? currentPickupStation.city : null);

    if (!stationId || !city) {
        showTestResult('error', '✗ No station to force. Calculate route first or enter values manually.');
        return;
    }

    const btn = document.getElementById('force-unavailable-btn');
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Forcing...';

    console.log('[Force] Forcing station:', stationId, 'in', city);

    const url = `http://localhost:9000/ProxyServiceRest/ForceStationUnavailable?stationId=${stationId}&city=${city}`;

    try {
        const response = await fetch(url, { method: 'POST' });
        const data = await response.json();

        console.log('[Force] Response:', data);

        if (data.Success) {
            showTestResult('success', `✓ Success! Wait for alert popup...`);
        } else {
            showTestResult('error', `✗ ${data.Message}`);
        }
    } catch (error) {
        console.error('[Force] Error:', error);
        showTestResult('error', `✗ ${error.message}`);
    }

    btn.disabled = false;
    btn.innerHTML = '<i class="fas fa-exclamation-triangle"></i> Force This Station Unavailable';
}

function showTestResult(type, message) {
    const el = document.getElementById('test-result');
    el.className = `test-result ${type} show`;
    el.innerHTML = message;
}

function extractCityFromAddress(address) {
    const cities = ['lyon', 'paris', 'marseille', 'toulouse', 'nice', 'nantes', 'strasbourg'];
    const lower = address.toLowerCase();
    return cities.find(city => lower.includes(city)) || 'lyon';
}

function extractStationId(name) {
    const match = name.match(/^(\d+)/);
    return match ? match[1] : null;
}

function showTestSection() {
    if (!currentPickupStation) return;

    document.getElementById('test-station-name').textContent = `📍 ${currentPickupStation.name}`;
    document.getElementById('test-station-details').innerHTML = `
        <div>City: ${currentPickupStation.city}</div>
        <div>Station ID: ${currentPickupStation.id}</div>
        <div>Available Bikes: ${currentPickupStation.bikes}</div>
    `;
    document.getElementById('test-section').style.display = 'block';
    console.log('[Test] Section shown for station:', currentPickupStation.name);
}

function hideTestSection() {
    document.getElementById('test-section').style.display = 'none';
}

// ========================================
// MAP ICONS
// ========================================
const MapIcons = {
    origin: L.divIcon({
        html: '<div style="background: #10b981; width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; border: 3px solid white; box-shadow: 0 3px 10px rgba(0,0,0,0.3);"><i class="fas fa-location-dot" style="color: white; font-size: 16px;"></i></div>',
        iconSize: [32, 32],
        iconAnchor: [16, 32],
        popupAnchor: [0, -32]
    }),
    destination: L.divIcon({
        html: '<div style="background: #ef4444; width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; border: 3px solid white; box-shadow: 0 3px 10px rgba(0,0,0,0.3);"><i class="fas fa-flag-checkered" style="color: white; font-size: 14px;"></i></div>',
        iconSize: [32, 32],
        iconAnchor: [16, 32],
        popupAnchor: [0, -32]
    }),
    bikeStation: L.divIcon({
        html: '<div style="background: #f59e0b; width: 28px; height: 28px; border-radius: 50%; display: flex; align-items: center; justify-content: center; border: 3px solid white; box-shadow: 0 3px 10px rgba(0,0,0,0.3);"><i class="fas fa-bicycle" style="color: white; font-size: 14px;"></i></div>',
        iconSize: [28, 28],
        iconAnchor: [14, 28],
        popupAnchor: [0, -28]
    })
};

// ========================================
// ROUTING API (OSRM)
// ========================================
async function getWalkingRoute(startLat, startLon, endLat, endLon) {
    try {
        const url = `${API_CONFIG.OSRM_BASE_URL}/foot/${startLon},${startLat};${endLon},${endLat}?overview=full&geometries=geojson`;
        const response = await fetch(url);
        if (!response.ok) throw new Error(`OSRM error: ${response.status}`);
        const data = await response.json();
        if (data.code === 'Ok' && data.routes && data.routes[0]) {
            return data.routes[0].geometry.coordinates.map(coord => [coord[1], coord[0]]);
        }
        return null;
    } catch (error) {
        console.error('Error fetching walking route:', error);
        return null;
    }
}

async function getCyclingRoute(startLat, startLon, endLat, endLon) {
    try {
        const url = `${API_CONFIG.OSRM_BASE_URL}/bike/${startLon},${startLat};${endLon},${endLat}?overview=full&geometries=geojson`;
        const response = await fetch(url);
        if (!response.ok) throw new Error(`OSRM error: ${response.status}`);
        const data = await response.json();
        if (data.code === 'Ok' && data.routes && data.routes[0]) {
            return data.routes[0].geometry.coordinates.map(coord => [coord[1], coord[0]]);
        }
        return null;
    } catch (error) {
        console.error('Error fetching cycling route:', error);
        return null;
    }
}

// ========================================
// MAP INITIALIZATION
// ========================================
function initializeMap() {
    map = L.map('map', {
        center: API_CONFIG.MAP_CENTER,
        zoom: API_CONFIG.MAP_ZOOM,
        zoomControl: false
    });

    L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
        attribution: '© OpenStreetMap contributors © CARTO',
        subdomains: 'abcd',
        maxZoom: 20
    }).addTo(map);

    L.control.zoom({ position: 'bottomleft' }).addTo(map);

    currentRouteLayer = L.layerGroup().addTo(map);
    markersLayer = L.layerGroup().addTo(map);
}

// ========================================
// AUTOCOMPLETE
// ========================================
function setupAutocomplete() {
    const originInput = document.getElementById('origin-input');
    const destinationInput = document.getElementById('destination-input');

    originInput.addEventListener('input', (e) => {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => fetchSuggestions(e.target.value, 'origin'), 300);
    });

    destinationInput.addEventListener('input', (e) => {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => fetchSuggestions(e.target.value, 'destination'), 300);
    });

    originInput.addEventListener('keydown', (e) => handleKeyNavigation(e, 'origin'));
    destinationInput.addEventListener('keydown', (e) => handleKeyNavigation(e, 'destination'));

    document.addEventListener('click', (e) => {
        if (!e.target.closest('.input-group')) {
            closeAllDropdowns();
        }
    });
}

async function fetchSuggestions(query, field) {
    if (query.length < 3) {
        document.getElementById(`${field}-suggestions`).classList.remove('active');
        return;
    }

    try {
        const response = await fetch(
            `${API_CONFIG.NOMINATIM_URL}?q=${encodeURIComponent(query + ', France')}&format=json&addressdetails=1&limit=5`
        );
        const data = await response.json();
        data.sort((a, b) => (b.importance || 0) - (a.importance || 0));
        displaySuggestions(data, field);
    } catch (error) {
        console.error('Error fetching suggestions:', error);
    }
}

function displaySuggestions(suggestions, field) {
    const container = document.getElementById(`${field}-suggestions`);

    if (suggestions.length === 0) {
        container.classList.remove('active');
        return;
    }

    container.innerHTML = suggestions.map((item, index) => {
        const mainText = item.display_name.split(',')[0];
        const subText = item.display_name.split(',').slice(1, 3).join(',');

        return `
            <div class="autocomplete-item" onclick="selectSuggestion('${field}', '${item.display_name.replace(/'/g, "\\'")}')">
                <i class="fas fa-map-marker-alt"></i>
                <div class="autocomplete-text">
                    <div class="autocomplete-main">${mainText}</div>
                    <div class="autocomplete-sub">${subText}</div>
                </div>
            </div>
        `;
    }).join('');

    container.classList.add('active');

    if (field === 'origin') selectedOriginIndex = -1;
    else selectedDestinationIndex = -1;
}

function selectSuggestion(field, value) {
    document.getElementById(`${field}-input`).value = value;
    document.getElementById(`${field}-suggestions`).classList.remove('active');
}

function handleKeyNavigation(event, field) {
    const container = document.getElementById(`${field}-suggestions`);
    const items = container.querySelectorAll('.autocomplete-item');
    let currentIndex = field === 'origin' ? selectedOriginIndex : selectedDestinationIndex;

    if (!container.classList.contains('active')) return;

    switch (event.key) {
        case 'ArrowDown':
            event.preventDefault();
            currentIndex = Math.min(currentIndex + 1, items.length - 1);
            break;
        case 'ArrowUp':
            event.preventDefault();
            currentIndex = Math.max(currentIndex - 1, -1);
            break;
        case 'Enter':
            event.preventDefault();
            if (currentIndex >= 0) {
                items[currentIndex].click();
            } else {
                calculateRoute();
            }
            return;
        case 'Escape':
            container.classList.remove('active');
            return;
        default:
            return;
    }

    items.forEach((item, index) => {
        item.classList.toggle('selected', index === currentIndex);
    });

    if (field === 'origin') selectedOriginIndex = currentIndex;
    else selectedDestinationIndex = currentIndex;
}

function closeAllDropdowns() {
    document.querySelectorAll('.autocomplete-dropdown').forEach(dropdown => {
        dropdown.classList.remove('active');
    });
}

// ========================================
// ROUTE CALCULATION
// ========================================
async function calculateRoute() {
    const origin = document.getElementById('origin-input').value.trim();
    const destination = document.getElementById('destination-input').value.trim();

    if (!origin || !destination) {
        showError('Please enter both origin and destination');
        return;
    }

    showLoading(true);
    hideError();
    hideResults();
    clearMap();

    try {
        const response = await fetch(
            `${API_CONFIG.ROUTING_URL}?origin=${encodeURIComponent(origin)}&destination=${encodeURIComponent(destination)}`
        );

        if (!response.ok) throw new Error('Failed to calculate route');

        const data = await response.json();

        currentCity = extractCityFromAddress(origin);

        if (data.StartStationDetails && !data.IsWalkingOnly) {
            currentPickupStation = {
                id: extractStationId(data.StartStationDetails.Name),
                name: data.StartStationDetails.Name,
                city: currentCity,
                bikes: data.StartStationDetails.AvailableBikes,
                stands: data.StartStationDetails.AvailableStands
            };
            showTestSection();
        } else {
            hideTestSection();
        }

        displayRoute(data);
        await drawRouteOnMap(data);
    } catch (error) {
        console.error('Error calculating route:', error);
        showError('Unable to calculate route. Please check if the server is running.');
    } finally {
        showLoading(false);
    }
}

// ========================================
// ROUTE DISPLAY
// ========================================
function displayRoute(data) {
    const totalKm = (data.TotalKm || 0).toFixed(2);
    const totalMinutes = Math.round((data.TotalSeconds || 0) / 60);

    document.getElementById('total-distance').textContent = `${totalKm} km`;
    document.getElementById('total-time').textContent = `${totalMinutes} min`;

    const detailsContainer = document.getElementById('route-details');
    detailsContainer.innerHTML = '';

    if (data.WalkToStartMeters > 0 && !data.IsWalkingOnly) {
        detailsContainer.appendChild(createSegmentCard('walking', 'Walk to bike station', data.WalkToStartMeters, data.StartStation, data.StartStationDetails));
    }

    if (data.BikeMeters > 0) {
        detailsContainer.appendChild(createSegmentCard('biking', 'Bike ride', data.BikeMeters, data.EndStation, data.EndStationDetails));
    }

    if (data.WalkToEndMeters > 0 && !data.IsWalkingOnly) {
        detailsContainer.appendChild(createSegmentCard('walking', 'Walk to destination', data.WalkToEndMeters));
    }

    if (data.IsWalkingOnly) {
        detailsContainer.appendChild(createSegmentCard('walking', 'Walk to destination', data.WalkToStartMeters));
    }

    showResults();
}

function createSegmentCard(type, title, meters, stationName, stationDetails) {
    const distance = (meters / 1000).toFixed(2);
    const time = Math.round(meters / (type === 'biking' ? 4.4 : 1.4) / 60);
    const icon = type === 'biking' ? 'fa-bicycle' : 'fa-person-walking';

    const card = document.createElement('div');
    card.className = `route-segment ${type}`;

    let stationHtml = '';
    if (stationName && stationDetails) {
        const bikes = stationDetails.AvailableBikes || 0;
        const stands = stationDetails.AvailableStands || 0;
        const bikesClass = bikes > 5 ? 'good' : bikes > 2 ? 'warning' : 'critical';
        const standsClass = stands > 5 ? 'good' : stands > 2 ? 'warning' : 'critical';

        stationHtml = `
            <div class="station-info">
                <div class="station-name">${stationName}</div>
                <div class="station-availability">
                    <div class="availability-item">
                        <i class="fas fa-bicycle"></i>
                        <span class="availability-value ${bikesClass}">${bikes}</span>
                        <span>bikes</span>
                    </div>
                    <div class="availability-item">
                        <i class="fas fa-parking"></i>
                        <span class="availability-value ${standsClass}">${stands}</span>
                        <span>spots</span>
                    </div>
                </div>
            </div>
        `;
    }

    card.innerHTML = `
        <div class="segment-header">
            <div class="segment-icon ${type}">
                <i class="fas ${icon}"></i>
            </div>
            <div class="segment-info">
                <div class="segment-type">${title}</div>
                <div class="segment-details">
                    <span class="segment-detail">
                        <i class="fas fa-route"></i>
                        ${distance} km
                    </span>
                    <span class="segment-detail">
                        <i class="fas fa-clock"></i>
                        ${time} min
                    </span>
                </div>
            </div>
        </div>
        ${stationHtml}
    `;

    return card;
}

// ========================================
// MAP DRAWING
// ========================================
async function drawRouteOnMap(data) {
    clearMap();
    const allCoords = [];

    if (data.OriginLat && data.OriginLon) {
        L.marker([data.OriginLat, data.OriginLon], { icon: MapIcons.origin })
            .bindPopup(`<strong>Origin</strong><br>${data.OriginLat.toFixed(5)}, ${data.OriginLon.toFixed(5)}`)
            .addTo(markersLayer);
        allCoords.push([data.OriginLat, data.OriginLon]);
    }

    if (data.DestLat && data.DestLon) {
        L.marker([data.DestLat, data.DestLon], { icon: MapIcons.destination })
            .bindPopup(`<strong>Destination</strong><br>${data.DestLat.toFixed(5)}, ${data.DestLon.toFixed(5)}`)
            .addTo(markersLayer);
        allCoords.push([data.DestLat, data.DestLon]);
    }

    if (data.IsWalkingOnly) {
        const walkRoute = await getWalkingRoute(data.OriginLat, data.OriginLon, data.DestLat, data.DestLon);
        const coords = walkRoute || [[data.OriginLat, data.OriginLon], [data.DestLat, data.DestLon]];
        L.polyline(coords, { color: '#10b981', weight: 5, opacity: 0.8, dashArray: '10, 10' }).addTo(currentRouteLayer);
    } else {
        if (data.StartStationDetails) {
            L.marker([data.StartStationDetails.Latitude, data.StartStationDetails.Longitude], { icon: MapIcons.bikeStation })
                .bindPopup(`<strong>Pickup</strong><br>${data.StartStationDetails.Name}<br>🚲 ${data.StartStationDetails.AvailableBikes} bikes`)
                .addTo(markersLayer);
            allCoords.push([data.StartStationDetails.Latitude, data.StartStationDetails.Longitude]);
        }

        if (data.EndStationDetails) {
            L.marker([data.EndStationDetails.Latitude, data.EndStationDetails.Longitude], { icon: MapIcons.bikeStation })
                .bindPopup(`<strong>Dropoff</strong><br>${data.EndStationDetails.Name}<br>🅿️ ${data.EndStationDetails.AvailableStands} spots`)
                .addTo(markersLayer);
            allCoords.push([data.EndStationDetails.Latitude, data.EndStationDetails.Longitude]);
        }

        if (data.StartStationDetails && data.WalkToStartMeters > 0) {
            const route = await getWalkingRoute(data.OriginLat, data.OriginLon, data.StartStationDetails.Latitude, data.StartStationDetails.Longitude);
            const coords = route || [[data.OriginLat, data.OriginLon], [data.StartStationDetails.Latitude, data.StartStationDetails.Longitude]];
            L.polyline(coords, { color: '#10b981', weight: 5, opacity: 0.8, dashArray: '10, 10' }).addTo(currentRouteLayer);
        }

        if (data.StartStationDetails && data.EndStationDetails && data.BikeMeters > 0) {
            const route = await getCyclingRoute(data.StartStationDetails.Latitude, data.StartStationDetails.Longitude, data.EndStationDetails.Latitude, data.EndStationDetails.Longitude);
            const coords = route || [[data.StartStationDetails.Latitude, data.StartStationDetails.Longitude], [data.EndStationDetails.Latitude, data.EndStationDetails.Longitude]];
            L.polyline(coords, { color: '#f59e0b', weight: 5, opacity: 0.8 }).addTo(currentRouteLayer);
        }

        if (data.EndStationDetails && data.WalkToEndMeters > 0) {
            const route = await getWalkingRoute(data.EndStationDetails.Latitude, data.EndStationDetails.Longitude, data.DestLat, data.DestLon);
            const coords = route || [[data.EndStationDetails.Latitude, data.EndStationDetails.Longitude], [data.DestLat, data.DestLon]];
            L.polyline(coords, { color: '#10b981', weight: 5, opacity: 0.8, dashArray: '10, 10' }).addTo(currentRouteLayer);
        }
    }

    if (allCoords.length > 0) {
        map.fitBounds(L.latLngBounds(allCoords), { padding: [50, 50] });
    }
}

function clearMap() {
    currentRouteLayer.clearLayers();
    markersLayer.clearLayers();
}

// ========================================
// UI HELPERS
// ========================================
function showLoading(show) {
    const loading = document.getElementById('loading');
    const searchBtn = document.getElementById('search-btn');
    if (show) {
        loading.classList.add('active');
        searchBtn.disabled = true;
    } else {
        loading.classList.remove('active');
        searchBtn.disabled = false;
    }
}

function showError(message) {
    const errorEl = document.getElementById('error');
    const errorText = document.getElementById('error-text');
    errorText.textContent = message;
    errorEl.classList.add('active');
}

function hideError() {
    document.getElementById('error').classList.remove('active');
}

function showResults() {
    document.getElementById('results').classList.add('active');
}

function hideResults() {
    document.getElementById('results').classList.remove('active');
}

function resetMapView() {
    if (currentRouteLayer.getLayers().length > 0) {
        const bounds = L.latLngBounds();
        currentRouteLayer.eachLayer(layer => {
            if (layer.getBounds) bounds.extend(layer.getBounds());
        });
        markersLayer.eachLayer(layer => {
            if (layer.getLatLng) bounds.extend(layer.getLatLng());
        });
        if (bounds.isValid()) {
            map.fitBounds(bounds, { padding: [50, 50] });
        }
    } else {
        map.setView(API_CONFIG.MAP_CENTER, API_CONFIG.MAP_ZOOM);
    }
}

function toggleFullscreen() {
    const mapContainer = document.querySelector('.map-container');
    if (!document.fullscreenElement) {
        mapContainer.requestFullscreen().catch(err => console.error('Fullscreen error:', err));
    } else {
        document.exitFullscreen();
    }
}

// ========================================
// INITIALIZATION
// ========================================
document.addEventListener('DOMContentLoaded', () => {
    console.log('==========================================');
    console.log('[App] Starting Let\'s Go Biking...');
    console.log('==========================================');

    initializeMap();
    console.log('[App] ✓ Map initialized');

    setupAutocomplete();
    console.log('[App] ✓ Autocomplete setup');

    connectWebSocket();

    document.getElementById('search-btn').addEventListener('click', calculateRoute);
    document.getElementById('reset-view-btn').addEventListener('click', resetMapView);
    document.getElementById('fullscreen-btn').addEventListener('click', toggleFullscreen);
    document.getElementById('force-unavailable-btn').addEventListener('click', forceStationUnavailable);

    document.getElementById('origin-input').addEventListener('keypress', (e) => {
        if (e.key === 'Enter' && !document.getElementById('origin-suggestions').classList.contains('active')) {
            calculateRoute();
        }
    });

    document.getElementById('destination-input').addEventListener('keypress', (e) => {
        if (e.key === 'Enter' && !document.getElementById('destination-suggestions').classList.contains('active')) {
            calculateRoute();
        }
    });

    window.addEventListener('resize', () => {
        if (map) map.invalidateSize();
    });

    console.log('==========================================');
    console.log('[App] ✓ Ready! Waiting for alerts...');
    console.log('==========================================');
});