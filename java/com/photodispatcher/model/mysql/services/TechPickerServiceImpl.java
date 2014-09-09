package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.Layer;
import com.photodispatcher.model.mysql.entities.LayerSequence;
import com.photodispatcher.model.mysql.entities.Layerset;
import com.photodispatcher.model.mysql.entities.LayersetGroup;
import com.photodispatcher.model.mysql.entities.LayersetSynonym;
import com.photodispatcher.model.mysql.entities.PrintGroup;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@Service("techPickerService")
public class TechPickerServiceImpl extends AbstractDAO implements TechPickerService {
	public static final int LAYER_EMPTY=0;
	public static final int LAYER_SHEET=1;
	public static final int LAYER_ENDPAPER=2;
	public static final int LAYERSET_TYPE_TEMPLATE=0;
	public static final int LAYERSET_TYPE_INTERLAYER=1;
	public static final int LAYERSET_TYPE_ENDPAPER=2;
	public static final int SEQGROUP_START=0;
	public static final int SEQGROUP_MID=2;
	public static final int SEQGROUP_END=3;

	
	@Override
	public SelectResult<LayersetGroup> loadLayersetGroups(){
		SelectResult<LayersetGroup> result;
		String sql="SELECT s.* FROM layerset_group s";
		result=runSelect(LayersetGroup.class, sql);
		return result;
	}

	@Override
	public SqlResult persistLayersetGroups(List<LayersetGroup> items){
		return runPersistBatch(items);
	}

	@Override
	public SelectResult<Layerset> loadLayersets(int type, int techGroup){
		SelectResult<Layerset> result;
		String sql="SELECT s.*, bt.name book_type_name"+
					" FROM layerset s"+
					" INNER JOIN book_type bt ON bt.id=s.book_type"+
					" WHERE s.subset_type=? AND ? IN (-1, s.layerset_group)"+
				" ORDER BY s.passover DESC, s.name";
		result= runSelect(Layerset.class, sql, type, techGroup);
		if (result.isComplete()){
			for(Layerset ls : result.getData()){
				SelectResult<LayerSequence> subRes=loadSequence(ls.getId());
				if(subRes.isComplete()){
					List<LayerSequence> seqStart=new ArrayList<LayerSequence>();
					List<LayerSequence> seqMid=new ArrayList<LayerSequence>();
					List<LayerSequence> seqEnd=new ArrayList<LayerSequence>();
					for(LayerSequence seq :subRes.getData()){
						if(seq.getLayer_group()==SEQGROUP_START){
							seqStart.add(seq);
						}else if(seq.getLayer_group()==SEQGROUP_MID){
							seqMid.add(seq);
						}else if(seq.getLayer_group()==SEQGROUP_END){
							seqEnd.add(seq);
						}
						if(seq.getSeqlayer()==LAYER_ENDPAPER) ls.setUsesEndPaper(true);
					}
					ls.setSequenceEnd(seqEnd);
					ls.setSequenceMiddle(seqMid);
					ls.setSequenceStart(seqStart);
				}else{
					result.cloneError(subRes);
					break;
				}
				//load synonyms
				List<String> synonyms= new ArrayList<String>();
				synonyms.add(ls.getName());
				SelectResult<LayersetSynonym> synRes=loadLayersetSynonyms(ls.getId());
				if(!synRes.isComplete()){
					result.cloneError(synRes);
					break;
				}
				if(synRes.getData()!=null && !synRes.getData().isEmpty()){
					for(LayersetSynonym syn:synRes.getData()) synonyms.add(syn.getSynonym());
				}
				ls.setSynonyms(synonyms);
			}
			
		}
		return result;
	}

	@Override
	public SqlResult persistLayersets(List<Layerset> items){
		return runPersistBatch(items);
	}

	@Override
	public SelectResult<LayersetSynonym> loadLayersetSynonyms(int itemId){
		String sql="SELECT s.* FROM layerset_synonym s";
		if(itemId!=-1){
			sql+=" WHERE s.item_id=?";
		}
		sql+=" ORDER BY s.item_id, s.synonym";
		if(itemId!=-1){
			return runSelect(LayersetSynonym.class, sql, itemId);
		}else{
			return runSelect(LayersetSynonym.class, sql);
		}
	}

