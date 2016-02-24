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
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.StaffActivity;
import com.photodispatcher.model.mysql.entities.TechReject;
import com.photodispatcher.model.mysql.entities.TechRejectItems;

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
			for(TechRejectItems item : reject.getItems()) item.setTech_reject(reject.getId());
			SqlResult subRes=runInsertBatch(reject.getItems());
			if(!subRes.isComplete()) result.cloneError(subRes);
		}else{
			result.cloneError(dres);
		}
		return result;
	}
	
	@Override
	public SelectResult<TechReject> loadReprintWaite(){
		String sql="SELECT * FROM tech_reject tr WHERE tr.state = 145 ORDER BY tr.order_id";
		return runSelect(TechReject.class, sql);
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
				String sql="SELECT * FROM tech_reject tr WHERE tr.id=?";
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
						SelectResult<TechRejectItems> itemsRes=loadItems(reject.getId());
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
	
	private SelectResult<TechRejectItems> loadItems(int rejectId){
		String sql="SELECT * FROM tech_reject_items tri WHERE tri.tech_reject=?";
		return runSelect(TechRejectItems.class,sql,rejectId);
	}
}
