using System;
using System.Runtime.Caching;

namespace LetsGoBiking.ProxyCacheServer
{
    public class GenericProxyCache<T> where T : class
    {
        private readonly MemoryCache _cache;

        public GenericProxyCache()
        {
            _cache = MemoryCache.Default;
        }

        // Get with factory function
        public T Get(string key, Func<T> factory)
        {
            return Get(key, factory, 300); // Default 5 minutes
        }

        // Get with factory function and expiration in seconds
        public T Get(string key, Func<T> factory, double seconds)
        {
            var expiration = DateTimeOffset.Now.AddSeconds(seconds);
            return Get(key, factory, expiration);
        }

        // Get with factory function and absolute expiration
        public T Get(string key, Func<T> factory, DateTimeOffset absoluteExpiration)
        {
            if (string.IsNullOrEmpty(key))
                throw new ArgumentNullException(nameof(key));

            // Try to get from cache
            var cachedItem = _cache.Get(key) as T;
            
            if (cachedItem != null)
            {
                Console.WriteLine($"Cache HIT for key: {key}");
                return cachedItem;
            }

            Console.WriteLine($"Cache MISS for key: {key}");
            
            // Create new item using factory
            var newItem = factory();
            
            if (newItem != null)
            {
                // Add to cache with expiration
                var policy = new CacheItemPolicy
                {
                    AbsoluteExpiration = absoluteExpiration
                };
                
                _cache.Set(key, newItem, policy);
                Console.WriteLine($"Added to cache: {key}, expires at {absoluteExpiration}");
            }

            return newItem;
        }

        // Simple Get without factory (returns null if not found)
        public T Get(string key)
        {
            if (string.IsNullOrEmpty(key))
                return null;

            return _cache.Get(key) as T;
        }

        // Remove item from cache
        public void Remove(string key)
        {
            if (!string.IsNullOrEmpty(key))
            {
                _cache.Remove(key);
                Console.WriteLine($"Removed from cache: {key}");
            }
        }

        // Clear entire cache
        public void Clear()
        {
            foreach (var item in _cache)
            {
                _cache.Remove(item.Key);
            }
            Console.WriteLine("Cache cleared");
        }
    }
}
