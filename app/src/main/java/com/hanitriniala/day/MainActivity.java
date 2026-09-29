package com.hanitriniala.day;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
// import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
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

public class MainActivity extends Activity {
  private static final int RED = Color.rgb(232, 14, 59);
  private static final int RED_DARK = Color.rgb(177, 17, 49);
  private static final int RED_LIGHT = Color.rgb(246, 58, 164);
  private static final int INK = Color.rgb(47, 34, 38);
  private static final int MUTED = Color.rgb(112, 91, 96);
  private static final int SURFACE = Color.rgb(250, 246, 247);

  private static final int RISK_MEDIUM =
    Color.rgb(230, 193, 92);

  private static final int RISK_HIGH =
    Color.rgb(252, 141, 38);

  private static final int RISK_VERY_HIGH =
    Color.rgb(104, 35, 199);

  private static final int OVULATION_GREEN =
    Color.rgb(55, 174, 104);

  private static final int TODAY_BLUE =
    Color.rgb(14, 198, 234);

  private PeriodStore db;
  private ReportStore reportStore;
  private PinManager pinManager;

  private LinearLayout body;
  private String tab = "Accueil";
  private Calendar shownMonth;

  private boolean unlocked = true;
  private long backgroundAt = 0;

  private final SimpleDateFormat key =
    new SimpleDateFormat("yyyy-MM-dd", Locale.US);

  private final SimpleDateFormat full =
    new SimpleDateFormat(
      "EEEE d MMMM yyyy",
      Locale.FRENCH
    );

  private final SimpleDateFormat shortDate =
    new SimpleDateFormat(
      "d MMM yyyy",
      Locale.FRENCH
    );

