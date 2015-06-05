package com.photodispatcher.model.mysql.services;

import javax.servlet.http.HttpServletRequest;

import org.granite.context.GraniteContext;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.HelloResponce;

@Service("halloService")
public class HalloServiceImpl implements HalloService {

	@Override
	public HelloResponce ping() {
		HelloResponce res= new HelloResponce();
		
		res.setResponce("Ping complite.");
		HttpGraniteContext ctx = (HttpGraniteContext)GraniteContext.getCurrentInstance();
		HttpServletRequest req= ctx.getRequest();
		if(req!= null){
			res.setHostName(req.getRemoteHost());
			res.setHostIP(req.getRemoteAddr());
		}
		return res;
	}

}