	@Override
	public SqlResult persistsLayersetSynonyms(List<LayersetSynonym> targetList){
		String delId=""; 
		List<LayersetSynonym> persistList=new ArrayList<LayersetSynonym>();
		for(LayersetSynonym item : targetList){
			if(item.getSynonym()==null || item.getSynonym().length()==0){
				if(item.getPersistState()!=0){
					if(delId.length()>0) delId+=",";
					delId+=Integer.toString(item.getId());
				}
			}else{
				persistList.add(item);
			}
		}

		SqlResult result=runPersistBatch(persistList);
		if(result.isComplete() && delId.length()>0){
			String sql="DELETE FROM layerset_synonym WHERE id IN("+delId+")";
			result=runDML(sql);
		}
		return result;
	}

	
	@Override
	public SelectResult<Layer> loadLayers(){
		SelectResult<Layer> result;
		String sql="SELECT s.id, s.name FROM layer s";
		result=runSelect(Layer.class, sql);
		return result;
	}

	@Override
	public SelectResult<Layer> persistLayers(List<Layer> items){
		SelectResult<Layer> result= new SelectResult<Layer>(); 
		SqlResult res=runPersistBatch(items);
		if(!res.isComplete()){
			result.cloneError(res);
			return result;
		}
		return loadLayers();
	}

	
	private SelectResult<LayerSequence> loadSequence(int layerset){
		SelectResult<LayerSequence> result;
		String sql="SELECT la.*, l.name  seqlayer_name"+
					" FROM layer_sequence la"+ 
					" INNER JOIN layer l ON l.id=la.seqlayer"+
					" WHERE la.layerset=?"+
					" ORDER BY la.layer_group, la.seqorder";
		result=runSelect(LayerSequence.class, sql, layerset);
		return result;
	}

	private SelectResult<LayerSequence> loadSequence(int layerset, int layergroup){
		SelectResult<LayerSequence> result;
		String sql="SELECT la.*, l.name  seqlayer_name"+
					" FROM layer_sequence la"+ 
					" INNER JOIN layer l ON l.id=la.seqlayer"+
					" WHERE la.layerset=? AND la.layer_group=?"+
					" ORDER BY la.layer_group, la.seqorder";
		result=runSelect(LayerSequence.class, sql, layerset, layergroup);
		return result;
	}

	@Override
	public SelectResult<LayerSequence> persistSequence(List<LayerSequence> items, int layerset, int layerGroup){
		SelectResult<LayerSequence> sRes= new SelectResult<LayerSequence>(); 
		SqlResult result= new SqlResult();
		if(items==null) return sRes;
		
		List<LayerSequence> updateList=new ArrayList<LayerSequence>();
		int seq=0;
		for(LayerSequence item : items){
			if(item.getSeqlayer()>1){
				//renum
				seq++;
				item.setSeqorder(seq);
				updateList.add(item);
			}
		}
		//save
		if(!updateList.isEmpty()){
			result=runUpdateBatch(updateList);
			if(result.isComplete()) result=runInsertBatch(updateList);
		}
		//del unused
		if(result.isComplete()){
			String sql="DELETE FROM layer_sequence WHERE layerset=? AND layer_group=? AND seqorder>?";
			result=runDML(sql, layerset, layerGroup, seq);
		}
		if(result.isComplete()){
			sRes=loadSequence(layerset, layerGroup);
		}else{
			sRes.cloneError(result);
		}
		return sRes;
	}

	@Override
	public SelectResult<Integer> bookNumByPGroup(String pgId){
		String sql="SELECT * FROM print_group WHERE id=?";
		SelectResult<PrintGroup> pgRes= runSelect(PrintGroup.class, sql, pgId);

		SelectResult<Integer> result= new SelectResult<Integer>();
		result.setData(new ArrayList<Integer>());
		if(pgRes.isComplete() && pgRes.getData()!=null && !pgRes.getData().isEmpty()){
			result.getData().add(pgRes.getData().get(0).getBook_num());
		}else{
			result.getData().add(-1);
		}
		return result;
	}

	/*
	@Override
	public SelectResult<Endpaper> loadEndpapers(){
		SelectResult<Endpaper> result;
		String sql="SELECT s.id, s.name FROM endpaper s";
		result=runSelect(Endpaper.class, sql);
		return result;
	}

	@Override
	public SqlResult persistEndpapers(List<Endpaper> items){
		for(Endpaper item : items){
			if(item.getId()==0){
				//predefined
				items.remove(item);
				break;
			}
		}
		return runPersistBatch(items);
	}
*/
}
