package com.scc.lofselectclub.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scc.lofselectclub.services.BreederService;
import com.scc.lofselectclub.template.breeder.ResponseObject;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.springframework.web.bind.annotation.RequestMethod;

@RestController
@RequestMapping(value="v1/clubs")
@Api(value="breeders selection", description="Return breeders data")
public class BreederServiceController {
   
	@Autowired
    private BreederService breederService;

    @ApiOperation(value = "View breeders information by id club",response = ResponseObject.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved breeders"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })    
    @RequestMapping(value="/{id}/breeders", method = RequestMethod.GET)
    public ResponseObject getStatisticsByIdClub(@ApiParam(value = "id club", required = true) @PathVariable("id") int id) {
        return breederService.getStatistics(id);
    }    
    
}
