package bebee.controller;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by Rivas on 30/3/17.
 */

public abstract class WebServiceController {

    static Logger logger = Logger.getLogger(WebServiceController.class);

    @POST
    @Path("/delete")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public abstract Response delete(@FormParam("filename") String filename);

    @POST
    @Path("/insert")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public abstract Response insert(@FormDataParam("files") List<FormDataBodyPart> files, @DefaultValue("") @FormDataParam("category") String category);

    @POST
    @Path("/match")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public abstract Response matchFile(@FormDataParam("files") List<FormDataBodyPart> files, @DefaultValue("") @FormDataParam("category") String category, @Context ServletContext servletContext);
}
