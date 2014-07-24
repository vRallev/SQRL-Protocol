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
import net.vrallev.java.sqrl.ecc.EccKeyPair;
import net.vrallev.java.sqrl.ecc.EccProvider25519;
import net.vrallev.java.sqrl.util.SqrlCipherTool;
import net.vrallev.java.sqrl.SqrlProtocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the whole body sent from a client to a server. Use the {@link SqrlClientBody.Builder} to
 * create an instance or use the {@link SqrlProtocol} class as entry point.
 *
 * @author Ralf Wondratschek
 */
@SuppressWarnings("UnusedDeclaration")
public class SqrlClientBody {

    private ClientParameter mClientParameter;
    private ServerParameter mServerParameter;

    private byte[] mIdentitySignature;

    private byte[] mPreviousIdentitySignature;
    private byte[] mUnlockRequestSignature;

    private String mBody;

    public SqrlClientBody(ClientParameter clientParameter, ServerParameter serverParameter, byte[] identitySignature, byte[] previousIdentitySignature, byte[] unlockRequestSignature) {
        mClientParameter = clientParameter;
        mServerParameter = serverParameter;
        mIdentitySignature = identitySignature;
        mPreviousIdentitySignature = previousIdentitySignature;
        mUnlockRequestSignature = unlockRequestSignature;

        StringBuilder builder = new StringBuilder()
                .append("client=").append(mClientParameter.getParameterEncoded())
                .append("&server=").append(mServerParameter.getParameterEncoded())
                .append("&ids=").append(SqrlRequestUtil.encodeBase64(mIdentitySignature));

        if (mPreviousIdentitySignature != null) {
            builder.append("&pids=").append(SqrlRequestUtil.encodeBase64(mPreviousIdentitySignature));
        }
        if (mUnlockRequestSignature != null) {
            builder.append("&urs=").append(SqrlRequestUtil.encodeBase64(mUnlockRequestSignature));
        }

        mBody = builder.toString();
    }

    /**
     * @return the flattened body.
     */
    public String getBodyEncoded() {
        return mBody;
    }

    public ClientParameter getClientParameter() {
        return mClientParameter;
    }

    public ServerParameter getServerParameter() {
        return mServerParameter;
    }

    public byte[] getIdentitySignatureDecoded() {
        return mIdentitySignature;
    }

    public byte[] getPreviousIdentitySignatureDecoded() {
        return mPreviousIdentitySignature;
    }

