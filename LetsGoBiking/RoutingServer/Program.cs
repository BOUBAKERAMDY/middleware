using System;
using System.ServiceModel;
using System.ServiceModel.Description;
using System.ServiceModel.Web;
using System.Linq; 

namespace LetsGoBiking.RoutingServer
{
    class Program
    {
        static void Main(string[] args)
        {
            string baseAddress = "http://localhost:8090/MyService";
            WebServiceHost host = null;

            try
            {
                // Create and configure the service host
                host = new WebServiceHost(typeof(ItineraryService), new Uri(baseAddress));
                
                // Add service endpoint
                var endpoint = host.AddServiceEndpoint(
                    typeof(IItineraryService),
                    new WebHttpBinding(),
                    "");

                // Configure web behavior
                var webBehavior = endpoint.EndpointBehaviors
                    .OfType<WebHttpBehavior>()
                    .FirstOrDefault();
                if (webBehavior == null)
                {
                    webBehavior = new WebHttpBehavior();
                    endpoint.EndpointBehaviors.Add(webBehavior);
                }
                
                webBehavior.HelpEnabled = true;
                webBehavior.AutomaticFormatSelectionEnabled = false;
                webBehavior.DefaultOutgoingResponseFormat = WebMessageFormat.Json;

                // Enable metadata
                ServiceMetadataBehavior smb = host.Description.Behaviors.Find<ServiceMetadataBehavior>();
                if (smb == null)
                {
                    smb = new ServiceMetadataBehavior();
                    smb.HttpGetEnabled = true;
                    host.Description.Behaviors.Add(smb);
                }
                else
                {
                    smb.HttpGetEnabled = true;
                }

                // Start the service
                host.Open();
                
                Console.WriteLine("========================================");
                Console.WriteLine("     ROUTING SERVER - WCF REST");
                Console.WriteLine("========================================");
                Console.WriteLine($"Service is running at: {baseAddress}");
                Console.WriteLine();
                Console.WriteLine("Available endpoints:");
                Console.WriteLine($"  GET {baseAddress}/Itinerary?origin=...&destination=...");
                Console.WriteLine($"  GET {baseAddress}/ItineraryByCoords?originLat=...&originLon=...&destLat=...&destLon=...");
                Console.WriteLine();
                Console.WriteLine("Press Enter to stop the service...");
                Console.ReadLine();
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error starting service: {ex.Message}");
                Console.WriteLine($"Stack trace: {ex.StackTrace}");
                Console.ReadLine();
            }
            finally
            {
                host?.Close();
            }
        }
    }
}
