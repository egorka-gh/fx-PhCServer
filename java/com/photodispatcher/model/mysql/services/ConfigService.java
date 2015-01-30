package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.Staff;

@RemoteDestination(id="configService", source="configService")
public interface ConfigService {

	SelectResult<Staff> loadStaff();
	SqlResult persistStaff(List<Staff> items);

}
