package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.GroupNetprint;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@RemoteDestination(id="groupNetprintService", source="groupNetprintService")
public interface GroupNetprintService {

	SelectResult<GroupNetprint> findeByNetprint(int source, String netprint, Boolean setSsend);
	SelectResult<GroupNetprint> loadByGroup(int source, int group);
	SqlResult save(List<GroupNetprint> items);

}
