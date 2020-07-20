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
    Collection<TimeRange> result = new ArrayList<TimeRange>();
      if (request.getDuration()> 24* 60) {
        return Arrays.asList();
      }
    if(events.isEmpty() || request.getAttendees().isEmpty() || request.getAttendees().equals(getAllAttendees(events))){
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    List<TimeRange> listOfRanges = new ArrayList<TimeRange>();
    for(Event event : events){
      listOfRanges.add(event.getWhen());
    }
    Collections.sort(listOfRanges,TimeRange.ORDER_BY_START);
    TimeRange availableTime;
    TimeRange startOfDay = TimeRange.fromStartEnd(TimeRange.START_OF_DAY,listOfRanges.get(0).start(), false);
    if (startOfDay.duration() >= request.getDuration()) {
      result.add(startOfDay);
    }
    for (int i = 0; i < listOfRanges.size(); i++) {
      if (i == listOfRanges.size()-1) {
        availableTime = TimeRange.fromStartEnd(listOfRanges.get(i).end(), TimeRange.END_OF_DAY, true);
        if (availableTime.duration() >= request.getDuration()) {
          result.add(availableTime);
          break;
        }   
      }
      else {
        availableTime = TimeRange.fromStartEnd(listOfRanges.get(i).end(), listOfRanges.get(i+1).start(), false);
        if(availableTime.duration() >= request.getDuration()) {
          result.add(availableTime);
        }   
      }
    }
    System.out.println(request.getAttendees().equals(getAllAttendees(events)))
    System.out.println(result);
    return result;
  }
  public Set<String> getAllAttendees(Collection<Event> events) {
    Set<String> result = new HashSet<String>();
    for(Event event: events){
      Set<String> attendees = event.getAttendees();
      for(String attendee: attendees) {
        result.add(attendee);
      }
    }
    return result;
  }
}
