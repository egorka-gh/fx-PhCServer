package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.Staff;

@Service("configService")
public class ConfigServiceImpl extends AbstractDAO implements ConfigService {

	@Override
	public SelectResult<Staff> loadStaff(){
		String sql="SELECT s.*, g.name staff_group_name"+
					" FROM staff s"+
					" INNER JOIN staff_group g ON s.staff_group = g.id"+
					" WHERE s.id!=0"+
					" ORDER BY s.name";
		return runSelect(Staff.class, sql);
	}
	
	@Override
	public SqlResult persistStaff(List<Staff> items){
		SqlResult result=new SqlResult();
		List<Staff> insertList=new ArrayList<Staff>();
		List<Staff> updateList=new ArrayList<Staff>();

		for(Staff item : items){
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
