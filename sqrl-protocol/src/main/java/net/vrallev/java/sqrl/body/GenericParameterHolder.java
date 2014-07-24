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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ralf Wondratschek
 */
/*package*/ class GenericParameterHolder {

    public static final String KEY_CMD = "cmd";
    public static final String KEY_SUK = "suk";
    public static final String KEY_VUK = "vuk";

    // client
    public static final String KEY_VER = "ver";
    public static final String KEY_IDK = "idk";
    public static final String KEY_PIDK = "pidk";

    // server
    public static final String KEY_NUT = "nut";
    public static final String KEY_TIF = "tif";
    public static final String KEY_SFN = "sfn";

    private final Map<String, String> mMap;

    GenericParameterHolder() {
        mMap = new HashMap<>();
    }

    public GenericParameterHolder putPlainParameter(String key, String value) {
        mMap.put(key, value);
        return this;
    }

    public GenericParameterHolder putEncodedParameter(String key, byte[] value) {
        if (key != null && value != null) {
            putPlainParameter(key, SqrlRequestUtil.encodeBase64(value));
        }
        return this;
    }

    public String getPlainParameter(String key) {
        return mMap.get(key);
    }

    public byte[] getDecodedParameter(String key) {
        if (containsKey(key)) {
            return SqrlRequestUtil.decodeBase64(getPlainParameter(key));
        } else {
            return null;
        }
    }

    public boolean containsKey(String key) {
        return mMap.containsKey(key);
    }

    public byte[] getDecoded() {
        StringBuilder builder = new StringBuilder();
        if (containsKey(KEY_VER)) {
            builder.append(KEY_VER).append('=').append(getPlainParameter(KEY_VER)).append(SqrlRequestUtil.LINE_SEPARATOR);
        }

        for (String key : mMap.keySet()) {
            if (key.equals(KEY_VER)) {
                continue;
            }

            builder.append(key).append('=').append(getPlainParameter(key)).append(SqrlRequestUtil.LINE_SEPARATOR);
        }

        return builder.toString().getBytes(SqrlRequestUtil.ASCII);
    }

    public String getEncoded() {
        return SqrlRequestUtil.encodeBase64(getDecoded());
    }

    @Override
    public String toString() {
        return new String(getDecoded(), SqrlRequestUtil.ASCII);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GenericParameterHolder)) return false;

        GenericParameterHolder that = (GenericParameterHolder) o;

        if (mMap.size() != that.mMap.size()) {
            return false;
        }

        for (String key : mMap.keySet()) {
            if (!that.mMap.containsKey(key)) {
                return false;
            }
            if (!mMap.get(key).equals(that.mMap.get(key))) {
                return false;
            }
        }

        return true;

    }

    @Override
    public int hashCode() {
        return mMap.hashCode();
    }
}
