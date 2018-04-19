package io.pivotal.cnspringcity.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class RefreshCityController {

	@Value("${hello.there}")
	private String refreshValue;

	@GetMapping("/city/helloThere")
    public String getRefreshValue() {
        return refreshValue;
    }
}
