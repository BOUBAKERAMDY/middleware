using System;
using System.ServiceModel;
using System.ServiceModel.Description;

namespace LetsGoBiking.ProxyCacheServer
{
    class Program
    {
        static void Main(string[] args)
        {
            string baseAddress = "http://localhost:9000/ProxyService";
            ServiceHost host = null;

            try
            {
                // Create service host
                host = new ServiceHost(typeof(ProxyService), new Uri(baseAddress));

                // Create binding with increased message size
                var binding = new BasicHttpBinding();
                binding.MaxReceivedMessageSize = 65536000; // 64 MB
                binding.MaxBufferSize = 65536000;
                binding.ReaderQuotas.MaxDepth = 32;
                binding.ReaderQuotas.MaxStringContentLength = 8192000;
                binding.ReaderQuotas.MaxArrayLength = 16384000;
                binding.ReaderQuotas.MaxBytesPerRead = 4096000;
                binding.ReaderQuotas.MaxNameTableCharCount = 16384;

                // Add service endpoint
                host.AddServiceEndpoint(
                    typeof(IProxyService),
                    binding,
                    "");

                // Enable metadata
                ServiceMetadataBehavior smb = host.Description.Behaviors.Find<ServiceMetadataBehavior>();
                if (smb == null)
                {
                    smb = new ServiceMetadataBehavior
                    {
                        HttpGetEnabled = true,
                        HttpGetUrl = new Uri(baseAddress)
                    };
                    host.Description.Behaviors.Add(smb);
                }
                else
                {
                    smb.HttpGetEnabled = true;
                    smb.HttpGetUrl = new Uri(baseAddress);
                }

                // Start the service
                host.Open();

                Console.WriteLine("========================================");
                Console.WriteLine("    PROXY CACHE SERVER - WCF SOAP");
                Console.WriteLine("========================================");
                Console.WriteLine($"Service is running at: {baseAddress}");
                Console.WriteLine($"WSDL available at: {baseAddress}?wsdl");
                Console.WriteLine();
                Console.WriteLine("Available operations:");
                Console.WriteLine("  - GetStations(city)");
                Console.WriteLine();
                Console.WriteLine("Supported cities:");
                Console.WriteLine("  lyon, paris, rouen, toulouse, nancy,");
                Console.WriteLine("  nantes, amiens, marseille, lille, etc.");
                Console.WriteLine();
                Console.WriteLine("Cache duration: 5 minutes");
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