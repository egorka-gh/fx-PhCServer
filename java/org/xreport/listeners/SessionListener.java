package org.xreport.listeners;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.xreport.constants.Constants;

public class SessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent arg0) {
		HttpSession session = arg0.getSession();
		//String path=session.getServletContext().getRealPath("/");
		String path=session.getServletContext().getInitParameter(Constants.OUT_FOLDER_INIT_PARAMETER);
		String sessionId = (String)session.getId();
		session.getServletContext().setAttribute(Constants.SESSION_ID_ATTRIBUTE, sessionId);
		path=path+"/"+sessionId+"/";
		session.getServletContext().setAttribute(Constants.OUT_FOLDER_SESSION_ATTRIBUTE, path);
        new File(path).mkdirs();
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent arg0) {
		HttpSession session = arg0.getSession();
		//String path=session.getServletContext().getRealPath("/");
		String path=session.getServletContext().getInitParameter(Constants.OUT_FOLDER_INIT_PARAMETER);
		String sessionId = (String)session.getId();  
		try {  
            FileUtils.deleteDirectory(new File(path+"/"+sessionId+"/"));  
        } catch (IOException e) {e.printStackTrace();}  
	}

}
