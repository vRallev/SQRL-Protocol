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
 * The library requires one instance to handle all elliptic curve operations.
 *
 * @author Ralf Wondratschek
 */
public interface EccProvider25519 {

    /**
     * Computes a key pair for the given private key.
     */
    public EccKeyPair computeKeyPair(byte[] privateKey);

    /**
     * @param message the original message of the signature.
     * @param signature corresponding to the #message.
     * @param publicKey the public key to check the #signature.
     * @return {@code true} if the signature is valid.
     */
    public boolean isValidSignature(byte[] message, byte[] signature, byte[] publicKey);

    /**
     * @param message the message, which should be signed.
     * @param privateKey the private key used to compute the signature.
     * @param publicKey the public key used to compute the signature (some external ECC libraries
     *                  may require the public key.
     * @return the signature.
     */
    public byte[] sign(byte[] message, byte[] privateKey, byte[] publicKey);

    /**
     * @return the shared secret
     */
    public byte[] diffieHellman(byte[] privateKey, byte[] publicKey);
}
