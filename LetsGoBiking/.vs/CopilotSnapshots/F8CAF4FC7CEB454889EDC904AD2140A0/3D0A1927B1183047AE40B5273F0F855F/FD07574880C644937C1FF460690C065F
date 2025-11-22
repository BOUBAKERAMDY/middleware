using Apache.NMS;
using Apache.NMS.ActiveMQ;
using Newtonsoft.Json;
using System;

namespace LetsGoBiking.ProxyCacheServer
{
    public class StationAlertMessage
    {
        public int StationId { get; set; }
        public string City { get; set; }
        public string StationName { get; set; }
        public double Latitude { get; set; }
        public double Longitude { get; set; }
        public int AvailableBikes { get; set; }
        public int AvailableStands { get; set; }
        public string Message { get; set; }
        public DateTime Timestamp { get; set; }
    }

    public sealed class ActiveMQProducerService : IDisposable
    {
        private static readonly Lazy<ActiveMQProducerService> _instance =
            new Lazy<ActiveMQProducerService>(() => new ActiveMQProducerService());

        public static ActiveMQProducerService Instance => _instance.Value;

        private const string BROKER_URI = "tcp://localhost:61616";
        private const string TOPIC_NAME = "stationAlerts";

        private IConnection _connection;
        private Apache.NMS.ISession _session;
        private IMessageProducer _producer;
        private bool _isInitialized;
        private readonly object _lock = new object();

        private ActiveMQProducerService() { }

        private bool EnsureInitialized()
        {
            if (_isInitialized) return true;

            lock (_lock)
            {
                if (_isInitialized) return true;

                try
                {
                    var factory = new ConnectionFactory(BROKER_URI);
                    _connection = factory.CreateConnection();
                    _connection.Start();

                    _session = _connection.CreateSession(AcknowledgementMode.AutoAcknowledge);
                    var topic = _session.GetTopic(TOPIC_NAME);

                    _producer = _session.CreateProducer(topic);
                    _producer.DeliveryMode = MsgDeliveryMode.NonPersistent;

                    _isInitialized = true;
                    Console.WriteLine($"[ActiveMQ] Connected to {BROKER_URI} on topic '{TOPIC_NAME}'");
                    return true;
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"[ActiveMQ] Connection failed: {ex.Message}");
                    return false;
                }
            }
        }

        public bool TestConnection()
        {
            return EnsureInitialized();
        }

        public void PublishStationAlert(StationAlertMessage alert)
        {
            if (!EnsureInitialized())
            {
                throw new InvalidOperationException("ActiveMQ is not available");
            }

            try
            {
                alert.Timestamp = DateTime.UtcNow;
                var jsonMessage = JsonConvert.SerializeObject(alert);

                var message = _session.CreateTextMessage(jsonMessage);
                message.Properties["stationId"] = alert.StationId;
                message.Properties["city"] = alert.City;
                message.Properties["messageType"] = alert.Message;

                _producer.Send(message);
                Console.WriteLine($"[ActiveMQ] Alert published: {alert.Message} for {alert.StationName} ({alert.City})");
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[ActiveMQ] Publish failed: {ex.Message}");
                throw;
            }
        }

        public void Dispose()
        {
            if (!_isInitialized) return;

            lock (_lock)
            {
                try
                {
                    _producer?.Close();
                    _session?.Close();
                    _connection?.Close();
                    _isInitialized = false;
                    Console.WriteLine("[ActiveMQ] Disconnected");
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"[ActiveMQ] Dispose error: {ex.Message}");
                }
            }
        }
    }
}