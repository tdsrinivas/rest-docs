package com.raj.restdocs.controller;

import com.raj.restdocs.UserNotFoundException;
import com.raj.restdocs.model.User;
import com.raj.restdocs.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    UserRepository userRepo;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<User> getAll() {
        return userRepo.findAll();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public User getUser(@PathVariable Long id) throws UserNotFoundException {
        User user = userRepo.findOne(id);
        if (user == null) {
            throw new UserNotFoundException();
        }
        return user;
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public void createPerson(@RequestBody User user) {
        userRepo.save(user);
    }
}
