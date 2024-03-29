package com.photodispatcher.model.mysql.services;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.sansorm.SqlClosureElf;
import org.sansorm.internal.OrmWriter;
import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.ConnectionFactory;
import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.Order;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.StaffActivity;
import com.photodispatcher.model.mysql.entities.TechReject;
import com.photodispatcher.model.mysql.entities.TechRejectItem;
import com.photodispatcher.model.mysql.entities.TechRejectPG;

@Service("techRejecService")
public class TechRejecServiceImpl extends AbstractDAO implements TechRejecService{

	@Override
	public SqlResult create(TechReject reject){
		SqlResult result= new SqlResult();
		
		if(reject==null || reject.getItems()==null || reject.getItems().isEmpty()) return result;
		if(reject.getActivityObj()!=null){
			//create activity
			StaffActivityServiceImpl as= new StaffActivityServiceImpl();
			DmlResult<StaffActivity> subres=as.logActivity(reject.getActivityObj());
			if (subres.isComplete()) reject.setActivity(reject.getActivityObj().getId());
		}
		DmlResult<TechReject> dres= runInsert(reject);
		if(dres.isComplete()){
			for(TechRejectItem item : reject.getItems()) item.setTech_reject(reject.getId());
			SqlResult subRes=runInsertBatch(reject.getItems());
			if(!subRes.isComplete()) result.cloneError(subRes);
		}else{
			result.cloneError(dres);
		}
		return result;
	}
	
	@Override
	public SelectResult<Order> loadReprintWaiteAsOrder(){
		String sql="SELECT o.id, o.source, tr.state, MIN(tr.state_date) state_date, s.name source_name, os.name state_name, o.ftp_folder, o.group_id, o.src_date, 'reject' tag"+
				  " FROM tech_reject tr"+
				    " INNER JOIN orders o ON tr.order_id = o.id"+
				    " INNER JOIN order_state os ON tr.state = os.id"+
				    " INNER JOIN sources s ON o.source = s.id"+
				  " WHERE tr.state = 145"+
				  " GROUP BY tr.order_id";
		return runSelect(Order.class, sql);
	}

	@Override
	public SelectResult<TechReject> loadByState(int stateFrom, int stateTo){
		String sql="SELECT tr.*, sa.remark sa_remark, s.name staff_name, sat.name sa_type_name, os.name state_name"+
					 " FROM tech_reject tr"+
					   " INNER JOIN order_state os ON os.id=tr.state"+
					   " LEFT OUTER JOIN staff_activity sa ON tr.activity = sa.id"+
					   " LEFT OUTER JOIN staff s ON sa.staff = s.id"+
					   " LEFT OUTER JOIN staff_activity_type sat ON sa.sa_type = sat.id"+
					  " WHERE tr.state BETWEEN ? AND ?";
		SelectResult<TechReject> result= runSelect(TechReject.class, sql, stateFrom, stateTo);
		if(!result.isComplete() || result.getData()==null) return result;
		
		for(TechReject reject : result.getData()){
			SelectResult<TechRejectItem> itemsRes=loadItems(reject.getId());
			if(!itemsRes.isComplete()){
				result.cloneError(itemsRes);
				break;
			}else{
				reject.setItems(itemsRes.getData());
			}
		}
		return result;
	}

	@Override
	public SelectResult<TechReject> loadByOrder(String orderId, int state){
		String sql="SELECT * FROM tech_reject tr WHERE tr.order_id= ? AND ? IN (tr.state,0)";
		return runSelect(TechReject.class, sql, orderId, state);
	}
	
	@Override
	public DmlResult<TechReject> updateReject(TechReject item){
		return runUpdate(item);
	}

	@Override
	public SqlResult updateRejectBatch(List<TechReject> items){
		SqlResult result;
		String sql="DELETE FROM tech_reject_pg WHERE tech_reject=?";
		ArrayList<TechRejectPG> pgLinks= new ArrayList<TechRejectPG>();
		for(TechReject reject : items){
			if(reject.getPgroups()!=null && !reject.getPgroups().isEmpty()){
				//recreate links
				runDML(sql, reject.getId());
				pgLinks.addAll(reject.getPgroups());
			}
		}
		if(!pgLinks.isEmpty()){
			result=runInsertBatch(pgLinks);
			if(!result.isComplete()) return result;
		}
		return runUpdateBatch(items);
	}

	@Override
	public SqlResult cancelReject(int itemId){
		String sql="DELETE FROM tech_reject WHERE id=? AND state=145";
		return runDML(sql, itemId);
	}

