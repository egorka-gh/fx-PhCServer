package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.FieldValue;
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
	public SqlResult createQueue(int strategy, int lab, PrintGroup params){
		String sql;
		SqlResult result= new SqlResult();
		result.setResultCode(0);
		SelectResult<FieldValue> val;
		int reprint=0;
		if(params.isIs_reprint()) reprint=1;
		/* strategy
		 * 1 prn_queue1_create(IN p_lab int, IN p_reprint int, IN p_paper int, IN p_width int, IN p_booksonly int)
		 * 3 prn_queue3_create(IN p_lab int, IN p_reprint int, IN p_alias int, IN p_book_part int, IN p_sheet_num int, IN p_booksonly int)
		 */
		switch (strategy) {
		case 1:
			sql= "{CALL prn_queue1_create(?, ?, ?, ?, ?)}";
			val=runCallSelect(FieldValue.class, sql, lab, reprint, params.getPaper(), params.getWidth(), params.getBook_type());
			if(val.getData()!=null && !val.getData().isEmpty() && val.getData().get(0).getValue()!=0) result.setResultCode(1);
			break;
		case 3:
			sql= "{CALL prn_queue3_create(?, ?, ?, ?, ?, ?)}";
			val=runCallSelect(FieldValue.class, sql, lab, reprint, params.getAlias(), params.getBook_part(), params.getSheet_num(), 1);
			if(val.getData()!=null && !val.getData().isEmpty() && val.getData().get(0).getValue()!=0) result.setResultCode(1);
			break;
		default:
			break;
		}
		return result;
	}

	@Override
	public SqlResult deleteQueue(int queue){
		//prn_queue_delete(IN p_queue int)
		SqlResult result= new SqlResult();
		result.setResultCode(0);
		String sql= "{CALL prn_queue_delete(?)}";
		SelectResult<FieldValue> val=runCallSelect(FieldValue.class, sql, queue);
		if(val.getData()!=null && !val.getData().isEmpty() && val.getData().get(0).getValue()!=0) result.setResultCode(1);
		return result;
	}

	@Override
	public SqlResult checkQueue2(){
		//PROCEDURE prn_queue2_check()
		String sql= "{CALL prn_queue2_check()}";
		return runCall(sql);
	}

	@Override
	public SqlResult checkQueues(){
		//PROCEDURE prn_queue_check()
		String sql= "{CALL prn_queue_check()}";
		return runCall(sql);
	}

	@Override
	public SqlResult startQueue(int queue, int subQueue, int lab){
		//PROCEDURE prn_queue_start(IN p_queue int, IN p_subqueue int, IN p_lab int)
		String sql= "{CALL prn_queue_start(?, ?, ?)}";
		return runCall(sql, queue, subQueue, lab);
	}

	@Override
	public SelectResult<PrnQueue> loadQueues(){
		SelectResult<PrnQueue> result= new SelectResult<PrnQueue>();
		SqlResult suRes=checkQueue2();
		if(!suRes.isComplete()){
			result.cloneError(suRes);
			return result;
		}
		suRes=checkQueues();
		if(!suRes.isComplete()){
			result.cloneError(suRes);
			return result;
		}
		String sql="SELECT IFNULL(psq.lab, pq.lab) lab, pst.default_priority priority, pq.created, pq.id, IFNULL(psq.sub_queue, 0) sub_queue,"+
							" pq.strategy, pq.is_active, IFNULL(psq.started, pq.started) started, pq.label,"+
							" lab.name lab_name, pq.strategy strategy_type, pst.name strategy_type_name"+
					 " FROM prn_queue pq"+
					 //  " INNER JOIN prn_strategy ps ON pq.strategy=ps.id"+
					 //  " INNER JOIN prn_strategy_type pst ON ps.strategy_type = pst.id"+
					   " INNER JOIN prn_strategy_type pst ON pq.strategy = pst.id"+
					   " LEFT OUTER JOIN prn_sub_queue psq ON pq.id = psq.prn_queue AND psq.complited IS NULL"+
					   " LEFT OUTER JOIN lab ON IFNULL(psq.lab, pq.lab) = lab.id"+
					 " WHERE pq.is_active = 1 AND pq.complited IS NULL"+
					 " ORDER BY 1 DESC, 2 DESC, 3, 4, 5";
		result=runSelect(PrnQueue.class, sql);
		if(result.isComplete()){
			// fill vs print groups
			for(PrnQueue item : result.getData()){
				//load print group
				SelectResult<PrintGroup> selRes=loadQueueItems(item.getId(), item.getSub_queue(), false);
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

	@Override
	public SelectResult<PrnQueue> loadComplitedQueues(Date date){
		SelectResult<PrnQueue> result= new SelectResult<PrnQueue>();
		String sql="SELECT psq.lab , pst.default_priority priority, pq.strategy strategy_type, pq.created, psq.complited, pq.id, psq.sub_queue, pq.strategy, pq.is_active, psq.started, pq.label, lab.name lab_name, pst.name strategy_type_name"+
					 " FROM prn_sub_queue psq"+
					   " INNER JOIN prn_queue pq ON pq.id = psq.prn_queue"+
					  // " INNER JOIN prn_strategy ps ON pq.strategy = ps.id"+
					  // " INNER JOIN prn_strategy_type pst ON ps.strategy_type = pst.id"+
					   " INNER JOIN prn_strategy_type pst ON pq.strategy = pst.id"+
					    " LEFT OUTER JOIN lab ON psq.lab = lab.id"+
					  " WHERE psq.complited > DATE(?) AND psq.complited < DATE_ADD(DATE(?), INTERVAL 1 DAY)"+
					" UNION ALL"+
					" SELECT pq.lab, pst.default_priority priority, pq.strategy strategy_type, pq.created, pq.complited, pq.id, 0 sub_queue, pq.strategy, pq.is_active, pq.started, pq.label, lab.name lab_name, pst.name strategy_type_name"+
					  " FROM prn_queue pq"+
					   // " INNER JOIN prn_strategy ps ON pq.strategy = ps.id"+
					   // " INNER JOIN prn_strategy_type pst ON ps.strategy_type = pst.id"+
					   " INNER JOIN prn_strategy_type pst ON pq.strategy = pst.id"+
					    " LEFT OUTER JOIN lab ON pq.lab = lab.id"+
					  " WHERE pq.complited > DATE(?) AND pq.complited < DATE_ADD(DATE(?), INTERVAL 1 DAY) AND pq.has_sub = 0"+
				  " ORDER BY complited";
		result=runSelect(PrnQueue.class, sql,date,date,date,date);
		if(result.isComplete()){
			// fill vs print groups
			for(PrnQueue item : result.getData()){
				//load print group
				SelectResult<PrintGroup> selRes=loadQueueItems(item.getId(), item.getSub_queue(),true);
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

	private SelectResult<PrintGroup> loadQueueItems(int queue, int subQueue, boolean all){
		String sql="SELECT pg.*, o.source source_id, o.ftp_folder order_folder, IFNULL(s.alias, pg.path) alias, os.name state_name, av.value paper_name"+
					 " FROM prn_queue_items pqi"+
					   " INNER JOIN print_group pg ON pg.id = pqi.print_group"+
					   " INNER JOIN orders o ON pg.order_id = o.id"+
					   " INNER JOIN order_state os ON pg.state = os.id"+
					   " INNER JOIN attr_value av ON pg.paper = av.id"+
					   " LEFT OUTER JOIN suborders s ON pg.order_id = s.order_id AND pg.sub_id = s.sub_id"+
					  " WHERE pqi.prn_queue = ? AND pqi.sub_queue = ?";
		if(!all) sql=sql +" AND o.state<450";
		return runSelect(PrintGroup.class, sql, queue, subQueue);
	}

}
