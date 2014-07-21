package com.photodispatcher.model.mysql.services;

import org.springframework.stereotype.Service;

@Service("halloService")
public class HalloServiceImpl implements HalloService {

	@Override
	public String ping() {
		return "Ping complite.";
	}

}
