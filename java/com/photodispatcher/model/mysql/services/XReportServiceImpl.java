package com.photodispatcher.model.mysql.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.granite.context.GraniteContext;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.sansorm.OrmElf;
import org.sansorm.SqlClosure;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xreport.constants.Constants;
import org.xreport.util.ValueDistributorImpl;

import com.photodispatcher.model.mysql.ConnectionFactory;
import com.photodispatcher.model.mysql.entities.DeliveryTypePrintForm;
import com.photodispatcher.model.mysql.entities.PrintFormFieldItem;
import com.photodispatcher.model.mysql.entities.PrintFormParametr;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.report.Parameter;
import com.photodispatcher.model.mysql.entities.report.Report;
import com.photodispatcher.model.mysql.entities.report.ReportGroup;
import com.photodispatcher.model.mysql.entities.report.ReportResult;
import com.photodispatcher.model.mysql.entities.report.ReportSource;
import com.photodispatcher.model.mysql.entities.report.ReportSourceType;
import com.reporter.XlsReporter;
import com.reporter.document.XLSDocumentWriter;


@Service("xReportService")
public class XReportServiceImpl extends AbstractDAO implements XReportService {

	//private Connection conn;
	/*
	public XlsReportServiceImpl() {
		super();
		try {
			conn= ConnectionFactory.getConnection();
		} catch (SQLException e) {
			conn=null;
			e.printStackTrace();
		}
	}
	*/

	@Override
	public List<ReportSourceType> getSourceTypes() {
		return new SqlClosure<List<ReportSourceType>>(ConnectionFactory.getDataSource()) {
			public List<ReportSourceType> execute(Connection connection) {
				try {
					PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM xrep_source_type ORDER BY id");
					return OrmElf.statementToList(pstmt, ReportSourceType.class);
				} catch (SQLException e) {
					e.printStackTrace();
					return null;
				}
			}
		}.execute();		
	}

	@Override
	public List<ReportSource> getSources() {
		return new SqlClosure<List<ReportSource>>(ConnectionFactory.getDataSource()) {
			public List<ReportSource> execute(Connection connection) {
				try {
					PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM xrep_source ORDER BY id DESC");
					return OrmElf.statementToList(pstmt, ReportSource.class);
				} catch (SQLException e) {
					e.printStackTrace();
					return null;
				}
			}
		}.execute();		
	}

	@Override
	public List<ReportGroup> getGroups(final int sourceType) {
		return new SqlClosure<List<ReportGroup>>(ConnectionFactory.getDataSource()) {
			public List<ReportGroup> execute(Connection connection) {
				String sql="SELECT rg.* FROM xrep_report_group rg WHERE rg.hidden = 0 AND rg.src_type IN(0, ?) ORDER BY rg.id";
				try {
					PreparedStatement pstmt = connection.prepareStatement(sql);
					return OrmElf.statementToList(pstmt, ReportGroup.class, sourceType);
				} catch (SQLException e) {
					e.printStackTrace();
					return null;
				}
			}
		}.execute();		
	}

	@Override
	public List<Report> getReports(final int sourceType) {
		return new SqlClosure<List<Report>>(ConnectionFactory.getDataSource()) {
			public List<Report> execute(Connection connection) {
				String sql="SELECT r.*, rg.name group_name"+
							" FROM xrep_report r INNER JOIN xrep_report_group rg ON r.rep_group = rg.id" +
							" WHERE r.hidden = 0 AND rg.hidden = 0 AND r.src_type = ?";
				try {
					PreparedStatement pstmt = connection.prepareStatement(sql);
					return OrmElf.statementToList(pstmt, Report.class, sourceType);
				} catch (SQLException e) {
					e.printStackTrace();
					return null;
				}
			}
		}.execute();		
	}

	@Override
	public List<Parameter> getReportParams(final String report) {
		return new SqlClosure<List<Parameter>>(ConnectionFactory.getDataSource()) {
			public List<Parameter> execute(Connection connection) {
				try {
					PreparedStatement pstmt = connection.prepareStatement("SELECT p.* FROM xrep_report_params rp INNER JOIN xrep_parameter p ON p.id = rp.parameter WHERE rp.report = ?");
					return OrmElf.statementToList(pstmt, Parameter.class, report);
				} catch (SQLException e) {
					e.printStackTrace();
					return null;
				}
			}
		}.execute();		
	}


	@Override
	public SqlResult releaseReport(final ReportResult report){
		SqlResult result= new SqlResult();
		if(report==null || report.getMessageId()==null || report.getMessageId().isEmpty()){
			return result;
		}
		HttpGraniteContext ctx = (HttpGraniteContext)GraniteContext.getCurrentInstance();
		String outPath=(String)ctx.getServletContext().getInitParameter(Constants.OUT_FOLDER_INIT_PARAMETER);
		outPath=outPath+"/"+report.getMessageId()+"/";
		//create dir
		File outDir=new File(outPath);
		if(!outDir.exists()) return result;
		try {  
            FileUtils.deleteDirectory(outDir);  
		} catch (Exception e) {
			result.setErrMesage(e.getMessage());
			result.setComplete(false);
		}  
		return result;
	}

