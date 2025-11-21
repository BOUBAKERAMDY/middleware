using System;
using System.Net.Http;
using System.Threading.Tasks;
using Newtonsoft.Json.Linq;

namespace LetsGoBiking.RoutingServer
{
    public class Geocoder
    {
        private static readonly HttpClient httpClient = new HttpClient();
        
        static Geocoder()
        {
            // Add User-Agent header required by Nominatim
            httpClient.DefaultRequestHeaders.Add("User-Agent", "LetsGoBiking/1.0");
        }

        public static async Task<(double lat, double lon, string city)> GeocodeAddressAsync(string address)
        {
            try
            {
                var encodedAddress = Uri.EscapeDataString(address);
                var url = $"https://nominatim.openstreetmap.org/search?q={encodedAddress}&format=json&limit=1&addressdetails=1";
                
                var response = await httpClient.GetStringAsync(url);
                var json = JArray.Parse(response);
                
                if (json.Count > 0)
                {
                    var result = json[0];
                    var lat = result["lat"].Value<double>();
                    var lon = result["lon"].Value<double>();
                    
                    // Extract city from address details
                    var city = ExtractCity(result);
                    
                    return (lat, lon, city);
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Geocoding error: {ex.Message}");
            }
            
            return (0, 0, null);
        }

        private static string ExtractCity(JToken result)
        {
            try
            {
                var addressDetails = result["address"];
                if (addressDetails != null)
                {
                    // Try different fields where city might be stored
                    var city = addressDetails["city"]?.ToString() ??
                              addressDetails["town"]?.ToString() ??
                              addressDetails["municipality"]?.ToString() ??
                              addressDetails["village"]?.ToString();
                    
                    if (!string.IsNullOrEmpty(city))
                    {
                        return city.ToLower();
                    }
                }
                
                // Fallback: try to extract from display_name
                var displayName = result["display_name"]?.ToString();
                if (!string.IsNullOrEmpty(displayName))
                {
                    var parts = displayName.Split(',');
                    if (parts.Length > 2)
                    {
                        // Usually city is the second or third part
                        return parts[parts.Length - 3].Trim().ToLower();
                    }
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error extracting city: {ex.Message}");
            }
            
            return null;
        }
    }
}
