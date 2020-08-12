package com.template.flows

import com.r3.conclave.client.EnclaveConstraint
import com.r3.conclave.client.InvalidEnclaveException
import com.r3.conclave.common.EnclaveInstanceInfo
import com.r3.conclave.mail.Curve25519KeyPairGenerator
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


object ConclaveConnector {

    val enclaveHost = "http://localhost:9999"
    val infoPoint = "/enclaveinstance"
    val trainingPoint = "/training"
    val predictPoint = "/predict"
    val algorithmPoint = "/algorithm"

    fun getEnclave(): EnclaveInstanceInfo? {

        var attestation: EnclaveInstanceInfo? = null
        val url = URL(enclaveHost + infoPoint)
        val getConn = url.openConnection() as HttpURLConnection
        try {
            getConn.setRequestMethod("GET")
            val buf = ByteArray(getConn.getInputStream().available())
            getConn.getInputStream().read(buf)
            attestation = EnclaveInstanceInfo.deserialize(buf)
            EnclaveConstraint.parse("S:4924CA3A9C8241A3C0AA1A24A407AA86401D2B79FA9FF84932DA798A942166D4 PROD:1 SEC:INSECURE").check(attestation)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InvalidEnclaveException) {
            e.printStackTrace()
        } finally {
            getConn.disconnect()
        }
        return attestation
    }

    fun postData(postEndpoint: String, enclave: EnclaveInstanceInfo, topic: String, sequenceNumber: Int, data: ByteArray): ByteArray {

        val myKey = Curve25519KeyPairGenerator().generateKeyPair()

        val mail = enclave.createMail(data)
        mail.sequenceNumber = sequenceNumber.toLong()
        mail.privateKey = myKey.private
        mail.topic = topic

        val encryptedMail = mail.encrypt()

        // Create a POST request to send the encrypted byte[] to Host server
        val url = URL(postEndpoint)
        val postConn = url.openConnection() as HttpURLConnection
        postConn.requestMethod = "POST"
        postConn.setRequestProperty("Content-Type", "image/jpeg")
        postConn.doOutput = true
        postConn.outputStream.use { os -> os.write(encryptedMail, 0, encryptedMail.size) }

        val response : ByteArray = try {
            val encryptedReply = ByteArray(postConn.inputStream.available())
            postConn.inputStream.read(encryptedReply)

            val replyMail = enclave.decryptMail(encryptedReply, myKey.private)
            replyMail.bodyAsBytes
        } catch (e: Exception) {
            e.printStackTrace()
            e.message!!.toByteArray()
        } finally {
            postConn.disconnect()
        }
        return response
    }
}