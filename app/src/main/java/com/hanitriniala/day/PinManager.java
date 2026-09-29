package com.hanitriniala.day;

import android.content.Context;
import android.content.SharedPreferences;
import java.security.MessageDigest;
import java.security.SecureRandom;

public final class PinManager {
  private final SharedPreferences preferences;

  public PinManager(Context context) {
    preferences = context.getSharedPreferences(
      "hanitriniala.security",
      Context.MODE_PRIVATE
    );
  }

  public boolean hasPin() {
    return preferences.getString("pin_hash", "").length() > 0;
  }

  public void setPin(String pin) {
    String salt = preferences.getString("pin_salt", "");

    if (salt.length() == 0) {
      byte[] randomBytes = new byte[16];
      new SecureRandom().nextBytes(randomBytes);
      salt = toHex(randomBytes);
    }

    preferences.edit()
      .putString("pin_salt", salt)
      .putString("pin_hash", hash(pin, salt))
      .apply();
  }

  public boolean matches(String pin) {
    String salt = preferences.getString("pin_salt", "");
    String expected = preferences.getString("pin_hash", "");

    return expected.length() > 0 &&
      expected.equals(hash(pin, salt));
  }

  public void clear() {
    preferences.edit().clear().apply();
  }

  private String hash(String pin, String salt) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");

      digest.update(fromHex(salt));

      return toHex(
        digest.digest(pin.getBytes("UTF-8"))
      );
    } catch (Exception ignored) {
      return "";
    }
  }

  private String toHex(byte[] data) {
    StringBuilder result = new StringBuilder();

    for (byte value : data) {
      result.append(
        String.format("%02x", value & 0xff)
      );
    }

    return result.toString();
  }

  private byte[] fromHex(String value) {
    byte[] result = new byte[value.length() / 2];

    for (int i = 0; i < result.length; i++) {
      int index = i * 2;

      result[i] = (byte) Integer.parseInt(
        value.substring(index, index + 2),
        16
      );
    }

    return result;
  }
}