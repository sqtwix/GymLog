package com.example.GymLogCore.services;

import com.example.GymLogCore.domain.Tip;
import com.example.GymLogCore.repository.TipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipService {
    private final TipRepository tipRepository;

    @Transactional
    public List<Tip> getAllTips() {
        return tipRepository.findAll()
                .stream().toList();
    }

    @Transactional(readOnly = true)
    public Tip gerRandomTip(){
        return tipRepository.getRandomTip()
                .orElseThrow(() -> new RuntimeException("No tips found"));

    }
}
