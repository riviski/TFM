package bebee.controller;

import bebee.beans.TextDocumentBean;
import bebee.dao.impl.MongoDAO;
import bebee.pojo.TextDocument;
import bebee.services.impl.DocumentServiceImpl;
import com.mongodb.MongoException;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.*;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by Rivas on 5/5/17.
 */
@Path("/document")
public class DocumentController extends WebServiceController  {

    static Logger logger = Logger.getLogger(DocumentController.class);

    @Inject
    private DocumentServiceImpl documentService;

    @Override
    public Response delete(String fileName) {
        if(null == fileName || fileName.isEmpty()) {
            logger.info("El nombre del fichero es obligatorio.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            TextDocument textDocument = new TextDocument(fileName);
            documentService.deleteDocument(textDocument);
        } catch(MongoException e){
            logger.info(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e2) {
            logger.info(e2.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        UriBuilder uri = UriBuilder.fromPath("../index.jsp");
        uri.queryParam("success", "true");
        return Response.temporaryRedirect(uri.build()).build();
    }

    @Override
    public Response insert(List<FormDataBodyPart> files, String category) {
        TextDocument newTextDocument = null;

        for (FormDataBodyPart bodyPart : files) {
            try {
                TextDocument textDocument = parseFile(category, bodyPart);
                TextDocumentBean textDocumentBean = new TextDocumentBean(textDocument);
                newTextDocument = documentService.insertDocument(textDocumentBean);
            }catch(IOException e){
                logger.error(e.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).build();
            }catch(Exception e2){
                logger.error(e2.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }

        if(null == newTextDocument){
            return Response.status(Response.Status.CONFLICT).build();
        }else{
            UriBuilder uri = UriBuilder.fromPath("../index.jsp");
            uri.queryParam("success", "true");
            return Response.temporaryRedirect(uri.build()).build();
        }
    }

    @Override
    public Response matchFile(List<FormDataBodyPart> files, String category, ServletContext servletContext) {
        List<TextDocument> documents = null;
        String image="";

        for (FormDataBodyPart bodyPart : files) {
            try {
                TextDocument textDocument = parseFile(category, bodyPart);
                TextDocumentBean textDocumentBean = new TextDocumentBean(textDocument);
                documents = documentService.compareDocument(textDocumentBean);
                image = textDocument.getFilename();
            }catch(IOException e){
                logger.error(e.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).build();
            }catch(Exception e2){
                logger.error(e2.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }

        if(null == documents || documents.size() == 0){
            return Response.status(Response.Status.NOT_FOUND).build();
        }else{
            UriBuilder uri = UriBuilder.fromPath("../result.jsp");
            uri.queryParam("filename", "csvFile.csv");
            uri.queryParam("filenameTags", "csvTags.csv");
            uri.queryParam("image", image);
            try {
                createCSVFile(documents, servletContext);
                createCSVTags(documents, servletContext);
            } catch (Exception e) {
                logger.error(e.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
            return Response.temporaryRedirect(uri.build()).build();
        }
    }

    /**
     * Parse the request returning the text document object
     *
     * @param category
     * @param bodyPart
     * @return
     * @throws IOException
     */
    private TextDocument parseFile(String category, FormDataBodyPart bodyPart) throws IOException{
        BodyPartEntity bodyPartEntity = (BodyPartEntity) bodyPart.getEntity();
        InputStream inputStream = bodyPartEntity.getInputStream();
        String fileName = bodyPart.getContentDisposition().getFileName();
        return new TextDocument(fileName, category, new Date(), inputStream);
    }

    private void createCSVFile(List<TextDocument> documents, ServletContext servletContext) throws Exception{
        final String NEXT_LINE = "\n";
        final String delim = ",";
        String filePath;
        try {
            String fileName = "csvFile";
            filePath = servletContext.getRealPath("/")+fileName+".csv";
            FileWriter fw = new FileWriter(filePath);
            fw.append("filename,similarity").append(NEXT_LINE);
            int i = 1;
            for(TextDocument doc : documents){
                fw.append(doc.getFilename());
                fw.append(delim);
                fw.append(String.valueOf((int)doc.getSimilarity()));
                if(i != documents.size()) {
                    fw.append(NEXT_LINE);
                }
                i++;
            }
            fw.flush();
            fw.close();
        } catch (IOException e) {
            // Error al crear el archivo
            throw new Exception("Error al crear el archivo CSV.", e);
        }
    }

    private void createCSVTags(List<TextDocument> documents, ServletContext servletContext) throws Exception{
        final String NEXT_LINE = "\n";
        final String delim = ",";
        String filePath;
        try {
            String fileName = "csvTags";
            filePath = servletContext.getRealPath("/")+fileName+".csv";
            FileWriter fw = new FileWriter(filePath);
            fw.append("text,size").append(NEXT_LINE);
            int i = 1;
           /* for(TextDocument doc : documents){*/
           TextDocument doc = documents.get(0);
                int j = 1;
                for(Map.Entry<String, Double> entry : doc.getTagsMap().entrySet()){
                    for(int m=0; m<2; m++) { //Se meten todos por duplicado... por que haya más palabras en la nube
                        fw.append(entry.getKey());
                        fw.append(delim);
                        fw.append(String.valueOf(entry.getValue().intValue()));
                        if (j < 10 || j != doc.getTagsMap().entrySet().size()) {
                            fw.append(NEXT_LINE);
                        }
                    }
                    if (j == 10) break;
                    j++;
                }
            /*    if(i != documents.size()) {
                    fw.append(NEXT_LINE);
                }
                i++;
            }*/
            fw.flush();
            fw.close();
        } catch (IOException e) {
            // Error al crear el archivo
            throw new Exception("Error al crear el archivo CSV.", e);
        }
    }


    /*Script para cargar documentos.
    Meterlos en la carpeta noticias del escritorio.*/
   /* public static void main(String args[])throws Exception{
        File f = new File(args[0]);
        File[] files = f.listFiles();
        for(File file : files){
            if(file.getName().startsWith("."))continue;
            InputStream inputStream = new FileInputStream(file);
            String fileName = file.getName();
            TextDocument td = new TextDocument(fileName, "", new Date(), inputStream);
            TextDocumentBean textDocumentBean = new TextDocumentBean(td);
            Map<String, Double> wordsMap = textDocumentBean.filterDocument();
            TextDocument textDocument = textDocumentBean.getTextDocument();
            textDocument.setTagsMap(wordsMap);
            try {
                MongoDAO.getInstance().saveDocument(textDocument);
            } catch (UnknownHostException e) {
                throw new Exception("Error al cargar la base de datos.", e);
            }
        }
    }*/
}
