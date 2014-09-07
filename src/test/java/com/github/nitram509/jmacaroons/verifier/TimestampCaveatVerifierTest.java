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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.fest.assertions.Assertions.assertThat;

public class TimestampCaveatVerifierTest {

  private TimestampCaveatVerifier verifier;

  @BeforeMethod
  public void setUp() throws Exception {
    verifier = new TimestampCaveatVerifier();
  }

  @Test
  public void is_valid_using_full_qualified_timestamp_with_timezone() {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    String caveat = "time < " + createTimeStamp1DayInFuture(dateFormat);

    assertThat(verifier.verifyCaveat(caveat)).isTrue();
  }

  @Test
  public void is_valid_using_full_qualified_timestamp() {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    String caveat = "time < " + createTimeStamp1DayInFuture(dateFormat);

    assertThat(verifier.verifyCaveat(caveat)).isTrue();
  }

  @Test
  public void is_valid_using_timestamp_onyl_precise_in_minutes() {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    String caveat = "time < " + createTimeStamp1DayInFuture(dateFormat);

    assertThat(verifier.verifyCaveat(caveat)).isTrue();
  }

  @Test
  public void is_valid_using_timestamp_onyl_precise_in_hours() {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH");
    String caveat = "time < " + createTimeStamp1DayInFuture(dateFormat);

    assertThat(verifier.verifyCaveat(caveat)).isTrue();
  }

  @Test
  public void is_valid_using_timestamp_onyl_precise_in_days() {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String caveat = "time < " + createTimeStamp1DayInFuture(dateFormat);

    assertThat(verifier.verifyCaveat(caveat)).isTrue();
  }

  @Test
  public void verifier_is_robust() {
    String caveat = "time < foobar";

    assertThat(verifier.verifyCaveat(caveat)).isFalse();
  }

  @Test
  public void is_NOT_valid_when_is_NOW() {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    String caveat = "time < " + dateFormat.format(new Date());

    assertThat(verifier.verifyCaveat(caveat)).isFalse();
  }

  private String createTimeStamp1DayInFuture(DateFormat dateFormat) {
    return dateFormat.format(new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 24)));
  }
}