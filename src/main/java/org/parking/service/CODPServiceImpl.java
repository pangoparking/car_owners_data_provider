package org.parking.service;

import java.time.LocalDateTime;
import org.parking.entities.*;
import org.parking.model.*;
import org.parking.repo.CarsRepository;
import org.parking.repo.ParkingLotsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@Transactional(readOnly = true)
public class CODPServiceImpl implements CODPService {

	@Autowired
	CarsRepository carsRepository;
	@Autowired
	ParkingLotsRepository parkingLotsRepository;
	@Autowired
	EntityManager em;

	@Value("${app.CODPServiceImpl.queryCarAndOwnerColumns}")
	String queryCarAndOwnerColumns;
	@Value("${app.CODPServiceImpl.queryCarAndOwnerFromStatement}")
	String queryCarAndOwnerFromStatement;
	@Value("${app.CODPServiceImpl.queryLotAndAreaColumns}")
	String queryLotAndAreaColumns;
	@Value("${app.CODPServiceImpl.queryLotAndAreaFromStatement}")
	String queryLotAndAreaFromStatement;

	ObjectMapper mapper;

	@Override
	public RawSqlData getCarInfo(CarData carData) {
		log.trace("CODPServiceImpl : getCarInfo :  carData == {}", carData);
		Object[] carAndOwnerData = getCarAndOwnerInfoFromDB(carData.carID);
		log.trace("getCarInfo :  carAndOwnerData == {}", carAndOwnerData);
		Object[] lotAndAreaData = getlotAndAreaInfoFromDB(carData.parkingID);
		log.trace("getCarInfo :  lotAndAreaData == {}", lotAndAreaData);
		return configureRawSqlData(carAndOwnerData, lotAndAreaData);
	}

	private Object[] getlotAndAreaInfoFromDB(long parkingID) {
		Object[] responseFromDB = null;
		try {
			String queryBody = getLotAndParkingQueryBody(parkingID);
			var query = em.createQuery(queryBody, Object[].class).setParameter(1, parkingID);
			responseFromDB = query.getSingleResult();
			log.trace("getCarInfo : parkingLotEntity ={}, AreaEntity={}", responseFromDB[0], responseFromDB[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return responseFromDB;
	}

	private String getLotAndParkingQueryBody(long parkingID) {
		return "SELECT" + " " + queryLotAndAreaColumns + " " + queryLotAndAreaFromStatement;
	}

	private Object[] getCarAndOwnerInfoFromDB(long carNumber) {
		Object[] responseFromDB = null;
		try {
			String queryBody = getCarAndOwnerQueryBody(carNumber);
			var query = em.createQuery(queryBody, Object[].class).setParameter(1, carNumber);
			responseFromDB = query.getSingleResult();
			log.trace("getCarInfo : carEntity ={}, OwnerEntity={}", responseFromDB[0], responseFromDB[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return responseFromDB;
	}

	private String getCarAndOwnerQueryBody(long carID) {
		return "SELECT" + " " + queryCarAndOwnerColumns + " " + queryCarAndOwnerFromStatement;
	}

	private RawSqlData configureRawSqlData(Object[] carOwnerData, Object[] lotAreaData) {
		log.trace("CODPServiceImpl : configureCarRowData : !!! ");

		CarEntity carEntity = (CarEntity) carOwnerData[0];
		log.trace("configureCarRowData : carEntity={}", carEntity);
		OwnerEntity ownerEntity = (OwnerEntity) carOwnerData[1];
		log.trace("configureCarRowData : ownerEntity={}", ownerEntity);
		ParkingLotEntity lotEntity = (ParkingLotEntity) lotAreaData[0];
		log.trace("configureCarRowData : lotEntity={}", lotEntity);
		AreaEntity areaEntity = (AreaEntity) lotAreaData[1];
		log.trace("configureCarRowData : areaEntity={}", areaEntity);
		String ownerName = ownerEntity.name + " " + ownerEntity.surname;
		log.trace("configureCarRowData : ownerName={}", ownerName);
		RawSqlData rawSqlData = new RawSqlData(ownerEntity.ownerID, ownerName, ownerEntity.email, carEntity.carID,
				String.valueOf(lotEntity.parkingLotId), LocalDateTime.now(), areaEntity.dailyFineCost);
		log.trace("configureCarRowData : rawSqlData={}", rawSqlData);
		return rawSqlData;
	}
}
