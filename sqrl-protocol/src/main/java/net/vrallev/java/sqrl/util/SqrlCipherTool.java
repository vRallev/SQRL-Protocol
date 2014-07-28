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
package net.vrallev.java.sqrl.util;

import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.modes.GCMBlockCipher;
import org.spongycastle.crypto.params.AEADParameters;
import org.spongycastle.crypto.params.KeyParameter;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * A helper class, which implements many necessary operations.
 *
 * @author Ralf Wondratschek
 */
@SuppressWarnings("UnusedDeclaration")
public class SqrlCipherTool {

    private static final String MAC_ALGO = "HmacSHA256";
    private static final String DEFAULT_HASH_ALGORITHM = "SHA-256";
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final SecureRandom mRandom;
    private final Mac mHMacSha256;

    private final GCMBlockCipher mAesGcmEngine;

    protected MessageDigest mMessageDigest;
    protected Charset mCharset;

    public SqrlCipherTool() {
        this(DEFAULT_HASH_ALGORITHM, UTF_8);
    }

    public SqrlCipherTool(String algorithm, Charset charset) {
        mRandom = new SecureRandom();
        mCharset = charset;

        mAesGcmEngine = new GCMBlockCipher(new AESFastEngine());

        try {
            mHMacSha256 = Mac.getInstance(MAC_ALGO);
            mMessageDigest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }


    public byte[] getHash(String clearText) {
        return getHash(clearText, 1);
    }

    public byte[] getHash(byte[] data) {
        return getHash(data, 1);
    }

    public String getHashString(String clearText) {
        return getHashString(clearText.getBytes(mCharset));
    }

    public String getHashString(byte[] data) {
        return getHashString(data, 1);
    }

    public String getHashString(String clearText, int iterations) {
        return getHashString(clearText.getBytes(mCharset), iterations);
    }

    public String getHashString(byte[] data, int iterations) {
        return bin2hex(getHash(data, iterations));
    }

    public byte[] getHash(String clearText, int iterations) {
        return getHash(clearText.getBytes(mCharset), iterations);
    }

    public byte[] getHash(byte[] data, int iterations) {
        iterations = Math.max(1, iterations);
        for (int i = 0; i < iterations; i++) {
            data = mMessageDigest.digest(data);
        }
        return data;
    }

    public byte[] createRandomHash(int lengthBit) {
        byte[] bytes = new byte[128];
        mRandom.nextBytes(bytes);
        byte[] hash = getHash(bytes);
        return Arrays.copyOf(hash, lengthBit / 8);
    }

    public String createRescueCode() {
        StringBuilder builder = new StringBuilder(24);
        for (int i = 0; i < 24; i++) {
            builder.append(mRandom.nextInt(10));
        }

        return builder.toString();
    }

    public byte[] computeHmac(byte[] data, String password) {
        return computeHmac(data, password.getBytes(mCharset));
    }

    public byte[] computeHmac(byte[] data, byte[] password) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(password, MAC_ALGO);
            mHMacSha256.init(secretKeySpec);

            return mHMacSha256.doFinal(data);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public byte[] getHashChained(byte[] data, int rounds) {
        byte[] hash = getHash(data);
        if (rounds == 1) {
            return hash;
        }

        byte[] xor = hash;

        for (int i = 1; i < rounds; i++) {
            hash = getHash(hash);
            xor = xor(xor, hash);
        }

        return xor;
    }

    public AesGcmResult aesGcmEncrypt(byte[] plainText, byte[] key) {
        return aesGcmEncrypt(plainText, key, new byte[12], null);
    }

    public AesGcmResult aesGcmEncrypt(byte[] plainText, byte[] key, byte[] iv, byte[] aad) {
        AEADParameters parameters = new AEADParameters(new KeyParameter(key), 128, iv, aad);

        mAesGcmEngine.init(true, parameters);

        byte[] encMsg = new byte[mAesGcmEngine.getOutputSize(plainText.length)];
        int encLen = mAesGcmEngine.processBytes(plainText, 0, plainText.length, encMsg, 0);

        try {
            mAesGcmEngine.doFinal(encMsg, encLen);

            byte[] tag = mAesGcmEngine.getMac();
            encMsg = Arrays.copyOf(encMsg, encMsg.length - tag.length);

            return new AesGcmResult(encMsg, tag);
        } catch (InvalidCipherTextException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public byte[] aesGcmDecrypt(AesGcmResult result, byte[] key) {
        return aesGcmDecrypt(result.getEncryptedMessage(), result.getTag(), key);
    }

    public byte[] aesGcmDecrypt(byte[] cipherText, byte[] tag, byte[] key) {
        return aesGcmDecrypt(cipherText, tag, key, new byte[12], null);
    }

    public byte[] aesGcmDecrypt(byte[] cipherText, byte[] tag, byte[] key, byte[] iv, byte[] aad) {
        AEADParameters parameters = new AEADParameters(new KeyParameter(key), 128, iv, aad);
        mAesGcmEngine.init(false, parameters);

        byte[] text = Arrays.copyOf(cipherText, cipherText.length + tag.length);
        System.arraycopy(tag, 0, text, cipherText.length, tag.length);

        byte[] decMsg = new byte[mAesGcmEngine.getOutputSize(text.length)];
        int decLen = mAesGcmEngine.processBytes(text, 0, text.length, decMsg, 0);

        try {
            mAesGcmEngine.doFinal(decMsg, decLen);
            return decMsg;
        } catch (InvalidCipherTextException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static class AesGcmResult {

        private final byte[] mEncryptedMessage;
        private final byte[] mTag;

        public AesGcmResult(byte[] encryptedMessage, byte[] tag) {
            mEncryptedMessage = encryptedMessage;
            mTag = tag;
        }

        public byte[] getEncryptedMessage() {
            return mEncryptedMessage;
        }

        public byte[] getTag() {
            return mTag;
        }
    }

    private static String bin2hex(byte[] data) {
        // http://stackoverflow.com/questions/7166129/how-can-i-calculate-the-sha-256-hash-of-a-string-in-android
        return String.format("%0" + (data.length * 2) + 'x', new BigInteger(1, data));
    }

    private static byte[] xor(byte[] array1, byte[] array2) {
        if (array1 == null || array2 == null || array1.length != array2.length) {
            throw new IllegalArgumentException("arrays must have the same length");
        }

        byte[] result = new byte[array1.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (array1[i] ^ array2[i]);
        }

        return result;
    }
}
