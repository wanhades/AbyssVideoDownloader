package com.abmo.util

import com.google.gson.Gson
import model.Video
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class CryptoHelper(
    private val javaScriptRunner: JavaScriptRunner
) {

    fun decodeEncryptedString(encryptedInput: String?): Video? {
        if (encryptedInput != null) {
            var sanitizedInput = encryptedInput
            val decryptionKey = "RB0fpH8ZEyVLkv7c2i6MAJ5u3IKFDxlS1NTsnGaqmXYdUrtzjwObCgQP94hoeW+/="
            var decodedString = ""
            var index = 0
            sanitizedInput = sanitizedInput.replace(Regex("[^A-Za-z0-9+/=]"), "")
            while (index < sanitizedInput.length) {
                val firstCharValue =
                    (decryptionKey.indexOf(sanitizedInput[index++]) shl 2) or (decryptionKey.indexOf(sanitizedInput[index])
                        .shr(4))
                val secondCharValue = decryptionKey.indexOf(sanitizedInput[index++])
                val thirdCharValue =
                    ((0xf and secondCharValue) shl 4) or (decryptionKey.indexOf(sanitizedInput[index]).shr(2))
                val fourthCharCode = decryptionKey.indexOf(sanitizedInput[index++])
                val fifthCharCode = ((0x3 and fourthCharCode) shl 6) or (decryptionKey.indexOf(sanitizedInput[index++]))
                decodedString += firstCharValue.toChar()
                if (fourthCharCode != 0x40) decodedString += thirdCharValue.toChar()
                if (fifthCharCode != 0x40) decodedString += fifthCharCode.toChar()
            }
            return Gson().fromJson(decodeUtf8String(decodedString), Video::class.java)
        } else {
            return null
        }
    }

    private fun decodeUtf8String(input: String): String {
        var result = ""
        var i = 0
        while (i < input.length) {
            val charCode = input[i].code
            if (charCode < 0x80) {
                result += charCode.toChar()
                i++
            } else if (charCode in 0xc0..0xdf) {
                val nextCharCode = input[i + 1].code
                result += (((charCode and 0x1f) shl 6) or (nextCharCode and 0x3f)).toChar()
                i += 2
            } else {
                val nextCode = input[i + 1].code
                val thirdCharCode = input[i + 2].code
                result += (((charCode and 0xf) shl 12) or ((nextCode and 0x3f) shl 6) or (thirdCharCode and 0x3f)).toChar()
                i += 3
            }
        }
        return result
    }


    private fun initCipher(mode: Int, key: String): Cipher {
        val keyBytes = key.toByteArray(StandardCharsets.UTF_8)
        val iv = keyBytes.sliceArray(0 until 16)
        val secretKey = SecretKeySpec(keyBytes, "AES")
        val cipher = Cipher.getInstance("AES/CTR/NoPadding")
        val ivSpec = IvParameterSpec(iv)
        cipher.init(mode, secretKey, ivSpec)
        return cipher
    }

    fun encryptAESCTR(data: String?, key: String): String {
        val cipher = initCipher(Cipher.ENCRYPT_MODE, key)
        val dataBytes = data?.toByteArray(StandardCharsets.UTF_8)
        val encryptedBytes = cipher.doFinal(dataBytes)
        return String(encryptedBytes, Charsets.ISO_8859_1)
    }


    fun decryptAESCTR(data: ByteArray, key: String): ByteArray {
        val cipher = initCipher(Cipher.DECRYPT_MODE, key)
        return cipher.doFinal(data)
    }

    fun getKey(value: Any?): String {
        return javaScriptRunner.runJavaScriptCode("keyGenerator.js", "generateKey", value)
    }

}