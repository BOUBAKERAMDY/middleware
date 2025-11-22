using LetsGoBiking.Shared;
using System;
using System.Linq;
using System.ServiceModel;
using System.ServiceModel.Web;

namespace LetsGoBiking.ProxyCacheServer
{
    [ServiceBehavior(InstanceContextMode = InstanceContextMode.Single)]
    public class ProxyService : IProxyServiceExtended
    {
        private readonly GenericProxyCache<JCDecauxStations> _cache;
        private const double CACHE_DURATION_SECONDS = 300;

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

                var stationData = _cache.Get(
                    $"stations_{city}",
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

        public ForceStationResponse ForceStationUnavailable(string stationId, string city)
        {
            try
            {
                EnableCORS();
                Console.WriteLine($"[ForceStation] stationId={stationId}, city={city}");

                if (string.IsNullOrWhiteSpace(stationId) || string.IsNullOrWhiteSpace(city))
                    return CreateErrorResponse("Missing parameters");

                city = city.ToLower().Trim();
                if (!int.TryParse(stationId, out int stationIdInt))
                    return CreateErrorResponse("Invalid stationId");

                var stationData = _cache.Get($"stations_{city}");
                if (stationData?.Stations == null || stationData.Stations.Length == 0)
                    return CreateErrorResponse("No station data. Call GetStations first.");

                var station = FindStation(stationData.Stations, stationIdInt);
                if (station == null)
                    return CreateErrorResponse($"Station {stationIdInt} not found");

                int originalBikes = station.AvailableBikes;
                station.AvailableBikes = 0;
                station.Status = "UNAVAILABLE";

                Console.WriteLine($"[ForceStation] {station.Name}: {originalBikes} → 0 bikes");

                bool alertPublished = PublishAlert(stationIdInt, city, station);

                return new ForceStationResponse
                {
                    Success = true,
                    Message = "Station forced unavailable",
                    StationName = station.Name,
                    StationId = stationIdInt,
                    City = city,
                    PreviousBikes = originalBikes,
                    CurrentBikes = 0,
                    AlertPublished = alertPublished
                };
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[ForceStation] Error: {ex.Message}");
                return CreateErrorResponse(ex.Message);
            }
        }

        public TestResponse TestActiveMQ()
        {
            EnableCORS();
            try
            {
                bool connected = ActiveMQProducerService.Instance.TestConnection();
                return new TestResponse
                {
                    Connected = connected,
                    Message = connected ? "ActiveMQ connection OK" : "ActiveMQ not available"
                };
            }
            catch (Exception ex)
            {
                return new TestResponse { Connected = false, Message = ex.Message };
            }
        }

        private bool PublishAlert(int stationId, string city, StationInfo station)
        {
            try
            {
                var alert = new StationAlertMessage
                {
                    StationId = stationId,
                    City = city,
                    StationName = station.Name,
                    Latitude = station.Latitude,
                    Longitude = station.Longitude,
                    AvailableBikes = 0,
                    AvailableStands = station.AvailableStands,
                    Message = "NO_BIKES"
                };

                ActiveMQProducerService.Instance.PublishStationAlert(alert);
                return true;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[ActiveMQ] Failed to publish: {ex.Message}");
                return false;
            }
        }

        private StationInfo FindStation(StationInfo[] stations, int id)
        {
            return stations.FirstOrDefault(s => ExtractStationNumber(s.Name) == id) ??
                   stations.FirstOrDefault(s => s.Name.Contains(id.ToString()));
        }

        private int ExtractStationNumber(string name)
        {
            if (string.IsNullOrEmpty(name)) return -1;

            foreach (var part in name.Split('-', ' ', '_'))
            {
                if (int.TryParse(part.Trim(), out int num))
                    return num;
            }
            return -1;
        }

        private ForceStationResponse CreateErrorResponse(string message)
        {
            return new ForceStationResponse { Success = false, Message = message };
        }

        private void EnableCORS()
        {
            if (WebOperationContext.Current != null)
            {
                var ctx = WebOperationContext.Current.OutgoingResponse;
                ctx.Headers.Add("Access-Control-Allow-Origin", "*");
                ctx.Headers.Add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
                ctx.Headers.Add("Access-Control-Allow-Headers", "Content-Type");
            }
        }
    }
}
