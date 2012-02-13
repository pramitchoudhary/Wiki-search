import parser.*;
import utilities.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;


public class WikiIndexer{

	private IndexWriter writer;
	String index_dir="Folder_Index" , doc_dir;
	public WikiUtils utils_Obj;
	private XMLFileManager xml_files;
	private XMLJDomParser xml_parser;
	final int NUM_OF_PAGES = 10;
	public Page page;

	private void initializeIndexWriter() throws IOException {
		Directory dir ;
		dir = FSDirectory.open(new File(getIndexDirectory()) );
	    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);
	    IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31, analyzer);
		writer = new IndexWriter( dir, iwc);
	}

	public void setIndexDirectory(String path){
		this.index_dir = path;
	}

	public String getIndexDirectory(){
		return this.index_dir;
	}

	public void setDocumentDirectory(String path){
		this.doc_dir = path;
	}

	public String getDocumentDirectory(){
		return this.doc_dir;
	}

	WikiIndexer(String path) throws IOException{
		//Intitialize all the objects

		//Initializing WikiUtils obj
		utils_Obj = new WikiUtils();
		if(utils_Obj.validateFolder(path) !=0)
		{
			System.out.println("Invalid folder . Exiting");
			System.exit(1);
		}
		setDocumentDirectory(path);

		//Initializing XMLFileManager 
		xml_files = new XMLFileManager(path);
		
		//Initializing Index Writer.
	 	initializeIndexWriter();

		page = new Page();
        page.setFlag(1);

	
	}

	public Document addFields(Document indexDoc , Page page){
		Field field1 = new Field("contents", page.getContent().toString() , Field.Store.NO, Field.Index.ANALYZED);
		indexDoc.add(field1);

		Field field2 = new Field("title" ,  page.getTitle().toString() ,Field.Store.YES, Field.Index.ANALYZED);
		indexDoc.add(field2);
		//field2.setBoost(1.5F); // The title needs to have a higher weight than other fields.
		System.out.println("Indexed : " + page.getTitle().toString() );

		/*
		Field field3 = new Field("Exacttitle" ,  page.getTitle().toString() ,Field.Store.YES, Field.Index.NOT_ANALYZED);
		indexDoc.add(field3);
		if( page.getContent().toString().contains("film") || page.getContent().toString().contains("films")){
			indexDoc.setBoost(2F);
		}
		*/
		
		return indexDoc;
	}

	public int indexFiles() throws Exception{
       	
		int i=0;
		while(xml_files.filesExist()){
       		String file_name = xml_files.returnFileName();
			xml_parser= new XMLJDomParser(file_name) ;
			while( xml_parser.getPageData(page) ==1 ){
			
				int type = page.getPageType();
				if(page.isRedirect() == true)	//Ignore redirect pages for now
					continue;			
				Document indexDoc = new Document();
				addFields(indexDoc, page);
				writer.addDocument(indexDoc);
				page.resetPage();
			}
		}

		return writer.numDocs();
	}

	public static void main(String args[]) throws Exception{
    	String usage = "java WikiIndexer [-docs DOCS_PATH]\n\n";

		if(args.length!=2 ){
			System.out.println("Usage : "+usage);
      		System.exit(1);
		}
		else{
			if(!args[0].equals("-docs") || args[1] == ""){
				System.out.println("Usage : "+usage);
      			System.exit(1);
			}
		}
    	Date start = new Date();

		String documentPath = args[1];
		WikiIndexer indexer_obj = new WikiIndexer(documentPath);

		int numOfDocs = indexer_obj.indexFiles();
		System.out.println("Number of articles indexed : " +numOfDocs);
		indexer_obj.close();
      	Date end = new Date();
      	System.out.println(end.getTime() - start.getTime() + " total milliseconds");

	}

	public void close() throws Exception{
		writer.close();
	}
}
