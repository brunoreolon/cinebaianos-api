package com.brunoreolon.cinebaianosapi.api.model.group.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupInviteCandidateSliceResponse {

    private int page;
    private int size;
    private int currentPageElements;
    private boolean hasNext;
    private List<GroupInviteCandidateResponse> candidates;

}

