package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Music;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MusicRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.time.LocalDateTime;

@Api(description = "Musics")
@RequestMapping("/api/musics")
@RestController
@Slf4j
public class MusicController extends ApiController {

    @Autowired
    MusicRepository movieRepository;

    @ApiOperation(value = "List all musics")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Music> allMusics() {
        Iterable<Music> musics = movieRepository.findAll();
        return musics;
    }

    @ApiOperation(value = "Get a single movie")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Music getById(
            @ApiParam("id") @RequestParam Long id) {
        Music movie = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Music.class, id));

        return movie;
    }

    @ApiOperation(value = "Create a new movie")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Music postMusic(
            @ApiParam("title") @RequestParam String title,
            @ApiParam("author") @RequestParam String author,
            @ApiParam("rating") @RequestParam float rating,
            @ApiParam("views") @RequestParam int views)
            throws JsonProcessingException {

        Music movie = new Music();
        movie.setTitle(title);
        movie.setAuthor(author);
        movie.setRating(rating);
        movie.setViews(views);

        Music savedMusic = movieRepository.save(movie);

        return savedMusic;
    }

    @ApiOperation(value = "Delete a Music")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteMusic(
            @ApiParam("id") @RequestParam Long id) {
        Music movie = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Music.class, id));

        movieRepository.delete(movie);
        return genericMessage("Music with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single movie")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Music updateMusic(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Music incoming) {

        Music movie = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Music.class, id));

        movie.setTitle(incoming.getTitle());
        movie.setAuthor(incoming.getAuthor());
        movie.setRating(incoming.getRating());
        movie.setViews(incoming.getViews());

        movieRepository.save(movie);

        return movie;
    }
}
