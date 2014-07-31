package org.dbpedia.mappings.missingbot.rest.resources;

import org.dbpedia.mappings.missingbot.Main;
import org.dbpedia.mappings.missingbot.rest.bean.Missing;
import org.dbpedia.mappings.missingbot.storage.Store;
import org.dbpedia.mappings.missingbot.token.TokenBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by peterr on 24.04.14.
 */
@Path("/missings")
public class MissingResource {
    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    private static final Logger log = LoggerFactory.getLogger(MissingResource.class);

    @GET
    @Path("{language}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Missing> getMissings(
            @PathParam("language") String language
    ) throws IOException {
        List<Missing> titles = new ArrayList<Missing>();
        titles.addAll(new Store().getAllByLang(language).values());
        return titles;
    }

    @Path("{language}/{title}")
    @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_XML})
    public TitleResource getMissing(
            @PathParam("language") String language,
            @PathParam("title") String title
    ) {
        return new TitleResource(uriInfo, request, language, title);
    }

    // TODO: Validate input data
    @POST
    @Path("approve")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void approve(
            @FormParam("language") String language,
            @FormParam("title") String title,
            @FormParam("translation") String translation,
            @FormParam("session_prefix") String session_prefix,
            @FormParam("session_id") String session_id,
            @FormParam("token") String token
    ) throws IOException {
        Store store = new Store();
        store.remove(title, language);

        TokenBot tokenBot = new TokenBot(session_prefix, session_id, Main.config.getString("wikihosturl"));

        tokenBot.save_article(title, translation, language, token);

        log.info(language + "\t" + title + "\t" + translation);

    }
}
