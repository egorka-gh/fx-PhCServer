package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.LayersetGroup;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@Service("techPickerService")
public class TechPickerServiceImpl extends AbstractDAO implements TechPickerService {

	
	@Override
	public SelectResult<LayersetGroup> loadLayersetGroups(){
		SelectResult<LayersetGroup> result;
		String sql="SELECT s.* FROM phcconfig.layerset_group s";
		result=runSelect(LayersetGroup.class, sql);
		return result;
	}

	@Override
	public SqlResult persistLayersetGroups(List<LayersetGroup> items){
		SqlResult result=new SqlResult();
		List<LayersetGroup> insertList=new ArrayList<LayersetGroup>();
		List<LayersetGroup> updateList=new ArrayList<LayersetGroup>();

		for(LayersetGroup item : items){
			if(item.getPersistState()==0){
				insertList.add(item);
			}else if(item.getPersistState()==-1){
				updateList.add(item);
			}
		}
		if(!insertList.isEmpty()){
			result=runInsertBatch(insertList);
		}
		if(result.isComplete() && !updateList.isEmpty()){
			result=runUpdateBatch(updateList);
		}
		return result;
	}

}
