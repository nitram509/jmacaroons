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

package com.github.nitram509.jmacaroons;

/**
 * A format for serializing a macaroon to or from a string representation. The supported formats are:
 * <ul>
 *     <li>{@link #V1} the original version 1 format. This format is simple but bulkier than the V2 format.</li>
 *     <li>{@link #V2} the version 2 binary format used by libmacaroons.</li>
 * </ul>
 */
public interface MacaroonsSerializer {
  /**
   * The version 1 format of libmacaroons. This format is much bulkier than {@link #V2}, which should be preferred
   * for new applications unless backwards compatibility with old macaroons libraries is required.
   */
  MacaroonsSerializer V1 = new MacaroonsSerializerV1();
  /**
   * The version 2 format of libmacaroons. This format uses an efficient binary encoding of macaroons.
   */
  MacaroonsSerializer V2 = new MacaroonsSerializerV2();

  /**
   * Serializes a macaroon into a string form.
   *
   * @param macaroon the macaroon to serialize.
   * @return the string form of the macaroon.
   */
  String serialize(Macaroon macaroon);

  /**
   * Deserializes a macaroon from a string. Note that this method doesn't validate the macaroon signature or
   * caveats, for which you should use a {@link MacaroonsVerifier}.
   *
   * @param serialized the serialized string form.
   * @return the deserialized macaroon.
   * @throws NotDeSerializableException if the macaroon can't be deserialized.
   */
  Macaroon deserialize(String serialized);
}