	@Override
	public SqlResult cancelRejectByOrder(String orderId){
		String sql="DELETE FROM tech_reject WHERE order_id=? AND state=145";
		return runDML(sql, orderId);
	}

	@Override
	public SelectResult<TechReject> captureState(List<TechReject> rejects){
		SelectResult<TechReject> result= new SelectResult<TechReject>();
		result.setData(new ArrayList<TechReject>());
		
		Connection connection = null;
		String sqlUpdate="UPDATE tech_reject tr"+
						  " SET tr.state=?, tr.state_date=?"+
						  " WHERE tr.id=? AND tr.state<?";
		boolean updated=false;
		
		for(TechReject reject : rejects){
			updated=false;
			try {
				connection=ConnectionFactory.getConnection();
				updated=OrmWriter.executeUpdate(connection, sqlUpdate, reject.getState(), reject.getState_date(), reject.getId(), reject.getState())>0;
			} catch (SQLException e) {
				updated=false;
			}finally{
				SqlClosureElf.quietClose(connection);
			}

			if(!updated){
				reject.setState(-300);
			}else{
				//reload reject
				String sql="SELECT tr.*, sat.name sa_type_name, sa.remark sa_remark"+
							 " FROM tech_reject tr"+ 
							 " LEFT OUTER JOIN staff_activity sa ON tr.activity=sa.id"+
							 " LEFT OUTER JOIN staff_activity_type sat ON sa.sa_type = sat.id"+
							 " WHERE tr.id=?";
				SelectResult<TechReject> selRes=runSelect(TechReject.class,sql,reject.getId());
				if(!selRes.isComplete() || selRes.getData()==null || selRes.getData().isEmpty()){
					updated=false;
					reject.setState(-309);
				}else{
					TechReject tr=selRes.getData().get(0);
					if(tr.getState()!=reject.getState()){
						updated=false;
						reject.setState(-300);
					}else{
						reject=tr;
						//load items
						SelectResult<TechRejectItem> itemsRes=loadItems(reject.getId());
						if(!itemsRes.isComplete()){
							updated=false;
							reject.setState(-309);
						}else{
							reject.setItems(itemsRes.getData());
						}
					}
				}
			}
			if(updated) result.getData().add(reject);
		}
		return result;
	}
	
	private SelectResult<TechRejectItem> loadItems(int rejectId){
		String sql="SELECT *, tu.name thech_unit_name"+
					 " FROM tech_reject_items tri"+
					   " LEFT OUTER JOIN tech_unit tu ON tu.id = tri.thech_unit"+
					 " WHERE tri.tech_reject = ?";
		return runSelect(TechRejectItem.class,sql,rejectId);
	}
	
	/*
	@Override
	public SelectResult<TechRejectItem> loadItemsByPg(String pgId){
		//TODO refactor
		String sql="SELECT tri.*, tu.name thech_unit_name, pgd.id pg_dst" +
					" FROM print_group pg" +
					 " INNER JOIN tech_reject tr ON pg.order_id=tr.order_id" +
					 " INNER JOIN tech_reject_items tri ON tr.id = tri.tech_reject AND (tri.pg_src=pg.id OR tri.thech_unit=3)" +
					 " INNER JOIN tech_unit tu ON tri.thech_unit=tu.id" +
					 " LEFT OUTER JOIN tech_reject_pg trp ON tr.id = trp.tech_reject AND trp.pg_src=pg.id" +
					 " LEFT OUTER JOIN print_group pgd ON trp.pg_dst=pgd.id AND pgd.state<450" +
					 " WHERE pg.id=?";
		return runSelect(TechRejectItem.class, sql, pgId);
	}

	@Override
	public SelectResult<TechRejectItem> loadItemsByOrder(String orderId){
		//TODO refactor
		String sql="SELECT tri.*, tu.name thech_unit_name, pgd.id pg_dst" +
					" FROM tech_reject tr" +
					 " INNER JOIN tech_reject_items tri ON tr.id = tri.tech_reject" +
					 " INNER JOIN tech_unit tu ON tri.thech_unit=tu.id" +
					 " LEFT OUTER JOIN tech_reject_pg trp ON tr.id = trp.tech_reject AND trp.pg_src=tri.pg_src" +
					 " LEFT OUTER JOIN print_group pgd ON trp.pg_dst=pgd.id AND pgd.state<450" +
					 " WHERE tr.order_id=?";
		return runSelect(TechRejectItem.class, sql, orderId);
	}
	*/

}
