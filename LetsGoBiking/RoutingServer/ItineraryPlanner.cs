using System;
using System.Collections.Generic;
using System.Linq;
using LetsGoBiking.Shared;

namespace LetsGoBiking.RoutingServer
{
    public class ItineraryPlanner
    {
        // Supported JCDecaux cities
        private static readonly string[] SupportedCities = new[]
        {
            "lyon", "paris", "rouen", "toulouse", "nancy", "nantes", "amiens",
            "marseille", "lille", "bruxelles", "valence", "cergy-pontoise",
            "creteil", "luxembourg", "mulhouse", "besancon"
        };

        public static bool IsCitySupported(string city)
        {
            if (string.IsNullOrEmpty(city))
                return false;

            return SupportedCities.Contains(city.ToLower());
        }

        public static ItineraryDto ComputeInterCity(
            double originLat, double originLon, string originCity,
            double destLat, double destLon, string destCity,
            StationInfo[] originStations, StationInfo[] destStations)
        {
            // Create the itinerary object
            var itinerary = new ItineraryDto
            {
                OriginCity = originCity,
                DestinationCity = destCity,
                IsInterCity = !string.Equals(originCity, destCity, StringComparison.OrdinalIgnoreCase),

                // Coordinates needed by the frontend
                OriginLat = originLat,
                OriginLon = originLon,
                DestLat = destLat,
                DestLon = destLon
            };

            // If no stations are available → WALK ONLY
            if (originStations == null || originStations.Length == 0 ||
                destStations == null || destStations.Length == 0)
            {
                return ComputeWalkingOnly(originLat, originLon, destLat, destLon);
            }

            // Find nearest bikeable station at origin
            var startStation = FindNearestAvailableStation(originLat, originLon, originStations, true);
            if (startStation == null)
            {
                return ComputeWalkingOnly(originLat, originLon, destLat, destLon);
            }

            // Find nearest station with stands at destination
            var endStation = FindNearestAvailableStation(destLat, destLon, destStations, false);
            if (endStation == null)
            {
                return ComputeWalkingOnly(originLat, originLon, destLat, destLon);
            }

            // Distances
            var walkToStart = GeoUtils.HaversineDistance(originLat, originLon,
                                                         startStation.Latitude, startStation.Longitude);

            var bikeDistance = GeoUtils.HaversineDistance(startStation.Latitude, startStation.Longitude,
                                                          endStation.Latitude, endStation.Longitude);

            var walkToEnd = GeoUtils.HaversineDistance(endStation.Latitude, endStation.Longitude,
                                                       destLat, destLon);

            // Times
            var walkToStartTime = GeoUtils.WalkingSecondsFromMeters(walkToStart);
            var bikeTime = GeoUtils.BikingSecondsFromMeters(bikeDistance);
            var walkToEndTime = GeoUtils.WalkingSecondsFromMeters(walkToEnd);

            // Populate the DTO for the frontend
            itinerary.StartStation = $"{startStation.Name} ({startStation.Address})";
            itinerary.EndStation = $"{endStation.Name} ({endStation.Address})";

            // CRITICAL: give the full objects so the front can draw
            itinerary.StartStationDetails = startStation;
            itinerary.EndStationDetails = endStation;

            // Values
            itinerary.WalkToStartMeters = Math.Round(walkToStart, 2);
            itinerary.BikeMeters = Math.Round(bikeDistance, 2);
            itinerary.WalkToEndMeters = Math.Round(walkToEnd, 2);

            itinerary.TotalKm = Math.Round((walkToStart + bikeDistance + walkToEnd) / 1000, 2);
            itinerary.TotalSeconds = Math.Round(walkToStartTime + bikeTime + walkToEndTime, 0);

            itinerary.IsWalkingOnly = false;

            // Notes
            if (itinerary.IsInterCity)
            {
                itinerary.Note = $"Inter-city itinerary from {originCity} to {destCity}. " +
                                $"Note: Bike segment assumes continuous biking between cities.";
            }
            else
            {
                itinerary.Note = $"Itinerary within {originCity} using bike-sharing stations.";
            }

            return itinerary;
        }

        private static StationInfo FindNearestAvailableStation(double lat, double lon,
                                                              StationInfo[] stations, bool needBike)
        {
            if (stations == null || stations.Length == 0)
                return null;

            // Filter open stations
            var openStations = stations.Where(s =>
                string.Equals(s.Status, "OPEN", StringComparison.OrdinalIgnoreCase) ||
                string.Equals(s.Status, "AVAILABLE", StringComparison.OrdinalIgnoreCase) ||
                string.IsNullOrEmpty(s.Status)).ToList();

            // Filter bike/stand availability
            var availableStations = openStations
                .Where(s => needBike ? s.AvailableBikes > 0 : s.AvailableStands > 0)
                .OrderBy(s => GeoUtils.HaversineDistance(lat, lon, s.Latitude, s.Longitude))
                .ToList();

            if (availableStations.Any())
            {
                return availableStations.First();
            }

            return null;
        }

        public static ItineraryDto ComputeWalkingOnly(double originLat, double originLon,
                                                      double destLat, double destLon)
        {
            var walkDistance = GeoUtils.HaversineDistance(originLat, originLon, destLat, destLon);
            var walkTime = GeoUtils.WalkingSecondsFromMeters(walkDistance);

            return new ItineraryDto
            {
                IsWalkingOnly = true,

                // Needed coordinates for front-end drawing
                OriginLat = originLat,
                OriginLon = originLon,
                DestLat = destLat,
                DestLon = destLon,

                WalkToStartMeters = Math.Round(walkDistance, 2),
                BikeMeters = 0,
                WalkToEndMeters = 0,

                TotalKm = Math.Round(walkDistance / 1000, 2),
                TotalSeconds = Math.Round(walkTime, 0),

                Note = "Walking-only itinerary (no bike-sharing available)."
            };
        }
    }
}
