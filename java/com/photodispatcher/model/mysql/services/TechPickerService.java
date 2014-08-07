package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.Endpaper;
import com.photodispatcher.model.mysql.entities.Layer;
import com.photodispatcher.model.mysql.entities.LayerSequence;
import com.photodispatcher.model.mysql.entities.Layerset;
import com.photodispatcher.model.mysql.entities.LayersetGroup;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@RemoteDestination(id="techPickerService", source="techPickerService")
public interface TechPickerService {

	public SelectResult<LayersetGroup> loadLayersetGroups();
	public SqlResult persistLayersetGroups(List<LayersetGroup> items);
	public SelectResult<Layerset> loadLayersets(int type);
	public SqlResult persistLayersets(List<Layerset> items);
	public SelectResult<Layer> loadLayers();
	public SelectResult<Layer> persistLayers(List<Layer> items);
	public SelectResult<LayerSequence> loadtSequence(int layerset);
	public SelectResult<LayerSequence> persistSequence(List<LayerSequence> items, int layerset, int layerGroup);
	public SelectResult<Endpaper> loadEndpapers();
	public SqlResult persistEndpapers(List<Endpaper> items);

}
