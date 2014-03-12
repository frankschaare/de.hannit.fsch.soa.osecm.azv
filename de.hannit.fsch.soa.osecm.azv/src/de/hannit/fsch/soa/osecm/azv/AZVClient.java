/**
 * 
 */
package de.hannit.fsch.soa.osecm.azv;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.runtime.FileLocator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.hannit.fsch.soa.osecm.IAZVClient;
import de.optimalsystems.schemas.osecm.ws.EcmWsMtomSoapService;
import de.optimalsystems.schemas.osecm.ws.OsecmWsFaultException;
import de.optimalsystems.schemas.osecm.ws.OsecmWsPortType;
import de.optimalsystems.schemas.osecm.ws.types.Authentication;
import de.optimalsystems.schemas.osecm.ws.types.Base64AsStringParameter;
import de.optimalsystems.schemas.osecm.ws.types.ExecuteParameter;
import de.optimalsystems.schemas.osecm.ws.types.IntegerParameter;
import de.optimalsystems.schemas.osecm.ws.types.Job;
import de.optimalsystems.schemas.osecm.ws.types.Parameter;
import de.optimalsystems.schemas.osecm.ws.types.StringParameter;

/**
 * @author fsch
 *
 */
public class AZVClient implements IAZVClient
{
private	Properties props = null;
private Authentication auth = null;
private URL xmlURL = null;
private Base64AsStringParameter xmlRequest = null;
private EcmWsMtomSoapService service = new EcmWsMtomSoapService();
private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
private Document request = null;
private XPathFactory xpathfactory = XPathFactory.newInstance();
private XPath xpath = xpathfactory.newXPath();
private XPathExpression xpBerichtsMonat = null;
private XPathExpression xpBerichtsJahr = null;
private TransformerFactory tFactory = TransformerFactory.newInstance();


