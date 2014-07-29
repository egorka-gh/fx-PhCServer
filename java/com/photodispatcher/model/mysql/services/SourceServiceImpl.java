package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;


import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.Source;
import com.photodispatcher.model.mysql.entities.SourceSvc;

@Service("sourceService")
public class SourceServiceImpl extends AbstractDAO implements SourceService {

	@Override
	public SelectResult<Source> loadAll(int locationType){
		SelectResult<Source> result;
		String sql="SELECT s.id, s.name, s.type, s.code, ifnull(ss.sync,0) sync, s.online, st.name type_name, st.loc_type"+
					" FROM phcconfig.sources s" +
					" INNER JOIN phcconfig.src_type st ON st.id = s.type"+
					" LEFT OUTER JOIN phcdata.sources_sync ss on s.id=ss.id" +
					" WHERE st.loc_type = ? ORDER BY s.name";
		result=runSelect(Source.class, sql, locationType);
		
		if (result.isComplete()&& (locationType==Source.LOCATION_TYPE_SOURCE || locationType==Source.LOCATION_TYPE_LAB)){
			//fill services
			for (Source source : result.getData()){
				sql="SELECT s.*, st.name type_name, st.loc_type"+
						" FROM phcconfig.services s INNER JOIN phcconfig.srvc_type st ON st.id = s.srvc_id"+
						" WHERE s.src_id = ?";
				SelectResult<SourceSvc> childs=runSelect(SourceSvc.class, sql, source.getId());
				if(childs.isComplete()){
					for (SourceSvc service : childs.getData()){
						switch (service.getSrvc_id()) {
						case SourceSvc.FBOOK_SERVICE:
							source.setFbookService(service);
							break;
						case SourceSvc.FTP_SERVICE:
							source.setFtpService(service);
							break;
						case SourceSvc.HOT_FOLDER:
							source.setHotFolder(service);
							break;
						case SourceSvc.WEB_SERVICE:
							source.setWebService(service);
							break;
						}
					}
				}else{
					result.cloneError(childs);
				}
			}
		}
		return result;
	}
	
	@Override
	public DmlResult persist(Source source){
		DmlResult result=new DmlResult();
		if(source.getPersistState()==0){
			//insert 
			result=runInsert(source,null);
			if(result.isComplete()) result.setLastId(source.getId());
		}else{
			if(source.getPersistState()==-1){
				result=runUpdate(source,null);
			}
			if(result.isComplete()){
				List<SourceSvc> insertList=new ArrayList<SourceSvc>();
				List<SourceSvc> updateList=new ArrayList<SourceSvc>();
				
				if(source.getFbookService()!=null){
					if(source.getFbookService().getPersistState()==0){
						insertList.add(source.getFbookService());
					}else if(source.getFbookService().getPersistState()==-1){
						updateList.add(source.getFbookService());
					}
				}
				if(source.getFtpService()!=null){
					if(source.getFtpService().getPersistState()==0){
						insertList.add(source.getFtpService());
					}else if(source.getFtpService().getPersistState()==-1){
						updateList.add(source.getFtpService());
					}
				}
				if(source.getHotFolder()!=null){
					if(source.getHotFolder().getPersistState()==0){
						insertList.add(source.getHotFolder());
					}else if(source.getHotFolder().getPersistState()==-1){
						updateList.add(source.getHotFolder());
					}
				}
				if(source.getWebService()!=null){
					if(source.getWebService().getPersistState()==0){
						insertList.add(source.getWebService());
					}else if(source.getWebService().getPersistState()==-1){
						updateList.add(source.getWebService());
					}
				}
				
				if(!insertList.isEmpty()){
					result=runInsertBatch(insertList, null);
				}
				if(result.isComplete() && !updateList.isEmpty()){
					result=runUpdateBatch(updateList, null);
				}
			}
		}

		return result;
	}
	
}
