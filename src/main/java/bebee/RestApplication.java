package bebee;

import bebee.services.DocumentService;
import bebee.services.ImageService;
import bebee.services.impl.DocumentServiceImpl;
import bebee.services.impl.ImageServiceImpl;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * Created by Rivas on 11/4/17.
 */
@ApplicationPath("api")
public class RestApplication extends ResourceConfig {

    public RestApplication() {
        packages("bebee");
        register(MultiPartFeature.class);

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(new ImageServiceImpl()).to(ImageService.class);
                bind(new DocumentServiceImpl()).to(DocumentService.class);
            }
        });

    }
}
