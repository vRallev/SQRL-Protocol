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
package net.vrallev.java.sqrl.ecc;

import net.vrallev.java.ecc.Ecc25519Helper;
import net.vrallev.java.ecc.KeyHolder;
import net.vrallev.java.ecc.fast.Ecc25519HelperFast;
import net.vrallev.java.ecc.fast.KeyHolderFast;

/**
 * @author Ralf Wondratschek
 */
public class JavaEccProvider implements EccProvider25519 {

    private final boolean mFast;
    private final Ecc25519Helper mHelper;

    public JavaEccProvider(boolean fast) {
        mFast = fast;
        mHelper = fast ? new Ecc25519HelperFast() : new Ecc25519Helper();
    }

    @Override
    public EccKeyPair computeKeyPair(byte[] privateKey) {
        KeyHolder keyHolder = mFast ? new KeyHolderFast(privateKey) : new KeyHolder(privateKey);
        return new EccKeyPair(keyHolder.getPrivateKey(), keyHolder.getPublicKeySignature(), keyHolder.getPublicKeyDiffieHellman());
    }

    @Override
    public boolean isValidSignature(byte[] message, byte[] signature, byte[] publicKey) {
        return mHelper.isValidSignature(message, signature, publicKey);
    }

    @Override
    public byte[] sign(byte[] message, byte[] privateKey, byte[] publicKey) {
        return mHelper.sign(message, privateKey, publicKey);
    }

    @Override
    public byte[] diffieHellman(byte[] privateKey, byte[] publicKey) {
        return mHelper.diffieHellman(privateKey, publicKey);
    }
}
