package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.LabResize;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

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
	public DmlResult<LabResize> persist(LabResize item){
		DmlResult<LabResize> result=new DmlResult<LabResize>();
		if(item.getPersistState()==0){
			//insert 
			result=runInsert(item);
		}else if(item.getPersistState()==-1){
			result=runUpdate(item);
		}
		return result;
	}
	
	@Override
	public SqlResult persistBatch(List<LabResize> items){
		SqlResult result=new SqlResult();
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
			result=runInsertBatch(insertList);
		}
		if(result.isComplete() && !updateList.isEmpty()){
			result=runUpdateBatch(updateList);
		}

		return result;
	}

}
