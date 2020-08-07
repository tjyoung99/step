// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.google.sps.Events;
import com.google.sps.TimeRange;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    List<TimeRange> collectionOfRanges = new ArrayList<TimeRange>();
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return collectionOfRanges;
    }

    if (events.isEmpty() || request.getAttendees().isEmpty() || 
        ifEventAttendeesNotRequested(events, request)) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    List<TimeRange> rangesByStart = new ArrayList<TimeRange>(events.size());
    for (Event event : events) {
      if (request.getAttendees().containsAll(event.getAttendees())) {
        rangesByStart.add(event.getWhen());
      }
    }
    List<TimeRange> rangesByEnd = new ArrayList<TimeRange>(rangesByStart);
    Collections.sort(rangesByStart, TimeRange.ORDER_BY_START);
    Collections.sort(rangesByEnd, TimeRange.ORDER_BY_END);
    
    int rangesLength = rangesByStart.size();
   
    TimeRange requestableTime = 
        TimeRange.fromStartEnd(TimeRange.START_OF_DAY, rangesByStart.get(0).start(), false);
    addTimeRange(requestableTime, request, collectionOfRanges);

     requestableTime = 
        TimeRange.fromStartEnd(rangesByEnd.get(rangesLength - 1).end(), TimeRange.END_OF_DAY, true);
    addTimeRange(requestableTime, request, collectionOfRanges);
    
    if (rangesLength > 1) {
      addAllOtherTimeRanges(rangesByStart, 
                            requestableTime, 
                            request, 
                            collectionOfRanges, 
                            rangesLength);
    }
    Collections.sort(collectionOfRanges, TimeRange.ORDER_BY_START);
    return collectionOfRanges;
  }
  
  public boolean ifEventAttendeesNotRequested(Collection<Event> events, MeetingRequest request) {
    Set<String> allAttendees = new HashSet<String>();
    Set<String> requestedAttendees = new HashSet<String>(request.getAttendees());
    for (Event event : events){
      allAttendees.addAll(event.getAttendees());
    }
    for (String attendee : requestedAttendees) {
      if (allAttendees.contains(attendee)) return false;
    }
    return true;
  }

  public void addTimeRange(TimeRange range, MeetingRequest request, List<TimeRange> listOfRanges){
    if (range.duration() >= request.getDuration()) {
      listOfRanges.add(range);
    }  
  }

  public List<TimeRange> getOverlappingRanges(TimeRange currentRange, List<TimeRange> 
      listOfRanges) {
    List<TimeRange> overlappingRanges = new ArrayList<TimeRange>(listOfRanges.size());
    for (TimeRange range : listOfRanges) {
      if (currentRange.overlaps(range)) {
        overlappingRanges.add(range);
      }
    }
    Collections.sort(overlappingRanges, TimeRange.ORDER_BY_END);
    return overlappingRanges;
  }
  
  public void addAllOtherTimeRanges(List<TimeRange> rangesByStart, 
                                    TimeRange requestableTime, 
                                    MeetingRequest request, 
                                    List<TimeRange> collectionOfRanges, 
                                    int rangesLength) {
    TimeRange currentRange;
    TimeRange nextRange;
    List<TimeRange> overlappingRanges;
    for (int i = 0; i < rangesLength - 1; i++) {
        currentRange = rangesByStart.get(i);
        nextRange = rangesByStart.get(i + 1);
        if (currentRange.overlaps(nextRange)){
          overlappingRanges = getOverlappingRanges(currentRange, rangesByStart);
          i = rangesByStart.indexOf(overlappingRanges.get(0));
          if (i + 1 >= rangesLength) break;
          nextRange = rangesByStart.get(i + 1);
          currentRange = overlappingRanges.get(0);
        }
        requestableTime = TimeRange.fromStartEnd(
            currentRange.end(), 
            nextRange.start(), 
            false);
        addTimeRange(requestableTime, request, collectionOfRanges); 
      }
  }
}
