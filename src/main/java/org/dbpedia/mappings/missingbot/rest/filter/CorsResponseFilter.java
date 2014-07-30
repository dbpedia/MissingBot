package org.dbpedia.mappings.missingbot.rest.filter;

/**
 * Created by peterr on 07.05.14.
 */

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

public class CorsResponseFilter implements ContainerResponseFilter {

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        response.getHttpHeaders().add("Access-Control-Allow-Origin", "*");
        response.getHttpHeaders().add("Access-Control-Allow-Methods", "GET, POST");
//        contResp.getHttpHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
//        contResp.getHttpHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
//        contResp.getHttpHeaders().add("Access-Control-Allow-Credentials", "true");
//        contResp.getHttpHeaders().add("Access-Control-Max-Age", "1209600");
        return response;
    }

}
