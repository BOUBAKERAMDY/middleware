using System;
using System.Globalization;
using System.ServiceModel;
using System.ServiceModel.Web;
using System.Threading.Tasks;
using LetsGoBiking.Shared;

namespace LetsGoBiking.RoutingServer
{
    [ServiceBehavior(InstanceContextMode = InstanceContextMode.Single)]
    public class ItineraryService : IItineraryService
    {
        public ItineraryDto GetItinerary(string origin, string destination)
        {
            try
            {
                // Enable CORS
                if (WebOperationContext.Current != null)
                {
                    WebOperationContext.Current.OutgoingResponse.Headers.Add(
                        "Access-Control-Allow-Origin", "*");
                }

                Console.WriteLine($"[GetItinerary] From '{origin}' to '{destination}'");

                // Normalize addresses (ensure "France")
                origin = NormalizeAddress(origin);
                destination = NormalizeAddress(destination);

                // Geocode in parallel
                var originGeoTask = Geocoder.GeocodeAddressAsync(origin);
                var destGeoTask = Geocoder.GeocodeAddressAsync(destination);
                Task.WaitAll(originGeoTask, destGeoTask);

                var (originLat, originLon, originCity) = originGeoTask.Result;
                var (destLat, destLon, destCity) = destGeoTask.Result;

                // If geocoding fails, we cannot compute anything good
                if (originLat == 0 || destLat == 0)
                {
                    Console.WriteLine("[GetItinerary] Geocoding failed, returning simple walking-only note.");
                    return new ItineraryDto
                    {
                        Note = "Could not geocode one or both addresses.",
                        IsWalkingOnly = true
                        // We don't have coordinates here, so front can't draw a line.
                    };
                }

                Console.WriteLine($"[GetItinerary] Origin city: {originCity}, Destination city: {destCity}");

                // Check if both cities are supported by JCDecaux
                bool originSupported = ItineraryPlanner.IsCitySupported(originCity);
                bool destSupported = ItineraryPlanner.IsCitySupported(destCity);

                if (!originSupported || !destSupported)
                {
                    Console.WriteLine("[GetItinerary] One or both cities are not JCDecaux cities → walking-only.");
                    // Walking-only with coordinates (front can draw a line)
                    return ItineraryPlanner.ComputeWalkingOnly(originLat, originLon, destLat, destLon);
                }

                // Get bike stations via proxy
                StationInfo[] originStations = ProxyClient.GetStations(originCity);
                StationInfo[] destStations = ProxyClient.GetStations(destCity);

                Console.WriteLine($"[GetItinerary] {originStations?.Length ?? 0} stations for {originCity}, " +
                                  $"{destStations?.Length ?? 0} stations for {destCity}");

                // Compute final itinerary (mixed walking/biking or walking-only if no stations available)
                var itinerary = ItineraryPlanner.ComputeInterCity(
                    originLat, originLon, originCity,
                    destLat, destLon, destCity,
                    originStations, destStations);

                return itinerary;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[GetItinerary] Error: {ex.Message}");
                return new ItineraryDto
                {
                    Note = $"Error computing itinerary: {ex.Message}",
                    IsWalkingOnly = true
                };
            }
        }

        public ItineraryDto GetItineraryByCoords(string originLat, string originLon,
                                                 string destLat, string destLon)
        {
            try
            {
                // Enable CORS
                if (WebOperationContext.Current != null)
                {
                    WebOperationContext.Current.OutgoingResponse.Headers.Add(
                        "Access-Control-Allow-Origin", "*");
                }

                // Parse coordinates
                double oLat = double.Parse(originLat, CultureInfo.InvariantCulture);
                double oLon = double.Parse(originLon, CultureInfo.InvariantCulture);
                double dLat = double.Parse(destLat, CultureInfo.InvariantCulture);
                double dLon = double.Parse(destLon, CultureInfo.InvariantCulture);

                Console.WriteLine($"[GetItineraryByCoords] From ({oLat},{oLon}) to ({dLat},{dLon})");

                // Reverse geocode to get cities (or some address)
                var originAddress = $"{oLat},{oLon}";
                var destAddress = $"{dLat},{dLon}";

                var originGeoTask = Geocoder.GeocodeAddressAsync(originAddress);
                var destGeoTask = Geocoder.GeocodeAddressAsync(destAddress);
                Task.WaitAll(originGeoTask, destGeoTask);

                var (_, __, originCity) = originGeoTask.Result;
                var (_, ___, destCity) = destGeoTask.Result;

                Console.WriteLine($"[GetItineraryByCoords] Origin city: {originCity}, Destination city: {destCity}");

                // If cities are not supported, walking-only
                if (!ItineraryPlanner.IsCitySupported(originCity) ||
                    !ItineraryPlanner.IsCitySupported(destCity))
                {
                    Console.WriteLine("[GetItineraryByCoords] Unsupported cities → walking-only.");
                    return ItineraryPlanner.ComputeWalkingOnly(oLat, oLon, dLat, dLon);
                }

                // Get stations
                StationInfo[] originStations = ProxyClient.GetStations(originCity);
                StationInfo[] destStations = ProxyClient.GetStations(destCity);

                // Compute itinerary
                var itinerary = ItineraryPlanner.ComputeInterCity(
                    oLat, oLon, originCity,
                    dLat, dLon, destCity,
                    originStations, destStations);

                return itinerary;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[GetItineraryByCoords] Error: {ex.Message}");
                return new ItineraryDto
                {
                    Note = $"Error computing itinerary: {ex.Message}",
                    IsWalkingOnly = true
                };
            }
        }

        private string NormalizeAddress(string address)
        {
            if (string.IsNullOrWhiteSpace(address))
                return address;

            address = address.Trim();
            // If user didn’t mention France, we add it to help Nominatim
            if (!address.ToLower().Contains("france"))
            {
                address += ", France";
            }

            return address;
        }
    }
}
