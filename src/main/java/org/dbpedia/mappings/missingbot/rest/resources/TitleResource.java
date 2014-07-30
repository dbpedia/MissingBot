package org.dbpedia.mappings.missingbot.rest.resources;

import com.sun.jersey.api.NotFoundException;
import org.dbpedia.mappings.missingbot.rest.bean.Missing;
import org.dbpedia.mappings.missingbot.storage.Store;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;

/**
 * Created by peterr on 24.04.14.
 */
@XmlRootElement
public class TitleResource {
    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    String title;
    String language;

    public TitleResource(UriInfo uriInfo, Request request,
                           String language, String title) {
        this.uriInfo = uriInfo;
        this.request = request;
        this.language = language;
        this.title = title;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Missing getTitle() throws IOException {
        Missing title = new Store().getAllByLang(this.language).get(this.title);
        if(title==null)
            throw new NotFoundException("No such Title.");
        return title;
    }
}
