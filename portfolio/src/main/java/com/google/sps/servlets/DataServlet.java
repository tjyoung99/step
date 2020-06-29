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

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles comments data. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
    Query query = 
        new Query("Comments").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    
    Date date = new Date();
    ArrayList<String> comments = new ArrayList<String>();
    SimpleDateFormat formatter = 
        new SimpleDateFormat("MMM, d ''yy 'at' hh:mm a");
    formatter.setTimeZone(TimeZone.getTimeZone("America/New_York"));

    for (Entity entity : results.asIterable()) {
      String comment = (String) entity.getProperty("user-comment");
      String user = (String) entity.getProperty("user");
      long timestamp = (long) entity.getProperty("timestamp");
      date.setTime(timestamp);
      String strDate = formatter.format(date);  
      String commentWithTime = 
          String.format("\"%s\"%nPosted by %s on %s", comment, user, strDate);
      comments.add(commentWithTime);
    }

    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String comment = request.getParameter("user-comment");
    long timestamp = System.currentTimeMillis();
    String user = request.getParameter("user");

    Entity taskEntity = new Entity("Comments");
    taskEntity.setProperty("user-comment", comment);
    taskEntity.setProperty("timestamp", timestamp);
    taskEntity.setProperty("user", user);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(taskEntity);
   
    response.sendRedirect("/index.html");
  }
}