    public byte[] getUnlockRequestSignatureDecoded() {
        return mUnlockRequestSignature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SqrlClientBody)) return false;

        SqrlClientBody that = (SqrlClientBody) o;

        if (!mBody.equals(that.mBody)) return false;
        if (!mClientParameter.equals(that.mClientParameter)) return false;
        if (!Arrays.equals(mIdentitySignature, that.mIdentitySignature)) return false;
        if (!Arrays.equals(mPreviousIdentitySignature, that.mPreviousIdentitySignature))
            return false;
        if (!mServerParameter.equals(that.mServerParameter)) return false;
        //noinspection RedundantIfStatement
        if (!Arrays.equals(mUnlockRequestSignature, that.mUnlockRequestSignature))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mClientParameter.hashCode();
        result = 31 * result + mServerParameter.hashCode();
        result = 31 * result + Arrays.hashCode(mIdentitySignature);
        result = 31 * result + (mPreviousIdentitySignature != null ? Arrays.hashCode(mPreviousIdentitySignature) : 0);
        result = 31 * result + (mUnlockRequestSignature != null ? Arrays.hashCode(mUnlockRequestSignature) : 0);
        result = 31 * result + mBody.hashCode();
        return result;
    }

    public static class Builder {

        private final SqrlCipherTool mCipherTool;
        private final EccProvider25519 mEccProvider;
        private final byte[] mMasterKey;
        private final String mSiteKey;

        private String mVersion;

        private ServerParameter mServerParameter;
        private String mSignatureUri;

        private final List<String> mCommands;

        private byte[] mIdentityUnlockKey;
        private byte[] mPreviousMasterKey;

        private byte[] mPreviousServerUnlockKey;
        private byte[] mPreviousVerifyUnlockKey;

        private byte[] mNewServerUnlockKey;
        private byte[] mNewVerifyUnlockKey;

        public Builder(SqrlCipherTool sqrlCipherTool, EccProvider25519 eccProvider, byte[] masterKey, String siteKey) {
            mCipherTool = sqrlCipherTool;
            mEccProvider = eccProvider;
            mMasterKey = masterKey;
            mSiteKey = siteKey;

            mCommands = new ArrayList<>();

            mVersion = "1";
        }

        /**
         * The version in the client parameter.
         */
        public Builder withVersion(String version) {
            mVersion = version;
            return this;
        }

        /**
         * A command like 'create', 'setkey' or 'login'.
         */
        public Builder addCommand(String command) {
            mCommands.add(command);
            return this;
        }

        /**
         * Required for several actions like updating the identity key at a server.
         */
        public Builder withIdentityUnlockKey(byte[] identityUnlockKey) {
            mIdentityUnlockKey = identityUnlockKey;
            return this;
        }

        /**
         * If a previous master key is available, you should add this key in the request. A server
         * may need to update the account's keys.
         */
        public Builder withPreviousMasterKey(byte[] previousMasterKey) {
            mPreviousMasterKey = previousMasterKey;
            return this;
        }

        /**
         * Set the keys, which you received from the server. These are required, if you want to update
         * the server keys with the <i>identity unlock key</i>.
         */
        public Builder withPreviousServerKeys(byte[] serverUnlockKey, byte[] verifyUnlockKey) {
            mPreviousServerUnlockKey = serverUnlockKey;
            mPreviousVerifyUnlockKey = verifyUnlockKey;
            return this;
        }

        /**
         * Set the new server keys, if you create a new account or if you update the server keys.
         */
        public Builder withNewServerKeys(byte[] serverUnlockKey, byte[] verifyUnlockKey) {
            mNewServerUnlockKey = serverUnlockKey;
            mNewVerifyUnlockKey = verifyUnlockKey;
            return this;
        }

        /**
         * Build a new fresh client request.
         *
         * @param signatureUri The whole uri without the scheme, e.g.
         *                     <i>sqrl-login.appspot.com:443/sqrl/auth?nut=5b21...</i>. Take a look
         *                     at the <a href="https://www.grc.com/sqrl/protocol.htm">specification</a>
         *                     for more information.
         * @throws SqrlException if checking a signature fails.
         */
        public SqrlClientBody buildRequest(String signatureUri) throws SqrlException {
            if (signatureUri == null) {
                throw new IllegalArgumentException("no null value allowed");
            }

            mSignatureUri = signatureUri;
            return build();
        }

        /**
         * Answer a previous server response.
         */
        public SqrlClientBody buildResponse(SqrlServerBody serverBody) throws SqrlException {
            return buildResponse(serverBody.getServerParameter());
        }

        /**
         * Answer a previous server response.
         */
        public SqrlClientBody buildResponse(ServerParameter serverParameter) throws SqrlException {
            if (serverParameter == null) {
                throw new IllegalArgumentException("no null value allowed");
            }

            mServerParameter = serverParameter;
            return build();
        }

        private SqrlClientBody build() throws SqrlException {
            byte[] privateSiteKey = mCipherTool.computeHmac(mSiteKey.getBytes(SqrlRequestUtil.UTF8), mMasterKey);

            EccKeyPair identityKeyPair = mEccProvider.computeKeyPair(privateSiteKey);
            byte[] identityKey = identityKeyPair.getPublicKeySignature();

            EccKeyPair previousKeyPair = null;
            byte[] previousIdentityKey = null;
            if (mPreviousMasterKey != null) {
                byte[] previousPrivateSiteKey = mCipherTool.computeHmac(mSiteKey.getBytes(SqrlRequestUtil.UTF8), mPreviousMasterKey);
                previousKeyPair = mEccProvider.computeKeyPair(previousPrivateSiteKey);
                previousIdentityKey = previousKeyPair.getPublicKeySignature();
            }

            if (mCommands.isEmpty()) {
                mCommands.add("login");
            }

            if (mCommands.contains("create")) {
                mCommands.add("setkey");
                mCommands.add("setlock");
            }

            ClientParameter clientParameter = new ClientParameter.Builder(mCommands, identityKey)
                    .withVersion(mVersion)
                    .setPreviousIdentityKey(previousIdentityKey)
                    .setServerUnlockKey(mNewServerUnlockKey)
                    .setVerifyUnlockKey(mNewVerifyUnlockKey)
                    .build();

            ServerParameter serverParameter;
            if (mServerParameter != null) {
                serverParameter = mServerParameter;
            } else {
                serverParameter = new ServerParameter(mSignatureUri);
            }
            byte[] concatenation = (clientParameter.getParameterEncoded() + serverParameter.getParameterEncoded()).getBytes(SqrlRequestUtil.ASCII);

            byte[] identitySignature = mEccProvider.sign(concatenation, identityKeyPair.getPrivateKey(), identityKeyPair.getPublicKeySignature());
            byte[] previousIdentitySignature;
            byte[] unlockRequestSignature;

            if (clientParameter.getPreviousIdentityKeyDecoded() != null && previousKeyPair != null) {
                previousIdentitySignature = mEccProvider.sign(concatenation, previousKeyPair.getPrivateKey(), previousKeyPair.getPublicKeySignature());
            } else {
                previousIdentitySignature = null;
            }

            if (mIdentityUnlockKey != null) {
                byte[] unlockRequestSigningKey = mEccProvider.diffieHellman(mIdentityUnlockKey, mPreviousServerUnlockKey);

                EccKeyPair ursKeyPair = mEccProvider.computeKeyPair(unlockRequestSigningKey);
                byte[] verifyUnlockKey = ursKeyPair.getPublicKeySignature();
                if (verifyUnlockKey == null || !Arrays.equals(verifyUnlockKey, mPreviousVerifyUnlockKey)) {
                    throw new SqrlException("Verify unlock key didn't match");
                }

                unlockRequestSignature = mEccProvider.sign(verifyUnlockKey, ursKeyPair.getPrivateKey(), ursKeyPair.getPublicKeySignature());
            } else {
                unlockRequestSignature = null;
            }

            return new SqrlClientBody(clientParameter, serverParameter, identitySignature, previousIdentitySignature, unlockRequestSignature);
        }
    }
}