	/**
	 * 
	 */
	public AZVClient()
	{
	props = new Properties();
	xmlURL = this.getClass().getClassLoader().getResource("osecmProperties.xml");
	
		try
		{
		File configFile = new File(FileLocator.toFileURL(xmlURL).getPath());
		InputStream in = new FileInputStream(configFile);
		props.loadFromXML(in);
		}
		catch (IOException e)
		{
		e.printStackTrace();
		}	
		
	auth = new Authentication();
	auth.setUser(props.getProperty("user"));
	auth.setPassword(props.getProperty("password"));

		try
		{
		xpBerichtsMonat = xpath.compile(props.getProperty("xPathBerichtsMonat"));
		xpBerichtsJahr = xpath.compile(props.getProperty("xPathBerichtsJahr"));
		}
		catch (XPathExpressionException e)
		{
		e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see de.hannit.fsch.soa.osecm.IAZVClient#getServiceInfo()
	 */
	@Override
	public String getServiceInfo()
	{
	return "Client für den Zugriff auf den OS/ECM Web Service";
	}

	@Override
	public String getServerInfo()
	{
	String serverInfo = null;
	
	IntegerParameter flags = new IntegerParameter();
	flags.setName(IAZVClient.PARAMETER_INTEGER_FLAGS);
	flags.setValue(Integer.parseInt(props.getProperty("flags")));
	
	IntegerParameter info = new IntegerParameter();
	info.setName(IAZVClient.PARAMETER_INTEGER_INFO);
	info.setValue(Integer.parseInt(props.getProperty("info")));
	
	Job job = new Job();
	job.setName(IAZVClient.JOB_GETSERVERINFO);
	List<Parameter> parameter = job.getParameter();
	parameter.add(flags);
	parameter.add(info);
	
	ExecuteParameter execPara = new ExecuteParameter();
	execPara.setAuthentication(auth);
	execPara.setJob(job);
	
	OsecmWsPortType port = service.getPort(OsecmWsPortType.class);
	
		try
		{
		ExecuteParameter response = port.execute(execPara);
		StringParameter comString = (StringParameter)response.getJob().getParameter().get(1);
		serverInfo = comString.getValue();
		}
		catch (OsecmWsFaultException e)
		{
		e.printStackTrace();
		}
		
	return serverInfo;
	}

	/**
	 * Liefert eine Liste mit OS/ECM Dokumenten
	 * @return 
	 */
	@Override
	public Document getResultList()
	{
	Document doc = null;	
	Job job = new Job();
    job.setName(IAZVClient.JOB_GETRESULTLIST);
    
    List<Parameter> parameter = job.getParameter();
    IntegerParameter flags = new IntegerParameter();
	flags.setName("Flags");
	flags.setValue(0);
	parameter.add(flags);
	
    StringParameter encoding = new StringParameter();
    encoding.setName("Encoding");
    encoding.setValue("UTF-8");
    parameter.add(encoding);
    
    StringParameter outputFormat = new StringParameter();
    outputFormat.setName("OutputFormat");
    outputFormat.setValue("HOL");
    parameter.add(outputFormat);
    
    parameter.add(xmlRequest);
    
	ExecuteParameter execPara = new ExecuteParameter();
	execPara.setAuthentication(auth);
	execPara.setJob(job);
	
	OsecmWsPortType port = service.getPort(OsecmWsPortType.class);
	
		try
		{
		ExecuteParameter response = port.execute(execPara);
		Base64AsStringParameter list = (Base64AsStringParameter) response.getJob().getParameter().get(1);
		String result = list.getValue();
        
		InputStream is = new ByteArrayInputStream(result.getBytes());
	 	BufferedReader br = new BufferedReader(new InputStreamReader(is));
		InputSource inputSource = new InputSource(br);
		inputSource.setEncoding("UTF-8");
	    
		DocumentBuilder builder = factory.newDocumentBuilder();
        doc = builder.parse(inputSource);
        }
		catch (OsecmWsFaultException e)
		{
		e.printStackTrace();
		}
		catch (ParserConfigurationException e)
		{
		e.printStackTrace();
		}
		catch (SAXException e)
		{
		e.printStackTrace();
		}
		catch (IOException e)
		{
		e.printStackTrace();
		}
	return doc;	
    }

	/*
	 * (non-Javadoc)
	 * @see de.hannit.fsch.soa.osecm.IAZVClient#setAZVRequest()
	 * Liest die Datei request.xml aus, welche den Anfragestring enthält.
	 * 
	 * Dann wird die Webservice Anfrage vorbereitet. Die vorbereitete Anfrage wird dabei in das DOM 
	 * eingelesen. Die Request - Parameter Monat und Jahr müssen verändert werden, wenn sich die Auswahl in der Oberfäche ändert. 
	 */
	@Override
	public Exception setAZVRequest(String berichtsMonat, String berichtsJahr)
	{
	Exception result = null;	
    xmlURL = this.getClass().getClassLoader().getResource("request.xml");
    StringWriter writer = new StringWriter();
    
    	try
		{
		DocumentBuilder requestBuilder = factory.newDocumentBuilder();
		File requestFile = new File(FileLocator.toFileURL(xmlURL).getPath());
		request = requestBuilder.parse(requestFile);
			    
		Node nodeMonat = (Node) xpBerichtsMonat.evaluate(request, XPathConstants.NODE);
		Node nodeJahr = (Node) xpBerichtsJahr.evaluate(request, XPathConstants.NODE);
		nodeMonat.setTextContent(berichtsMonat);
		nodeJahr.setTextContent(berichtsJahr);
		
		Transformer transformer = tFactory.newTransformer();
		DOMSource source = new DOMSource(request);
		StreamResult output = new StreamResult(writer);
	    transformer.transform(source, output);
		}
		catch (ParserConfigurationException | IOException | SAXException | XPathExpressionException | TransformerException pcE)
		{
		result = pcE;	
		pcE.printStackTrace();
		}
    
		xmlRequest = new Base64AsStringParameter();
		xmlRequest.setName("XML");
		xmlRequest.setValue(writer.toString());
			
	return result;
	}
}
