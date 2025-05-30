package com.axelor.apps.audio.controller.web;

import com.axelor.apps.audio.dto.SearchRequestDto;
import com.axelor.apps.audio.dto.SearchResponseDto;
import com.axelor.apps.audio.service.SearchService;
import com.google.inject.Inject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    @Path("/find-object")
    public Response getCustomsOffices(@BeanParam SearchRequestDto searchRequestDto) {
        try {
            SearchResponseDto filteredObjects = searchService.findAndSort(
                    searchRequestDto.getModel(), searchRequestDto.getWhere(), searchRequestDto.getOrderBy(), searchRequestDto.getName());
            return Response.ok(filteredObjects).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
}
