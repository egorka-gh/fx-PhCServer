package org.xreport.util;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import com.mavaris.webcaravella.element.ValueDistributor;
import com.photodispatcher.model.mysql.entities.report.Parameter;

public class ValueDistributorImpl implements ValueDistributor {

	private Map<String, String> values;
	
	public ValueDistributorImpl(Parameter[] parameters){
		values = new HashMap<String, String>();
		Parameter p;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
		if(parameters!=null){
			for(int i=0; i<parameters.length; i++){
				p=parameters[i];
				if(p.getId().equals(ParameterType.PPeriod.getType())){
					values.put("pfrom",	dateFormat.format(p.getValFrom()));
					values.put("pto", dateFormat.format(p.getValTo()));
				}else if(p.getId().equals(ParameterType.PDate.getType())){
					values.put(p.getId().toLowerCase(), dateFormat.format(p.getValDate()));
				}else if(p.getId().equals(ParameterType.PDateTime.getType())){
					dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					values.put(p.getId().toLowerCase(), dateFormat.format(p.getValDate()));
				/*}else if(p.getId().equals(ParameterType.PState.getType())
						|| p.getId().equals(ParameterType.PStateTo.getType())){
					values.put(p.getId().toLowerCase(), p.getValInt() );*/
				}else{
					values.put(p.getId().toLowerCase(), p.getValString());
				}
			}
		}
	}

	@Override
	public String getValue(String elementName) {
   	 if (elementName == null){
		 return null;
	 }
     return values.get(elementName);
	}

}
