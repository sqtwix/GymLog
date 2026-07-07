package com.example.GymLogCore.controller;

import com.example.GymLogCore.domain.BodyPart;
import com.example.GymLogCore.repository.BodyPartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/body-parts")
@RequiredArgsConstructor
public class BodyPartController {
    private final BodyPartRepository bodyPartRepository;

    @GetMapping
    public ResponseEntity<List<BodyPart>> getAllBodyParts() {
        return ResponseEntity.ok(bodyPartRepository.findAll());
    }
}
