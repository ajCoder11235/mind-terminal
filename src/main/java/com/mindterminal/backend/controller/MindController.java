package com.mindterminal.backend.controller; // Make sure this matches your package

import com.mindterminal.backend.model.MindNode;
import com.mindterminal.backend.repository.MindRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional; // Import for data safety
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MindController {

    private final MindRepository repository;

    public MindController(MindRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/nodes")
    public List<MindNode> getAllNodes() {
        return repository.findAll();
    }

    @PostMapping("/add")
    public String addNode(@RequestParam String label, @RequestParam(required = false) String parentLabel) {
        if (repository.findByLabelIgnoreCase(label).isPresent()) {
            return "Error: Topic '" + label + "' already exists.";
        }

        MindNode node = new MindNode();
        node.setLabel(label);
        node.setNextReview(LocalDate.now().plusDays(1));

        if (parentLabel != null) {
            Optional<MindNode> parent = repository.findByLabelIgnoreCase(parentLabel);
            if (parent.isEmpty()) return "Error: Parent '" + parentLabel + "' not found.";
            node.setParentId(parent.get().getId());
        }

        repository.save(node);
        return "[REC] Added '" + label + "'.";
    }

    // --- NEW: DELETE FUNCTIONALITY ---
    @DeleteMapping("/delete")
    @Transactional // Ensures if one part fails, everything rolls back
    public String deleteNode(@RequestParam String label) {
        Optional<MindNode> nodeOpt = repository.findByLabelIgnoreCase(label);
        if (nodeOpt.isEmpty()) return "Error: Topic '" + label + "' not found.";

        MindNode nodeToDelete = nodeOpt.get();

        // 1. Find all descendants (Recursive)
        List<MindNode> allNodes = repository.findAll();
        List<Long> idsToDelete = new ArrayList<>();
        collectDescendantIds(nodeToDelete.getId(), allNodes, idsToDelete);
        idsToDelete.add(nodeToDelete.getId()); // Add the node itself

        // 2. Delete them
        repository.deleteAllById(idsToDelete);
        return "[DEL] Deleted '" + label + "' and " + (idsToDelete.size() - 1) + " sub-topics.";
    }

    // --- NEW: MOVE FUNCTIONALITY ---
    @PutMapping("/move")
    public String moveNode(@RequestParam String label, @RequestParam String newParentLabel) {
        Optional<MindNode> nodeOpt = repository.findByLabelIgnoreCase(label);
        Optional<MindNode> parentOpt = repository.findByLabelIgnoreCase(newParentLabel);

        if (nodeOpt.isEmpty()) return "Error: Topic '" + label + "' not found.";
        if (parentOpt.isEmpty()) return "Error: New parent '" + newParentLabel + "' not found.";

        MindNode nodeToMove = nodeOpt.get();
        MindNode newParent = parentOpt.get();

        // 1. Cycle Check: You cannot move a Parent under its own Child!
        List<MindNode> allNodes = repository.findAll();
        if (isDescendant(newParent.getId(), nodeToMove.getId(), allNodes)) {
            return "Error: Paradox! Cannot move '" + label + "' inside itself.";
        }

        // 2. Execute Move
        nodeToMove.setParentId(newParent.getId());
        repository.save(nodeToMove);
        return "[MOV] Moved '" + label + "' under '" + newParentLabel + "'.";
    }

    // --- HELPER FUNCTIONS ---
    private void collectDescendantIds(Long parentId, List<MindNode> allNodes, List<Long> result) {
        List<MindNode> children = allNodes.stream()
                .filter(n -> Objects.equals(n.getParentId(), parentId))
                .collect(Collectors.toList());

        for (MindNode child : children) {
            result.add(child.getId());
            collectDescendantIds(child.getId(), allNodes, result);
        }
    }

    private boolean isDescendant(Long potentialChildId, Long parentId, List<MindNode> allNodes) {
        if (Objects.equals(potentialChildId, parentId)) return true;

        // Find parent of potential child
        MindNode node = allNodes.stream().filter(n -> n.getId().equals(potentialChildId)).findFirst().orElse(null);
        if (node == null || node.getParentId() == null) return false;

        return isDescendant(node.getParentId(), parentId, allNodes); // Recurse up
    }
}