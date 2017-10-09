package bebee.services.impl;

import bebee.beans.TextDocumentBean;
import bebee.pojo.TextDocument;
import bebee.services.DocumentService;
import com.mongodb.MongoException;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Rivas on 5/5/17.
 */
public class DocumentServiceImpl extends SingletonService implements DocumentService {

    @Override
    public List<TextDocument> compareDocument(TextDocumentBean textDocumentBean) throws Exception {
        Map<String, Double> wordsMap = textDocumentBean.filterDocument();
        TextDocument textDocument = textDocumentBean.getTextDocument();
        textDocument.setTagsMap(wordsMap);

        List<TextDocument> documents = getSimilarTextDocuments(textDocument); //buscar similares
        return documents;
    }

    @Override
    public TextDocument insertDocument(TextDocumentBean textDocumentBean) throws Exception {
        Map<String, Double> wordsMap = textDocumentBean.filterDocument();
        TextDocument textDocument = textDocumentBean.getTextDocument();
        textDocument.setTagsMap(wordsMap);
        try {
            getMongoDAO().saveDocument(textDocument);
        } catch (UnknownHostException e) {
            throw new Exception("Error al cargar la base de datos.", e);
        }
        return textDocument;
    }

    @Override
    public void deleteDocument(TextDocument textDocument) throws Exception {
        try {
            getMongoDAO().deleteDocument(textDocument);
        } catch (UnknownHostException e) {
            throw new Exception("Error al cargar la base de datos.", e);
        } catch (MongoException me){
            throw new MongoException("No se ha podido eliminar el recurso indicado.", me);
        }
    }

    /**
     * Busca documentos similares existentes en la base de datos.
     *
     * @param textDocument imagen de entrada
     * @return devuelve el documento mas similar (en caso de haberlo). En caso contrario devuelve null
     * @throws IOException
     */
    private List<TextDocument> getSimilarTextDocuments(TextDocument textDocument) throws Exception {
        List<TextDocument> documents = new ArrayList<>();
        try {
            documents = getMongoDAO().findSimilarDocument(textDocument);
        } catch (UnknownHostException e) {
            throw new Exception("Error al cargar la base de datos.", e);
        }
        return documents;
    }
}
