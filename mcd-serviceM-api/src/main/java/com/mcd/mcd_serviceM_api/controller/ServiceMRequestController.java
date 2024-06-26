package com.mcd.mcd_serviceM_api.controller;

import com.mcd.mcd_serviceM_api.model.McdServiceMRequest;
import com.mcd.mcd_serviceM_api.model.McdServiceMResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class ServiceMRequestController {
  @PostMapping(value = "loyalty", produces = MediaType.APPLICATION_JSON_VALUE)
  public McdServiceMResponse submitLoyaty(@RequestBody McdServiceMRequest request) {
    return new McdServiceMResponse("Loyalty submitted successfully");
  }
}
