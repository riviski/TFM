package bebee.services.impl;

import com.mongodb.*;
import bebee.beans.HashedImageBean;
import bebee.pojo.HashedImage;
import bebee.dao.impl.MongoDAO;
import bebee.services.ImageService;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Created by Rivas on 3/4/17.
 */
public class ImageServiceImpl extends SingletonService implements ImageService{

    /**
     * Compara una imagen contra la base de datos. Primero busca las identicas,
     * si no encuentra, busca similares.
     *
     * @param hashedImageBean imagen a comparar
     * @return String en formato JSON con el resultado
     * @throws java.io.IOException
     */
    @Override
    public HashedImage compareImage(HashedImageBean hashedImageBean) throws Exception{
        String hash = hashedImageBean.getHash();
        HashedImage hashedImage = hashedImageBean.getHashedImage();
        hashedImage.setHash(hash);

        HashedImage hi = getIdenticalImage(hashedImage); //buscar identicas
        if (hi == null){ //buscar parecidas
            hi = getSimilarImage(hashedImage);
        }
        return hi;
    }

    /**
     * Inserta una imagen en la base de datos.
     *
     * @param hashedImageBean objeto imagen
     * @return
     * @throws java.io.IOException
     */
    @Override
    public HashedImage insertImage(HashedImageBean hashedImageBean) throws Exception {
        String hash = hashedImageBean.getHash();
        HashedImage hashedImage = hashedImageBean.getHashedImage();
        hashedImage.setHash(hash);

        HashedImage hi = getIdenticalImage(hashedImage); //buscar identicas
        if(null != hi) { //Ya existe la imagen que queremos insertar
            return null;
        }else{
            try {
                getMongoDAO().saveImage(hashedImage);
            } catch (UnknownHostException e) {
                throw new Exception("Error al cargar la base de datos.", e);
            }
            return hashedImage;
        }
    }

    /**
     * Elimina una imagen de la base de datos.
     *
     * @param hashedImage objeto imagen
     * @return
     * @throws java.io.IOException
     */
    @Override
    public void deleteImage(HashedImage hashedImage) throws Exception{
        try {
            getMongoDAO().deleteImage(hashedImage);
        } catch (UnknownHostException e) {
            throw new Exception("Error al cargar la base de datos.", e);
        } catch (MongoException me){
            throw new MongoException("No se ha podido eliminar el recurso indicado.", me);
        }
    }

    /**
     * Busca imagenes identicas existentes en la base de datos con el hash
     *
     * @param hashedImage imagen a buscar en la base de datos
     * @return imagen existente o null si no existe
     * @throws java.net.ConnectException
     */
    private HashedImage getIdenticalImage(HashedImage hashedImage) throws Exception{
        HashedImage hi;
        try {
            hi = getMongoDAO().findIdenticalImage(hashedImage);
        } catch (UnknownHostException e) {
            throw new Exception("Error al cargar la base de datos.", e);
        }
        return hi;
    }

    /**
     * Busca imagenes similares existentes en la base de datos.
     *
     * @param hashedImage imagen de entrada
     * @return devuelve la imagen mas similar (en caso de haberla). En caso contrario devuelve null
     * @throws IOException
     */
    private HashedImage getSimilarImage(HashedImage hashedImage) throws Exception {
        HashedImage hi;
        try {
            hi = getMongoDAO().findSimilarImage(hashedImage);
        } catch (UnknownHostException e) {
            throw new Exception("Error al cargar la base de datos.", e);
        }
        return hi;
    }

}
