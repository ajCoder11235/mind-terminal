package com.mindterminal.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class MindNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label;
    private Long parentId; // The ID of the topic this belongs to

    // Spaced Repetition Fields
    private LocalDate nextReview;
    private int streak = 0; // How many times you've reviewed it correctly

    // Standard Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public LocalDate getNextReview() { return nextReview; }
    public void setNextReview(LocalDate nextReview) { this.nextReview = nextReview; }
}