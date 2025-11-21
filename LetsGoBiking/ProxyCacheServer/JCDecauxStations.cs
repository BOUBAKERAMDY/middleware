using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Threading.Tasks;
using Newtonsoft.Json;
using LetsGoBiking.Shared;

namespace LetsGoBiking.ProxyCacheServer
{
    public class JCDecauxStations
    {
        private static readonly HttpClient httpClient = new HttpClient();
        private const string API_KEY = "a253b479d84f52d5d4fe84d010aeacb1c4839ea0"; // Replace with your API key
        private const string BASE_URL = "https://api.jcdecaux.com/vls/v3/stations";

        public StationInfo[] Stations { get; private set; }
        public string City { get; private set; }

        public JCDecauxStations(string city)
        {
            City = city;
            Fetch(city);
        }

        private void Fetch(string city)
        {
            try
            {
                var task = FetchAsync(city);
                task.Wait();
                Stations = task.Result;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error fetching stations for {city}: {ex.Message}");
                Stations = new StationInfo[0];
            }
        }

        private async Task<StationInfo[]> FetchAsync(string city)
        {
            try
            {
                string url = $"{BASE_URL}?contract={city}&apiKey={API_KEY}";
                Console.WriteLine($"Fetching stations from JCDecaux API for city: {city}");

                var response = await httpClient.GetStringAsync(url);
                var jcdStations = JsonConvert.DeserializeObject<List<JCDecauxStationDto>>(response);

                if (jcdStations == null)
                {
                    Console.WriteLine($"No stations found for city: {city}");
                    return new StationInfo[0];
                }

                Console.WriteLine($"Found {jcdStations.Count} stations for {city}");

                // Map JCDecaux stations to our StationInfo DTO
                var stations = new List<StationInfo>();
                foreach (var jcd in jcdStations)
                {
                    var station = new StationInfo
                    {
                        Name = jcd.name,
                        Address = jcd.address,
                        Latitude = jcd.position?.latitude ?? 0,
                        Longitude = jcd.position?.longitude ?? 0,
                        AvailableBikes = jcd.totalStands?.availabilities?.bikes ?? 0,
                        AvailableStands = jcd.totalStands?.availabilities?.stands ?? 0,
                        Status = jcd.status,
                        ContractName = jcd.contractName
                    };

                    // Debug first few stations
                    if (stations.Count < 3)
                    {
                        Console.WriteLine($"  Station: {station.Name}");
                        Console.WriteLine($"    Status: {station.Status}");
                        Console.WriteLine($"    Bikes: {station.AvailableBikes}, Stands: {station.AvailableStands}");
                    }

                    stations.Add(station);
                }

                return stations.ToArray();
            }
            catch (HttpRequestException httpEx)
            {
                Console.WriteLine($"HTTP error fetching stations for {city}: {httpEx.Message}");
                return new StationInfo[0];
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error processing stations for {city}: {ex.Message}");
                return new StationInfo[0];
            }
        }
    }

    // DTOs for deserializing JCDecaux API response
    internal class JCDecauxStationDto
    {
        public int number { get; set; }
        public string contractName { get; set; }
        public string name { get; set; }
        public string address { get; set; }
        public JCDecauxPosition position { get; set; }
        public string status { get; set; }
        public JCDecauxTotalStands totalStands { get; set; }
    }

    internal class JCDecauxPosition
    {
        public double latitude { get; set; }
        public double longitude { get; set; }
    }

    internal class JCDecauxTotalStands
    {
        public JCDecauxAvailabilities availabilities { get; set; }
        public int capacity { get; set; }
    }

    internal class JCDecauxAvailabilities
    {
        public int bikes { get; set; }
        public int stands { get; set; }
    }
}