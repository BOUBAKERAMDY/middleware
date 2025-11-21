# Let's Go Biking - Complete Middleware Project

A complete multi-tier middleware solution for bike route planning in France using JCDecaux bike-sharing stations.

## 🚀 Features

- **Multi-city routing**: Support for all JCDecaux contract cities (Lyon, Paris, Toulouse, etc.)
- **Inter-city itineraries**: Route between different cities (e.g., Lyon → Paris)
- **Smart caching**: Generic caching system using MemoryCache
- **Walking fallback**: Automatic fallback to walking when bike-sharing is unavailable
- **Address autocomplete**: Real-time suggestions using OpenStreetMap Nominatim
- **Self-hosted services**: Both WCF services run as console applications (no IIS required)

## 📁 Project Structure

```
LetsGoBiking/
│
├── RoutingServer/              # WCF REST Service (Port 8090)
│   ├── Program.cs              # Self-hosting configuration
│   ├── IItineraryService.cs    # REST service contract
│   ├── ItineraryService.cs     # Main routing logic
│   ├── ItineraryPlanner.cs     # Route calculation algorithms
│   ├── Geocoder.cs            # Address to coordinates conversion
│   ├── GeoUtils.cs            # Geographic calculations (Haversine)
│   ├── ProxyClient.cs         # SOAP client for proxy server
│   └── ProxyContracts.cs      # Shared DTOs
│
├── ProxyCacheServer/          # WCF SOAP Service (Port 9000)
│   ├── Program.cs             # Self-hosting configuration
│   ├── IProxyService.cs       # SOAP service contract
│   ├── ProxyService.cs        # Service implementation
│   ├── GenericProxyCache.cs   # Generic caching system
│   ├── JCDecauxStations.cs    # JCDecaux API client
│   └── ProxyContracts.cs      # Shared DTOs
│
├── FrontendClient/            # HTML/JS Client
│   └── index.html             # Complete web interface
│
└── Shared/                    # Shared contracts (source)
    └── ProxyContracts.cs      # Original DTO definitions
```

## 🔧 Prerequisites

- .NET Framework 4.7.2 or higher
- Visual Studio 2019 or later (recommended)
- NuGet packages will be automatically restored

## 📦 Dependencies

Both servers require:
- `Newtonsoft.Json` (13.0.3)
- `System.Runtime.Caching` (4.7.0)

## 🚀 How to Build and Run

### Option 1: Using Visual Studio

1. Open `LetsGoBiking.sln` in Visual Studio
2. Right-click solution → "Restore NuGet Packages"
3. Build solution (Ctrl+Shift+B)
4. Start both projects:
   - Right-click `ProxyCacheServer` → Debug → Start New Instance
   - Right-click `RoutingServer` → Debug → Start New Instance
5. Open `FrontendClient/index.html` in a web browser

### Option 2: Using Command Line

```bash
# Navigate to solution directory
cd LetsGoBiking

# Restore NuGet packages
nuget restore LetsGoBiking.sln

# Build the solution
msbuild LetsGoBiking.sln /p:Configuration=Release

# Run Proxy Cache Server (in first terminal)
cd ProxyCacheServer\bin\Release
ProxyCacheServer.exe

# Run Routing Server (in second terminal)
cd RoutingServer\bin\Release
RoutingServer.exe

# Open the frontend in a browser
start FrontendClient\index.html
```

## 🌐 Service Endpoints

### Proxy Cache Server (SOAP)
- **URL**: http://localhost:9000/ProxyService
- **WSDL**: http://localhost:9000/ProxyService?wsdl
- **Operation**: `GetStations(string city)`

### Routing Server (REST)
- **Base URL**: http://localhost:8090/MyService
- **Endpoints**:
  - `GET /Itinerary?origin={origin}&destination={destination}`
  - `GET /ItineraryByCoords?originLat={lat}&originLon={lon}&destLat={lat}&destLon={lon}`

## 🗺️ Supported Cities

All JCDecaux contract cities including:
- Lyon
- Paris
- Toulouse
- Marseille
- Lille
- Rouen
- Nancy
- Nantes
- Amiens
- And more...

## 💡 Usage Examples

### Basic Usage
1. Enter "Place Bellecour, Lyon" as origin
2. Enter "Tour Eiffel, Paris" as destination
3. Click "Calculate Route"
4. View detailed itinerary with walking and biking segments

### Inter-city Routing
The system automatically detects when routing between different cities and provides appropriate instructions.

### Walking Fallback
If no bike stations are available or the city is not supported, the system automatically provides a walking-only route.

## ⚙️ Configuration

### JCDecaux API Key
Update the API key in `ProxyCacheServer/JCDecauxStations.cs`:
```csharp
private const string API_KEY = "your-api-key-here";
```

### Cache Duration
Modify cache duration in `ProxyCacheServer/ProxyService.cs`:
```csharp
private const double CACHE_DURATION_SECONDS = 300; // 5 minutes default
```

### Speed Settings
Adjust walking/biking speeds in `RoutingServer/GeoUtils.cs`:
```csharp
private const double WALK_SPEED_MS = 1.4; // 1.4 m/s
private const double BIKE_SPEED_MS = 4.4; // 4.4 m/s
```

## 🐛 Troubleshooting

### "Access Denied" when starting services
- Run Visual Studio or command prompt as Administrator
- Or change ports in Program.cs files

### No stations returned
- Verify JCDecaux API key is valid
- Check city name is correct (lowercase)
- Ensure internet connectivity

### CORS errors in browser
- Routing Server includes CORS headers automatically
- Check that service is running on correct port (8090)

## 📊 Architecture Flow

1. **User** enters addresses in web interface
2. **Frontend** provides autocomplete suggestions via Nominatim
3. **Frontend** sends request to **Routing Server** (REST)
4. **Routing Server**:
   - Geocodes addresses using Nominatim
   - Extracts city names
   - Calls **Proxy Cache Server** for station data
5. **Proxy Cache Server**:
   - Checks cache for station data
   - If miss, fetches from JCDecaux API
   - Returns stations array
6. **Routing Server**:
   - Calculates optimal route
   - Returns JSON itinerary
7. **Frontend** displays formatted results

## 📝 License

Educational project for Middleware course - "Let's Go Biking"

## 🤝 Credits

- JCDecaux API for bike-sharing data
- OpenStreetMap Nominatim for geocoding
- .NET Framework WCF for service implementation
