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

import java.util.Arrays;
import java.util.List;

/**
 * Represents the client parameter in a request from a client sent to the server.
 *
 * @author Ralf Wondratschek
 */
@SuppressWarnings("UnusedDeclaration")
public class ClientParameter {

    private final GenericParameterHolder mParameterHolder;

    private ClientParameter(GenericParameterHolder parameterHolder) {
        mParameterHolder = parameterHolder;
    }

    public String getParameterEncoded() {
        return mParameterHolder.getEncoded();
    }

    public byte[] getParameterDecoded() {
        return mParameterHolder.getDecoded();
    }

    public List<String> getCommands() {
        String commands = mParameterHolder.getPlainParameter(GenericParameterHolder.KEY_CMD);
        if (commands == null) {
            return null;
        }

        return Arrays.asList(commands.split("~"));
    }

    public byte[] getIdentityKeyDecoded() {
        return mParameterHolder.getDecodedParameter(GenericParameterHolder.KEY_IDK);
    }

    public byte[] getPreviousIdentityKeyDecoded() {
        return mParameterHolder.getDecodedParameter(GenericParameterHolder.KEY_PIDK);
    }

    public byte[] getServerUnlockKeyDecoded() {
        return mParameterHolder.getDecodedParameter(GenericParameterHolder.KEY_SUK);
    }

    public byte[] getVerifyUnlockKeyDecoded() {
        return mParameterHolder.getDecodedParameter(GenericParameterHolder.KEY_VUK);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientParameter)) return false;

        ClientParameter that = (ClientParameter) o;

        return mParameterHolder.equals(that.mParameterHolder);
    }

    @Override
    public int hashCode() {
        return mParameterHolder.hashCode();
    }

    public static class Builder {

        private final GenericParameterHolder mParameterHolder;

        public Builder(List<String> commands, byte[] identityKey) {
            if (commands == null || commands.isEmpty() || identityKey == null) {
                throw new IllegalArgumentException("command and identityKey can't be null");
            }

            checkKey(identityKey);

            mParameterHolder = new GenericParameterHolder();
            mParameterHolder.putPlainParameter(GenericParameterHolder.KEY_CMD, getCommandsAsString(commands));
            mParameterHolder.putEncodedParameter(GenericParameterHolder.KEY_IDK, identityKey);

            withVersion("1");
        }

        public Builder withVersion(String version) {
            mParameterHolder.putPlainParameter(GenericParameterHolder.KEY_VER, version);
            return this;
        }

        public Builder setPreviousIdentityKey(byte[] previousIdentityKey) {
            mParameterHolder.putEncodedParameter(GenericParameterHolder.KEY_PIDK, previousIdentityKey);
            return this;
        }

        public Builder setServerUnlockKey(byte[] serverUnlockKey) {
            mParameterHolder.putEncodedParameter(GenericParameterHolder.KEY_SUK, serverUnlockKey);
            return this;
        }

        public Builder setVerifyUnlockKey(byte[] verifyUnlockKey) {
            mParameterHolder.putEncodedParameter(GenericParameterHolder.KEY_VUK, verifyUnlockKey);
            return this;
        }

        public ClientParameter build() {
            if (!mParameterHolder.containsKey(GenericParameterHolder.KEY_VER)) {
                mParameterHolder.putPlainParameter(GenericParameterHolder.KEY_VER, "1");
            }

            return new ClientParameter(mParameterHolder);
        }

        private void checkKey(byte[] key) {
            if (key != null && key.length != 32) {
                throw new IllegalArgumentException("illegal key length");
            }
        }
    }

    /*package*/ static String getCommandsAsString(List<String> commands) {
        StringBuilder builder = new StringBuilder();
        for (String command : commands) {
            if (builder.length() != 0) {
                builder.append('~');
            }
            builder.append(command);
        }
        return builder.toString();
    }
}
