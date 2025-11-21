using System;

namespace LetsGoBiking.RoutingServer
{
    public static class GeoUtils
    {
        private const double EARTH_RADIUS_KM = 6371.0;
        private const double WALK_SPEED_MS = 1.4; // Walking speed: 1.4 m/s
        private const double BIKE_SPEED_MS = 4.4; // Biking speed: 4.4 m/s

        // Calculate distance between two points using Haversine formula
        public static double HaversineDistance(double lat1, double lon1, double lat2, double lon2)
        {
            var dLat = ToRad(lat2 - lat1);
            var dLon = ToRad(lon2 - lon1);
            
            var a = Math.Sin(dLat / 2) * Math.Sin(dLat / 2) +
                    Math.Cos(ToRad(lat1)) * Math.Cos(ToRad(lat2)) *
                    Math.Sin(dLon / 2) * Math.Sin(dLon / 2);
            
            var c = 2 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1 - a));
            
            return EARTH_RADIUS_KM * c * 1000; // Return distance in meters
        }

        // Convert degrees to radians
        private static double ToRad(double degrees)
        {
            return degrees * (Math.PI / 180);
        }

        // Calculate time in seconds from distance in meters for walking
        public static double WalkingSecondsFromMeters(double meters)
        {
            return meters / WALK_SPEED_MS;
        }

        // Calculate time in seconds from distance in meters for biking
        public static double BikingSecondsFromMeters(double meters)
        {
            return meters / BIKE_SPEED_MS;
        }

        // Generic method for calculating time
        public static double SecondsFromMeters(double meters, bool isBiking)
        {
            return isBiking ? BikingSecondsFromMeters(meters) : WalkingSecondsFromMeters(meters);
        }
    }
}
