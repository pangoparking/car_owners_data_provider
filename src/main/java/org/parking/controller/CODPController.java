package org.parking.controller;

import org.parking.model.*;
import org.parking.service.CODPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/parking")
public class CODPController {

	@Autowired
	CODPService service;
	
	@GetMapping("/getCarInfo")
	RawSqlData getOwnerData (@RequestBody CarData carData) {
		log.trace("CODPController : getOwnerData : carData = {}", carData);
		return service.getCarInfo(carData);
	}
}
