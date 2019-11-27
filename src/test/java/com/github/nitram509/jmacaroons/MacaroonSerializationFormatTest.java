/*
 * Copyright (c) 2019 Neil Madden.
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

import static org.fest.assertions.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MacaroonSerializationFormatTest {

    // Implements the serialization tests described in https://github.com/rescrv/libmacaroons/blob/master/doc/tests.txt
    // The v1 examples are double base64-encoded for reasons that are not entirely clear, so I have removed one layer of
    // base64-encoding from the test data.

    @DataProvider
    public Object[][] testCases() {
        return new Object[][] {
                {
                        "MDAyMWxvY2F0aW9uIGh0dHA6Ly9leGFtcGxlLm9yZy8KMDAxNWlkZW50aWZpZXIga2V5aWQKMDAyZnNpZ25hdHVyZSB83ue"
                                + "SURxbxvUoSFgF3-myTnheKOKpkwH51xHGCeOO9wo",
                        "AgETaHR0cDovL2V4YW1wbGUub3JnLwIFa2V5aWQAAAYgfN7nklEcW8b1KEhYBd_psk54XijiqZMB-dcRxgnjjvc"
                },
                {
                        "MDAyMWxvY2F0aW9uIGh0dHA6Ly9leGFtcGxlLm9yZy8KMDAxNWlkZW50aWZpZXIga2V5aWQKMDAxZGNpZCBhY2NvdW50ID0"
                                + "gMzczNTkyODU1OQowMDJmc2lnbmF0dXJlIPVIB_bcbt-Ivw9zBrOCJWKjYlM9v3M5umF2XaS9JZ2HCg",
                        "AgETaHR0cDovL2V4YW1wbGUub3JnLwIFa2V5aWQAAhRhY2NvdW50ID0gMzczNTkyODU1OQAABiD1SAf23G7fiL8PcwazgiV"
                                + "io2JTPb9zObphdl2kvSWdhw"
                },
                {
                        "MDAyMWxvY2F0aW9uIGh0dHA6Ly9leGFtcGxlLm9yZy8KMDAxNWlkZW50aWZpZXIga2V5aWQKMDAxZGNpZCBhY2NvdW50ID0"
                                + "gMzczNTkyODU1OQowMDE1Y2lkIHVzZXIgPSBhbGljZQowMDJmc2lnbmF0dXJlIEvpZ80eoMaya69qSpTumwWxWI"
                                + "baC6hejEKpPI0OEl78Cg",
                        "AgETaHR0cDovL2V4YW1wbGUub3JnLwIFa2V5aWQAAhRhY2NvdW50ID0gMzczNTkyODU1OQACDHVzZXIgPSBhbGljZQAABiB"
                                + "L6WfNHqDGsmuvakqU7psFsViG2guoXoxCqTyNDhJe_A"
                }

        };
    }

    @Test(dataProvider = "testCases")
    public void shouldMatchSerializationTests(String v1, String v2) {
        // Given

        // When
        Macaroon v1Macaroon = MacaroonsBuilder.deserialize(v1, MacaroonSerializationFormat.V1);
        Macaroon v2Macaroon = MacaroonsBuilder.deserialize(v2, MacaroonSerializationFormat.V2);

        // Then
        assertThat(v1Macaroon).isEqualTo(v2Macaroon);
        assertThat(v1Macaroon.serialize(MacaroonSerializationFormat.V1)).isEqualTo(v1);
        assertThat(v2Macaroon.serialize(MacaroonSerializationFormat.V2)).isEqualTo(v2);
    }

}