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

import net.vrallev.java.sqrl.SqrlProtocol;

/**
 * Represents the server parameter either in a client request or server response. Use a {@link ServerParameter.Builder}
 * or {@link SqrlProtocol#answerClient(SqrlClientBody, int...)} to create an instance.
 *
 * @author Ralf Wondratschek
 */
@SuppressWarnings("UnusedDeclaration")
public class ServerParameter {

    public static final int ID_MATCH = 0x0001;
    public static final int PREVIOUS_ID_MATCH = 0x0002;
    public static final int IP_MATCH = 0x0004;
    public static final int SQRL_ENABLED = 0x0008;
    public static final int USER_LOGGED_IN = 0x0010;
    public static final int SQRL_ACCOUNT_CREATION_ALLOWED = 0x0020;
    public static final int COMMAND_FAILED = 0x0040;
    public static final int SQRL_FAILURE = 0x0080;

    // missing: qry, lnk, ask

    private final boolean mIsUri;

    private final String mEncodedUri;

    private final GenericParameterHolder mParameterHolder;
    private final String mDecodedFromServer;

    public ServerParameter(String uriSignaturePart) {
        byte[] subUri = uriSignaturePart.getBytes(SqrlRequestUtil.UTF8);
        mEncodedUri = SqrlRequestUtil.encodeBase64(subUri);

        mParameterHolder = null;
        mDecodedFromServer = null;

        mIsUri = true;
    }

    private ServerParameter(GenericParameterHolder parameterHolder, String decodedFromServer) {
        mParameterHolder = parameterHolder;
        mDecodedFromServer = decodedFromServer;
        mEncodedUri = null;
        mIsUri = false;
    }

    public String getParameterEncoded() {
        if (mIsUri) {
            return mEncodedUri;
        } else if (mDecodedFromServer != null) {
            return SqrlRequestUtil.encodeBase64(mDecodedFromServer.getBytes(SqrlRequestUtil.ASCII));
        } else {
            return mParameterHolder.getEncoded();
        }
    }

    public byte[] getParameterDecoded() {
        if (mIsUri) {
            return SqrlRequestUtil.decodeBase64(mEncodedUri);
        } else if (mDecodedFromServer != null) {
            return mDecodedFromServer.getBytes(SqrlRequestUtil.ASCII);
        } else {
            return mParameterHolder.getDecoded();
        }
    }

    public String getVersionDecoded() {
        return mIsUri ? null : mParameterHolder.getPlainParameter(GenericParameterHolder.KEY_VER);
    }

    public String getNutDecoded() {
        if (mIsUri) {
            String string = new String(getParameterDecoded(), SqrlRequestUtil.ASCII);

            int nutBegin = string.indexOf("nut=") + "nut=".length();
            int nutEnd = string.indexOf("&", nutBegin);
            if (nutEnd == -1) {
                nutEnd = string.length();
            }

            return string.substring(nutBegin, nutEnd);

        } else {
            return new String(SqrlRequestUtil.decodeBase64(mParameterHolder.getPlainParameter(GenericParameterHolder.KEY_NUT)), SqrlRequestUtil.UTF8);
        }
    }

    public int getTransactionInformationFlag() {
        return mIsUri ? -1 : Integer.parseInt(mParameterHolder.getPlainParameter(GenericParameterHolder.KEY_TIF));
    }

    public boolean hasTransactionFlag(int flag) {
        return (getTransactionInformationFlag() & flag) == flag;
    }

    public String getServerFriendlyNameDecoded() {
        return mIsUri ? null : mParameterHolder.getPlainParameter(GenericParameterHolder.KEY_SFN);
    }

    public byte[] getServerUnlockKeyDecoded() {
        return mIsUri ? null : mParameterHolder.getDecodedParameter(GenericParameterHolder.KEY_SUK);
    }

    public byte[] getVerifyUnlockKeyDecoded() {
        return mIsUri ? null : mParameterHolder.getDecodedParameter(GenericParameterHolder.KEY_VUK);
    }

    public boolean isUri() {
        return mIsUri;
    }

    public SqrlServerBody asSqrlServerBody() {
        return new SqrlServerBody(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerParameter)) return false;

        ServerParameter that = (ServerParameter) o;

        if (mIsUri != that.mIsUri) return false;
        if (mEncodedUri != null ? !mEncodedUri.equals(that.mEncodedUri) : that.mEncodedUri != null)
            return false;
        //noinspection RedundantIfStatement
        if (mParameterHolder != null ? !mParameterHolder.equals(that.mParameterHolder) : that.mParameterHolder != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (mIsUri ? 1 : 0);
        result = 31 * result + (mEncodedUri != null ? mEncodedUri.hashCode() : 0);
        result = 31 * result + (mParameterHolder != null ? mParameterHolder.hashCode() : 0);
        return result;
    }

    public static class Builder {

        private final GenericParameterHolder mParameterHolder;
        private final String mDecodedParameter;

        public Builder(String nut, int tif) {
            this(nut, tif, null);
        }

        public Builder(String nut, int tif, String decodedParameter) {
            if (nut == null || nut.isEmpty()) {
                throw new IllegalArgumentException("nut can't be null or empty");
            }

            mParameterHolder = new GenericParameterHolder();
            mParameterHolder.putPlainParameter(GenericParameterHolder.KEY_NUT, SqrlRequestUtil.encodeBase64(nut.getBytes(SqrlRequestUtil.UTF8)));
            mParameterHolder.putPlainParameter(GenericParameterHolder.KEY_TIF, String.valueOf(tif));

            mDecodedParameter = decodedParameter;

            withVersion("1");
        }

        /**
         * The protocol's version. The default value is <i>'1'</i>.
         */
        public Builder withVersion(String version) {
            mParameterHolder.putPlainParameter(GenericParameterHolder.KEY_VER, version);
            return this;
        }

        /**
         * Set the optional server friendly name.
         */
        public Builder withServerFriendlyName(String serverFriendlyName) {
            mParameterHolder.putPlainParameter(GenericParameterHolder.KEY_SFN, serverFriendlyName);
            return this;
        }

        /**
         * Set the account's stored keys. You usually should add them as these are no secret keys
         * and the client may require them.
         */
        public Builder withStoredKeys(byte[] serverUnlockKey, byte[] verifyUnlockKey) {
            mParameterHolder.putEncodedParameter(GenericParameterHolder.KEY_SUK, serverUnlockKey);
            mParameterHolder.putEncodedParameter(GenericParameterHolder.KEY_VUK, verifyUnlockKey);
            return this;
        }

        public ServerParameter create() {
            if (!mParameterHolder.containsKey(GenericParameterHolder.KEY_VER)) {
                mParameterHolder.putPlainParameter(GenericParameterHolder.KEY_VER, "1");
            }

            return new ServerParameter(mParameterHolder, mDecodedParameter);
        }

        private void checkKey(byte[] key) {
            if (key != null && key.length != 32) {
                throw new IllegalArgumentException("illegal key length");
            }
        }
    }
}
