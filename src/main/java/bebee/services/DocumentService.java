package bebee.services;

import bebee.beans.TextDocumentBean;
import bebee.pojo.TextDocument;

import java.util.List;

/**
 * Created by Rivas on 5/5/17.
 */
public interface DocumentService {
    List<TextDocument> compareDocument(TextDocumentBean textDocumentBean) throws Exception;
    TextDocument insertDocument(TextDocumentBean textDocumentBean) throws Exception;
    void deleteDocument(TextDocument textDocument) throws Exception;
}
