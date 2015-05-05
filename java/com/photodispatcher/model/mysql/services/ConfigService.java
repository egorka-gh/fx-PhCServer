package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.AliasForward;
import com.photodispatcher.model.mysql.entities.AppConfig;
import com.photodispatcher.model.mysql.entities.Rack;
import com.photodispatcher.model.mysql.entities.RackSpace;
import com.photodispatcher.model.mysql.entities.RackTechPoint;
import com.photodispatcher.model.mysql.entities.RackType;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.Staff;

@RemoteDestination(id="configService", source="configService")
public interface ConfigService {

	SelectResult<Staff> loadStaff();
	SqlResult persistStaff(List<Staff> items);
	SelectResult<AppConfig> loadConfig();
	SelectResult<AliasForward> loadAliasForward();
	SqlResult persistAliasForward(List<AliasForward> items);
	SelectResult<Rack> loadRacks();
	SqlResult persistRacks(List<Rack> items);
	SelectResult<RackSpace> loadRackSpace();
	SqlResult persistRackSpace(List<RackSpace> items);
	SelectResult<RackType> loadRackTypes();
	SqlResult persistRackTypes(List<RackType> items);
	SelectResult<RackTechPoint> loadRackTPoint(List<Integer> tpTypes);
	SqlResult persistRackTPoint(List<RackTechPoint> items);
	SqlResult saveConfig(AppConfig cofig);

}
