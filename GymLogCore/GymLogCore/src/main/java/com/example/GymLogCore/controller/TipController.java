package com.example.GymLogCore.controller;

import com.example.GymLogCore.domain.Tip;
import com.example.GymLogCore.services.TipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tips")
@RequiredArgsConstructor
public class TipController {
    private final TipService tipService;

    @GetMapping
    public ResponseEntity<List<Tip>> getAllTips(){
        List<Tip> response = tipService.getAllTips();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/random")
    public ResponseEntity<Tip> getRandomTip(){
        return ResponseEntity.ok(tipService.gerRandomTip());
    }
}
