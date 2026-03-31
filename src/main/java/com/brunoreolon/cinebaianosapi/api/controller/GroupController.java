package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.GroupConverter;
import com.brunoreolon.cinebaianosapi.api.model.group.request.GroupRequest;
import com.brunoreolon.cinebaianosapi.api.model.group.request.GroupUpdateRequest;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupResponse;
import com.brunoreolon.cinebaianosapi.domain.model.CustomUserDetails;
import com.brunoreolon.cinebaianosapi.domain.model.Group;
import com.brunoreolon.cinebaianosapi.domain.service.GroupService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;
    private final GroupConverter groupConverter;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody GroupRequest request,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        Group group = groupConverter.toEntiy(request);

        String ownerId = userDetails.getUser().getDiscordId();

        Group newGroup = groupService.save(group, ownerId);

        return ResponseEntity.status(HttpStatus.CREATED).body(groupConverter.toResponse(newGroup));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<GroupResponse> update(@Valid @RequestBody GroupUpdateRequest request) {
        Group group = groupConverter.toEntiy(request);
        Group updatedGroup = groupService.update(group);

        return ResponseEntity.ok(groupConverter.toResponse(updatedGroup));
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getAll() {
        List<Group> groups = groupService.getAll();
        return ResponseEntity.ok(groupConverter.toResponseList(groups));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getById(@PathVariable("groupId") Long groupId) {
        Group group = groupService.getById(groupId);
        return ResponseEntity.ok(groupConverter.toResponse(group));
    }

    @GetMapping("/{groupId}/movies")
    public ResponseEntity<GroupDetailResponse> getGroupWithMovies(@PathVariable Long groupId) {
        try {
            Group group = groupService.getGroupWithMovies(groupId);
            GroupDetailResponse groupResponse = groupConverter.toGroupWithMoviesResponse(group);

            return ResponseEntity.ok(groupResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

//    @GetMapping("/{groupId}/vote-types")
//    public ResponseEntity<GroupDetailResponse> getVoteTypes(@PathVariable Long groupId) {
//        try {
//            Group group = groupService.getVoteTypes(groupId);
//            GroupDetailResponse groupResponse = groupConverter.toGroupWithMoviesResponse(group);
//
//            return ResponseEntity.ok(groupResponse);
//        } catch (RuntimeException e) {
//            return ResponseEntity.notFound().build();
//        }
//    }

}