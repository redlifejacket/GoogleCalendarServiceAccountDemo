package testgoogle;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GoogleCalendarServiceAccountDemo {

  private static void usage() {
    System.out
        .println("Usage: <service_account_email> <service_account_pkcs12_path> <email_addresses>");
    System.out.println(
        "  service_account_email:       Service Account ID from Google Cloud console (eg: my-sa@calendardemo.iam.gserviceaccount.com)");
    System.out.println("  service_account_private_key: Service Account private key in P12 format");
    System.out.println("  email_addresses:             Comma-separated list of email addresses");
    System.exit(0);
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 3) {
      usage();
    }
    Set<String> scopes = new HashSet<String>();
    scopes.add(DirectoryScopes.ADMIN_DIRECTORY_USER);
    scopes.add(CalendarScopes.CALENDAR);
    HttpTransport httpTransport = new NetHttpTransport();
    try {
      for (String email : args[2].split(",")) {
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        GoogleCredential credential = new GoogleCredential.Builder()
            .setTransport(httpTransport)
            .setJsonFactory(jsonFactory)
            .setServiceAccountId(args[0])
            .setServiceAccountUser(email)
            .setServiceAccountPrivateKeyFromP12File(new File(args[1]))
            .setServiceAccountScopes(scopes)
            .build();
        Calendar calendar = new Calendar.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName(GoogleCalendarServiceAccountDemo.class.getName())
            .build();
        GoogleCalendarServiceAccountDemo googleCalendarServiceAccountDemo = new GoogleCalendarServiceAccountDemo();
        googleCalendarServiceAccountDemo.getCalendarEventsForUser(calendar, email);
      }

    } catch (GeneralSecurityException e) {
      e.printStackTrace();
    }
  }

  private void getCalendarEventsForUser(Calendar calendar, String email)
      throws IOException {

    DateTime now = new DateTime(System.currentTimeMillis());
    Events events = calendar.events().list(email)
        .setMaxResults(10)
        .setTimeMin(now)
        .setOrderBy("startTime")
        .setSingleEvents(true)
        .execute();
    List<Event> items = events.getItems();
    if (items.size() == 0) {
      System.out.println("No upcoming events found.");
      return;
    }

    System.out.println("Upcoming events for [" + email + "]");
    for (Event event : items) {
      DateTime start = event.getStart().getDateTime();
      DateTime end = event.getEnd().getDateTime();
      if (start == null) {
        start = event.getStart().getDate();

      }
      System.out.printf("%s (%s)(%s)\n", event.getSummary(), start, end);
    }
  }
}