package com.scc.lofselectclub.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scc.lofselectclub.exceptions.ApiError;
import com.scc.lofselectclub.exceptions.EntityNotFoundException;
import com.scc.lofselectclub.services.BreederService;
import com.scc.lofselectclub.template.breeder.BreederResponseObject;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.springframework.web.bind.annotation.RequestMethod;

@RestController
@RequestMapping(value = "v1/clubs")
@Api(value = "breeders selection", description = "Return breeders data")
public class BreederServiceController {

   @Autowired
   private BreederService breederService;

   @ApiOperation(value = "View breeders information by id club", response = BreederResponseObject.class)
   @ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully retrieved breeders"),
         @ApiResponse(code = 400, message = "You are trying to reach the resource with invalid parameters"),
         @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
         @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
         @ApiResponse(code = 404, message = "The resource you were trying to reach is not found", response = ApiError.class) })
   @RequestMapping(value = "/{id}/breeders", method = RequestMethod.GET)
   public BreederResponseObject getBreedersStatisticsByIdClub(
         @ApiParam(value = "id club", required = true) @PathVariable("id") int id) throws EntityNotFoundException {
      return breederService.getStatistics(id);
   }

}
