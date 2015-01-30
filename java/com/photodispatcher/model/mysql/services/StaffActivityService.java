package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.StaffActivity;
import com.photodispatcher.model.mysql.entities.StaffActivityGroup;
import com.photodispatcher.model.mysql.entities.StaffActivityType;

@RemoteDestination(id="staffActivityService", source="staffActivityService")
public interface StaffActivityService {

	SelectResult<StaffActivityGroup> loadGroup();

	SelectResult<StaffActivityType> loadType(int group);
	SqlResult persistTypes(List<StaffActivityType> items);
	SelectResult<StaffActivityType> delType(int id);

	DmlResult<StaffActivity> logActivity(StaffActivity item);

	SqlResult logActivityBatch(List<StaffActivity> items);

}
