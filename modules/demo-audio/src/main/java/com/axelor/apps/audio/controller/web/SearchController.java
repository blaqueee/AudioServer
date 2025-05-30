package com.axelor.apps.audio.controller.web;

import com.axelor.apps.audio.dto.SearchRequestDto;
import com.axelor.apps.audio.service.SearchService;
import com.google.inject.Inject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/public/search")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SearchController {

    private final SearchService searchService;

    @Inject
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GET
    @Path("/find-by")
    public Response getCustomsOffices(@BeanParam SearchRequestDto searchRequestDto) throws ClassNotFoundException {
        List<Map<String, Object>> filteredObjects = searchService.findAndSort(
                searchRequestDto.getModel(), searchRequestDto.getWhere(), searchRequestDto.getOrderBy(), searchRequestDto.getName());
        return Response.ok(filteredObjects).build();
    }



}
