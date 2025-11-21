using System;
using System.ServiceModel;
using LetsGoBiking.Shared;

namespace LetsGoBiking.ProxyCacheServer
{
    [ServiceBehavior(InstanceContextMode = InstanceContextMode.Single)]
    public class ProxyService : IProxyService
    {
        private readonly GenericProxyCache<JCDecauxStations> _cache;
        private const double CACHE_DURATION_SECONDS = 300; // 5 minutes

        public ProxyService()
        {
            _cache = new GenericProxyCache<JCDecauxStations>();
        }

        public StationInfo[] GetStations(string city)
        {
            try
            {
                if (string.IsNullOrWhiteSpace(city))
                {
                    Console.WriteLine("GetStations called with empty city");
                    return new StationInfo[0];
                }

                city = city.ToLower().Trim();
                Console.WriteLine($"GetStations called for city: {city}");

                // Use cache with factory function
                var cacheKey = $"stations_{city}";
                
                var stationData = _cache.Get(
                    cacheKey,
                    () => new JCDecauxStations(city),
                    CACHE_DURATION_SECONDS
                );

                if (stationData?.Stations != null)
                {
                    Console.WriteLine($"Returning {stationData.Stations.Length} stations for {city}");
                    return stationData.Stations;
                }
                
                Console.WriteLine($"No stations found for {city}");
                return new StationInfo[0];
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error in GetStations for {city}: {ex.Message}");
                return new StationInfo[0];
            }
        }
    }
}
