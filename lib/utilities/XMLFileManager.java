package utilities;

import java.io.*;
import java.util.*;

import com.ximpleware.*;

public class XMLFileManager {
	
	Vector<String> 	splitFileName = new Vector<String>();
	String srcFileName= null;
	int file_count = 0;
	public XMLFileManager(String fileName)
	{
		srcFileName = fileName;
		try{
			System.out.println("Coming to constructor ");
    		splitFiles();
		}catch(Exception e){
		}
	}
					
     public void splitFiles() throws XPathEvalException, NavException, IOException, XPathParseException
     {
        VTDGen vg = new VTDGen();
        if (vg.parseFile(srcFileName, true)){
				System.out.println("Coming to splitFIles ");
                VTDNav vn = vg.getNav();
                AutoPilot ap = new AutoPilot(vn);

				ap.selectXPath("/mediawiki/page");
                int i=0,k=0;
                byte[] ba = vn.getXML().getBytes();
                int count =0;
                
                FileOutputStream fos = new FileOutputStream("temp/out"+count+".xml"); 
                // FileName
                String fileName;
                splitFileName.add("out0.xml");
                while((i=ap.evalXPath())!=-1)
                {	   
                	fos.write("<mediawiki>".getBytes());
                    long l = vn.getElementFragment();
                    fos.write(ba, (int)l, (int)(l>>32));
                    fos.write("</mediawiki>".getBytes());
                    k++;
                    if(k == 3000)
                    {
                    	fos.close();
                    	count= count +1;
                    	k = 0;
                    	// open a new one for write 
                    	fos = new FileOutputStream("temp/out"+count+".xml");
                    	fileName = "out"+count +".xml";
                    	splitFileName.add(fileName);
                    }
                }
                fos.close();
        } 
		//file_count = count + 1;

    }

	public boolean filesExist(){
		System.out.println("File count = " +file_count + " SplitFileName.size : " +  splitFileName.size()  );
		if(file_count < splitFileName.size() )
			return true;
		else 
			return false;
	}
     
     public String returnFileName()
     {
	       	 Iterator<String> itr = splitFileName.iterator();
	       	 String result = null;
	       	 while(itr.hasNext())
	       	 {
	       		result =  itr.next().toString();
				file_count++;
	       	 }
			return result;
     }
     
	 /*
     public static void main(String[] argv) throws XPathParseException, XPathEvalException, NavException, IOException
     {
    	 XMLFileManager mangerObj = new XMLFileManager("/home/maverick/irproject/wikidump/enwiki.xml");
    	 mangerObj.splitFiles();
    	 Iterator<String> itr = mangerObj.splitFileName.iterator();
    	 while(itr.hasNext())
    	 {
    		 System.out.println(itr.next());
    	 }
     }
	 */
}