  private final SimpleDateFormat monthName =
    new SimpleDateFormat(
      "MMMM yyyy",
      Locale.FRENCH
    );

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);


    db = new PeriodStore(this);
    reportStore = new ReportStore(this);
    pinManager = new PinManager(this);

    shownMonth = Calendar.getInstance();
    shownMonth.set(Calendar.DAY_OF_MONTH, 1);
    resetTime(shownMonth);

    if (pinManager.hasPin()) {
      unlocked = false;
      showLockScreen();
    } else {
      unlocked = true;
      render();
    }
  }

  @Override
  protected void onStop() {
    super.onStop();

    if (
      pinManager != null &&
      pinManager.hasPin() &&
      unlocked
    ) {
      backgroundAt = System.currentTimeMillis();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (
      pinManager != null &&
      pinManager.hasPin() &&
      unlocked &&
      backgroundAt > 0 &&
      System.currentTimeMillis() - backgroundAt > 3000
    ) {
      unlocked = false;
      showLockScreen();
    }
  }

  private int dp(int value) {
    return (int) (
      value *
      getResources().getDisplayMetrics().density +
      0.5f
    );
  }

  private void resetTime(Calendar calendar) {
    calendar.set(Calendar.HOUR_OF_DAY, 12);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
  }

  private GradientDrawable shape(
    int color,
    int radius
  ) {
    GradientDrawable drawable =
      new GradientDrawable();

    drawable.setColor(color);
    drawable.setCornerRadius(dp(radius));

    return drawable;
  }

  private GradientDrawable shape(
    int color,
    int radius,
    int lineColor
  ) {
    GradientDrawable drawable =
      shape(color, radius);

    drawable.setStroke(dp(1), lineColor);

    return drawable;
  }

  private GradientDrawable shape(
    int color,
    int radius,
    int lineColor,
    int lineWidth
  ) {
    GradientDrawable drawable =
      shape(color, radius);

    drawable.setStroke(
      dp(lineWidth),
      lineColor
    );

    return drawable;
  }

  private GradientDrawable gradient(
    int first,
    int second,
    int radius
  ) {
    GradientDrawable drawable =
      new GradientDrawable(
        GradientDrawable.Orientation.TL_BR,
        new int[] {first, second}
      );

    drawable.setCornerRadius(dp(radius));

    return drawable;
  }

  private TextView text(
    String value,
    int size,
    int color
  ) {
    TextView view = new TextView(this);

    view.setText(value);
    view.setTextSize(size);
    view.setTextColor(color);
    view.setLineSpacing(0, 1.12f);

    return view;
  }

  private void margin(View view, int top) {
    LinearLayout.LayoutParams params =
      new LinearLayout.LayoutParams(-1, -2);

    params.setMargins(0, dp(top), 0, 0);
    view.setLayoutParams(params);
  }

  private LinearLayout card() {
    LinearLayout card = new LinearLayout(this);

    card.setOrientation(LinearLayout.VERTICAL);

    card.setPadding(
      dp(16),
      dp(14),
      dp(16),
      dp(14)
    );

    card.setBackground(
      shape(
        Color.WHITE,
        20,
        Color.rgb(242, 224, 226)
      )
    );

    card.setElevation(dp(4));
    margin(card, 12);
    body.addView(card);

    return card;
  }

  private TextView action(
    String label,
    View.OnClickListener listener,
    boolean primary
  ) {
    TextView button = text(
      label,
      15,
      primary ? Color.WHITE : RED
    );

    button.setTypeface(Typeface.DEFAULT_BOLD);
    button.setGravity(Gravity.CENTER);

    button.setPadding(
      dp(12),
      dp(13),
      dp(12),
      dp(13)
    );

    button.setBackground(
      shape(
        primary ?
          RED :
          Color.rgb(255, 239, 242),
        18
      )
    );

    button.setClickable(true);
    button.setOnClickListener(listener);

    margin(button, 10);
    body.addView(button);

    return button;
  }

  private TextView smallButton(
    String label,
    View.OnClickListener listener
  ) {
    TextView button = text(label, 14, RED);

    button.setTypeface(Typeface.DEFAULT_BOLD);
    button.setGravity(Gravity.CENTER);

    button.setPadding(
      dp(9),
      dp(12),
      dp(9),
      dp(12)
    );

    button.setBackground(
      shape(
        Color.rgb(255, 235, 238),
        14,
        Color.rgb(245, 170, 185)
      )
    );

    button.setOnClickListener(listener);

    LinearLayout.LayoutParams params =
      new LinearLayout.LayoutParams(-1, -2);
    params.setMargins(0, dp(10), 0, dp(4));
    button.setLayoutParams(params);

    return button;
  }

  private String iconTypeFor(String title) {
    if ("Calendrier".equals(title)) {
      return "calendar";
    }

    if ("Rapports".equals(title)) {
      return "report";
    }

    if ("Historique".equals(title)) {
      return "history";
    }

    if ("Paramètres".equals(title)) {
      return "settings";
    }

    return "home";
  }

  private final class ModernIconView extends View {
    private final String type;
    private final int iconColor;

    ModernIconView(String type, int color) {
      super(MainActivity.this);
      this.type = type;
      this.iconColor = color;
      setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);

      float scale = Math.min(
        getWidth(),
        getHeight()
      ) / 24f;

      float left =
        (getWidth() - 24f * scale) / 2f;
      float top =
        (getHeight() - 24f * scale) / 2f;

      canvas.save();
      canvas.translate(left, top);
      canvas.scale(scale, scale);

      Paint paint = new Paint(
        Paint.ANTI_ALIAS_FLAG
      );

      paint.setColor(iconColor);
      paint.setStyle(Paint.Style.STROKE);
      paint.setStrokeWidth(1.8f);
      paint.setStrokeCap(Paint.Cap.ROUND);
      paint.setStrokeJoin(Paint.Join.ROUND);

      if ("calendar".equals(type)) {
        canvas.drawRoundRect(
          new RectF(3, 5, 21, 21),
          2.4f,
          2.4f,
          paint
        );
        canvas.drawLine(3, 9, 21, 9, paint);
        canvas.drawLine(8, 3, 8, 7, paint);
        canvas.drawLine(16, 3, 16, 7, paint);

        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(8, 13, 1.15f, paint);
        canvas.drawCircle(12, 13, 1.15f, paint);
        canvas.drawCircle(16, 13, 1.15f, paint);
        canvas.drawCircle(8, 17, 1.15f, paint);
        canvas.drawCircle(12, 17, 1.15f, paint);
        canvas.drawCircle(16, 17, 1.15f, paint);
      } else if ("report".equals(type)) {
        canvas.drawRoundRect(
          new RectF(4, 4, 20, 20),
          4,
          4,
          paint
        );
        paint.setStrokeWidth(2.5f);
        canvas.drawLine(8, 8, 16, 16, paint);
        canvas.drawLine(16, 8, 8, 16, paint);
      } else if ("history".equals(type)) {
        canvas.drawCircle(12, 12, 8, paint);
        canvas.drawLine(12, 7, 12, 12, paint);
        canvas.drawLine(12, 12, 16, 14, paint);

        Path arrow = new Path();
        arrow.moveTo(4, 8);
        arrow.lineTo(4, 4);
        arrow.lineTo(8, 4);
        canvas.drawPath(arrow, paint);
      } else if ("ovulation".equals(type)) {
        canvas.drawCircle(12, 12, 7.5f, paint);
        canvas.drawCircle(12, 12, 2.4f, paint);
        canvas.drawLine(12, 2.5f, 12, 5, paint);
        canvas.drawLine(12, 19, 12, 21.5f, paint);
        canvas.drawLine(2.5f, 12, 5, 12, paint);
        canvas.drawLine(19, 12, 21.5f, 12, paint);
      } else if ("settings".equals(type)) {
        canvas.drawCircle(12, 12, 4, paint);
        canvas.drawCircle(12, 12, 8, paint);

        for (int i = 0; i < 8; i++) {
          double angle = Math.PI * i / 4.0;
          float x1 = 12f +
            (float) Math.cos(angle) * 8f;
          float y1 = 12f +
            (float) Math.sin(angle) * 8f;
          float x2 = 12f +
            (float) Math.cos(angle) * 10f;
          float y2 = 12f +
            (float) Math.sin(angle) * 10f;
          canvas.drawLine(x1, y1, x2, y2, paint);
        }
      } else {
        Path home = new Path();
        home.moveTo(3.5f, 11);
        home.lineTo(12, 3.5f);
        home.lineTo(20.5f, 11);
        home.lineTo(20.5f, 20.5f);
        home.lineTo(14.5f, 20.5f);
        home.lineTo(14.5f, 14.5f);
        home.lineTo(9.5f, 14.5f);
        home.lineTo(9.5f, 20.5f);
        home.lineTo(3.5f, 20.5f);
        home.close();
        canvas.drawPath(home, paint);
      }

      canvas.restore();
    }
  }

  private final class CurveDividerView extends View {
    CurveDividerView() {
      super(MainActivity.this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);

      Paint paint = new Paint(
        Paint.ANTI_ALIAS_FLAG
      );
      paint.setColor(SURFACE);
      paint.setStyle(Paint.Style.FILL);

      float width = getWidth();
      float height = getHeight();

      Path curve = new Path();
      curve.moveTo(0, height * 0.30f);
      curve.cubicTo(
        width * 0.24f,
        height * 0.12f,
        width * 0.46f,
        height * 0.94f,
        width * 0.66f,
        height * 0.68f
      );
      curve.cubicTo(
        width * 0.82f,
        height * 0.48f,
        width * 0.90f,
        height * 0.18f,
        width,
        height * 0.24f
      );
      curve.lineTo(width, height);
      curve.lineTo(0, height);
      curve.close();
      canvas.drawPath(curve, paint);
    }
  }

  private void render() {
    if (
      pinManager != null &&
      pinManager.hasPin() &&
      !unlocked
    ) {
      showLockScreen();
      return;
    }

    LinearLayout root =
      new LinearLayout(this);

    root.setOrientation(LinearLayout.VERTICAL);
    root.setBackgroundColor(SURFACE);

    ScrollView scroll =
      new ScrollView(this);

    scroll.setFillViewport(true);

    body = new LinearLayout(this);
    body.setOrientation(LinearLayout.VERTICAL);

    body.setPadding(
      dp(18),
      dp(18),
      dp(18),
      dp(12)
    );

    scroll.addView(body);

    root.addView(
      scroll,
      new LinearLayout.LayoutParams(
        -1,
        0,
        1
      )
    );

    root.addView(bottomNavigation());

    setContentView(root);

    if ("Accueil".equals(tab)) {
      home();
    } else if ("Calendrier".equals(tab)) {
      calendar();
    } else if ("Rapports".equals(tab)) {
      reportsTab();
    } else if ("Historique".equals(tab)) {
      history();
    } else {
      settings();
    }

    ReminderScheduler.schedule(this);
  }

  private View bottomNavigation() {
    LinearLayout navigation =
      new LinearLayout(this);

    navigation.setGravity(Gravity.CENTER);

    navigation.setPadding(
      dp(5),
      dp(6),
      dp(5),
      dp(7)
    );

    navigation.setBackgroundColor(Color.WHITE);

    String[] labels = {
      "Accueil",
      "Calendrier",
      "Rapports",
      "Historique",
      "Paramètres"
    };

    final String[] identifiers = {
      "Accueil",
      "Calendrier",
      "Rapports",
      "Historique",
      "Paramètres"
    };

    for (int i = 0; i < labels.length; i++) {
      final int index = i;

      boolean selected =
        identifiers[i].equals(tab);

      LinearLayout item =
        new LinearLayout(this);

      item.setOrientation(
        LinearLayout.VERTICAL
      );

      item.setGravity(Gravity.CENTER);

      item.setPadding(
        dp(2),
        dp(5),
        dp(2),
        dp(5)
      );

      if (selected) {
        item.setBackground(
          shape(
            Color.rgb(255, 232, 237),
            16
          )
        );
      }

      ModernIconView icon =
        new ModernIconView(
          iconTypeFor(identifiers[i]),
          selected ? RED : MUTED
        );

      if (selected) {
        icon.setBackground(
          shape(
            Color.rgb(255, 219, 227),
            13
          )
        );
      }

      item.addView(
        icon,
        new LinearLayout.LayoutParams(
          dp(34),
          dp(34)
        )
      );

      TextView label = text(
        labels[i],
        9,
        selected ? RED : MUTED
      );

      label.setGravity(Gravity.CENTER);
      label.setTypeface(Typeface.DEFAULT_BOLD);

      item.addView(label);

      item.setOnClickListener(
        new View.OnClickListener() {
          public void onClick(View view) {
            tab = identifiers[index];
            render();
          }
        }
      );

      LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(
          0,
          -2,
          1
        );

      params.setMargins(
        dp(2),
        0,
        dp(2),
        0
      );

      navigation.addView(item, params);
    }

    return navigation;
  }

  private void heading(
    String title,
    String subtitle
  ) {
    LinearLayout hero =
      new LinearLayout(this);

    hero.setOrientation(
      LinearLayout.HORIZONTAL
    );

    hero.setGravity(
      Gravity.CENTER_VERTICAL
    );

    hero.setPadding(
      dp(17),
      dp(18),
      dp(17),
      dp(18)
    );

    hero.setBackground(
      gradient(RED, RED_DARK, 24)
    );

    ModernIconView icon =
      new ModernIconView(
        iconTypeFor(title),
        Color.WHITE
      );

    icon.setBackground(
      shape(
        Color.argb(52, 255, 255, 255),
        16
      )
    );

    hero.addView(
      icon,
      new LinearLayout.LayoutParams(
        dp(49),
        dp(49)
      )
    );

    LinearLayout words =
      new LinearLayout(this);

    words.setOrientation(
      LinearLayout.VERTICAL
    );

    TextView heading = text(
      title,
      26,
      Color.WHITE
    );

    heading.setTypeface(
      Typeface.DEFAULT_BOLD
    );

    words.addView(heading);

    if (subtitle.length() > 0) {
      TextView description = text(
        subtitle,
        13,
        Color.rgb(255, 226, 232)
      );

      description.setPadding(
        0,
        dp(2),
        0,
        0
      );

      words.addView(description);
    }

    LinearLayout.LayoutParams params =
      new LinearLayout.LayoutParams(
        0,
        -2,
        1
      );

    params.setMargins(
      dp(13),
      0,
      0,
      0
    );

    hero.addView(words, params);
    body.addView(hero);
  }

  private String today() {
    return key.format(new Date());
  }

  private Date parse(String value) {
    try {
      return key.parse(value);
    } catch (Exception ignored) {
      return new Date();
    }
  }

  private String show(String value) {
    return shortDate.format(parse(value));
  }

  private Calendar calendarOf(String value) {
    Calendar calendar =
      Calendar.getInstance();

    calendar.setTime(parse(value));
    resetTime(calendar);

    return calendar;
  }

  private String plusDays(
    String value,
    int amount
  ) {
    Calendar calendar =
      calendarOf(value);

    calendar.add(
      Calendar.DAY_OF_YEAR,
      amount
    );

    return key.format(calendar.getTime());
  }

  private int days(
    String first,
    String second
  ) {
    return (int) (
      (
        calendarOf(second).getTimeInMillis() -
        calendarOf(first).getTimeInMillis()
      ) / 86400000L
    );
  }

  private ArrayList<PeriodStore.Period> periods() {
    return db.all();
  }

  private ArrayList<PeriodStore.Period> ascending() {
    ArrayList<PeriodStore.Period> values =
      periods();

    Collections.sort(
      values,
      new Comparator<PeriodStore.Period>() {
        public int compare(
          PeriodStore.Period first,
          PeriodStore.Period second
        ) {
          return first.start.compareTo(
            second.start
          );
        }
      }
    );

    return values;
  }

  private String latestStart() {
    ArrayList<PeriodStore.Period> values =
      periods();

    return values.size() == 0 ?
      "" :
      values.get(0).start;
  }

  private int cycleLength() {
    ArrayList<PeriodStore.Period> values =
      ascending();

    int total = 0;
    int count = 0;

    for (
      int i = Math.max(1, values.size() - 6);
      i < values.size();
      i++
    ) {
      int difference = days(
        values.get(i - 1).start,
        values.get(i).start
      );

      if (
        difference >= 15 &&
        difference <= 60
      ) {
        total += difference;
        count++;
      }
    }

    if (count > 0) {
      return Math.round(
        (float) total / count
      );
    }

    try {
      return Integer.parseInt(
        db.get("cycle", "28")
      );
    } catch (Exception ignored) {
      return 28;
    }
  }

  private int periodLength() {
    int total = 0;
    int count = 0;

    for (PeriodStore.Period period : periods()) {
      if (
        PeriodStore.COMPLETE.equals(
          period.state
        )
      ) {
        int duration =
          days(period.start, period.end) + 1;

        if (
          duration >= 1 &&
          duration <= 14
        ) {
          total += duration;
          count++;

          if (count == 6) {
            break;
          }
        }
      }
    }

    return count == 0 ?
      5 :
      Math.round((float) total / count);
  }

  private ArrayList<String> estimateStarts() {
    ArrayList<String> result =
      new ArrayList<String>();

    String latest = latestStart();

    if (latest.length() == 0) {
      return result;
    }

    int cycle = cycleLength();

    for (int i = 1; i <= 8; i++) {
      result.add(
        plusDays(latest, cycle * i)
      );
    }

    return result;
  }

  private boolean between(
    String value,
    String start,
    String end
  ) {
    return value.compareTo(start) >= 0 &&
      value.compareTo(end) <= 0;
  }

  private boolean realPeriodDay(String value) {
    for (PeriodStore.Period period : periods()) {
      if (
        PeriodStore.COMPLETE.equals(period.state) &&
        between(
          value,
          period.start,
          period.end
        )
      ) {
        return true;
      }

      if (
        PeriodStore.ONGOING.equals(period.state) &&
        between(
          value,
          period.start,
          today()
        )
      ) {
        return true;
      }

      if (
        PeriodStore.UNKNOWN_END.equals(period.state) &&
        value.equals(period.start)
      ) {
        return true;
      }
    }

    return false;
  }

  private boolean predictedPeriodDay(
    String value
  ) {
    for (String start : estimateStarts()) {
      if (
        between(
          value,
          start,
          plusDays(
            start,
            periodLength() - 1
          )
        )
      ) {
        return true;
      }
    }

    return false;
  }

  private String closestOvulation(
    String value
  ) {
    String closest = "";
    int best = 10000;

    for (String start : estimateStarts()) {
      String ovulation =
        plusDays(start, -14);

      int distance = Math.abs(
        days(ovulation, value)
      );

      if (distance < best) {
        best = distance;
        closest = ovulation;
      }
    }

    return best <= cycleLength() ?
      closest :
      "";
  }

  private boolean isOvulationDay(
    String value
  ) {
    String ovulation =
      closestOvulation(value);

    return ovulation.length() > 0 &&
      value.equals(ovulation);
  }

  private int pregnancyRisk(String value) {
    String ovulation =
      closestOvulation(value);

    if (ovulation.length() == 0) {
      return 0;
    }

    int offset = days(
      ovulation,
      value
    );

    if (
      offset >= -1 &&
      offset <= 1
    ) {
      return 4;
    }

    if (
      offset >= -3 &&
      offset <= 2
    ) {
      return 3;
    }

    if (
      offset >= -5 &&
      offset <= 3
    ) {
      return 2;
    }

    return 1;
  }

  private String riskLabel(int risk) {
    if (risk == 4) {
      return "TRÈS ÉLEVÉ";
    }

    if (risk == 3) {
      return "ÉLEVÉ";
    }

    if (risk == 2) {
      return "MOYEN";
    }

    if (risk == 1) {
      return "FAIBLE";
    }

    return "INDÉTERMINÉ";
  }

  private int riskColor(int risk) {
    if (risk == 4) {
      return RISK_VERY_HIGH;
    }

    if (risk == 3) {
      return RISK_HIGH;
    }

    if (risk == 2) {
      return Color.rgb(211, 159, 32);
    }

    if (risk == 1) {
      return Color.rgb(87, 151, 100);
    }

    return MUTED;
  }

  private String nextEstimatedStart() {
    ArrayList<String> estimates =
      estimateStarts();

    if (estimates.size() == 0) {
      return "";
    }

    for (String estimate : estimates) {
      if (estimate.compareTo(today()) >= 0) {
        return estimate;
      }
    }

    return estimates.get(
      estimates.size() - 1
    );
  }

  private void addProgressStat(
    LinearLayout parent,
    String label,
    String value,
    int progress,
    int maximum,
    int color
  ) {
    LinearLayout block =
      new LinearLayout(this);
    block.setOrientation(LinearLayout.VERTICAL);

    LinearLayout header =
      new LinearLayout(this);
    header.setGravity(Gravity.CENTER_VERTICAL);

    TextView name = text(label, 13, MUTED);
    TextView number = text(value, 13, INK);
    number.setTypeface(Typeface.DEFAULT_BOLD);
    number.setGravity(Gravity.RIGHT);

    header.addView(
      name,
      new LinearLayout.LayoutParams(0, -2, 1)
    );
    header.addView(number);
    block.addView(header);

    LinearLayout track =
      new LinearLayout(this);
    track.setOrientation(LinearLayout.HORIZONTAL);
    track.setBackground(
      shape(Color.rgb(244, 229, 233), 5)
    );

    int safeMaximum = Math.max(1, maximum);
    int safeProgress = Math.max(
      0,
      Math.min(progress, safeMaximum)
    );

    View filled = new View(this);
    filled.setBackground(shape(color, 5));
    track.addView(
      filled,
      new LinearLayout.LayoutParams(
        0,
        dp(8),
        safeProgress
      )
    );

    View remaining = new View(this);
    track.addView(
      remaining,
      new LinearLayout.LayoutParams(
        0,
        dp(8),
        safeMaximum - safeProgress
      )
    );

    LinearLayout.LayoutParams trackParams =
      new LinearLayout.LayoutParams(-1, dp(8));
    trackParams.setMargins(0, dp(6), 0, 0);
    block.addView(track, trackParams);

    LinearLayout.LayoutParams blockParams =
      new LinearLayout.LayoutParams(-1, -2);
    blockParams.setMargins(0, dp(12), 0, 0);
    parent.addView(block, blockParams);
  }

  private LinearLayout estimateTile(
    String iconType,
    String label,
    String value,
    int color
  ) {
    LinearLayout tile =
      new LinearLayout(this);
    tile.setOrientation(LinearLayout.VERTICAL);
    tile.setPadding(
      dp(13),
      dp(13),
      dp(13),
      dp(13)
    );
    tile.setBackground(
      shape(Color.WHITE, 20, Color.rgb(242, 220, 225))
    );
    tile.setElevation(dp(3));

    ModernIconView icon =
      new ModernIconView(iconType, color);
    icon.setBackground(
      shape(Color.rgb(255, 235, 239), 14)
    );
    tile.addView(
      icon,
      new LinearLayout.LayoutParams(dp(38), dp(38))
    );

    TextView title = text(label, 12, MUTED);
    title.setPadding(0, dp(9), 0, 0);
    tile.addView(title);

    TextView information = text(value, 16, INK);
    information.setTypeface(Typeface.DEFAULT_BOLD);
    information.setPadding(0, dp(3), 0, 0);
    tile.addView(information);

    return tile;
  }

  private void home() {
    final String next = nextEstimatedStart();

    LinearLayout hero =
      new LinearLayout(this);
    hero.setOrientation(LinearLayout.VERTICAL);
    hero.setPadding(dp(18), dp(18), dp(18), 0);
    hero.setBackground(gradient(RED, RED_DARK, 28));
    hero.setElevation(dp(6));

    LinearLayout brand =
      new LinearLayout(this);
    brand.setGravity(Gravity.CENTER_VERTICAL);

    ModernIconView homeIcon =
      new ModernIconView("home", Color.WHITE);
    homeIcon.setBackground(
      shape(Color.argb(55, 255, 255, 255), 16)
    );
    brand.addView(
      homeIcon,
      new LinearLayout.LayoutParams(dp(48), dp(48))
    );

    LinearLayout brandWords =
      new LinearLayout(this);
    brandWords.setOrientation(LinearLayout.VERTICAL);

    TextView appName = text(
      "Accueil",
      20,
      Color.WHITE
    );
    appName.setTypeface(Typeface.DEFAULT_BOLD);
    brandWords.addView(appName);

    TextView greeting = text(
      "Hantriniala Day · Coucou Piso",
      13,
      Color.rgb(255, 220, 227)
    );
    brandWords.addView(greeting);

    LinearLayout.LayoutParams brandWordsParams =
      new LinearLayout.LayoutParams(0, -2, 1);
    brandWordsParams.setMargins(dp(12), 0, 0, 0);
    brand.addView(brandWords, brandWordsParams);
    hero.addView(brand);

    TextView estimateLabel = text(
      next.length() == 0 ?
        "COMMENCER LE SUIVI" :
        "PROCHAINES RÈGLES ESTIMÉES",
      12,
      Color.rgb(255, 218, 226)
    );
    estimateLabel.setTypeface(Typeface.DEFAULT_BOLD);
    estimateLabel.setPadding(0, dp(24), 0, 0);
    hero.addView(estimateLabel);

    TextView mainDate = text(
      next.length() == 0 ?
        "Ajoutez une période" :
        show(next),
      29,
      Color.WHITE
    );
    mainDate.setTypeface(Typeface.DEFAULT_BOLD);
    mainDate.setPadding(0, dp(4), 0, 0);
    hero.addView(mainDate);

    String heroDetail;

    if (next.length() == 0) {
      heroDetail =
        "Enregistrez le premier jour réel des règles.";
    } else {
      int remaining = days(today(), next);
      heroDetail = remaining > 0 ?
        "Dans environ " + remaining + " jours" :
        "Aujourd’hui ou en retard";
    }

    TextView detail = text(
      heroDetail,
      15,
      Color.WHITE
    );
    detail.setPadding(0, dp(4), 0, dp(4));
    hero.addView(detail);

    LinearLayout quickEstimates =
      new LinearLayout(this);
    quickEstimates.setOrientation(
      LinearLayout.HORIZONTAL
    );
    quickEstimates.setPadding(0, dp(12), 0, 0);

    TextView cycleQuick = text(
      "CYCLE MOYEN\n" + cycleLength() + " jours",
      12,
      Color.WHITE
    );
    cycleQuick.setTypeface(Typeface.DEFAULT_BOLD);
    cycleQuick.setPadding(
      dp(12), dp(10), dp(12), dp(10)
    );
    cycleQuick.setBackground(
      shape(Color.argb(44, 255, 255, 255), 16)
    );

    String quickOvulation = next.length() == 0 ?
      "À calculer" :
      show(plusDays(next, -14));
    TextView ovulationQuick = text(
      "OVULATION ESTIMÉE\n" + quickOvulation,
      12,
      Color.WHITE
    );
    ovulationQuick.setTypeface(Typeface.DEFAULT_BOLD);
    ovulationQuick.setPadding(
      dp(12), dp(10), dp(12), dp(10)
    );
    ovulationQuick.setBackground(
      shape(Color.argb(44, 255, 255, 255), 16)
    );

    LinearLayout.LayoutParams quickLeft =
      new LinearLayout.LayoutParams(0, -2, 1);
    quickLeft.setMargins(0, 0, dp(5), 0);
    quickEstimates.addView(cycleQuick, quickLeft);

    LinearLayout.LayoutParams quickRight =
      new LinearLayout.LayoutParams(0, -2, 1);
    quickRight.setMargins(dp(5), 0, 0, 0);
    quickEstimates.addView(
      ovulationQuick,
      quickRight
    );
    hero.addView(quickEstimates);

    CurveDividerView curve =
      new CurveDividerView();
    hero.addView(
      curve,
      new LinearLayout.LayoutParams(-1, dp(42))
    );
    body.addView(hero);

    LinearLayout statistics =
      new LinearLayout(this);
    statistics.setOrientation(LinearLayout.VERTICAL);
    statistics.setPadding(
      dp(17),
      dp(15),
      dp(17),
      dp(17)
    );
    statistics.setBackground(
      shape(Color.WHITE, 22, Color.rgb(242, 224, 228))
    );
    statistics.setElevation(dp(5));

    TextView statisticsTitle = text(
      "Petit aperçu",
      18,
      INK
    );
    statisticsTitle.setTypeface(Typeface.DEFAULT_BOLD);
    statistics.addView(statisticsTitle);

    addProgressStat(
      statistics,
      "Cycle moyen",
      cycleLength() + " jours",
      cycleLength(),
      40,
      RED
    );
    addProgressStat(
      statistics,
      "Durée des règles",
      periodLength() + " jours",
      periodLength(),
      10,
      RED_LIGHT
    );
    addProgressStat(
      statistics,
      "Historique utilisé",
      Math.min(periods().size(), 6) + " / 6 cycles",
      Math.min(periods().size(), 6),
      6,
      RISK_VERY_HIGH
    );

    LinearLayout.LayoutParams statisticsParams =
      new LinearLayout.LayoutParams(-1, -2);
    statisticsParams.setMargins(0, -dp(15), 0, 0);
    body.addView(statistics, statisticsParams);

    LinearLayout estimates =
      new LinearLayout(this);
    estimates.setOrientation(LinearLayout.HORIZONTAL);
    estimates.setPadding(0, dp(14), 0, 0);

    String periodValue = next.length() == 0 ?
      "À calculer" :
      show(next);
    String ovulationValue = next.length() == 0 ?
      "À calculer" :
      show(plusDays(next, -14));

    LinearLayout periodTile = estimateTile(
      "calendar",
      "Prochain cycle",
      periodValue,
      RED
    );
    LinearLayout ovulationTile = estimateTile(
      "ovulation",
      "Ovulation estimée",
      ovulationValue,
      OVULATION_GREEN
    );

    LinearLayout.LayoutParams leftTile =
      new LinearLayout.LayoutParams(0, -2, 1);
    leftTile.setMargins(0, 0, dp(6), 0);
    estimates.addView(periodTile, leftTile);

    LinearLayout.LayoutParams rightTile =
      new LinearLayout.LayoutParams(0, -2, 1);
    rightTile.setMargins(dp(6), 0, 0, 0);
    estimates.addView(ovulationTile, rightTile);
    body.addView(estimates);

    action(
      "Mes règles commencent aujourd’hui",
      new View.OnClickListener() {
        public void onClick(View view) {
          beginOngoing(today());
        }
      },
      true
    );

    action(
      "Ajouter une période",
      new View.OnClickListener() {
        public void onClick(View view) {
          chooseStartDate();
        }
      },
      false
    );
  }

  private void calendar() {
    heading(
      "Calendrier",
      "Touchez une date pour afficher son détail."
    );

    LinearLayout controls =
      new LinearLayout(this);

    controls.setGravity(
      Gravity.CENTER_VERTICAL
    );

    TextView previous = smallButton(
      "",
      new View.OnClickListener() {
        public void onClick(View view) {
          shownMonth.add(
            Calendar.MONTH,
            -1
          );

          render();
        }
      }
    );

    controls.addView(
      previous,
      new LinearLayout.LayoutParams(
        dp(45),
        dp(42)
      )
    );

    TextView month = text(
      monthName.format(
        shownMonth.getTime()
      ),
      20,
      INK
    );

    month.setGravity(Gravity.CENTER);
    month.setTypeface(Typeface.DEFAULT_BOLD);

    controls.addView(
      month,
      new LinearLayout.LayoutParams(
        0,
        dp(42),
        1
      )
    );

    TextView next = smallButton(
      ">",
      new View.OnClickListener() {
        public void onClick(View view) {
          shownMonth.add(
            Calendar.MONTH,
            1
          );

          render();
        }
      }
    );

    controls.addView(
      next,
      new LinearLayout.LayoutParams(
        dp(45),
        dp(42)
      )
    );

    margin(controls, 14);
    body.addView(controls);

    GridLayout grid =
      new GridLayout(this);

    grid.setColumnCount(7);
    grid.setUseDefaultMargins(false);

    margin(grid, 12);
    body.addView(grid);

    String[] weekdays = {
      "L", "M", "M", "J", "V", "S", "D"
    };

    for (String weekday : weekdays) {
      addCell(
        grid,
        weekday,
        Color.TRANSPARENT,
        INK,
        false,
        null
      );
    }

    Calendar first =
      (Calendar) shownMonth.clone();

    first.set(Calendar.DAY_OF_MONTH, 1);

    int leading =
      (first.get(Calendar.DAY_OF_WEEK) + 5) % 7;

    for (int i = 0; i < leading; i++) {
      addCell(
        grid,
        "",
        Color.TRANSPARENT,
        MUTED,
        false,
        null
      );
    }

    int maximum =
      first.getActualMaximum(
        Calendar.DAY_OF_MONTH
      );

    for (int day = 1; day <= maximum; day++) {
      Calendar calendar =
        (Calendar) first.clone();

      calendar.set(
        Calendar.DAY_OF_MONTH,
        day
      );

      final String value =
        key.format(calendar.getTime());

      int fill = Color.WHITE;
      int ink = INK;
      String marker = "";

      int risk = pregnancyRisk(value);

      if (realPeriodDay(value)) {
        fill = RED;
        ink = Color.WHITE;
        marker = "●";
      }  else if (predictedPeriodDay(value)) {
       fill = RED_LIGHT;
       ink = Color.WHITE;
       marker = "●";
      } else if (isOvulationDay(value)) {
        fill = OVULATION_GREEN;
        ink = Color.WHITE;
        marker = "●";
      } else if (risk == 4) {
        fill = RISK_VERY_HIGH;
        ink = Color.WHITE;
        marker = "◆";
      } else if (risk == 3) {
        fill = RISK_HIGH;
        marker = "◆";
      } else if (risk == 2) {
        fill = RISK_MEDIUM;
        marker = "·";
      }

      TextView dayCell = addCell(
        grid,
        day + "\n" + marker,
        fill,
        ink,
        true,
        new View.OnClickListener() {
          public void onClick(View view) {
            dayDetails(value);
          }
        }
      );

      if (value.equals(today())) {
        dayCell.setBackground(
          shape(
            fill,
            12,
            TODAY_BLUE,
            3
          )
        );
      }
    }

    LinearLayout legend = card();

    TextView legendTitle = text(
      "Légende",
      17,
      INK
    );

    legendTitle.setTypeface(
      Typeface.DEFAULT_BOLD
    );

    legend.addView(legendTitle);

    legendRow(
      legend,
      RED,
      "Règles enregistrées"
    );

    legendRow(
      legend,
      RED_LIGHT,
      "Règles prévues"
    );

    legendRow(
      legend,
      OVULATION_GREEN,
      "Jour d’ovulation estimé"
    );

    legendRow(
      legend,
      RISK_VERY_HIGH,
      "Risque estimé très élevé"
    );

    legendRow(
      legend,
      RISK_HIGH,
      "Risque estimé élevé"
    );

    legendRow(
      legend,
      RISK_MEDIUM,
      "Risque estimé moyen"
    );

    legendRow(
      legend,
      Color.WHITE,
      "Risque estimé faible"
    );

    action(
      "Ajouter une période",
      new View.OnClickListener() {
        public void onClick(View view) {
          chooseStartDate();
        }
      },
      true
    );
  }

  private TextView addCell(
    GridLayout grid,
    String value,
    int background,
    int color,
    boolean clickable,
    View.OnClickListener listener
  ) {
    TextView cell = text(
      value,
      14,
      color
    );

    cell.setGravity(Gravity.CENTER);

    cell.setPadding(
      0,
      dp(4),
      0,
      dp(2)
    );

    cell.setBackground(
      shape(
        background,
        12,
        Color.rgb(246, 232, 233)
      )
    );

    if (clickable) {
      cell.setClickable(true);
      cell.setOnClickListener(listener);
    }

    GridLayout.LayoutParams params =
      new GridLayout.LayoutParams();

    params.width = 0;
    params.height = dp(53);

    params.columnSpec =
      GridLayout.spec(
        GridLayout.UNDEFINED,
        1f
      );

    params.setMargins(
      dp(2),
      dp(2),
      dp(2),
      dp(2)
    );

    grid.addView(cell, params);

    return cell;
  }

  private void legendRow(
    LinearLayout parent,
    int color,
    String label
  ) {
    LinearLayout row =
      new LinearLayout(this);

    row.setOrientation(
      LinearLayout.HORIZONTAL
    );

    row.setGravity(
      Gravity.CENTER_VERTICAL
    );

    row.setPadding(
      0,
      dp(5),
      0,
      dp(5)
    );

    TextView colorBox =
      new TextView(this);

    colorBox.setBackground(
      shape(
        color,
        6,
        Color.rgb(225, 209, 213)
      )
    );

    row.addView(
      colorBox,
      new LinearLayout.LayoutParams(
        dp(22),
        dp(22)
      )
    );

    TextView labelView = text(
      label,
      14,
      MUTED
    );

    LinearLayout.LayoutParams params =
      new LinearLayout.LayoutParams(
        0,
        -2,
        1
      );

    params.setMargins(
      dp(11),
      0,
      0,
      0
    );

    row.addView(labelView, params);
    parent.addView(row);
  }

  private void dayDetails(
    final String value
  ) {
    String status;

    if (realPeriodDay(value)) {
      status = "Règles enregistrées";
    } else if (predictedPeriodDay(value)) {
      status = "Règles prévues";
    } else {
      status = "Aucune règle enregistrée";
    }

    final PeriodStore.Period existing =
      periodStarting(value);

    int risk = pregnancyRisk(value);
    String ovulation =
      closestOvulation(value);

    LinearLayout panel =
      new LinearLayout(this);

    panel.setOrientation(
      LinearLayout.VERTICAL
    );

    panel.setBackground(
      shape(Color.WHITE, 24)
    );

    LinearLayout header =
      new LinearLayout(this);

    header.setOrientation(
      LinearLayout.VERTICAL
    );

    header.setPadding(
      dp(20),
      dp(18),
      dp(20),
      dp(18)
    );

    header.setBackground(
      gradient(RED, RED_DARK, 22)
    );

    TextView detailLabel = text(
      "▣ DÉTAIL DU JOUR",
      13,
      Color.rgb(255, 225, 232)
    );

    detailLabel.setTypeface(
      Typeface.DEFAULT_BOLD
    );

    header.addView(detailLabel);

    TextView date = text(
      full.format(parse(value)),
      21,
      Color.WHITE
    );

    date.setTypeface(Typeface.DEFAULT_BOLD);
    date.setPadding(0, dp(4), 0, 0);

    header.addView(date);
    panel.addView(header);

    LinearLayout content =
      new LinearLayout(this);

    content.setOrientation(
      LinearLayout.VERTICAL
    );

    content.setPadding(
      dp(20),
      dp(18),
      dp(20),
      dp(16)
    );

    TextView statusText =
      text(status, 15, INK);

    statusText.setTypeface(
      Typeface.DEFAULT_BOLD
    );

    content.addView(statusText);

    TextView riskTitle = text(
      "RISQUE DE GROSSESSE ESTIMÉ",
      12,
      MUTED
    );

    riskTitle.setTypeface(
      Typeface.DEFAULT_BOLD
    );

    riskTitle.setPadding(
      0,
      dp(17),
      0,
      dp(7)
    );

    content.addView(riskTitle);

    TextView badge = text(
      riskLabel(risk),
      21,
      Color.WHITE
    );

    badge.setTypeface(
      Typeface.DEFAULT_BOLD
    );

    badge.setGravity(Gravity.CENTER);

    badge.setPadding(
      dp(12),
      dp(11),
      dp(12),
      dp(11)
    );

    badge.setBackground(
      shape(riskColor(risk), 16)
    );

    content.addView(badge);

    if (isOvulationDay(value)) {
      TextView ovulationDay = text(
        "Jour d’ovulation estimé",
        15,
        OVULATION_GREEN
      );

      ovulationDay.setTypeface(
        Typeface.DEFAULT_BOLD
      );

      ovulationDay.setPadding(
        0,
        dp(11),
        0,
        0
      );

      content.addView(ovulationDay);
    } else if (ovulation.length() > 0) {
      TextView ovulationText = text(
        "Ovulation estimée autour du " +
        show(ovulation),
        14,
        MUTED
      );

      ovulationText.setPadding(
        0,
        dp(11),
        0,
        0
      );

      content.addView(ovulationText);
    }

    panel.addView(content);

    new AlertDialog.Builder(this)
      .setView(panel)
      .setNegativeButton("Fermer", null)
      .setPositiveButton(
        existing == null ?
          "Règles ce jour" :
          "Modifier",
        new DialogInterface.OnClickListener() {
          public void onClick(
            DialogInterface dialog,
            int which
          ) {
            if (existing == null) {
              beginOngoing(value);
            } else {
              editPeriod(existing);
            }
          }
        }
      )
      .show();
  }

  private PeriodStore.Period periodStarting(
    String value
  ) {
    for (PeriodStore.Period period : periods()) {
      if (period.start.equals(value)) {
        return period;
      }
    }

    return null;
  }

  private void reportsTab() {
    heading(
      "Rapports",
      "Enregistrez une date et une heure précises."
    );

    action(
      "X Enregistrer maintenant",
      new View.OnClickListener() {
        public void onClick(View view) {
          reportStore.add(
            System.currentTimeMillis()
          );

          toast("Rapport enregistré.");
          render();
        }
      },
      true
    );

    action(
      "Choisir la date et l’heure",
      new View.OnClickListener() {
        public void onClick(View view) {
          chooseReportDateTime();
        }
      },
      false
    );

    ArrayList<ReportStore.Entry> entries =
      reportStore.all();

    if (entries.size() == 0) {
      LinearLayout empty = card();

      TextView emptyText = text(
        "Aucun rapport enregistré",
        18,
        INK
      );

      emptyText.setTypeface(
        Typeface.DEFAULT_BOLD
      );

      empty.addView(emptyText);
    }

    final SimpleDateFormat reportFormat =
      new SimpleDateFormat(
        "EEEE d MMMM yyyy 'à' HH:mm",
        Locale.FRENCH
      );

    for (
      final ReportStore.Entry entry :
      entries
    ) {
      LinearLayout reportCard = card();

      TextView icon = text(
        "X",
        30,
        RED
      );

      icon.setTypeface(
        Typeface.DEFAULT_BOLD
      );

      icon.setGravity(Gravity.CENTER);

      icon.setBackground(
        shape(
          Color.rgb(255, 232, 237),
          16
        )
      );

      reportCard.addView(
        icon,
        new LinearLayout.LayoutParams(
          -1,
          dp(48)
        )
      );

      TextView date = text(
        reportFormat.format(
          new Date(entry.timestamp)
        ),
        16,
        INK
      );

      date.setGravity(Gravity.CENTER);

      date.setTypeface(
        Typeface.DEFAULT_BOLD
      );

      date.setPadding(
        0,
        dp(8),
        0,
        0
      );

      reportCard.addView(date);

      TextView deleteText = text(
        "Toucher pour supprimer",
        12,
        MUTED
      );

      deleteText.setGravity(Gravity.CENTER);

      deleteText.setPadding(
        0,
        dp(5),
        0,
        0
      );

      reportCard.addView(deleteText);

      reportCard.setOnClickListener(
        new View.OnClickListener() {
          public void onClick(View view) {
            confirmRemoveReport(entry);
          }
        }
      );
    }
  }

  private void chooseReportDateTime() {
    final Calendar selected =
      Calendar.getInstance();

    new DatePickerDialog(
      this,
      new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(
          DatePicker picker,
          int year,
          int month,
          int day
        ) {
          selected.set(
            Calendar.YEAR,
            year
          );

          selected.set(
            Calendar.MONTH,
            month
          );

          selected.set(
            Calendar.DAY_OF_MONTH,
            day
          );

          new TimePickerDialog(
            MainActivity.this,
            new TimePickerDialog.OnTimeSetListener() {
              public void onTimeSet(
                TimePicker picker,
                int hour,
                int minute
              ) {
                selected.set(
                  Calendar.HOUR_OF_DAY,
                  hour
                );

                selected.set(
                  Calendar.MINUTE,
                  minute
                );

                selected.set(
                  Calendar.SECOND,
                  0
                );

                selected.set(
                  Calendar.MILLISECOND,
                  0
                );

                reportStore.add(
                  selected.getTimeInMillis()
                );

                toast("Rapport enregistré.");
                render();
              }
            },
            selected.get(
              Calendar.HOUR_OF_DAY
            ),
            selected.get(
              Calendar.MINUTE
            ),
            true
          ).show();
        }
      },
      selected.get(Calendar.YEAR),
      selected.get(Calendar.MONTH),
      selected.get(Calendar.DAY_OF_MONTH)
    ).show();
  }

  private void confirmRemoveReport(
    final ReportStore.Entry entry
  ) {
    new AlertDialog.Builder(this)
      .setTitle("Supprimer ce rapport ?")
      .setMessage(
        "La date et l’heure seront supprimées."
      )
      .setNegativeButton("Annuler", null)
      .setPositiveButton(
        "Supprimer",
        new DialogInterface.OnClickListener() {
          public void onClick(
            DialogInterface dialog,
            int which
          ) {
            reportStore.remove(entry.id);
            toast("Rapport supprimé.");
            render();
          }
        }
      )
      .show();
  }

  private void history() {
    heading(
      "Historique",
      "Vos périodes enregistrées."
    );

    ArrayList<PeriodStore.Period> values =
      periods();

    if (values.size() == 0) {
      LinearLayout empty = card();

      empty.addView(
        text(
          "Aucune période enregistrée",
          18,
          INK
        )
      );
    }

    for (
      final PeriodStore.Period period :
      values
    ) {
      LinearLayout row = card();

      TextView date = text(
        show(period.start),
        18,
        INK
      );

      date.setTypeface(
        Typeface.DEFAULT_BOLD
      );

      row.addView(date);

      String details;

      if (
        PeriodStore.COMPLETE.equals(
          period.state
        )
      ) {
        details =
          "Fin : " +
          show(period.end) +
          " · Durée : " +
          (
            days(
              period.start,
              period.end
            ) + 1
          ) +
          " jour(s)";
      } else if (
        PeriodStore.ONGOING.equals(
          period.state
        )
      ) {
        details = "Règles en cours";
      } else {
        details =
          "Dernier jour non renseigné";
      }

      TextView metadata = text(
        details +
        "\nIntensité : " +
        intensityLabel(period.intensity),
        14,
        MUTED
      );

      margin(metadata, 3);
      row.addView(metadata);

      row.setOnClickListener(
        new View.OnClickListener() {
          public void onClick(View view) {
            editPeriod(period);
          }
        }
      );
    }

    action(
      "Ajouter une période",
      new View.OnClickListener() {
        public void onClick(View view) {
          chooseStartDate();
        }
      },
      true
    );
  }

  private String intensityLabel(
    String intensity
  ) {
    if ("light".equals(intensity)) {
      return "légère";
    }

    if ("heavy".equals(intensity)) {
      return "abondante";
    }

    return "moyenne";
  }

  private void editPeriod(
    final PeriodStore.Period period
  ) {
    String[] options;

    if (
      PeriodStore.ONGOING.equals(
        period.state
      )
    ) {
      options = new String[] {
        "Modifier le premier jour",
        "Choisir le dernier jour",
        "Terminer aujourd’hui",
        "Dernier jour inconnu",
        "Modifier l’intensité",
        "Supprimer"
      };
    } else if (
      PeriodStore.UNKNOWN_END.equals(
        period.state
      )
    ) {
      options = new String[] {
        "Modifier le premier jour",
        "Ajouter le dernier jour",
        "Modifier l’intensité",
        "Supprimer"
      };
    } else {
      options = new String[] {
        "Modifier le premier jour",
        "Modifier le dernier jour",
        "Modifier l’intensité",
        "Supprimer"
      };
    }

    new AlertDialog.Builder(this)
      .setTitle(
        "Période du " +
        show(period.start)
      )
      .setItems(
        options,
        new DialogInterface.OnClickListener() {
          public void onClick(
            DialogInterface dialog,
            int position
          ) {
            if (position == 0) {
              chooseStartDateFor(period);
              return;
            }

            if (
              PeriodStore.ONGOING.equals(
                period.state
              )
            ) {
              if (position == 1) {
                chooseEndDate(period);
              } else if (position == 2) {
                complete(period, today());
              } else if (position == 3) {
                markUnknownEnd(period);
              } else if (position == 4) {
                chooseIntensity(period);
              } else {
                removePeriod(period);
              }
            } else {
              if (position == 1) {
                chooseEndDate(period);
              } else if (position == 2) {
                chooseIntensity(period);
              } else {
                removePeriod(period);
              }
            }
          }
        }
      )
      .show();
  }

  private void removePeriod(
    final PeriodStore.Period period
  ) {
    new AlertDialog.Builder(this)
      .setMessage("Supprimer cette période ?")
      .setNegativeButton("Annuler", null)
      .setPositiveButton(
        "Supprimer",
        new DialogInterface.OnClickListener() {
          public void onClick(
            DialogInterface dialog,
            int which
          ) {
            ArrayList<PeriodStore.Period> values =
              periods();

            for (
              int i = values.size() - 1;
              i >= 0;
              i--
            ) {
              if (
                values.get(i).id.equals(
                  period.id
                )
              ) {
                values.remove(i);
              }
            }

            db.save(values);
            render();
          }
        }
      )
      .show();
  }

  private void showLockScreen() {
    unlocked = false;
    backgroundAt = 0;

    LinearLayout root =
      new LinearLayout(this);

    root.setOrientation(
      LinearLayout.VERTICAL
    );

    root.setGravity(Gravity.CENTER);

    root.setPadding(
      dp(28),
      dp(28),
      dp(28),
      dp(28)
    );

    root.setBackgroundColor(SURFACE);

    TextView lockIcon = text(
      "●",
      42,
      RED
    );

    lockIcon.setGravity(Gravity.CENTER);
    root.addView(lockIcon);

    TextView title = text(
      "Hanitriniala Day",
      27,
      INK
    );

    title.setGravity(Gravity.CENTER);

    title.setTypeface(
      Typeface.DEFAULT_BOLD
    );

    title.setPadding(
      0,
      dp(12),
      0,
      dp(5)
    );

    root.addView(title);

    TextView message = text(
      "Entrez votre code PIN",
      15,
      MUTED
    );

    message.setGravity(Gravity.CENTER);
    root.addView(message);

    final EditText pinInput =
      createPinField(
        "Votre code PIN"
      );

    LinearLayout.LayoutParams inputParams =
      new LinearLayout.LayoutParams(
        -1,
        -2
      );

    inputParams.setMargins(
      0,
      dp(22),
      0,
      0
    );

    root.addView(pinInput, inputParams);

    TextView unlockButton = text(
      "DÉVERROUILLER",
      15,
      Color.WHITE
    );

    unlockButton.setTypeface(
      Typeface.DEFAULT_BOLD
    );

    unlockButton.setGravity(Gravity.CENTER);

    unlockButton.setPadding(
      dp(12),
      dp(14),
      dp(12),
      dp(14)
    );

    unlockButton.setBackground(
      gradient(RED, RED_DARK, 18)
    );

    unlockButton.setOnClickListener(
      new View.OnClickListener() {
        public void onClick(View view) {
          String value = pinInput
            .getText()
            .toString()
            .trim();

          if (pinManager.matches(value)) {
            unlocked = true;
            backgroundAt = 0;
            render();
          } else {
            pinInput.setError(
              "Code PIN incorrect."
            );
          }
        }
      }
    );

    LinearLayout.LayoutParams buttonParams =
      new LinearLayout.LayoutParams(
        -1,
        -2
      );

    buttonParams.setMargins(
      0,
      dp(13),
      0,
      0
    );

    root.addView(
      unlockButton,
      buttonParams
    );

    setContentView(root);
  }

  private EditText createPinField(
    String hint
  ) {
    EditText input =
      new EditText(this);

    input.setHint(hint);
    input.setGravity(Gravity.CENTER);
    input.setSingleLine(true);

    input.setInputType(
      InputType.TYPE_CLASS_NUMBER |
      InputType.TYPE_NUMBER_VARIATION_PASSWORD
    );

    return input;
  }

  private void settings() {
    heading("Paramètres", "");

    LinearLayout prediction = card();

    prediction.addView(
      text("Prévisions", 18, INK)
    );

    final EditText cycleInput =
      new EditText(this);

    cycleInput.setInputType(
      InputType.TYPE_CLASS_NUMBER
    );

    cycleInput.setText(
      "" + cycleLength()
    );

    cycleInput.setHint(
      "Durée habituelle du cycle"
    );

    prediction.addView(cycleInput);

    TextView saveCycle = smallButton(
      "Enregistrer la durée",
      new View.OnClickListener() {
        public void onClick(View view) {
          try {
            int value = Integer.parseInt(
              cycleInput
                .getText()
                .toString()
                .trim()
            );

            if (
              value < 15 ||
              value > 60
            ) {
              throw new Exception();
            }

            db.put(
              "cycle",
              "" + value
            );

            ReminderScheduler.schedule(
              MainActivity.this
            );

            toast("Durée enregistrée.");
            render();
          } catch (Exception error) {
            cycleInput.setError(
              "Indiquez une durée entre 15 et 60 jours."
            );
          }
        }
      }
    );

    prediction.addView(saveCycle);

    LinearLayout reminders = card();

    TextView reminderTitle = text(
      "Rappel des prochaines règles",
      18,
      INK
    );

    reminderTitle.setTypeface(
      Typeface.DEFAULT_BOLD
    );

    reminders.addView(reminderTitle);

    final CheckBox enabled =
      new CheckBox(this);

    enabled.setText(
      "Activer les notifications"
    );

    enabled.setTextColor(INK);

    enabled.setChecked(
      "true".equals(
        db.get(
          "reminder_enabled",
          "false"
        )
      )
    );

    reminders.addView(enabled);

    final EditText daysInput =
      new EditText(this);

    daysInput.setInputType(
      InputType.TYPE_CLASS_NUMBER
    );

    daysInput.setHint(
      "Nombre de jours avant"
    );

    daysInput.setText(
      db.get("reminder_days", "2")
    );

    reminders.addView(daysInput);

    int savedHour;
    int savedMinute;

    try {
      savedHour = Integer.parseInt(
        db.get("reminder_hour", "8")
      );
    } catch (Exception ignored) {
      savedHour = 8;
    }

    try {
      savedMinute = Integer.parseInt(
        db.get("reminder_minute", "0")
      );
    } catch (Exception ignored) {
      savedMinute = 0;
    }

    final int[] selectedTime = {
      savedHour,
      savedMinute
    };

    final TextView[] timeButton =
      new TextView[1];

    timeButton[0] = smallButton(
      String.format(
        Locale.US,
        "Heure du rappel : %02d:%02d",
        selectedTime[0],
        selectedTime[1]
      ),
      new View.OnClickListener() {
        public void onClick(View view) {
          new TimePickerDialog(
            MainActivity.this,
            new TimePickerDialog.OnTimeSetListener() {
              public void onTimeSet(
                TimePicker picker,
                int hour,
                int minute
              ) {
                selectedTime[0] = hour;
                selectedTime[1] = minute;

                timeButton[0].setText(
                  String.format(
                    Locale.US,
                    "Heure du rappel : %02d:%02d",
                    hour,
                    minute
                  )
                );
              }
            },
            selectedTime[0],
            selectedTime[1],
            true
          ).show();
        }
      }
    );

    reminders.addView(timeButton[0]);

    TextView saveReminder = smallButton(
      "Enregistrer le rappel",
      new View.OnClickListener() {
        public void onClick(View view) {
          try {
            int daysBefore =
              Integer.parseInt(
                daysInput
                  .getText()
                  .toString()
                  .trim()
              );

            if (
              daysBefore < 0 ||
              daysBefore > 7
            ) {
              throw new Exception();
            }

            db.put(
              "reminder_enabled",
              enabled.isChecked() ?
                "true" :
                "false"
            );

            db.put(
              "reminder_days",
              "" + daysBefore
            );

            db.put(
              "reminder_hour",
              "" + selectedTime[0]
            );

            db.put(
              "reminder_minute",
              "" + selectedTime[1]
            );

            if (enabled.isChecked()) {
              requestNotificationPermissionIfNeeded();
            }

            ReminderScheduler.schedule(
              MainActivity.this
            );

            toast(
              "Configuration du rappel enregistrée."
            );
          } catch (Exception error) {
            daysInput.setError(
              "Indiquez une valeur entre 0 et 7."
            );
          }
        }
      }
    );

    reminders.addView(saveReminder);

    TextView testReminder = smallButton(
      "Tester la notification",
      new View.OnClickListener() {
        public void onClick(View view) {
          requestNotificationPermissionIfNeeded();

          ReminderScheduler.test(
            MainActivity.this
          );
        }
      }
    );

    reminders.addView(testReminder);

    LinearLayout security = card();

    TextView securityTitle = text(
      "Verrouillage de l’application",
      18,
      INK
    );

    securityTitle.setTypeface(
      Typeface.DEFAULT_BOLD
    );

    security.addView(securityTitle);

    TextView securityDescription = text(
      pinManager.hasPin() ?
        "Le code PIN est activé." :
        "Protégez l’ouverture avec un code PIN.",
      14,
      MUTED
    );

    securityDescription.setPadding(
      0,
      dp(5),
      0,
      dp(8)
    );

    security.addView(
      securityDescription
    );

    TextView pinButton = smallButton(
      pinManager.hasPin() ?
        "Modifier le code PIN" :
        "Créer un code PIN",
      new View.OnClickListener() {
        public void onClick(View view) {
          showPinEditor(
            pinManager.hasPin()
          );
        }
      }
    );

    security.addView(pinButton);

    if (pinManager.hasPin()) {
      TextView removePin = smallButton(
        "Supprimer le code PIN",
        new View.OnClickListener() {
          public void onClick(View view) {
            showRemovePinDialog();
          }
        }
      );

      security.addView(removePin);
    }

    LinearLayout signature = card();

    TextView by = text(
      "BY Aboalakely\npour toi\nTiffakely Mon Amour.",
      18,
      RED
    );

    by.setGravity(Gravity.CENTER);

    by.setTypeface(
      Typeface.DEFAULT_BOLD
    );

    signature.addView(by);

    action(
      "Supprimer toutes les données",
      new View.OnClickListener() {
        public void onClick(View view) {
          new AlertDialog.Builder(
            MainActivity.this
          )
            .setTitle(
              "Supprimer les données ?"
            )
            .setMessage(
              "Toutes les données seront supprimées."
            )
            .setNegativeButton(
              "Annuler",
              null
            )
            .setPositiveButton(
              "Supprimer",
              new DialogInterface.OnClickListener() {
                public void onClick(
                  DialogInterface dialog,
                  int which
                ) {
                  ReminderScheduler.cancel(
                    MainActivity.this
                  );

                  reportStore.clear();
                  pinManager.clear();
                  db.clear();

                  unlocked = true;
                  render();
                }
              }
            )
            .show();
        }
      },
      false
    );
  }

  private void showPinEditor(
    final boolean changing
  ) {
    LinearLayout form =
      new LinearLayout(this);

    form.setOrientation(
      LinearLayout.VERTICAL
    );

    form.setPadding(
      dp(20),
      dp(4),
      dp(20),
      0
    );

    final EditText currentPin;

    if (changing) {
      currentPin = createPinField(
        "Code PIN actuel"
      );

      form.addView(currentPin);
    } else {
      currentPin = null;
    }

    final EditText newPin =
      createPinField(
        "Nouveau PIN (4 chiffres minimum)"
      );

    final EditText confirmPin =
      createPinField(
        "Confirmer le nouveau PIN"
      );

    form.addView(newPin);
    form.addView(confirmPin);

    final AlertDialog dialog =
      new AlertDialog.Builder(this)
        .setTitle(
          changing ?
            "Modifier le code PIN" :
            "Créer un code PIN"
        )
        .setView(form)
        .setNegativeButton(
          "Annuler",
          null
        )
        .setPositiveButton(
          "Enregistrer",
          null
        )
        .create();

    dialog.setOnShowListener(
      new DialogInterface.OnShowListener() {
        public void onShow(
          DialogInterface value
        ) {
          dialog.getButton(
            AlertDialog.BUTTON_POSITIVE
          ).setOnClickListener(
            new View.OnClickListener() {
              public void onClick(View view) {
                if (
                  changing &&
                  !pinManager.matches(
                    currentPin
                      .getText()
                      .toString()
                      .trim()
                  )
                ) {
                  currentPin.setError(
                    "Code PIN actuel incorrect."
                  );

                  return;
                }

                String first = newPin
                  .getText()
                  .toString()
                  .trim();

                String second = confirmPin
                  .getText()
                  .toString()
                  .trim();

                if (!first.matches("\\d{4,}")) {
                  newPin.setError(
                    "Le PIN doit contenir au moins 4 chiffres."
                  );

                  return;
                }

                if (!first.equals(second)) {
                  confirmPin.setError(
                    "Les codes sont différents."
                  );

                  return;
                }

                pinManager.setPin(first);
                unlocked = true;

                dialog.dismiss();

                toast(
                  "Code PIN enregistré."
                );

                render();
              }
            }
          );
        }
      }
    );

    dialog.show();
  }

  private void showRemovePinDialog() {
    final EditText pinInput =
      createPinField(
        "Code PIN actuel"
      );

    final AlertDialog dialog =
      new AlertDialog.Builder(this)
        .setTitle(
          "Supprimer le code PIN"
        )
        .setView(pinInput)
        .setNegativeButton(
          "Annuler",
          null
        )
        .setPositiveButton(
          "Supprimer",
          null
        )
        .create();

    dialog.setOnShowListener(
      new DialogInterface.OnShowListener() {
        public void onShow(
          DialogInterface value
        ) {
          dialog.getButton(
            AlertDialog.BUTTON_POSITIVE
          ).setOnClickListener(
            new View.OnClickListener() {
              public void onClick(View view) {
                String pin = pinInput
                  .getText()
                  .toString()
                  .trim();

                if (!pinManager.matches(pin)) {
                  pinInput.setError(
                    "Code PIN incorrect."
                  );

                  return;
                }

                pinManager.clear();
                unlocked = true;

                dialog.dismiss();

                toast(
                  "Code PIN supprimé."
                );

                render();
              }
            }
          );
        }
      }
    );

    dialog.show();
  }

  private void chooseStartDate() {
    Calendar calendar =
      Calendar.getInstance();

    new DatePickerDialog(
      this,
      new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(
          DatePicker picker,
          int year,
          int month,
          int day
        ) {
          Calendar selected =
            Calendar.getInstance();

          selected.set(
            year,
            month,
            day
          );

          resetTime(selected);

          startOptions(
            key.format(
              selected.getTime()
            )
          );
        }
      },
      calendar.get(Calendar.YEAR),
      calendar.get(Calendar.MONTH),
      calendar.get(
        Calendar.DAY_OF_MONTH
      )
    ).show();
  }

  private void startOptions(
    final String start
  ) {
    new AlertDialog.Builder(this)
      .setTitle(
        "Début : " + show(start)
      )
      .setItems(
        new String[] {
          "Règles en cours",
          "Choisir le dernier jour",
          "Dernier jour inconnu"
        },
        new DialogInterface.OnClickListener() {
          public void onClick(
            DialogInterface dialog,
            int position
          ) {
            if (position == 0) {
              beginOngoing(start);
            } else if (position == 1) {
              chooseEndForNew(start);
            } else {
              createPeriod(
                start,
                "",
                PeriodStore.UNKNOWN_END
              );
            }
          }
        }
      )
      .show();
  }

  private void beginOngoing(
    String start
  ) {
    for (PeriodStore.Period period : periods()) {
      if (
        PeriodStore.ONGOING.equals(
          period.state
        )
      ) {
        toast(
          "Une période est déjà en cours."
        );

        return;
      }
    }

    createPeriod(
      start,
      "",
      PeriodStore.ONGOING
    );
  }

  private void chooseEndForNew(
    final String start
  ) {
    Calendar calendar =
      calendarOf(start);

    new DatePickerDialog(
      this,
      new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(
          DatePicker picker,
          int year,
          int month,
          int day
        ) {
          Calendar selected =
            Calendar.getInstance();

          selected.set(
            year,
            month,
            day
          );

          resetTime(selected);

          String end = key.format(
            selected.getTime()
          );

          if (end.compareTo(start) < 0) {
            toast(
              "La fin ne peut pas précéder le début."
            );

            return;
          }

          createPeriod(
            start,
            end,
            PeriodStore.COMPLETE
          );
        }
      },
      calendar.get(Calendar.YEAR),
      calendar.get(Calendar.MONTH),
      calendar.get(
        Calendar.DAY_OF_MONTH
      )
    ).show();
  }

  private boolean sameStart(String start) {
    for (PeriodStore.Period period : periods()) {
      if (period.start.equals(start)) {
        return true;
      }
    }

    return false;
  }

  private void createPeriod(
    String start,
    String end,
    String state
  ) {
    if (sameStart(start)) {
      toast(
        "Une période commence déjà à cette date."
      );

      return;
    }

    ArrayList<PeriodStore.Period> values =
      periods();

    final PeriodStore.Period period =
      new PeriodStore.Period(
        UUID.randomUUID().toString(),
        start,
        end,
        "medium",
        state
      );

    values.add(period);
    db.save(values);

    chooseIntensity(period);
  }

  private void chooseStartDateFor(
    final PeriodStore.Period period
  ) {
    Calendar calendar =
      calendarOf(period.start);

    new DatePickerDialog(
      this,
      new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(
          DatePicker picker,
          int year,
          int month,
          int day
        ) {
          Calendar selected =
            Calendar.getInstance();

          selected.set(
            year,
            month,
            day
          );

          resetTime(selected);

          changeStart(
            period,
            key.format(
              selected.getTime()
            )
          );
        }
      },
      calendar.get(Calendar.YEAR),
      calendar.get(Calendar.MONTH),
      calendar.get(
        Calendar.DAY_OF_MONTH
      )
    ).show();
  }

  private void changeStart(
    PeriodStore.Period period,
    String start
  ) {
    if (start.compareTo(today()) > 0) {
      toast(
        "Le premier jour ne peut pas être dans le futur."
      );

      return;
    }

    if (
      period.end.length() > 0 &&
      start.compareTo(period.end) > 0
    ) {
      toast(
        "Le premier jour ne peut pas être après la fin."
      );

      return;
    }

    period.start = start;
    saveChange(period);

    toast(
      "Date corrigée et prévisions recalculées."
    );

    render();
  }

  private void chooseEndDate(
    final PeriodStore.Period period
  ) {
    Calendar calendar =
      calendarOf(period.start);

    new DatePickerDialog(
      this,
      new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(
          DatePicker picker,
          int year,
          int month,
          int day
        ) {
          Calendar selected =
            Calendar.getInstance();

          selected.set(
            year,
            month,
            day
          );

          resetTime(selected);

          complete(
            period,
            key.format(
              selected.getTime()
            )
          );
        }
      },
      calendar.get(Calendar.YEAR),
      calendar.get(Calendar.MONTH),
      calendar.get(
        Calendar.DAY_OF_MONTH
      )
    ).show();
  }

  private void saveChange(
    PeriodStore.Period changed
  ) {
    ArrayList<PeriodStore.Period> values =
      periods();

    for (PeriodStore.Period period : values) {
      if (period.id.equals(changed.id)) {
        period.start = changed.start;
        period.end = changed.end;
        period.intensity =
          changed.intensity;
        period.state = changed.state;
        break;
      }
    }

    db.save(values);
  }

  private void markUnknownEnd(
    PeriodStore.Period period
  ) {
    period.end = "";
    period.state =
      PeriodStore.UNKNOWN_END;

    saveChange(period);

    toast(
      "Dernier jour laissé inconnu."
    );

    render();
  }

  private void complete(
    PeriodStore.Period period,
    String end
  ) {
    if (end.compareTo(period.start) < 0) {
      toast(
        "La fin ne peut pas précéder le début."
      );

      return;
    }

    period.end = end;
    period.state = PeriodStore.COMPLETE;

    saveChange(period);

    toast("Période mise à jour.");
    render();
  }

  private void chooseIntensity(
    final PeriodStore.Period period
  ) {
    int selected =
      "light".equals(period.intensity) ?
        0 :
        "heavy".equals(period.intensity) ?
          2 :
          1;

    new AlertDialog.Builder(this)
      .setTitle("Intensité")
      .setSingleChoiceItems(
        new String[] {
          "Légère",
          "Moyenne",
          "Abondante"
        },
        selected,
        new DialogInterface.OnClickListener() {
          public void onClick(
            DialogInterface dialog,
            int position
          ) {
            period.intensity =
              position == 0 ?
                "light" :
                position == 2 ?
                  "heavy" :
                  "medium";

            saveChange(period);
            dialog.dismiss();

            toast("Période enregistrée.");
            render();
          }
        }
      )
      .show();
  }

  private void requestNotificationPermissionIfNeeded() {
    if (
      android.os.Build.VERSION.SDK_INT < 33
    ) {
      return;
    }

    try {
      java.lang.reflect.Method check =
        Activity.class.getMethod(
          "checkSelfPermission",
          String.class
        );

      int result = ((Integer)
        check.invoke(
          this,
          "android.permission.POST_NOTIFICATIONS"
        )
      ).intValue();

      if (result != 0) {
        java.lang.reflect.Method request =
          Activity.class.getMethod(
            "requestPermissions",
            String[].class,
            int.class
          );

        request.invoke(
          this,
          new Object[] {
            new String[] {
              "android.permission.POST_NOTIFICATIONS"
            },
            Integer.valueOf(700)
          }
        );
      }
    } catch (Exception ignored) {
    }
  }

  private void toast(String message) {
    Toast.makeText(
      this,
      message,
      Toast.LENGTH_LONG
    ).show();
  }
}
