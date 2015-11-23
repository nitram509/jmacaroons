/*
 * Copyright 2014 Martin W. Kirst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nitram509.jmacaroons.verifier;

import com.github.nitram509.jmacaroons.GeneralCaveatVerifier;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * <p>
 * A verifier that is able to verify timestamps against current time.
 * Thus, it only supports general caveats i.e. <code>"time&nbsp;&lt;&nbsp;2085-12-31T00:00"</code>.
 * In general, ISO8601 timestamp format with optional parts is allowed.
 * </p>
 *
 * <table>
 * <caption><strong>Supported formats</strong></caption>
 * <tr>
 * <th>Supported pattern</th>
 * <th>Example</th>
 * </tr>
 * <tr>
 * <td><code>yyyy-MM-dd'T'HH:mm:ssZ</code></td>
 * <td>2014-09-23T17:42:35+200 (only precise up to 1 second, the RFC 822 4-digit time zone format is used)</td>
 * </tr>
 * <tr>
 * <td><code>yyyy-MM-dd'T'HH:mm:ss</code></td>
 * <td>2014-09-23T17:42:35 (only precise up to 1 second)</td>
 * </tr>
 * <tr>
 * <td><code>yyyy-MM-dd'T'HH:mm</code></td>
 * <td>2014-09-23T17:42 (only precise up to 1 minute)</td>
 * </tr>
 * <tr>
 * <td><code>yyyy-MM-dd'T'HH</code></td>
 * <td>2014-09-23T17 (only precise up to 1 hour)</td>
 * </tr>
 * <tr>
 * <td><code>yyyy-MM-dd</code></td>
 * <td>2014-09-23 (only precise up to 1 day)</td>
 * </tr>
 * </table>
 * <br>
 * <strong>Applying a time based caveat</strong>
 * <pre>{@code
 * Macaroon m = new MacaroonsBuilder("location", "secret", "identifiert")
 *    .add_first_party_caveat("time &lt; 2042-09-23T17:42:35")
 *    .getMacaroon();
 * new MacaroonsVerifier(m)
 *    .satisfyGeneral(new TimestampCaveatVerifier())
 *    .assertIsValid("secret");
 * }</pre>
 */
public class TimestampCaveatVerifier implements GeneralCaveatVerifier {

  public static final String CAVEAT_PREFIX = "time < ";
  public static final int CAVEAT_PREFIX_LEN = CAVEAT_PREFIX.length();

  private SimpleDateFormat ISO_DateFormat_DAY = new SimpleDateFormat("yyyy-MM-dd");
  private SimpleDateFormat ISO_DateFormat_HOUR = new SimpleDateFormat("yyyy-MM-dd'T'HH");
  private SimpleDateFormat ISO_DateFormat_MINUTE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
  private SimpleDateFormat ISO_DateFormat_SECOND = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  private SimpleDateFormat ISO_DateFormat_TIMEZONE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

  @Override
  public boolean verifyCaveat(String caveat) {
    if (caveat.startsWith(CAVEAT_PREFIX)) {
      Date parsedDate = ISO_DateFormat_TIMEZONE.parse(caveat, new ParsePosition(CAVEAT_PREFIX_LEN));
      if (parsedDate == null) parsedDate = ISO_DateFormat_SECOND.parse(caveat, new ParsePosition(CAVEAT_PREFIX_LEN));
      if (parsedDate == null) parsedDate = ISO_DateFormat_MINUTE.parse(caveat, new ParsePosition(CAVEAT_PREFIX_LEN));
      if (parsedDate == null) parsedDate = ISO_DateFormat_HOUR.parse(caveat, new ParsePosition(CAVEAT_PREFIX_LEN));
      if (parsedDate == null) parsedDate = ISO_DateFormat_DAY.parse(caveat, new ParsePosition(CAVEAT_PREFIX_LEN));
      Date now = Calendar.getInstance().getTime();
      return parsedDate != null && now.before(parsedDate);
    }
    return false;
  }
}
