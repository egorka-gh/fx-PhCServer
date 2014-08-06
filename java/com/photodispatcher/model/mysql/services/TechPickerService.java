package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.LayersetGroup;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@RemoteDestination(id="techPickerService", source="techPickerService")
public interface TechPickerService {

	public SelectResult<LayersetGroup> loadLayersetGroups();
	public SqlResult persistLayersetGroups(List<LayersetGroup> items);

}
