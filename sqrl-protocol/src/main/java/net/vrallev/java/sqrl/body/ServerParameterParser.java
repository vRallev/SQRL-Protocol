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


import net.vrallev.java.sqrl.SqrlException;

import java.util.Map;

/**
 * @author Ralf Wondratschek
 */
/*package*/ class ServerParameterParser {

    public ServerParameter parse(String parameter, boolean encoded) throws SqrlException {
        if (encoded) {
            parameter = new String(SqrlRequestUtil.decodeBase64(parameter), SqrlRequestUtil.ASCII);
        }

        if (!parameter.startsWith("ver=")) {
            // only check for version begin, we check the signature later
            return new ServerParameter(parameter);
        }

        Map<String, String> map = SqrlRequestUtil.splitKeyValue(parameter, SqrlRequestUtil.LINE_SEPARATOR, "=");

        if (!map.containsKey("ver") || !map.containsKey("nut") || !map.containsKey("tif")) {
            throw new SqrlException("missing important parameter");
        }

        String version = map.remove("ver");
        if (!"1".equals(version)) {
            throw new SqrlException("wrong version");
        }

        String nut = new String(SqrlRequestUtil.decodeBase64(map.remove("nut")), SqrlRequestUtil.UTF8);
        int tif = Integer.parseInt(map.remove("tif"));
        String serverFriendlyName = map.remove("sfn");

        byte[] serverUnlockKey = null;
        if (map.containsKey("suk")) {
            serverUnlockKey = SqrlRequestUtil.decodeBase64(map.remove("suk"));
        }

        byte[] verifyUnlockKey = null;
        if (map.containsKey("vuk")) {
            verifyUnlockKey = SqrlRequestUtil.decodeBase64(map.remove("vuk"));
        }

        return new ServerParameter.Builder(nut, tif, parameter)
                .withVersion(version)
                .withServerFriendlyName(serverFriendlyName)
                .withStoredKeys(serverUnlockKey, verifyUnlockKey)
                .create();
    }
}
