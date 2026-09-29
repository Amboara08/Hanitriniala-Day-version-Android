package com.hanitriniala.day;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/** Private local storage. This class never uses a network connection. */
public final class PeriodStore {
  public static final String COMPLETE="complete", ONGOING="ongoing", UNKNOWN_END="unknown_end";
  public static final class Period {
    public String id, start, end, intensity, state;
    Period(String id,String start,String end,String intensity,String state){
      this.id=id; this.start=start; this.end=end; this.intensity=intensity; this.state=state;
    }
  }
  private final SharedPreferences prefs;
  public PeriodStore(Context context){ prefs=context.getSharedPreferences("hanitriniala.local.v2",Context.MODE_PRIVATE); }
  public ArrayList<Period> all(){
    ArrayList<Period> result=new ArrayList<Period>();
    try{
      JSONArray data=new JSONArray(prefs.getString("periods","[]"));
      for(int i=0;i<data.length();i++){
        JSONObject v=data.getJSONObject(i);
        String end=v.optString("end","");
        String state=v.optString("state",end.length()>0?COMPLETE:ONGOING);
        result.add(new Period(v.getString("id"),v.getString("start"),end,v.optString("intensity","medium"),state));
      }
    }catch(Exception ignored){}
    Collections.sort(result,new Comparator<Period>(){public int compare(Period a,Period b){return b.start.compareTo(a.start);}});
    return result;
  }
  public void save(ArrayList<Period> periods){
    JSONArray data=new JSONArray();
    try{ for(Period p:periods){ JSONObject v=new JSONObject();v.put("id",p.id);v.put("start",p.start);v.put("end",p.end);v.put("intensity",p.intensity);v.put("state",p.state);data.put(v); } }catch(Exception ignored){}
    prefs.edit().putString("periods",data.toString()).apply();
  }
  public String get(String key,String fallback){return prefs.getString(key,fallback);}
  public void put(String key,String value){prefs.edit().putString(key,value).apply();}
  public void clear(){prefs.edit().clear().apply();}
}
