package com.hanitriala.day;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/** Hanitriala Day: a private, offline menstrual-cycle journal. */
public class MainActivity extends Activity {
  private static final int RED=Color.rgb(232,14,59), RED_DARK=Color.rgb(177,17,49), RED_LIGHT=Color.rgb(255,219,226);
  private static final int INK=Color.rgb(47,34,38), MUTED=Color.rgb(112,91,96), SURFACE=Color.rgb(250,246,247);
  private static final int RISK_MEDIUM=Color.rgb(255,224,137), RISK_HIGH=Color.rgb(247,151,64), RISK_VERY_HIGH=Color.rgb(124, 8, 135);
  private PeriodStore db;
  private LinearLayout body;
  private String tab="Accueil";
  private Calendar shownMonth;
  private final SimpleDateFormat key=new SimpleDateFormat("yyyy-MM-dd",Locale.US);
  private final SimpleDateFormat full=new SimpleDateFormat("EEEE d MMMM yyyy",Locale.FRENCH);
  private final SimpleDateFormat shortDate=new SimpleDateFormat("d MMM yyyy",Locale.FRENCH);
  private final SimpleDateFormat monthName=new SimpleDateFormat("MMMM yyyy",Locale.FRENCH);

  @Override public void onCreate(Bundle state){
    super.onCreate(state);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);
    db=new PeriodStore(this);
    shownMonth=Calendar.getInstance(); shownMonth.set(Calendar.DAY_OF_MONTH,1); resetTime(shownMonth);
    render();
  }

  private int dp(int value){return (int)(value*getResources().getDisplayMetrics().density+0.5f);}
  private void resetTime(Calendar c){c.set(Calendar.HOUR_OF_DAY,12);c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);}
  private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
  private GradientDrawable shape(int color,int radius,int lineColor){GradientDrawable d=shape(color,radius);d.setStroke(dp(1),lineColor);return d;}
  private GradientDrawable gradient(int first,int second,int radius){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{first,second});d.setCornerRadius(dp(radius));return d;}
  private TextView text(String value,int size,int color){TextView v=new TextView(this);v.setText(value);v.setTextSize(size);v.setTextColor(color);v.setLineSpacing(0,1.12f);return v;}
  private void margin(View v,int top){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(top),0,0);v.setLayoutParams(p);}
  private LinearLayout card(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(16),dp(14),dp(16),dp(14));c.setBackground(shape(Color.WHITE,20,Color.rgb(242,224,226)));c.setElevation(dp(4));margin(c,12);body.addView(c);return c;}
  private TextView action(String label,View.OnClickListener listener,boolean primary){TextView v=text(label,15,primary?Color.WHITE:RED);v.setTypeface(Typeface.DEFAULT_BOLD);v.setGravity(Gravity.CENTER);v.setPadding(dp(12),dp(13),dp(12),dp(13));v.setBackground(shape(primary?RED:Color.rgb(255,239,242),18));v.setClickable(true);v.setOnClickListener(listener);margin(v,10);body.addView(v);return v;}
  private TextView smallButton(String label,View.OnClickListener listener){TextView v=text(label,14,RED);v.setTypeface(Typeface.DEFAULT_BOLD);v.setGravity(Gravity.CENTER);v.setPadding(dp(9),dp(9),dp(9),dp(9));v.setBackground(shape(Color.rgb(255,235,238),14));v.setOnClickListener(listener);return v;}

  private void render(){
    LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(SURFACE);
    ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
    body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(18),dp(18),dp(18),dp(12));scroll.addView(body);
    root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));root.addView(bottomNavigation());setContentView(root);
    if("Accueil".equals(tab)) home(); else if("Calendrier".equals(tab)) calendar(); else if("Historique".equals(tab)) history(); else settings();
    ReminderScheduler.schedule(this);
  }
  private View bottomNavigation(){
    LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);nav.setPadding(dp(7),dp(6),dp(7),dp(7));nav.setBackgroundColor(Color.WHITE);String[] icons={"⌂","▦","◷","⚙"};String[] labels={"Accueil","Calendrier","Historique","Paramètres"};final String[] ids={"Accueil","Calendrier","Historique","Paramètres"};
    for(int i=0;i<labels.length;i++){final int index=i;boolean selected=ids[i].equals(tab);LinearLayout item=new LinearLayout(this);item.setOrientation(LinearLayout.VERTICAL);item.setGravity(Gravity.CENTER);item.setPadding(dp(4),dp(5),dp(4),dp(5));if(selected)item.setBackground(shape(Color.rgb(255,232,237),16));TextView icon=text(icons[i],25,selected?RED:MUTED);icon.setGravity(Gravity.CENTER);icon.setTypeface(Typeface.DEFAULT_BOLD);item.addView(icon,new LinearLayout.LayoutParams(-1,dp(29)));TextView label=text(labels[i],10,selected?RED:MUTED);label.setGravity(Gravity.CENTER);label.setTypeface(Typeface.DEFAULT_BOLD);item.addView(label);item.setOnClickListener(new View.OnClickListener(){public void onClick(View view){tab=ids[index];render();}});LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.setMargins(dp(3),0,dp(3),0);nav.addView(item,p);}return nav;
  }
  private void heading(String title,String subtitle){LinearLayout hero=new LinearLayout(this);hero.setOrientation(LinearLayout.HORIZONTAL);hero.setGravity(Gravity.CENTER_VERTICAL);hero.setPadding(dp(17),dp(18),dp(17),dp(18));hero.setBackground(gradient(RED,RED_DARK,24));TextView icon=text("▣",27,Color.WHITE);icon.setGravity(Gravity.CENTER);icon.setBackground(shape(Color.argb(52,255,255,255),16));hero.addView(icon,new LinearLayout.LayoutParams(dp(49),dp(49)));LinearLayout words=new LinearLayout(this);words.setOrientation(LinearLayout.VERTICAL);TextView h=text(title,26,Color.WHITE);h.setTypeface(Typeface.DEFAULT_BOLD);words.addView(h);if(subtitle.length()>0){TextView s=text(subtitle,13,Color.rgb(255,226,232));s.setPadding(0,dp(2),0,0);words.addView(s);}LinearLayout.LayoutParams wp=new LinearLayout.LayoutParams(0,-2,1);wp.setMargins(dp(13),0,0,0);hero.addView(words,wp);body.addView(hero);}
  private String today(){return key.format(new Date());}
  private Date parse(String value){try{return key.parse(value);}catch(Exception ignored){return new Date();}}
  private String show(String value){return shortDate.format(parse(value));}
  private Calendar calendarOf(String value){Calendar c=Calendar.getInstance();c.setTime(parse(value));resetTime(c);return c;}
  private String plusDays(String value,int days){Calendar c=calendarOf(value);c.add(Calendar.DAY_OF_YEAR,days);return key.format(c.getTime());}
  private int days(String first,String second){return (int)((calendarOf(second).getTimeInMillis()-calendarOf(first).getTimeInMillis())/86400000L);}
  private ArrayList<PeriodStore.Period> periods(){return db.all();}
  private ArrayList<PeriodStore.Period> ascending(){ArrayList<PeriodStore.Period> all=periods();Collections.sort(all,new Comparator<PeriodStore.Period>(){public int compare(PeriodStore.Period a,PeriodStore.Period b){return a.start.compareTo(b.start);}});return all;}
  private String latestStart(){ArrayList<PeriodStore.Period> a=periods();return a.size()==0?"":a.get(0).start;}
  private int cycleLength(){ArrayList<PeriodStore.Period> a=ascending();int sum=0,count=0;for(int i=Math.max(1,a.size()-6);i<a.size();i++){int d=days(a.get(i-1).start,a.get(i).start);if(d>=15&&d<=60){sum+=d;count++;}}if(count>0)return Math.round((float)sum/count);try{return Integer.parseInt(db.get("cycle","28"));}catch(Exception ignored){return 28;}}
  private int periodLength(){int sum=0,count=0;for(PeriodStore.Period p:periods())if(PeriodStore.COMPLETE.equals(p.state)){int d=days(p.start,p.end)+1;if(d>=1&&d<=14){sum+=d;count++;if(count==6)break;}}return count==0?5:Math.round((float)sum/count);}
  private ArrayList<String> estimateStarts(){ArrayList<String> result=new ArrayList<String>();String last=latestStart();if(last.length()==0)return result;int cycle=cycleLength();for(int i=1;i<=8;i++)result.add(plusDays(last,cycle*i));return result;}
  private boolean between(String value,String start,String end){return value.compareTo(start)>=0&&value.compareTo(end)<=0;}
  private boolean realPeriodDay(String value){for(PeriodStore.Period p:periods()){if(PeriodStore.COMPLETE.equals(p.state)&&between(value,p.start,p.end))return true;if(PeriodStore.ONGOING.equals(p.state)&&between(value,p.start,today()))return true;if(PeriodStore.UNKNOWN_END.equals(p.state)&&value.equals(p.start))return true;}return false;}
  private boolean predictedPeriodDay(String value){for(String start:estimateStarts())if(between(value,start,plusDays(start,periodLength()-1)))return true;return false;}
  private int fertileState(String value){for(String start:estimateStarts()){String ov=plusDays(start,-14);if(between(value,plusDays(ov,-5),plusDays(ov,1)))return 2;if(between(value,plusDays(ov,-7),plusDays(ov,3)))return 1;}return 0;}
  private String closestOvulation(String value){String closest="";int best=10000;for(String start:estimateStarts()){String ov=plusDays(start,-14);int distance=Math.abs(days(ov,value));if(distance<best){best=distance;closest=ov;}}return best<=cycleLength()?closest:"";}
  private int pregnancyRisk(String value){String ov=closestOvulation(value);if(ov.length()==0)return 0;int offset=days(ov,value);if(offset>=-1&&offset<=1)return 4;if(offset>=-3&&offset<=2)return 3;if(offset>=-5&&offset<=3)return 2;return 1;}
  private String riskLabel(int risk){return risk==4?"TRÈS ÉLEVÉ":risk==3?"ÉLEVÉ":risk==2?"MOYEN":risk==1?"FAIBLE":"INDÉTERMINÉ";}
  private int riskColor(int risk){return risk==4?RISK_VERY_HIGH:risk==3?RISK_HIGH:risk==2?Color.rgb(211,159,32):risk==1?Color.rgb(87,151,100):MUTED;}
  private String nextEstimatedStart(){ArrayList<String> a=estimateStarts();if(a.size()==0)return "";String now=today();for(String s:a)if(s.compareTo(now)>=0)return s;return a.get(0);}

  private void home(){
    heading("Hanitriala Day","");
    String next=nextEstimatedStart();
    LinearLayout top=card();
    if(next.length()==0){top.addView(text("Commencez votre suivi",20,INK));TextView d=text("Ajoutez le premier jour réel des règles. Les prévisions s’amélioreront avec l’historique.",15,MUTED);margin(d,5);top.addView(d);}else{
      top.addView(text("Prochaines règles estimées",14,MUTED));TextView date=text(show(next),24,RED);date.setTypeface(Typeface.DEFAULT_BOLD);margin(date,2);top.addView(date);
      int remaining=days(today(),next);TextView count=text(remaining>0?"Dans environ "+remaining+" jours":"Aujourd’hui ou en retard",16,INK);margin(count,3);top.addView(count);
    }
    String latest=latestStart();if(latest.length()>0&&next.length()>0&&today().compareTo(plusDays(next,1))>0){TextView late=text("Retard possible : enregistrez le début réel si les règles ont commencé.",14,RED);margin(late,9);top.addView(late);}
    LinearLayout metrics=card();metrics.addView(text("Estimations du cycle",18,INK));TextView info=text("Cycle moyen : "+cycleLength()+" jours\nDurée des règles : environ "+periodLength()+" jours",15,MUTED);margin(info,6);metrics.addView(info);
    if(next.length()>0){String ov=plusDays(next,-14);TextView fertile=text("Fenêtre fertile estimée : "+show(plusDays(ov,-5))+" au "+show(plusDays(ov,1))+"\nOvulation estimée : "+show(ov),15,INK);margin(fertile,8);metrics.addView(fertile);}
    action("＋  Mes règles commencent aujourd’hui",new View.OnClickListener(){public void onClick(View v){beginOngoing(today());}},true);
    action("Ajouter une période ou un ancien cycle",new View.OnClickListener(){public void onClick(View v){chooseStartDate();}},false);
  }

  private void calendar(){
    heading("Calendrier","Touchez un jour pour voir ses informations et son estimation.");
    LinearLayout controls=new LinearLayout(this);controls.setGravity(Gravity.CENTER_VERTICAL);TextView previous=smallButton("‹",new View.OnClickListener(){public void onClick(View v){shownMonth.add(Calendar.MONTH,-1);render();}});controls.addView(previous,new LinearLayout.LayoutParams(dp(45),dp(42)));TextView label=text(monthName.format(shownMonth.getTime()),20,INK);label.setGravity(Gravity.CENTER);label.setTypeface(Typeface.DEFAULT_BOLD);controls.addView(label,new LinearLayout.LayoutParams(0,dp(42),1));TextView next=smallButton("›",new View.OnClickListener(){public void onClick(View v){shownMonth.add(Calendar.MONTH,1);render();}});controls.addView(next,new LinearLayout.LayoutParams(dp(45),dp(42)));margin(controls,14);body.addView(controls);
    GridLayout grid=new GridLayout(this);grid.setColumnCount(7);grid.setUseDefaultMargins(false);margin(grid,12);body.addView(grid);
    String[] weekday={"L","M","M","J","V","S","D"};for(String d:weekday)addCell(grid,d,Color.TRANSPARENT,INK,false,null);
    Calendar first=(Calendar)shownMonth.clone();first.set(Calendar.DAY_OF_MONTH,1);int leading=(first.get(Calendar.DAY_OF_WEEK)+5)%7;for(int i=0;i<leading;i++)addCell(grid,"",Color.TRANSPARENT,MUTED,false,null);
    int max=first.getActualMaximum(Calendar.DAY_OF_MONTH);for(int day=1;day<=max;day++){Calendar c=(Calendar)first.clone();c.set(Calendar.DAY_OF_MONTH,day);final String value=key.format(c.getTime());int fill=Color.WHITE,ink=INK;String mark="";int risk=pregnancyRisk(value);if(realPeriodDay(value)){fill=RED;ink=Color.WHITE;mark="●";}else if(predictedPeriodDay(value)){fill=RED_LIGHT;mark="●";}else if(risk==4){fill=RISK_VERY_HIGH;ink=Color.WHITE;mark="◆";}else if(risk==3){fill=RISK_HIGH;mark="◆";}else if(risk==2){fill=RISK_MEDIUM;mark="·";}if(value.equals(today())&&!realPeriodDay(value))fill=Color.rgb(255,235,238);addCell(grid,day+"\n"+mark,fill,ink,true,new View.OnClickListener(){public void onClick(View v){dayDetails(value);}});}
    LinearLayout legend=card();TextView legendTitle=text("Légende",17,INK);legendTitle.setTypeface(Typeface.DEFAULT_BOLD);legend.addView(legendTitle);legendRow(legend,RED,"Règles enregistrées");legendRow(legend,RED_LIGHT,"Règles prévues");legendRow(legend,RISK_VERY_HIGH,"Risque estimé très élevé");legendRow(legend,RISK_HIGH,"Risque estimé élevé");legendRow(legend,RISK_MEDIUM,"Risque estimé moyen");legendRow(legend,Color.WHITE,"Risque estimé faible");
    action("Ajouter une période",new View.OnClickListener(){public void onClick(View v){chooseStartDate();}},true);
  }
  private void addCell(GridLayout grid,String value,int background,int color,boolean click,View.OnClickListener listener){TextView cell=text(value,14,color);cell.setGravity(Gravity.CENTER);cell.setPadding(0,dp(4),0,dp(2));cell.setBackground(shape(background,12,Color.rgb(246,232,233)));if(click){cell.setClickable(true);cell.setOnClickListener(listener);}GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(53);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(2),dp(2),dp(2),dp(2));grid.addView(cell,p);}
  private void legendRow(LinearLayout parent,int color,String label){LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,dp(5),0,dp(5));TextView colorBox=new TextView(this);colorBox.setBackground(shape(color,6,Color.rgb(225,209,213)));row.addView(colorBox,new LinearLayout.LayoutParams(dp(22),dp(22)));TextView labelView=text(label,14,MUTED);LinearLayout.LayoutParams labelParams=new LinearLayout.LayoutParams(0,-2,1);labelParams.setMargins(dp(11),0,0,0);row.addView(labelView,labelParams);parent.addView(row);}
  private void dayDetails(final String value){
    String status;if(realPeriodDay(value))status="Règles enregistrées";else if(predictedPeriodDay(value))status="Règles prévues";else status="Aucune règle enregistrée";
    final PeriodStore.Period existing=periodStarting(value);int risk=pregnancyRisk(value);String ov=closestOvulation(value);
    LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setBackground(shape(Color.WHITE,24));
    LinearLayout header=new LinearLayout(this);header.setOrientation(LinearLayout.VERTICAL);header.setPadding(dp(20),dp(18),dp(20),dp(18));header.setBackground(gradient(RED,RED_DARK,22));TextView calendarIcon=text("▣  DÉTAIL DU JOUR",13,Color.rgb(255,225,232));calendarIcon.setTypeface(Typeface.DEFAULT_BOLD);header.addView(calendarIcon);TextView date=text(full.format(parse(value)),21,Color.WHITE);date.setTypeface(Typeface.DEFAULT_BOLD);date.setPadding(0,dp(4),0,0);header.addView(date);panel.addView(header);
    LinearLayout content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(20),dp(18),dp(20),dp(16));TextView state=text(status,15,INK);state.setTypeface(Typeface.DEFAULT_BOLD);content.addView(state);TextView riskTitle=text("RISQUE DE GROSSESSE ESTIMÉ",12,MUTED);riskTitle.setTypeface(Typeface.DEFAULT_BOLD);riskTitle.setPadding(0,dp(17),0,dp(7));content.addView(riskTitle);TextView badge=text(riskLabel(risk),21,Color.WHITE);badge.setTypeface(Typeface.DEFAULT_BOLD);badge.setGravity(Gravity.CENTER);badge.setPadding(dp(12),dp(11),dp(12),dp(11));badge.setBackground(shape(riskColor(risk),16));content.addView(badge);if(ov.length()>0){TextView ovulation=text("Ovulation estimée autour du "+show(ov),14,MUTED);ovulation.setPadding(0,dp(11),0,0);content.addView(ovulation);}TextView note=text(risk==1?"Estimation du calendrier : un niveau faible ne signifie jamais un risque nul.":"Niveau calculé d’après l’historique du cycle et l’ovulation estimée.",12,MUTED);note.setPadding(0,dp(12),0,0);content.addView(note);panel.addView(content);
    final AlertDialog dialog=new AlertDialog.Builder(this).setView(panel).setNegativeButton("Fermer",null).setPositiveButton(existing==null?"Règles ce jour":"Modifier",new DialogInterface.OnClickListener(){public void onClick(DialogInterface d,int which){if(existing==null)beginOngoing(value);else editPeriod(existing);}}).create();dialog.setOnShowListener(new DialogInterface.OnShowListener(){public void onShow(DialogInterface d){dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(RED);dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(MUTED);}});dialog.show();
  }
  private PeriodStore.Period periodStarting(String value){for(PeriodStore.Period p:periods())if(p.start.equals(value))return p;return null;}

  private void history(){
    heading("Historique","Vos périodes réelles, de la plus récente à la plus ancienne.");ArrayList<PeriodStore.Period> all=periods();if(all.size()==0){LinearLayout empty=card();empty.addView(text("Aucune période enregistrée",18,INK));TextView hint=text("Ajoutez un début de règles pour créer les premières prévisions.",14,MUTED);margin(hint,5);empty.addView(hint);}
    for(final PeriodStore.Period p:all){LinearLayout row=card();TextView date=text(show(p.start),18,INK);date.setTypeface(Typeface.DEFAULT_BOLD);row.addView(date);String detail;if(PeriodStore.COMPLETE.equals(p.state))detail="Fin : "+show(p.end)+" · Durée : "+(days(p.start,p.end)+1)+" jour(s)";else if(PeriodStore.ONGOING.equals(p.state))detail="Règles en cours";else detail="Ancien cycle · dernier jour non renseigné";TextView meta=text(detail+"\nIntensité : "+intensityLabel(p.intensity),14,MUTED);margin(meta,3);row.addView(meta);row.setClickable(true);row.setOnClickListener(new View.OnClickListener(){public void onClick(View v){editPeriod(p);}});}
    action("Ajouter une période",new View.OnClickListener(){public void onClick(View v){chooseStartDate();}},true);
  }
  private String intensityLabel(String i){return "light".equals(i)?"légère":"heavy".equals(i)?"abondante":"moyenne";}
  private void editPeriod(final PeriodStore.Period p){String[] options;if(PeriodStore.ONGOING.equals(p.state))options=new String[]{"Modifier le premier jour","Choisir le dernier jour","Terminer aujourd’hui","Dernier jour inconnu","Modifier l’intensité","Supprimer"};else if(PeriodStore.UNKNOWN_END.equals(p.state))options=new String[]{"Modifier le premier jour","Ajouter le dernier jour","Modifier l’intensité","Supprimer"};else options=new String[]{"Modifier le premier jour","Modifier le dernier jour","Modifier l’intensité","Supprimer"};new AlertDialog.Builder(this).setTitle("Période du "+show(p.start)).setItems(options,new DialogInterface.OnClickListener(){public void onClick(DialogInterface d,int w){if(w==0){chooseStartDateFor(p);return;}if(PeriodStore.ONGOING.equals(p.state)){if(w==1)chooseEndDate(p);else if(w==2)complete(p,today());else if(w==3)markUnknownEnd(p);else if(w==4)chooseIntensity(p);else remove(p);}else{if(w==1)chooseEndDate(p);else if(w==2)chooseIntensity(p);else remove(p);}}}).show();}
  private void remove(final PeriodStore.Period p){new AlertDialog.Builder(this).setMessage("Supprimer cette période ?").setNegativeButton("Annuler",null).setPositiveButton("Supprimer",new DialogInterface.OnClickListener(){public void onClick(DialogInterface d,int w){ArrayList<PeriodStore.Period> a=periods();for(int i=a.size()-1;i>=0;i--)if(a.get(i).id.equals(p.id))a.remove(i);db.save(a);render();}}).show();}

  private void settings(){
    heading("Paramètres","");
    LinearLayout prediction=card();prediction.addView(text("Prévisions",18,INK));
    TextView explanation=text("L’application utilise la moyenne des derniers débuts de règles réels. Vous pouvez fournir une durée habituelle pour le premier calcul.",14,MUTED);margin(explanation,5);prediction.addView(explanation);
    final EditText input=new EditText(this);input.setInputType(2);input.setText(""+cycleLength());input.setHint("Durée habituelle du cycle");margin(input,9);prediction.addView(input);
    TextView save=smallButton("Enregistrer la durée",new View.OnClickListener(){public void onClick(View v){try{int value=Integer.parseInt(input.getText().toString().trim());if(value<15||value>60)throw new Exception();db.put("cycle",""+value);ReminderScheduler.schedule(MainActivity.this);toast("Durée enregistrée.");render();}catch(Exception e){input.setError("Indiquez une durée entre 15 et 60 jours.");}}});
    LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.setMargins(0,dp(8),0,0);prediction.addView(save,sp);

    LinearLayout reminders=card();TextView reminderTitle=text("Rappel des prochaines règles",18,INK);reminderTitle.setTypeface(Typeface.DEFAULT_BOLD);reminders.addView(reminderTitle);
    TextView reminderDescription=text("La notification peut être envoyée même lorsque l’application n’est pas ouverte.",14,MUTED);reminderDescription.setPadding(0,dp(5),0,dp(7));reminders.addView(reminderDescription);
    final CheckBox enabled=new CheckBox(this);enabled.setText("Activer les notifications");enabled.setTextColor(INK);enabled.setChecked("true".equals(db.get("reminder_enabled","false")));reminders.addView(enabled);
    final EditText daysInput=new EditText(this);daysInput.setInputType(2);daysInput.setHint("Nombre de jours avant");daysInput.setText(db.get("reminder_days","2"));reminders.addView(daysInput);
    int savedHour;int savedMinute;try{savedHour=Integer.parseInt(db.get("reminder_hour","8"));}catch(Exception error){savedHour=8;}try{savedMinute=Integer.parseInt(db.get("reminder_minute","0"));}catch(Exception error){savedMinute=0;}
    final int[] selectedTime={savedHour,savedMinute};final TextView[] timeButton=new TextView[1];
    timeButton[0]=smallButton(String.format(Locale.US,"Heure du rappel : %02d:%02d",selectedTime[0],selectedTime[1]),new View.OnClickListener(){public void onClick(View view){TimePickerDialog picker=new TimePickerDialog(MainActivity.this,new TimePickerDialog.OnTimeSetListener(){public void onTimeSet(TimePicker timePicker,int hour,int minute){selectedTime[0]=hour;selectedTime[1]=minute;timeButton[0].setText(String.format(Locale.US,"Heure du rappel : %02d:%02d",hour,minute));}},selectedTime[0],selectedTime[1],true);picker.show();}});
    LinearLayout.LayoutParams timeParams=new LinearLayout.LayoutParams(-1,-2);timeParams.setMargins(0,dp(8),0,0);reminders.addView(timeButton[0],timeParams);
    TextView saveReminder=smallButton("Enregistrer le rappel",new View.OnClickListener(){public void onClick(View view){try{int daysBefore=Integer.parseInt(daysInput.getText().toString().trim());if(daysBefore<0||daysBefore>7)throw new Exception();db.put("reminder_enabled",enabled.isChecked()?"true":"false");db.put("reminder_days",""+daysBefore);db.put("reminder_hour",""+selectedTime[0]);db.put("reminder_minute",""+selectedTime[1]);if(enabled.isChecked())requestNotificationPermissionIfNeeded();ReminderScheduler.schedule(MainActivity.this);toast("Configuration du rappel enregistrée.");}catch(Exception error){daysInput.setError("Indiquez une valeur entre 0 et 7 jours.");}}});
    LinearLayout.LayoutParams reminderButtonParams=new LinearLayout.LayoutParams(-1,-2);reminderButtonParams.setMargins(0,dp(8),0,0);reminders.addView(saveReminder,reminderButtonParams);
    TextView testReminder=smallButton("Tester la notification",new View.OnClickListener(){public void onClick(View view){requestNotificationPermissionIfNeeded();ReminderScheduler.test(MainActivity.this);}});
    LinearLayout.LayoutParams testParams=new LinearLayout.LayoutParams(-1,-2);testParams.setMargins(0,dp(8),0,0);reminders.addView(testReminder,testParams);

    LinearLayout signature=card();TextView by=text("BY Aboalakely\npour toi\nTiffakeliko.",18,RED);by.setGravity(Gravity.CENTER);by.setTypeface(Typeface.DEFAULT_BOLD);by.setLineSpacing(dp(3),1.1f);signature.addView(by);
    action("Supprimer toutes les données",new View.OnClickListener(){public void onClick(View v){new AlertDialog.Builder(MainActivity.this).setTitle("Supprimer les données ?").setMessage("Cette action effacera définitivement l’historique et les prévisions de ce téléphone.").setNegativeButton("Annuler",null).setPositiveButton("Supprimer",new DialogInterface.OnClickListener(){public void onClick(DialogInterface d,int w){ReminderScheduler.cancel(MainActivity.this);db.clear();render();}}).show();}},false);
  }

  private void chooseStartDate(){Calendar c=Calendar.getInstance();new DatePickerDialog(this,new DatePickerDialog.OnDateSetListener(){public void onDateSet(DatePicker picker,int year,int month,int day){Calendar chosen=Calendar.getInstance();chosen.set(year,month,day);resetTime(chosen);startOptions(key.format(chosen.getTime()));}},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();}
  private void chooseStartDateFor(final PeriodStore.Period p){Calendar c=calendarOf(p.start);new DatePickerDialog(this,new DatePickerDialog.OnDateSetListener(){public void onDateSet(DatePicker picker,int year,int month,int day){Calendar chosen=Calendar.getInstance();chosen.set(year,month,day);resetTime(chosen);changeStart(p,key.format(chosen.getTime()));}},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();}
  private void changeStart(PeriodStore.Period p,String start){if(start.compareTo(today())>0){toast("Le premier jour réel ne peut pas être dans le futur.");return;}if(p.end.length()>0&&start.compareTo(p.end)>0){toast("Le premier jour ne peut pas être après le dernier jour.");return;}for(PeriodStore.Period other:periods())if(!other.id.equals(p.id)&&other.start.equals(start)){toast("Une autre période commence déjà à cette date.");return;}p.start=start;saveChange(p);toast("Date corrigée et prévisions recalculées.");render();}
  private void startOptions(final String start){new AlertDialog.Builder(this).setTitle("Début : "+show(start)).setMessage("Comment souhaitez-vous enregistrer cette période ?").setItems(new String[]{"Règles en cours","Choisir le dernier jour","Dernier jour inconnu (ancien cycle)"},new DialogInterface.OnClickListener(){public void onClick(DialogInterface d,int which){if(which==0)beginOngoing(start);else if(which==1)chooseEndForNew(start);else createPeriod(start,"",PeriodStore.UNKNOWN_END);}}).show();}
  private void beginOngoing(String start){for(PeriodStore.Period p:periods())if(PeriodStore.ONGOING.equals(p.state)){toast("Une période est déjà en cours.");return;}createPeriod(start,"",PeriodStore.ONGOING);}
  private void chooseEndForNew(final String start){Calendar c=calendarOf(start);new DatePickerDialog(this,new DatePickerDialog.OnDateSetListener(){public void onDateSet(DatePicker picker,int y,int m,int day){Calendar chosen=Calendar.getInstance();chosen.set(y,m,day);resetTime(chosen);String end=key.format(chosen.getTime());if(end.compareTo(start)<0){toast("La fin ne peut pas précéder le début.");return;}createPeriod(start,end,PeriodStore.COMPLETE);}},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();}
  private boolean sameStart(String start){for(PeriodStore.Period p:periods())if(p.start.equals(start))return true;return false;}
  private void createPeriod(String start,String end,String state){if(sameStart(start)){toast("Une période commence déjà à cette date.");return;}ArrayList<PeriodStore.Period> a=periods();final PeriodStore.Period p=new PeriodStore.Period(UUID.randomUUID().toString(),start,end,"medium",state);a.add(p);db.save(a);chooseIntensity(p);}
  private void chooseEndDate(final PeriodStore.Period p){Calendar c=calendarOf(p.start);new DatePickerDialog(this,new DatePickerDialog.OnDateSetListener(){public void onDateSet(DatePicker picker,int y,int m,int day){Calendar chosen=Calendar.getInstance();chosen.set(y,m,day);resetTime(chosen);complete(p,key.format(chosen.getTime()));}},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();}
  private void saveChange(PeriodStore.Period changed){ArrayList<PeriodStore.Period> a=periods();for(PeriodStore.Period p:a)if(p.id.equals(changed.id)){p.start=changed.start;p.end=changed.end;p.intensity=changed.intensity;p.state=changed.state;break;}db.save(a);}
  private void markUnknownEnd(PeriodStore.Period p){p.end="";p.state=PeriodStore.UNKNOWN_END;saveChange(p);toast("Dernier jour laissé inconnu.");render();}
  private void complete(PeriodStore.Period p,String end){if(end.compareTo(p.start)<0){toast("La fin ne peut pas précéder le début.");return;}p.end=end;p.state=PeriodStore.COMPLETE;saveChange(p);toast("Période mise à jour.");render();}
  private void chooseIntensity(final PeriodStore.Period p){new AlertDialog.Builder(this).setTitle("Intensité").setSingleChoiceItems(new String[]{"Légère","Moyenne","Abondante"},"light".equals(p.intensity)?0:"heavy".equals(p.intensity)?2:1,new DialogInterface.OnClickListener(){public void onClick(DialogInterface d,int which){p.intensity=which==0?"light":which==2?"heavy":"medium";saveChange(p);d.dismiss();toast("Période enregistrée.");render();}}).show();}
  private void requestNotificationPermissionIfNeeded(){if(android.os.Build.VERSION.SDK_INT<33)return;try{java.lang.reflect.Method checkPermission=Activity.class.getMethod("checkSelfPermission",String.class);int result=((Integer)checkPermission.invoke(this,"android.permission.POST_NOTIFICATIONS")).intValue();if(result!=0){java.lang.reflect.Method requestPermissions=Activity.class.getMethod("requestPermissions",String[].class,int.class);requestPermissions.invoke(this,new Object[]{new String[]{"android.permission.POST_NOTIFICATIONS"},Integer.valueOf(700)});}}catch(Exception ignored){}}
  private void toast(String message){Toast.makeText(this,message,Toast.LENGTH_LONG).show();}
}
