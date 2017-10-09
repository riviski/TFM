package bebee.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import bebee.beans.HashedImageBean;
import com.mongodb.MongoException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import bebee.pojo.HashedImage;
import bebee.services.impl.ImageServiceImpl;
import org.apache.log4j.Logger;

import java.io.IOException;

@Path("/image")
public class ImageController extends WebServiceController {

    static Logger logger = Logger.getLogger(ImageController.class);

    @Inject
    private ImageServiceImpl imageService;

    public Response delete(String fileName) {
        if(null == fileName || fileName.isEmpty()) {
            logger.info("El nombre del fichero es obligatorio.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            HashedImage hashedImage = new HashedImage(fileName);
            imageService.deleteImage(hashedImage);
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

    public Response insert(List<FormDataBodyPart> files, String category) {
        HashedImage newHashedImage = null;

        for (FormDataBodyPart bodyPart : files) {
            try {
                HashedImage hashedImage = parseFile(category, bodyPart);
                HashedImageBean hashedImageBean = new HashedImageBean(hashedImage, 32, 12);
                newHashedImage = imageService.insertImage(hashedImageBean);
            }catch(IOException e){
                logger.error(e.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).build();
            }catch(Exception e2){
                logger.error(e2.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }

        if(null == newHashedImage){
            return Response.status(Response.Status.CONFLICT).build();
        }else{
            UriBuilder uri = UriBuilder.fromPath("../index.jsp");
            uri.queryParam("success", "true");
            return Response.temporaryRedirect(uri.build()).build();
        }
    }

    public Response matchFile(List<FormDataBodyPart> files, String category, ServletContext servletContext) {
        HashedImage newHashedImage = null;
        HashedImage hashedImage = null;
        String originalImage = "";
        String comparedImage = "";

        for (FormDataBodyPart bodyPart : files) {
            try {
                hashedImage = parseFile(category, bodyPart);
                originalImage = hashedImage.getFilename();
                HashedImageBean hashedImageBean = new HashedImageBean(hashedImage, 32, 12);
                newHashedImage = imageService.compareImage(hashedImageBean);
                comparedImage = newHashedImage.getFilename();
            }catch(IOException e){
                logger.error(e.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).build();
            }catch(Exception e2){
                logger.error(e2.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }

        if(null == newHashedImage){
            return Response.status(Response.Status.NOT_FOUND).build();
        }else{
            UriBuilder uri = UriBuilder.fromPath("../imageResult.jsp");
            String originalHash = hashedImage.getHash();
            String similarity = String.valueOf((int)newHashedImage.getSimilarity());

            try {
                String file1 = "originalHash.csv";
                String file2 = "comparedHash.csv";
                String comparedHash = calculateHash(originalHash, newHashedImage.getHash());
                createCSVFile(originalHash, similarity, file1, servletContext);
                createCSVFile(comparedHash, similarity, file2, servletContext);
                uri.queryParam("filename1", file1);
                uri.queryParam("filename2", file2);
                uri.queryParam("originalImage", originalImage);
                uri.queryParam("comparedImage", comparedImage);
                uri.queryParam("similarity", similarity);
            } catch (Exception e) {
                logger.error(e.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
            return Response.temporaryRedirect(uri.build()).build();
        }
    }

    /**
     * Check whether or not the input file is an image.
     *
     * @param inputStream
     * @return
     */
    private void checkInputFile(InputStream inputStream) throws IOException {
        try {
            ImageIO.read(inputStream).toString();
        } catch (IOException | NullPointerException e) {
            throw new IOException("El fichero de entrada es incorrecto.", e);
        }
    }

    /**
     * Parse the request returning the image object
     *
     * @param category
     * @param bodyPart
     * @return
     * @throws IOException
     */
    private HashedImage parseFile(String category, FormDataBodyPart bodyPart) throws IOException {
        BodyPartEntity bodyPartEntity = (BodyPartEntity) bodyPart.getEntity();
        checkInputFile(bodyPartEntity.getInputStream());
        InputStream inputStream = bodyPartEntity.getInputStream();
        String fileName = bodyPart.getContentDisposition().getFileName();
        return new HashedImage(inputStream, fileName, category);
    }

    private void createCSVFile(String hash, String similarity, String fileName, ServletContext servletContext) throws Exception{
        final String NEXT_LINE = "\n";
        final String delim = ",";
        String filePath;
        try {
            filePath = servletContext.getRealPath("/")+fileName;
            FileWriter fw = new FileWriter(filePath);
            fw.append("hash,similarity").append(NEXT_LINE);
            for (int i=0; i<hash.length(); i++){
                fw.append(hash.charAt(i));
                fw.append(delim);
                fw.append(similarity);
                if(i != hash.length()-1) {
                    fw.append(NEXT_LINE);
                }
            }
            fw.flush();
            fw.close();
        } catch (IOException e) {
            // Error al crear el archivo
            throw new Exception("Error al crear el archivo CSV.", e);
        }
    }

    private String calculateHash(String hash1, String hash2) throws Exception{
        StringBuffer hash = new StringBuffer();
        if(hash1.length() != hash2.length()){
            throw new Exception("Error calculando los hashes");
        }

        for (int i=0; i<hash1.length(); i++){
            if(hash1.charAt(i) != hash2.charAt(i)){
                if(hash2.charAt(i) == '0') {
                    hash.append("2");
                }else{
                    hash.append("3");
                }
            }else{
                hash.append(hash2.charAt(i));
            }
        }
        return hash.toString();
    }
}