	@Override
	public ReportResult buildReport(final Report report, String source) {
		//TODO clear report folders (getMessageId) older then current date
		ReportResult result= new ReportResult();
		if(report == null){
			result.assignError("null report");
			return result;
		}
		result.setId(report.getId());
		
		HttpGraniteContext ctx = (HttpGraniteContext)GraniteContext.getCurrentInstance();

		result.setMessageId(ctx.getAMFContext().getRequest().getMessageId());
		
		if(result.getMessageId()==null || result.getMessageId().isEmpty()){
			result.assignError("Empty MessageId");
            return result;
		}
		
		//String outPath=(String)ctx.getServletContext().getAttribute(Constants.OUT_FOLDER_SESSION_ATTRIBUTE);
		String outPath=(String)ctx.getServletContext().getInitParameter(Constants.OUT_FOLDER_INIT_PARAMETER);
		outPath=outPath+"/"+result.getMessageId()+"/";
		//create dir
		File outDir=new File(outPath);
		try {
			outDir.mkdirs();
		} catch (Exception e) {
			result.assignError("Can't create: "+outPath+"; err:"+e.getMessage());
            return result;
		}
		
		//String outputUrl = (String)ctx.getServletContext().getAttribute(Constants.SESSION_ID_ATTRIBUTE);
		String outputUrl = result.getMessageId();
		outputUrl=Constants.URL_REPORT_BASE_URL+"/"+outputUrl+"/"+report.getId()+ Constants.REPORT_EXT;
		Date dts= new Date();
		outputUrl+="?"+dts.getTime();
		result.setUrl(outputUrl);
		
		String outputName = outPath+report.getId()+ Constants.REPORT_EXT;
		File outFile = new File(outputName);
		
		OutputStream outputStream=null;
		try {
			outputStream = new FileOutputStream(outFile);
		} catch (Exception e) {
			result.assignError("Can't open: "+outFile+"; err"+e.getMessage());
            return result;
		}
		
		String rootPath = ctx.getServletContext().getRealPath("/");
		rootPath+="/"+Constants.REPORTS_FOLDER+"/";
        String templateName=rootPath+report.getId()+ Constants.REPORT_EXT;
        String templateXml=rootPath+report.getId()+ Constants.XML_EXT;
		
		XlsReporter reporter = new XlsReporter();
		
        File inFile;
        InputStream inputStream;
        try {
            inFile = new File(templateName);
            inputStream = new FileInputStream(inFile);
        } catch (Exception e1) {
            XLSDocumentWriter documentWriter = new XLSDocumentWriter();
        	HSSFWorkbook outputTemplate = new HSSFWorkbook();
        	reporter.fillOutputWithException(outputTemplate, e1);
            documentWriter.writeDocument(outputTemplate, outputStream);
            e1.printStackTrace();
            result.assignError(e1.getMessage());
            return result;
        }

		InputStream xmlStream;
    	File xmlFile = new File(templateXml);
        //parse data xml
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document document = null;
        try {
    		xmlStream = new FileInputStream(xmlFile);
            builder = factory.newDocumentBuilder();
            document = builder.parse(xmlStream);
        } catch (Exception e1) {
            XLSDocumentWriter documentWriter = new XLSDocumentWriter();
        	HSSFWorkbook outputTemplate = new HSSFWorkbook();
        	reporter.fillOutputWithException(outputTemplate, e1);
            documentWriter.writeDocument(outputTemplate, outputStream);
            e1.printStackTrace();
            result.assignError(e1.getMessage());
            return result;
        }
        try {
			xmlStream.close();
		} catch (IOException e2) {}
        
        Connection cnn=null;
        try {
        	cnn=ConnectionFactory.getConnection(source);
        	//fill ValueDistributor
        	ValueDistributorImpl vd = new ValueDistributorImpl(report.getParameters());
        	//build report
			reporter.process(inputStream, outputStream, document,cnn, vd); 
        } catch (Exception e1) {
            XLSDocumentWriter documentWriter = new XLSDocumentWriter();
        	HSSFWorkbook outputTemplate = new HSSFWorkbook();
        	reporter.fillOutputWithException(outputTemplate, e1);
            documentWriter.writeDocument(outputTemplate, outputStream);
            e1.printStackTrace();
            result.assignError(e1.getMessage());
            return result;
        }

        try {
			if(cnn!=null) cnn.close();
	        if(outputStream!=null) outputStream.close();
	        //inputStream.close();
		} catch (Exception e) {
		}
        
        return result;
        /*
		//4 debug
		String res="id:"+report.getId()+";";
		res+=" outPath:"+outPath;
		if(report.getParameters()!=null){
			res=res+" parms:"+report.getParameters().length+";";
			for (Parameter p: report.getParameters()){
				res=res+"\n"+"parm:"+p.getName()+":"+p.getValFrom()+"-"+p.getValTo();
			}
		}else{
			res=res+" parms:null;";
		}
		return res;
		*/
	}

	
	@Override
	public SelectResult<PrintFormFieldItem> getPrintFormFieldItems() {
		String sql="SELECT it.*, a.field property"+
					 " FROM form_field_items it"+
					   " LEFT OUTER JOIN attr_type a ON a.id = it.attr_type"+
					 " ORDER BY form_field, sequence";
		
		return runSelect(PrintFormFieldItem.class,sql);
	}

	@Override
	public SelectResult<DeliveryTypePrintForm> getPrintForms() {
		String sql="SELECT df.*, dt.name delivery_type_name, f.name form_name, f.report"+
					 " FROM delivery_type_form df"+
					   " INNER JOIN delivery_type dt ON dt.id = df.delivery_type"+
					   " INNER JOIN form f ON f.id = df.form"+
					 " ORDER BY df.delivery_type, df.form";
	
		return runSelect(DeliveryTypePrintForm.class,sql);
	}

	@Override
	public SelectResult<PrintFormParametr> getPrintFormParameters() {
		String sql="SELECT fp.*, ff.parametr, ff.simplex"+
					 " FROM form_parametr fp"+
					   " INNER JOIN form_field ff ON ff.id = fp.form_field"+
					 " ORDER BY fp.form";
		return runSelect(PrintFormParametr.class,sql);
	}

}
