package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.StaffActivity;
import com.photodispatcher.model.mysql.entities.StaffActivityGroup;
import com.photodispatcher.model.mysql.entities.StaffActivityType;

@Service("staffActivityService")
public class StaffActivityServiceImpl extends AbstractDAO implements StaffActivityService {
	
	@Override
	public SelectResult<StaffActivityGroup> loadGroup(){
		String sql="SELECT * FROM staff_activity_group g WHERE g.id!=0 ORDER BY g.name";
		return runSelect(StaffActivityGroup.class, sql);
	}

	@Override
	public SelectResult<StaffActivityType> loadType(int group){
		//group==0 - any exept id==0
		String sql="SELECT t.*, g.name sa_group_name"+
					 " FROM staff_activity_group g"+
					   " INNER JOIN staff_activity_type t ON g.id = t.staff_activity_group"+
					 " WHERE t.id != 0 AND (0=? OR g.id=?)"+
					 " ORDER BY t.name";
		return runSelect(StaffActivityType.class, sql, group, group);
	}

	@Override
	public SqlResult persistTypes(List<StaffActivityType> items){
		SqlResult result=new SqlResult();
		List<StaffActivityType> insertList=new ArrayList<StaffActivityType>();
		List<StaffActivityType> updateList=new ArrayList<StaffActivityType>();

		for(StaffActivityType item : items){
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

	@Override
	public SelectResult<StaffActivityType> delType(int id){
		SelectResult<StaffActivityType> result= new SelectResult<StaffActivityType>();
		String sql="DELETE FROM staff_activity_type WHERE id=?";
		SqlResult delRes= runDML(sql, id);
		if(!delRes.isComplete()){
			result.cloneError(delRes);
			return result;
		}
		return loadType(0);
	}

	@Override
	public DmlResult<StaffActivity> logActivity(StaffActivity item){
		return runInsert(item);
	}
	
	@Override
	public SqlResult logActivityBatch(List<StaffActivity> items){
		return runInsertBatch(items);
	}

}
