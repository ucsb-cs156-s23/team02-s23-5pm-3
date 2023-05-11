package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Music;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MusicRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
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



@Api(description = "Music")
@RequestMapping("/api/music")
@RestController
@Slf4j

public class MusicController extends ApiController {
    @Autowired
    MusicRepository musicRepository;

    @ApiOperation(value = "List all songs")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Music> allSongs() {
        Iterable<Music> songs = musicRepository.findAll();
        return songs;
    }


    @ApiOperation(value = "Get a single song")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Music getById(
            @ApiParam("id") @RequestParam Long code) {
        Music songs = musicRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(Music.class, code));

        return songs;
        }
    
        @ApiOperation(value = "Create a new song")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        @PostMapping("/post")
        public Music postSongMusic(
            @ApiParam("id") @RequestParam long id,
            @ApiParam("title") @RequestParam String title,
            @ApiParam("author") @RequestParam String author,
            @ApiParam("rating") @RequestParam float rating,
            @ApiParam("views") @RequestParam int views
            )
            {
    
            Music music = new Music();
            music.setId(id);
            music.setTitle(title);
            music.setAuthor(author);
            music.setRating(rating);
            music.setViews(views);

            
            Music savedSongs = musicRepository.save(music);
    
            return savedSongs;
        }
        @ApiOperation(value = "Delete a UCSBDiningCommons")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        @DeleteMapping("")
        public Object deleteCommons(
                @ApiParam("code") @RequestParam Long code) {
            Music songs = musicRepository.findById(code)
                    .orElseThrow(() -> new EntityNotFoundException(Music.class, code));
    
            musicRepository.delete(songs);
            return genericMessage("Music with id %s deleted".formatted(code));
        }
    
        @ApiOperation(value = "Update a single song")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        @PutMapping("")
        public Music updateCommons(
                @ApiParam("code") @RequestParam Long code,
                @RequestBody @Valid Music incoming) {
    
            Music songs = musicRepository.findById(code)
                    .orElseThrow(() -> new EntityNotFoundException(Music.class, code));
    
    
            songs.setId(incoming.getId());  
            songs.setTitle(incoming.getTitle());
            songs.setAuthor(incoming.getAuthor());
            songs.setRating(incoming.getRating());
            songs.setViews(incoming.getViews());
    
            musicRepository.save(songs);
    
            return songs;
        }

}
