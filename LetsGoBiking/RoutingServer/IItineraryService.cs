using System.ServiceModel;
using System.ServiceModel.Web;
using LetsGoBiking.Shared;

namespace LetsGoBiking.RoutingServer
{
    [ServiceContract]
    public interface IItineraryService
    {
        [OperationContract]
        [WebGet(UriTemplate = "Itinerary?origin={origin}&destination={destination}",
                ResponseFormat = WebMessageFormat.Json)]
        ItineraryDto GetItinerary(string origin, string destination);

        [OperationContract]
        [WebGet(UriTemplate = "ItineraryByCoords?originLat={originLat}&originLon={originLon}&destLat={destLat}&destLon={destLon}",
                ResponseFormat = WebMessageFormat.Json)]
        ItineraryDto GetItineraryByCoords(string originLat, string originLon, string destLat, string destLon);
    }
}
