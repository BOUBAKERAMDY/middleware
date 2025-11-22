using System;
using System.ServiceModel;
using System.ServiceModel.Description;
using System.ServiceModel.Web;

namespace LetsGoBiking.ProxyCacheServer
{
    class Program
    {
        static void Main(string[] args)
        {
            string soapAddress = "http://localhost:9000/ProxyService";
            string restAddress = "http://localhost:9000/ProxyServiceRest";
            ServiceHost soapHost = null;
            WebServiceHost restHost = null;

            try
            {
                Console.WriteLine("????????????????????????????????????????????????????");
                Console.WriteLine("?  PROXY CACHE SERVER - SOAP + REST + ActiveMQ    ?");
                Console.WriteLine("????????????????????????????????????????????????????");
                Console.WriteLine();

                // ============================
                // SOAP SERVICE
                // ============================
                soapHost = new ServiceHost(typeof(ProxyService), new Uri(soapAddress));

                var soapBinding = new BasicHttpBinding();
                soapBinding.MaxReceivedMessageSize = 65536000;
                soapBinding.MaxBufferSize = 65536000;

                soapHost.AddServiceEndpoint(typeof(IProxyService), soapBinding, "");

                var smbSoap = new ServiceMetadataBehavior
                {
                    HttpGetEnabled = true,
                    HttpGetUrl = new Uri(soapAddress)
                };
                soapHost.Description.Behaviors.Add(smbSoap);

                soapHost.Open();
                Console.WriteLine("? SOAP Service: " + soapAddress);

                // ============================
                // REST SERVICE
                // ============================
                restHost = new WebServiceHost(typeof(ProxyService), new Uri(restAddress));

                var restBinding = new WebHttpBinding();
                restBinding.CrossDomainScriptAccessEnabled = true;
                restBinding.MaxReceivedMessageSize = 65536000;

                var endpoint = restHost.AddServiceEndpoint(typeof(IProxyServiceExtended), restBinding, "");
                endpoint.EndpointBehaviors.Add(new WebHttpBehavior
                {
                    HelpEnabled = true,
                    DefaultOutgoingResponseFormat = WebMessageFormat.Json
                });

                restHost.Open();
                Console.WriteLine("? REST Service:  " + restAddress);
                Console.WriteLine();

                // ============================
                // TEST ACTIVEMQ
                // ============================
                Console.WriteLine("Testing ActiveMQ connection...");
                bool mqOk = ActiveMQProducerService.Instance.TestConnection();
                Console.WriteLine(mqOk ? "? ActiveMQ:      Connected" : "? ActiveMQ:      Failed");
                Console.WriteLine();

                Console.WriteLine("Available endpoints:");
                Console.WriteLine("  SOAP: GetStations(city)");
                Console.WriteLine("  REST: POST /ForceStationUnavailable?stationId=...&city=...");
                Console.WriteLine();
                Console.WriteLine("Press ENTER to stop...");
                Console.ReadLine();
            }
            catch (Exception ex)
            {
                Console.WriteLine("ERROR: " + ex.Message);
                Console.ReadLine();
            }
            finally
            {
                soapHost?.Close();
                restHost?.Close();
                ActiveMQProducerService.Instance?.Dispose();
            }
        }
    }
}