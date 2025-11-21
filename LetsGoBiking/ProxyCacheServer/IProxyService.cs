using System.ServiceModel;
using LetsGoBiking.Shared;

namespace LetsGoBiking.ProxyCacheServer
{
    [ServiceContract]
    public interface IProxyService
    {
        [OperationContract]
        StationInfo[] GetStations(string city);
    }
}
