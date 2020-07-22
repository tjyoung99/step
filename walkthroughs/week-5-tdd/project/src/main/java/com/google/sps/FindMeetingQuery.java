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
    System.out.println(collectionOfRanges);
      if (request.getDuration()> TimeRange.WHOLE_DAY.duration()) {
        return collectionOfRanges;
      }

    if(events.isEmpty() || request.getAttendees().isEmpty() || !collectionContainsSet(request.getAttendees(),getAllAttendees(events))){
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    List<TimeRange> RangesByStart = new ArrayList<TimeRange>();
    List<TimeRange> RangesByEnd = new ArrayList<TimeRange>();
    for(Event event : events){
      RangesByStart.add(event.getWhen());
      RangesByEnd.add(event.getWhen());
    }
    Collections.sort(RangesByStart,TimeRange.ORDER_BY_START);
    Collections.sort(RangesByEnd,TimeRange.ORDER_BY_END);
    int length = RangesByStart.size();
    System.out.println(RangesByStart);
    System.out.println(RangesByEnd);
    TimeRange availableTime = TimeRange.fromStartEnd(TimeRange.START_OF_DAY,RangesByStart.get(0).start(), false);

    if (availableTime.duration() >= request.getDuration()) {
      collectionOfRanges.add(availableTime);
    }

     availableTime = TimeRange.fromStartEnd(RangesByEnd.get(length-1).end(), TimeRange.END_OF_DAY, true);
    if (availableTime.duration() >= request.getDuration()) {
      collectionOfRanges.add(availableTime);
    }

    TimeRange currentRange;
    TimeRange nextRange;
    if(length > 1) {
      for (int i = 0; i < length; i++) {
        currentRange = RangesByStart.get(i);
        if (i + 1 >= length) break;
        nextRange = RangesByStart.get(i + 1);
        if (currentRange.overlaps(nextRange)){
          List<TimeRange> overlappingRanges = new ArrayList<TimeRange>();
          int j = 0;
          for (TimeRange range : RangesByStart) {
            j++;
            if (currentRange.overlaps(range)) {
              overlappingRanges.add(range);
            }
          }
          Collections.sort(overlappingRanges,TimeRange.ORDER_BY_END);
          i = j;
          if (i + 1 >= length) break;
          nextRange = RangesByStart.get(i + 1);
          TimeRange latestRange = overlappingRanges.get(0);
          availableTime = TimeRange.fromStartEnd(
              latestRange.end(), 
              nextRange.start(), 
              false);
    
          if (availableTime.duration() >= request.getDuration()) {
            collectionOfRanges.add(availableTime);
            continue;
          }
        } else {
          availableTime = TimeRange.fromStartEnd(
              currentRange.end(), 
              nextRange.start(), 
              false);
          if(availableTime.duration() >= request.getDuration()) {
            collectionOfRanges.add(availableTime);
          }   
        }
      }
    }
    Collections.sort(collectionOfRanges,TimeRange.ORDER_BY_START);
    return collectionOfRanges;
  }
  
  public Set<String> getAllAttendees(Collection<Event> events) {
    Set<String> allAttendees = new HashSet<String>();
    for(Event event: events){
      Set<String> attendees = event.getAttendees();
      for(String attendee: attendees) {
        allAttendees.add(attendee);
      }
    }
    return allAttendees;
  }

  public boolean collectionContainsSet(Collection<String> stringCollection, Set<String> stringSet) {
    for(String strings: stringCollection) {
      if (!(stringSet.contains(strings))){
        return false;
      } 
    }
    return true;
  }
  public boolean timerangeOverlapsAny(TimeRange timerange, List<TimeRange> TimeRangeList){
    for(TimeRange timeranges: TimeRangeList){
      if (timerange.overlaps(timeranges)){
        return true;
      }
    }
    return false;
  }
}
