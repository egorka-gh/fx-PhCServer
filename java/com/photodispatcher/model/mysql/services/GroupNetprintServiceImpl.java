package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.GroupNetprint;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@Service("groupNetprintService")
public class GroupNetprintServiceImpl extends AbstractDAO  implements GroupNetprintService {
	
	@Override
	public SelectResult<GroupNetprint> findeByNetprint(int source, String netprint, Boolean setSend){
		String sql="SELECT n.* FROM group_netprint n WHERE n.source = ? AND n.netprint_id = ?";
		SelectResult<GroupNetprint> res =runSelect(GroupNetprint.class, sql, source, netprint);
		if (!res.isComplete() || res.getData() == null || res.getData().size() == 0) return res;
		int group =res.getData().get(0).getGroupId();
		if (setSend){
			sql="UPDATE group_netprint SET send = 1 WHERE source = ? AND group_id = ? AND netprint_id = ?";
			SqlResult r = runDML(sql, source, group, netprint);			
			if(!r.isComplete()) {
				res.cloneError(r);
				return res;
			}
		}
		return loadByGroup(source, group);
	}

	@Override
	public SelectResult<GroupNetprint> loadByGroup(int source, int group){
		String sql="SELECT * FROM group_netprint WHERE source = ? AND group_id = ?";
		return runSelect(GroupNetprint.class, sql, source, group);
	}

	@Override
	public SqlResult save(List<GroupNetprint> items){		
		return runUpdateBatch(items) ;
	}

}
