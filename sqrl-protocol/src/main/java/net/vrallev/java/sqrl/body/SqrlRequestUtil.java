/*
 * Copyright (C) 2014 Ralf Wondratschek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.vrallev.java.sqrl.body;


import android.util.changed.Base64;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ralf Wondratschek
 */
/*package*/ final class SqrlRequestUtil {

    public static final String LINE_SEPARATOR = "\r\n";

    public static final Charset ASCII = Charset.forName("US-ASCII");
    public static final Charset UTF8 = Charset.forName("UTF-8");

    private SqrlRequestUtil() {
    }

    public static String encodeBase64(byte[] data) {
        return Base64.encodeToString(data, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
    }

    public static byte[] decodeBase64(String data) {
        return Base64.decode(data, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
    }

    public static Map<String, String> splitKeyValue(String string, String listSeparator, String entrySeparator) throws IllegalArgumentException {
        String[] split = string.split(listSeparator);
        Map<String, String> map = new HashMap<>();
        for (String value : split) {
            String[] keyValue = value.split(entrySeparator);
            if (keyValue.length < 2) {
                throw new IllegalArgumentException("unexpected size");
            }
            if (keyValue.length > 2) {
                map.put(keyValue[0], concat(Arrays.copyOfRange(keyValue, 1, keyValue.length), entrySeparator));
            } else {
                map.put(keyValue[0], keyValue[1]);
            }
        }

        return map;
    }

    private static String concat(String[] array, String delimiter) {
        StringBuilder builder = new StringBuilder(array[0]);
        for (int i = 1; i < array.length; i++) {
            builder.append(delimiter).append(array[i]);
        }
        return builder.toString();
    }
}
