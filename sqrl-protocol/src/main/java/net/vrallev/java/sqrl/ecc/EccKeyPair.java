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

/**
 * Represents a key pair for elliptic curve cryptography with Curve25519. Notice that for a given
 * private key the public keys may differ. For a signature you need to pass a Curve25519 public key
 * and for diffie hellman you need to pass an Ed25519 public key.
 *
 * @author Ralf Wondratschek
 */
public class EccKeyPair {

    private final byte[] mPrivateKey;
    private final byte[] mPublicKeySignature;
    private final byte[] mPublicKeyDiffieHellman;

    public EccKeyPair(byte[] privateKey, byte[] publicKeySignature, byte[] publicKeyDiffieHellman) {
        mPrivateKey = privateKey;
        mPublicKeySignature = publicKeySignature;
        mPublicKeyDiffieHellman = publicKeyDiffieHellman;
    }

    public byte[] getPrivateKey() {
        return mPrivateKey;
    }

    public byte[] getPublicKeySignature() {
        return mPublicKeySignature;
    }

    public byte[] getPublicKeyDiffieHellman() {
        return mPublicKeyDiffieHellman;
    }
}
