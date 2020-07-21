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
    //throw new UnsupportedOperationException("TODO: Implement this method.");
    Collection<TimeRange> collectionOfRanges = new ArrayList<TimeRange>();
      if (request.getDuration()> 24* 60) {
        return Arrays.asList();
      }

    if(events.isEmpty() || request.getAttendees().isEmpty() || !collectionContainsSet(request.getAttendees(),getAllAttendees(events))){
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    List<TimeRange> listOfRanges = new ArrayList<TimeRange>();
    for(Event event : events){
      listOfRanges.add(event.getWhen());
    }
    Collections.sort(listOfRanges,TimeRange.ORDER_BY_START);
    
    TimeRange availableTime = TimeRange.fromStartEnd(TimeRange.START_OF_DAY,listOfRanges.get(0).start(), false);
    if (availableTime.duration() >= request.getDuration()) {
      collectionOfRanges.add(availableTime);
    }
    TimeRange current;
    TimeRange Next;
    for (int i = 0; i < listOfRanges.size(); i++) {
      current = listOfRanges.get(i);
      next = listOfRanges.get(i + 1);

      if (i == listOfRanges.size()-1) {
        availableTime = TimeRange.fromStartEnd(current.end(), TimeRange.END_OF_DAY, true);
        
        if (availableTime.duration() >= request.getDuration()) {
          collectionOfRanges.add(availableTime);
          break;
        }
      } else if (current.overlaps(next)){
          List<TimeRange> overlappingRanges = new ArrayList<TimeRange>();
          overlappingRanges.add(current);

          do {
            overlappingRanges.add(next);
            i++;
            current = listOfRanges.get(i);
            if (i < listOfRanges.size()-1){
              next = listOfRanges.get(i+1);
            } else {
              Collections.sort(overlappingRanges,TimeRange.ORDER_BY_END);
              TimeRange latestRange = overlappingRanges.get(0);
              availableTime = TimeRange.fromStartEnd(latestRange.end(), next.start(), true);
        
              if (availableTime.duration() >= request.getDuration()) {
                collectionOfRanges.add(availableTime);
                break;
              }
            }
          }while (current.overlaps(next));
          Collections.sort(overlappingRanges,TimeRange.ORDER_BY_END);
          
          TimeRange latestRange = overlappingRanges.get(0);
          next = listOfRanges.get(i);
          availableTime = TimeRange.fromStartEnd(latestRange.end(), next.start(), true);
          if (availableTime.duration() >= request.getDuration()) {
          collectionOfRanges.add(availableTime);
          }
      }
      } else {
        availableTime = TimeRange.fromStartEnd(current.end(), listOfRanges.get(i+1).start(), false);
        if (current.overlaps(next)){
          List<TimeRange> overlappingRanges = new ArrayList<TimeRange>();
          overlappingRanges.add(current);
          for(TimeRange range : listOfRanges) {
            if (current.overlaps(range)) {
              overlappingRanges.add(range);
            }
          }
          Collections.sort(overlappingRanges,TimeRange.ORDER_BY_END);
          availableTime = TimeRange.fromStartEnd(overlappingRanges.get(0).end(), next.start(), false);
        }
        if(availableTime.duration() >= request.getDuration()) {
          collectionOfRanges.add(availableTime);
        }   
      }
    }
    System.out.println(request.getAttendees());
    System.out.println(getAllAttendees(events));
    System.out.println(collectionContainsSet(request.getAttendees(),getAllAttendees(events)));
    System.out.println(collectionOfRanges);
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
