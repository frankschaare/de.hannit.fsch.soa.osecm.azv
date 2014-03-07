/**
 * 
 */
package de.hannit.fsch.soa.osecm.azv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;

import de.hannit.fsch.soa.osecm.IAZVClient;
import de.hannit.fsch.soa.osecm.azv.ecmApi.ecmDocument;
import de.optimalsystems.schemas.osecm.ws.EcmWsMtomSoapService;
import de.optimalsystems.schemas.osecm.ws.OsecmWsFaultException;
import de.optimalsystems.schemas.osecm.ws.OsecmWsPortType;
import de.optimalsystems.schemas.osecm.ws.types.Authentication;
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
private ecmApi osecmAPI = new ecmApi();
private EcmWsMtomSoapService service = new EcmWsMtomSoapService();

	/**
	 * 
	 */
	public AZVClient()
	{
	props = new Properties();
	URL configURL = this.getClass().getClassLoader().getResource("osecmProperties.xml");
	
		try
		{
		File configFile = new File(FileLocator.toFileURL(configURL).getPath());
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
	public List<ecmDocument> getResultList()
	{
	ArrayList<ecmDocument> Documents = null;
	
	return null;
	}

}
