package io.pivotal.cnspringgateway;

import java.util.HashMap;
import java.util.Map;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cnspringgateway.domain.City;
import io.pivotal.cnspringgateway.domain.Weather;

@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableFeignClients
@EnableZuulProxy
public class CnSpringGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(CnSpringGatewayApplication.class, args);
	}
}

@RestController
@RequestMapping("/api")
class GatewayController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayController.class);
	private final CityService cityService;
	private final WeatherService weatherService;

	private final Map<String, String> cityCacheMap = new HashMap();
	private final Map<String, String> weatherCacheMap = new HashMap();

	public GatewayController(CityService cityService, WeatherService weatherService) {
		this.cityService = cityService;
		this.weatherService = weatherService;
	}

	@RequestMapping("/{postalCode}")
	@HystrixCommand(fallbackMethod = "getFallBackData")
	public String getAggregatedData(@PathVariable("postalCode") String postalCode) {
        LOGGER.info("Get aggregated data");
		return String.format("%s = %s", getCityByPostalCode(postalCode).getName(),
				getWeatherByPostalCode(postalCode).getWeather());

	}

	private City getCityByPostalCode(String postalCode) {
        LOGGER.info("Get real time city data");
		City city = cityService.getCityByPostalCode(postalCode).getContent().stream()
				.findFirst().get();
		cityCacheMap.put(city.getPostalCode(), city.getName());
		return city;
	}

	private Weather getWeatherByPostalCode(String postalCode) {
        LOGGER.info("Get real time weather data");
		Weather weather = weatherService.getWeatherByPostalCode(postalCode).getContent()
				.stream().findFirst().get();
		weatherCacheMap.put(weather.getPostalCode(), weather.getWeather());
		return weather;
	}

	private String getFallBackData(String postalCode) {
        LOGGER.info("Get fallback city and weather data");
		return String.format("%s = %s",
				cityCacheMap.getOrDefault(postalCode, new City("XXX").getName()),
				weatherCacheMap.getOrDefault(postalCode, new Weather("YYY").getWeather()));
	}

}

@FeignClient(name = "city-service")
interface CityService {
	@RequestMapping("/cities/search/postalCode?postalCode={postalCode}")
	Resources<City> getCityByPostalCode(@PathVariable("postalCode") String postalCode);
}

@FeignClient(name = "weather-service")
interface WeatherService {
    @RequestMapping("/weather/search/postalCode?postalCode={postalCode}")
	Resources<Weather> getWeatherByPostalCode(@PathVariable("postalCode") String postalCode);
}
