package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.PrintGroup;
import com.photodispatcher.model.mysql.entities.PrnQueue;
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

	@Override
	public SqlResult startStrategy2(int strategy){
		//PROCEDURE prn_strategy2_start(IN p_strategy int)
		String sql= "{CALL prn_strategy2_start(?)}";
		return runCall(sql, strategy);
	}

	@Override
	public SelectResult<PrnQueue> loadQueues(){
		SelectResult<PrnQueue> result= new SelectResult<PrnQueue>(); 
		String sql="SELECT IFNULL(psq.lab, pq.lab) lab, ps.priority, pq.created, pq.id, IFNULL(psq.sub_queue, 0) sub_queue, pq.strategy, pq.is_active, IFNULL(psq.started, pq.started) started, pq.label"+
					 " FROM prn_queue pq"+
					   " INNER JOIN prn_strategy ps ON pq.strategy=ps.id"+
					   " LEFT OUTER JOIN prn_sub_queue psq ON pq.id = psq.prn_queue AND psq.complited IS NULL"+
					 " WHERE pq.is_active = 1 AND pq.complited IS NULL"+
					 " ORDER BY 1 DESC, 2 DESC, 3, 4, 5";
		result=runSelect(PrnQueue.class, sql);
		if(result.isComplete()){
			// fill vs print groups
			for(PrnQueue item : result.getData()){
				//reload print group
				sql="SELECT pg.*, o.source source_id, o.ftp_folder order_folder, IFNULL(s.alias, pg.path) alias"+
						 " FROM prn_queue_items pqi"+
						   " INNER JOIN print_group pg ON pg.id = pqi.print_group"+
						   " INNER JOIN orders o ON pg.order_id = o.id"+
						   " LEFT OUTER JOIN suborders s ON pg.order_id = s.order_id AND pg.sub_id = s.sub_id"+
						  " WHERE pqi.prn_queue = ? AND pqi.sub_queue = ?";
				SelectResult<PrintGroup> selRes=runSelect(PrintGroup.class, sql, item.getId(), item.getSub_queue());
				if(selRes.isComplete()){
					item.setPrintGroups(selRes.getData());
				}else{
					result.cloneError(selRes);
					break;
				}
			}
		}
		return result;
	}

}
