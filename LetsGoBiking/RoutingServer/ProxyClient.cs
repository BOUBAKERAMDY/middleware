using System;
using System.ServiceModel;
using LetsGoBiking.Shared;

namespace LetsGoBiking.RoutingServer
{
    public class ProxyClient
    {
        private static readonly string ProxyEndpoint = "http://localhost:9000/ProxyService";

        public static StationInfo[] GetStations(string city)
        {
            ChannelFactory<IProxyService> channelFactory = null;
            IProxyService proxy = null;

            try
            {
                // Create binding and endpoint with increased message size
                var binding = new BasicHttpBinding();
                binding.MaxReceivedMessageSize = 65536000; // 64 MB
                binding.MaxBufferSize = 65536000;
                binding.ReaderQuotas.MaxDepth = 32;
                binding.ReaderQuotas.MaxStringContentLength = 8192000;
                binding.ReaderQuotas.MaxArrayLength = 16384000;
                binding.ReaderQuotas.MaxBytesPerRead = 4096000;
                binding.ReaderQuotas.MaxNameTableCharCount = 16384;

                var endpoint = new EndpointAddress(ProxyEndpoint);

                // Create channel factory and proxy
                channelFactory = new ChannelFactory<IProxyService>(binding, endpoint);
                proxy = channelFactory.CreateChannel();

                // Call the service
                var stations = proxy.GetStations(city);

                return stations ?? new StationInfo[0];
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error calling proxy service: {ex.Message}");
                return new StationInfo[0];
            }
            finally
            {
                // Properly close the channel
                if (proxy != null)
                {
                    var channel = proxy as ICommunicationObject;
                    try
                    {
                        if (channel.State == CommunicationState.Faulted)
                            channel.Abort();
                        else
                            channel.Close();
                    }
                    catch
                    {
                        channel?.Abort();
                    }
                }

                channelFactory?.Close();
            }
        }
    }
}