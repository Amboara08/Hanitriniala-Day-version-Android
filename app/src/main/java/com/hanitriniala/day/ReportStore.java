package com.hanitriniala.day;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

public final class ReportStore {
  public static final class Entry {
    public String id;
    public long timestamp;

    Entry(String id, long timestamp) {
      this.id = id;
      this.timestamp = timestamp;
    }
  }

  private final SharedPreferences preferences;

  public ReportStore(Context context) {
    preferences = context.getSharedPreferences(
      "hanitriniala.reports",
      Context.MODE_PRIVATE
    );
  }

  public ArrayList<Entry> all() {
    ArrayList<Entry> result = new ArrayList<Entry>();

    try {
      JSONArray data = new JSONArray(
        preferences.getString("entries", "[]")
      );

      for (int i = 0; i < data.length(); i++) {
        JSONObject value = data.getJSONObject(i);

        result.add(
          new Entry(
            value.getString("id"),
            value.getLong("timestamp")
          )
        );
      }
    } catch (Exception ignored) {
    }

    Collections.sort(
      result,
      new Comparator<Entry>() {
        public int compare(Entry first, Entry second) {
          if (first.timestamp == second.timestamp) {
            return 0;
          }

          return first.timestamp < second.timestamp ? 1 : -1;
        }
      }
    );

    return result;
  }

  public void add(long timestamp) {
    ArrayList<Entry> entries = all();

    entries.add(
      new Entry(
        UUID.randomUUID().toString(),
        timestamp
      )
    );

    save(entries);
  }

  public void remove(String id) {
    ArrayList<Entry> entries = all();

    for (int i = entries.size() - 1; i >= 0; i--) {
      if (entries.get(i).id.equals(id)) {
        entries.remove(i);
      }
    }

    save(entries);
  }

  private void save(ArrayList<Entry> entries) {
    JSONArray data = new JSONArray();

    try {
      for (Entry entry : entries) {
        JSONObject value = new JSONObject();

        value.put("id", entry.id);
        value.put("timestamp", entry.timestamp);

        data.put(value);
      }
    } catch (Exception ignored) {
    }

    preferences.edit()
      .putString("entries", data.toString())
      .apply();
  }

  public void clear() {
    preferences.edit().clear().apply();
  }
}