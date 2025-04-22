package cn.ac.bestheme.toolsets.pdf;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class IndexResource {

    @Inject
    @Location("index.html")
    Template index;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        return index.render();
    }
} 