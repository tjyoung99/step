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
        !collectionContainsSet(request.getAttendees(), getAllAttendees(events))){
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    List<TimeRange> RangesByStart = new ArrayList<TimeRange>(events.size());
    for (Event event : events){
      RangesByStart.add(event.getWhen());
    }
    List<TimeRange> RangesByEnd = new ArrayList<TimeRange>(RangesByStart);
    Collections.sort(RangesByStart, TimeRange.ORDER_BY_START);
    Collections.sort(RangesByEnd, TimeRange.ORDER_BY_END);
    
    int length = RangesByStart.size();
   
    TimeRange requestableTime = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, RangesByStart.get(0).start(), false);

    if (requestableTime.duration() >= request.getDuration()) {
      collectionOfRanges.add(requestableTime);
    }

     requestableTime = 
        TimeRange.fromStartEnd(RangesByEnd.get(length - 1).end(), TimeRange.END_OF_DAY, true);
    if (requestableTime.duration() >= request.getDuration()) {
      collectionOfRanges.add(requestableTime);
    }

    TimeRange currentRange;
    TimeRange nextRange;
    List<TimeRange> overlappingRanges;
    if (length > 1) {
      for (int i = 0; i < length; i++) {
        currentRange = RangesByStart.get(i);
        if (i + 1 >= length) break;
        nextRange = RangesByStart.get(i + 1);
        if (currentRange.overlaps(nextRange)){
          overlappingRanges = new ArrayList<TimeRange>(length);
          int j = 0;
          for (TimeRange range : RangesByStart) {
            j++;
            if (currentRange.overlaps(range)) {
              overlappingRanges.add(range);
            }
          }
          Collections.sort(overlappingRanges, TimeRange.ORDER_BY_END);
          i = j;
          if (i + 1 >= length) break;
          nextRange = RangesByStart.get(i + 1);
          TimeRange latestRange = overlappingRanges.get(0);
          requestableTime = TimeRange.fromStartEnd(
              latestRange.end(), 
              nextRange.start(), 
              false);
    
          if (requestableTime.duration() >= request.getDuration()) {
            collectionOfRanges.add(requestableTime);
            continue;
          }
        } else {
          requestableTime = TimeRange.fromStartEnd(
              currentRange.end(), 
              nextRange.start(), 
              false);
          if (requestableTime.duration() >= request.getDuration()) {
            collectionOfRanges.add(requestableTime);
          }   
        }
      }
    }
    Collections.sort(collectionOfRanges, TimeRange.ORDER_BY_START);
    return collectionOfRanges;
  }
  
  public Set<String> getAllAttendees(Collection<Event> events) {
    Set<String> allAttendees = new HashSet<String>();
    for (Event event: events){
      allAttendees.addAll(event.getAttendees());
    }
    return allAttendees;
  }

  public boolean collectionContainsSet(Collection<String> stringCollection, Set<String> stringSet) {
    for (String strings: stringCollection) {
      if (!(stringSet.contains(strings))){
        return false;
      } 
    }
    return true;
  }
}
