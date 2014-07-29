package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.LabResize;
import com.photodispatcher.model.mysql.entities.SelectResult;

@Service("labResizeService")
public class LabResizeServiceImpl extends AbstractDAO implements LabResizeService {

	@Override
	public SelectResult<LabResize> loadAll(){
		SelectResult<LabResize> result;
		String sql="SELECT l.* FROM phcconfig.lab_resize l ORDER BY l.width";
		result=runSelect(LabResize.class, sql);
		return result;
	}
	
	@Override
	public DmlResult persist(LabResize item){
		DmlResult result=new DmlResult();
		if(item.getPersistState()==0){
			//insert 
			result=runInsert(item,null);
		}else if(item.getPersistState()==-1){
			result=runUpdate(item,null);
		}
		return result;
	}
	
	@Override
	public DmlResult persistBatch(List<LabResize> items){
		DmlResult result=new DmlResult();
		List<LabResize> insertList=new ArrayList<LabResize>();
		List<LabResize> updateList=new ArrayList<LabResize>();

		for(LabResize item : items){
			if(item.getPersistState()==0){
				insertList.add(item);
			}else if(item.getPersistState()==-1){
				updateList.add(item);
			}
		}
		if(!insertList.isEmpty()){
			result=runInsertBatch(insertList, null);
		}
		if(result.isComplete() && !updateList.isEmpty()){
			result=runUpdateBatch(updateList, null);
		}

		return result;
	}

}
