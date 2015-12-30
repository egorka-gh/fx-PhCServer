package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.Lab;
import com.photodispatcher.model.mysql.entities.LabPrintCode;
import com.photodispatcher.model.mysql.entities.PrnStrategy;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@Service("prnStrategyService")
public class PrnStrategyServiceImpl extends AbstractDAO implements PrnStrategyService {
	

	@Override
	public SelectResult<PrnStrategy> persistStrategies(List<PrnStrategy> items){
		SelectResult<PrnStrategy> res = new SelectResult<PrnStrategy>();
		SqlResult result=new SqlResult();
		List<PrnStrategy> insertList=new ArrayList<PrnStrategy>();
		List<PrnStrategy> updateList=new ArrayList<PrnStrategy>();

		for(PrnStrategy item : items){
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
		if(!result.isComplete()){
			res.cloneError(result);
		}else{
			res=loadStrategies();
		}
		return res;
	}

	@Override
	public SelectResult<PrnStrategy> loadStrategies(){
		String sql="SELECT ps.*, pst.name strategy_type_name"+
					 " FROM prn_strategy ps"+
					   " INNER JOIN prn_strategy_type pst ON ps.strategy_type = pst.id"+
					 " ORDER BY ps.priority DESC";
		return runSelect(PrnStrategy.class, sql);
	}

}
