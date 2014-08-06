package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.LabRoll;
import com.photodispatcher.model.mysql.entities.Roll;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@RemoteDestination(id="rollService", source="rollService")
public interface RollService {

	public SelectResult<Roll> loadAll();
	public SelectResult<LabRoll> getByDevice(int device, boolean forEdit);
	public SelectResult<LabRoll> fillByChannels(int device);
	public SqlResult persistBatch(List<LabRoll> items);
}
