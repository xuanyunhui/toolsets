package cn.ac.bestheme.toolsets.mqa.controller;

import cn.ac.bestheme.toolsets.mqa.model.University;
import cn.ac.bestheme.toolsets.mqa.service.UniversityService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/universities")
public class UniversityController {

    @Inject
    UniversityService universityService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<University> getAllUniversities() {
        return universityService.getAllUniversities();
    }

    @GET
    @Path("/test")
    @Produces(MediaType.APPLICATION_JSON)
    public University getTestUniversity() {
        return universityService.getTestUniversity();
    }
} 