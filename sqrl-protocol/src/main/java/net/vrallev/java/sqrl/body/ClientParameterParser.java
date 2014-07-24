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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Ralf Wondratschek
 */
/*package*/ class ClientParameterParser {

    public ClientParameter parse(String parameter) throws SqrlException {
        Map<String, String> map = SqrlRequestUtil.splitKeyValue(parameter, SqrlRequestUtil.LINE_SEPARATOR, "=");
        if (!map.containsKey("ver") || !"1".equals(map.get("ver"))) {
            throw new SqrlException("Wrong version");
        }

        if (!map.containsKey("cmd") || !map.containsKey("idk")) {
            throw new SqrlException("missing important parameter");
        }

        String commandList = map.get("cmd");
        List<String> commands = Arrays.asList(commandList.split("~"));

        byte[] identityKey = SqrlRequestUtil.decodeBase64(map.get("idk"));

        ClientParameter.Builder builder = new ClientParameter.Builder(commands, identityKey);

        if (map.containsKey("pidk")) {
            builder.setPreviousIdentityKey(SqrlRequestUtil.decodeBase64(map.get("pidk")));
        }
        if (map.containsKey("suk")) {
            builder.setServerUnlockKey(SqrlRequestUtil.decodeBase64(map.get("suk")));
        }
        if (map.containsKey("vuk")) {
            builder.setVerifyUnlockKey(SqrlRequestUtil.decodeBase64(map.get("vuk")));
        }

        return builder.build();
    }
}
