package de.hannit.fsch.soa.osecm.azv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;

import de.hannit.fsch.soa.osecm.azv.ecmApi.ecmDocument;
import de.hannit.fsch.soa.osecm.azv.ecmApi.ecmField;
import de.hannit.fsch.soa.osecm.azv.ecmApi.ecmFolder;
import de.optimalsystems.schemas.osecm.ws.EcmWsMtomSoapService;
import de.optimalsystems.schemas.osecm.ws.OsecmWsFaultException;
import de.optimalsystems.schemas.osecm.ws.OsecmWsPortType;
import de.optimalsystems.schemas.osecm.ws.types.Authentication;
import de.optimalsystems.schemas.osecm.ws.types.ExecuteParameter;
import de.optimalsystems.schemas.osecm.ws.types.FileParameter;
import de.optimalsystems.schemas.osecm.ws.types.IntegerParameter;
import de.optimalsystems.schemas.osecm.ws.types.Job;
import de.optimalsystems.schemas.osecm.ws.types.Parameter;

@SuppressWarnings("unused")
public class OsEcmWsTest
{
        private String Foldername = "Bauamt Wunstorf";
        private String Documentname = "Dokument";
        private String DocumentIdentifierFieldName ="Gekos-DokId";
		private String FolderIdentifierFieldName ="Gekos-ID";
        private String DmsUserName = "sysadm";
        private String DmsPassword = "freund";
        private String DestinationPath = "D:\\";
        
	private OsecmWsPortType getServicePort() throws MalformedURLException
	{
		EcmWsMtomSoapService service = new EcmWsMtomSoapService();
		QName qn = service.getServiceName();
		System.out.println("ServiceName:"+qn);
		System.out.println("WSDL:"+service.getWSDLDocumentLocation());
		return service.getPort(OsecmWsPortType.class);
	}
	
        private int GetDocId(int GekosID)
        {
           
               ecmApi Api = new ecmApi();
               
               
               ecmFolder Folder = new ecmFolder();
               Folder.Name = Foldername;
               
               ecmDocument Document = new ecmDocument();
               Document.Name = Documentname;
               
               Document.ParentFolder=Folder;
               Document.Fields.add(new ecmField(DocumentIdentifierFieldName,Integer.toString(GekosID)));
               
               List<ecmDocument> Documents;
               Documents = Api.Search(DmsUserName, DmsPassword, Document);
               
               if (Documents != null)
               {
                   if (Documents.size()==0)
                   {
                       return -1;
                   }
                   else
                   {
                        if (Documents.get(0).Id == "")
                        {
                            return -1;
                        }
                        else
                        {
                            String DocId = Documents.get(0).Id;
                            return  Integer.parseInt(DocId);
                        }
                   }
               }
               else
               {
                   return -1;
               }
              
            
            
        }
	
	private File getFile(int id)
	{
		try
		{
                        int DocId = GetDocId(id);
                    
                        if (DocId > -1)
                        {
                            
                            int ConvertFileToType = 1;
                            
                            Authentication authentication = new Authentication();
                            authentication.setUser(DmsUserName);
                            authentication.setPassword(DmsPassword);

                            IntegerParameter flags = new IntegerParameter();
                            flags.setName("Flags");
                            flags.setValue(0);

                            IntegerParameter convert = new IntegerParameter();
                            convert.setName("Convert");
                            convert.setValue(ConvertFileToType);

                            IntegerParameter dwObjId = new IntegerParameter();
                            dwObjId.setName("dwObjectID");
                            dwObjId.setValue(DocId);

                            Job job = new Job();
                            job.setName("std.StoreInCacheByID");

                            List<Parameter> parameter = job.getParameter();
                            parameter.add(flags);
                            parameter.add(dwObjId);
                            parameter.add(convert);

                            ExecuteParameter execPara = new ExecuteParameter();
                            execPara.setAuthentication(authentication);
                            execPara.setJob(job);

                            OsecmWsPortType port = getServicePort();
                            ExecuteParameter res = port.execute(execPara);

                            FileParameter fp = res.getJob().getFileParameter().get(0);
                            DataHandler dh = fp.getContent().getAttachment();
                            

                            FileOutputStream fos = new FileOutputStream(DestinationPath + fp.getFileName());
                            dh.writeTo(fos);
                            File fs = new File(DestinationPath + fp.getFileName());
                            return fs;
                        }
                        else
                        {
                            return null;
                        }
                    
                        
		}
		catch (MalformedURLException | OsecmWsFaultException  e)
		{
			System.out.println(e.getStackTrace());
			
		}
		catch( IOException o)
                {
                    System.out.println(o.getStackTrace());
                }
		
		return null;
	}
	
	private boolean exists(int id)
	{
                    int DocId = GetDocId(id);
                    
                    if (DocId > -1)
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
	}
	
	private boolean putFile(File file)
	{
		//TODO dev
		return false;
	}
	
	
	public static void main(String[] args)
	{
		OsEcmWsTest test = new OsEcmWsTest();
		
		int id = 1;
		//if (test.exists(id))
		{
			boolean Check = test.exists(id);
                        
                        File file = test.getFile(id);
                        String Filename = file.toString();
		}
		
		System.exit(0);
	}
}