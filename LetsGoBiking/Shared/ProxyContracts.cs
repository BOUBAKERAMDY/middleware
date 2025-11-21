using System;
using System.Runtime.Serialization;
using System.ServiceModel;

namespace LetsGoBiking.Shared
{
    // DTO for station information
    [DataContract]
    public class StationInfo
    {
        [DataMember] public string Name { get; set; }
        [DataMember] public string Address { get; set; }
        [DataMember] public double Latitude { get; set; }
        [DataMember] public double Longitude { get; set; }
        [DataMember] public int AvailableBikes { get; set; }
        [DataMember] public int AvailableStands { get; set; }
        [DataMember] public string Status { get; set; }
        [DataMember] public string ContractName { get; set; }
    }

    // DTO for itinerary
    [DataContract]
    public class ItineraryDto
    {
        // --- ROUTE DETAILS ---
        [DataMember] public string StartStation { get; set; }
        [DataMember] public string EndStation { get; set; }

        [DataMember] public double WalkToStartMeters { get; set; }
        [DataMember] public double BikeMeters { get; set; }
        [DataMember] public double WalkToEndMeters { get; set; }

        [DataMember] public double TotalKm { get; set; }
        [DataMember] public double TotalSeconds { get; set; }

        [DataMember] public string Note { get; set; }

        // --- CITY INFORMATION ---
        [DataMember] public string OriginCity { get; set; }
        [DataMember] public string DestinationCity { get; set; }
        [DataMember] public bool IsInterCity { get; set; }
        [DataMember] public bool IsWalkingOnly { get; set; }

        // --- COORDINATES NEEDED FOR THE FRONT (CRITICAL!) ---
        [DataMember] public double OriginLat { get; set; }
        [DataMember] public double OriginLon { get; set; }
        [DataMember] public double DestLat { get; set; }
        [DataMember] public double DestLon { get; set; }

        // --- COMPLETE STATION OBJECTS FOR DRAWING ---
        [DataMember] public StationInfo StartStationDetails { get; set; }
        [DataMember] public StationInfo EndStationDetails { get; set; }
    }

    // SOAP Service Contract for Proxy
    [ServiceContract]
    public interface IProxyService
    {
        [OperationContract]
        StationInfo[] GetStations(string city);
    }
}
