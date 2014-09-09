package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.TechPoint;

@Service("techPointService")
public class TechPointServiceImpl extends AbstractDAO implements TechPointService {
	
	@Override
	public SelectResult<TechPoint> loadAll(int type){
		SelectResult<TechPoint> result;
		String sql="SELECT s.*, os.name tech_type_name, os.book_part tech_book_part"+
				" FROM tech_point s"+
				" INNER JOIN order_state os ON s.tech_type = os.id"+
				" WHERE s.id!=0";
		if (type!=0) sql+=" AND s.tech_type = ?";
			sql+=" ORDER BY s.name";
			
		if (type!=0){
			result=runSelect(TechPoint.class, sql, type);
		}else{
			result=runSelect(TechPoint.class, sql);
		}
		return result;
	}

	@Override
	public SqlResult persistBatch(List<TechPoint> items){
		SqlResult result=new SqlResult();
		List<TechPoint> insertList=new ArrayList<TechPoint>();
		List<TechPoint> updateList=new ArrayList<TechPoint>();

		for(TechPoint item : items){
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
