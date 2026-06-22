package com.sena.backend.encryption;

import org.springframework.stereotype.Service;

/**
 * EncryptionService skeleton.
 * Important notes (commented):
 * - Use an external KMS (Vault/AWS KMS) to store/rotate master keys.
 * - Derive data-encryption-keys (DEKs) per-record if desired and encrypt DEKs with KMS (envelope encryption).
 * - Use AES-GCM with 256-bit keys. Store only: ciphertext (with tag), iv/nonce, alg, kid.
 * - Never log plaintext or keys. Decryption occurs in-memory and should be audited.
 */
@Service
public class EncryptionService {

    // Placeholder methods. Implement using javax.crypto.Cipher (AES/GCM) and secure key retrieval.

    public byte[] encrypt(byte[] plaintext, byte[] key, byte[] iv) {
        throw new UnsupportedOperationException("Implement AES-GCM encryption using KMS-managed keys");
    }

    public byte[] decrypt(byte[] ciphertext, byte[] key, byte[] iv) {
        throw new UnsupportedOperationException("Implement AES-GCM decryption using KMS-managed keys");
    }
}
