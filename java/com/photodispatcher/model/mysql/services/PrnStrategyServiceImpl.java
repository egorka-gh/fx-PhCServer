package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.FieldValue;
import com.photodispatcher.model.mysql.entities.PrintGroup;
import com.photodispatcher.model.mysql.entities.PrintGroupReject;
import com.photodispatcher.model.mysql.entities.PrnQueue;
import com.photodispatcher.model.mysql.entities.PrnQueueLink;
import com.photodispatcher.model.mysql.entities.PrnQueueTimetable;
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

	/**
	 * returns first and last PrintGroup (to draw queue mark on first/last print)
	 */
	@Override
	public SelectResult<PrintGroup> getQueueMarkPGs(int queue){
		SelectResult<PrintGroup> result= new SelectResult<PrintGroup>();
		
		SelectResult<PrintGroup> selRes=loadQueueItems(queue, 0,true);
		if(!selRes.isComplete()){
			result.cloneError(selRes);
		}else{
			if(selRes.getData()!=null && !selRes.getData().isEmpty()){
				List<PrintGroup> resultList=new ArrayList<PrintGroup>();
				PrintGroupServiceImpl pgsvc= new PrintGroupServiceImpl();
				//get first
				SelectResult<PrintGroup> pgRes=pgsvc.load(selRes.getData().get(0).getId());
				if(!pgRes.isComplete()){
					result.cloneError(pgRes);
					return result;
				}
				if(pgRes.getData()!=null && !pgRes.getData().isEmpty()) resultList.add(pgRes.getData().get(0));
				//get last
				pgRes=pgsvc.load(selRes.getData().get(selRes.getData().size()-1).getId());
				if(!pgRes.isComplete()){
					result.cloneError(pgRes);
					return result;
				}
				if(pgRes.getData()!=null && !pgRes.getData().isEmpty()) resultList.add(pgRes.getData().get(0));
				result.setData(resultList);
			}
		}
		return result;
	}

	@Override
	public SelectResult<PrnQueueLink> getLink(int queue){
		String sql="SELECT ql.* FROM prn_queue_link ql WHERE ql.prn_queue = ?";
		return runSelect(PrnQueueLink.class, sql, queue);
	}

	@Override
	public SelectResult<PrnQueueLink> getLinkByPG(String pgId){
		String sql="SELECT ql.*"+
					 " FROM print_group pg"+
					   " INNER JOIN prn_queue_link ql ON pg.prn_queue = ql.prn_queue"+
					  " WHERE pg.id = ?";
		return runSelect(PrnQueueLink.class, sql, pgId);
	}

	/**
	 * returns created queue Id in resultCode (0 - not created)  
	 */
	@Override
	public SqlResult createQueue(int strategy, int lab, PrintGroup params){
		String sql;
		SqlResult result= new SqlResult();
		SelectResult<FieldValue> val=null;
		int queueId=0;
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
			if(val.getData()!=null && !val.getData().isEmpty()) queueId=val.getData().get(0).getValue();
			break;
		case 3:
			sql= "{CALL prn_queue3_create(?, ?, ?, ?, ?, ?)}";
			val=runCallSelect(FieldValue.class, sql, lab, reprint, params.getAlias(), params.getBook_part(), params.getSheet_num(), 1);
			if(val.getData()!=null && !val.getData().isEmpty()) queueId=val.getData().get(0).getValue();
			break;
		default:
			break;
		}
		if(val!=null && !val.isComplete()) result.cloneError(val);
		result.setResultCode(queueId);
		
		return result;
	}

	/**
	 * gets PrnQueueTimetable list 
	 * returns non zero if create some queue  
	 * reprintsMode
	 * 	-1 join in queue
	 *   0 (!=-1) create separate queue 
	 */
	@Override
	public SqlResult createQueueBatch(List<PrnQueueTimetable> params, int reprintsMode){
		SqlResult result= new SqlResult();
		SelectResult<FieldValue> val=null;
		int books=1;

		//PROCEDURE prn_queues_create(IN p_lab_type int, IN p_strategy_type int, IN p_booksonly int, IN p_reprintsmode int)
		String sql="{CALL prn_queues_create(?, ?, ?, ?)}";
		for(PrnQueueTimetable param : params){
			books=1;
			if(!param.isBooksonly()) books=0;
			val=runCallSelect(FieldValue.class, sql, param.getLab_type(), param.getStrategy_type(),books, reprintsMode);
			if(!val.isComplete()){
				result.cloneError(val);
				break;
			}
			if(val.getData()!=null && !val.getData().isEmpty()) result.setResultCode(Math.max(result.getResultCode(), val.getData().get(0).getValue()));
			param.setLast_start(new Date());
		}
		//update time table
		SqlResult ures=runUpdateBatch(params);
		if (!ures.isComplete()) result.cloneError(ures);
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
	public SqlResult releaseQueue(int queue){
		//prn_queue_release(IN p_queue int)
		String sql= "{CALL prn_queue_release(?)}";
		return runCall(sql, queue);
	}

	@Override
	public SelectResult<PrnQueue> loadQueue(int queue, int subQueue){
		/*
		String sql="SELECT IFNULL(psq.lab, pq.lab) lab, pst.default_priority priority, pq.created, pq.id, IFNULL(psq.sub_queue, 0) sub_queue,"+
				" pq.strategy, pq.is_active, IFNULL(psq.started, pq.started) started, pq.label,"+
				" lab.name lab_name, pq.strategy strategy_type, pst.name strategy_type_name, pq.is_reprint, pq.lab_type"+
		 " FROM prn_queue pq"+
		 //  " INNER JOIN prn_strategy ps ON pq.strategy=ps.id"+
		 //  " INNER JOIN prn_strategy_type pst ON ps.strategy_type = pst.id"+
		   " INNER JOIN prn_strategy_type pst ON pq.strategy = pst.id"+
		   " LEFT OUTER JOIN prn_sub_queue psq ON pq.id = psq.prn_queue AND psq.sub_queue = ?"+
		   " LEFT OUTER JOIN lab ON IFNULL(psq.lab, pq.lab) = lab.id"+
		 " WHERE pq.id = ?";
		 */
		SelectResult<PrnQueue> result=loadQueueInternal( queue, subQueue);
		if(!result.isComplete() || result.getData().isEmpty()) return result;
		//load print group
		SelectResult<PrintGroup> selRes=loadQueueItems(result.getData().get(0).getId(), result.getData().get(0).getSub_queue(), false);
		if(selRes.isComplete()){
			result.getData().get(0).setPrintGroups(selRes.getData());
		}else{
			result.cloneError(selRes);
		}
		return result;
	}
	
	private SelectResult<PrnQueue> loadQueueInternal(int queue, int subQueue){
		String sql="SELECT IFNULL(psq.lab, pq.lab) lab, pst.default_priority priority, pq.created, pq.id, IFNULL(psq.sub_queue, 0) sub_queue,"+
				" pq.strategy, pq.is_active, IFNULL(psq.started, pq.started) started, pq.label,"+
				" lab.name lab_name, pq.strategy strategy_type, pst.name strategy_type_name, pq.is_reprint, pq.lab_type"+
		 " FROM prn_queue pq"+
		 //  " INNER JOIN prn_strategy ps ON pq.strategy=ps.id"+
		 //  " INNER JOIN prn_strategy_type pst ON ps.strategy_type = pst.id"+
		   " INNER JOIN prn_strategy_type pst ON pq.strategy = pst.id"+
		   " LEFT OUTER JOIN prn_sub_queue psq ON pq.id = psq.prn_queue AND psq.sub_queue = ?"+
		   " LEFT OUTER JOIN lab ON IFNULL(psq.lab, pq.lab) = lab.id"+
		 " WHERE pq.id = ?";
		return runSelect(PrnQueue.class, sql, subQueue, queue);
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
							" lab.name lab_name, pq.strategy strategy_type, pst.name strategy_type_name, pq.is_reprint, pq.lab_type"+
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
		String sql="SELECT psq.lab , pst.default_priority priority, pq.strategy strategy_type, pq.created, psq.complited, pq.id, psq.sub_queue, pq.strategy, pq.is_active, psq.started, pq.label, lab.name lab_name, pst.name strategy_type_name, pq.is_reprint, pq.lab_type"+
					 " FROM prn_sub_queue psq"+
					   " INNER JOIN prn_queue pq ON pq.id = psq.prn_queue"+
					  // " INNER JOIN prn_strategy ps ON pq.strategy = ps.id"+
					  // " INNER JOIN prn_strategy_type pst ON ps.strategy_type = pst.id"+
					   " INNER JOIN prn_strategy_type pst ON pq.strategy = pst.id"+
					    " LEFT OUTER JOIN lab ON psq.lab = lab.id"+
					  " WHERE psq.complited > DATE(?) AND psq.complited < DATE_ADD(DATE(?), INTERVAL 1 DAY)"+
					" UNION ALL"+
					" SELECT pq.lab, pst.default_priority priority, pq.strategy strategy_type, pq.created, pq.complited, pq.id, 0 sub_queue, pq.strategy, pq.is_active, pq.started, pq.label, lab.name lab_name, pst.name strategy_type_name, pq.is_reprint, pq.lab_type"+
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

	@Override
	public SelectResult<PrnQueue> loadQueueItemsByPG(String pgId, boolean loadRejects){
		SelectResult<PrnQueue> result= new SelectResult<PrnQueue>();
		SelectResult<PrintGroup> resultPG;
		String sql="SELECT pg.* FROM print_group pg WHERE pg.id =?";
		resultPG=runSelect(PrintGroup.class, sql, pgId);
		if(!resultPG.isComplete() || resultPG.getData()==null || resultPG.getData().isEmpty()){
			result.cloneError(resultPG);
			return result;
		}
		int queue=resultPG.getData().get(0).getPrn_queue();
		if(queue!=0){
			result=loadQueueInternal(queue, 0);
			if(!result.isComplete() || result.getData()==null || result.getData().isEmpty()) return result;
			resultPG= loadQueueItems(queue,0,true);
			if(!resultPG.isComplete()){
				result.cloneError(resultPG);
				return result;
			}
			result.getData().get(0).setPrintGroups(resultPG.getData());
		}else{
			//create dummy queue
			result.setData(new ArrayList<PrnQueue>());
			result.getData().add(new PrnQueue());
			result.getData().get(0).setLabel("Простая печать");
			result.getData().get(0).setPrintGroups(resultPG.getData());
		}
		if(loadRejects && resultPG.getData()!=null){
			OrderServiceImpl svc= new OrderServiceImpl();
			for(PrintGroup item : resultPG.getData()){
				SelectResult<PrintGroupReject> subres=svc.loadRejects4Order(item.getId());
				if(!subres.isComplete()){
					result.cloneError(subres);
					return result;
				}
				item.setRejects(subres.getData());
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
		sql=sql +" ORDER BY pqi.seq";
		return runSelect(PrintGroup.class, sql, queue, subQueue);
	}

	@Override
	public SelectResult<PrnQueueTimetable> loadStartTimetable(){
		//" (SELECT MAX(tt2.last_start) FROM prn_queue_start_timetable tt2 WHERE tt2.lab_type = tt.lab_type) last_start_c"+
		String sql="SELECT tt.*, lt.name lab_type_name, st.name strategy_type_name"+
				  " FROM prn_queue_start_timetable tt"+
				    " LEFT OUTER JOIN src_type lt ON tt.lab_type = lt.id"+
				    " LEFT OUTER JOIN prn_strategy_type st ON tt.strategy_type = st.id"+
				  " ORDER BY tt.time_start, tt.lab_type, tt.strategy_type";
		return runSelect(PrnQueueTimetable.class, sql);
	}

	@Override
	public SelectResult<PrnQueueTimetable> persistStartTimetable(List<PrnQueueTimetable> items){
		SelectResult<PrnQueueTimetable> res = new SelectResult<PrnQueueTimetable>();
		SqlResult result=new SqlResult();
		List<PrnQueueTimetable> insertList=new ArrayList<PrnQueueTimetable>();
		List<PrnQueueTimetable> updateList=new ArrayList<PrnQueueTimetable>();

		for(PrnQueueTimetable item : items){
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
			res=loadStartTimetable();
		}
		return res;
	}

	@Override
	public SelectResult<PrnQueueTimetable> deleteStartTimetable(int id){
		String sql="DELETE FROM prn_queue_start_timetable WHERE id = ?";
		runDML(sql, id);
		return loadStartTimetable();
	}

}
