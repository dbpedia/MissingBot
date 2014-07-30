package org.dbpedia.mappings.missingbot.rest.filter;

/**
 * Created by peterr on 10.06.14.
 * Source: http://stephen.genoprime.com/2011/05/29/jersey-charset-in-content-type.html
 */
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

import javax.ws.rs.core.MediaType;

public class CharsetResponseFilter implements ContainerResponseFilter {

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {

        MediaType contentType = response.getMediaType();

        if (contentType != null) {
            response.getHttpHeaders().add("Content-Type", contentType.toString() + "; charset=UTF-8");
        }


        return response;
    }
}

