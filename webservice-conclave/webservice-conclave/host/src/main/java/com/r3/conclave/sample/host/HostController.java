package com.r3.conclave.sample.host;

import com.r3.conclave.common.OpaqueBytes;
import com.r3.conclave.host.EnclaveHost;
import com.r3.conclave.host.EnclaveLoadException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicReference;

@RestController
public class HostController {
    EnclaveHost enclave = null;
    AtomicReference<byte[]> requestToDeliver = new AtomicReference<>();

    @GetMapping(path = "/", produces = "application/json")
    public String status() {
        return (enclave == null) ? "Not initialized yet." : "Running";
    }

    // Initilize and get attestation.
    @GetMapping(path = "/enclaveinstance")
    public byte[] getEnclaveInstanceInfo() throws EnclaveLoadException {
        // to avoid twice initiate
        if (enclave != null) {
            return enclave.getEnclaveInstanceInfo().serialize();
        }

        try {
            EnclaveHost.checkPlatformSupportsEnclaves(true);
            System.out.println("This platform supports enclaves in simulation, debug and release mode.");
        } catch (EnclaveLoadException e) {
            System.out.println("This platform currently only supports enclaves in simulation mode: " + e.getMessage());
        }

        // Load our enclave
        enclave = EnclaveHost.load("com.r3.conclave.sample.enclave.DiabetesEnclave");

        OpaqueBytes spid = new OpaqueBytes(new byte[16]);
        String attestationKey = "any-key";

        // Start the Enclave.
        enclave.start(spid, attestationKey, new EnclaveHost.MailCallbacks() {
            @Override
            public void postMail(byte[] encryptedBytes, String routingHint) {
                requestToDeliver.set(encryptedBytes);
            }
        });

        return enclave.getEnclaveInstanceInfo().serialize();
    }

    // Get latest algo
    @PostMapping(path = "/algorithm")
    public byte[] algorithm(@RequestBody byte[] data) {
        return deliverMail(data);
    }

    // The below POST endpoints will accept encrypted data from client and send to enclave directly
    @PostMapping(path = "/training")
    public byte[] training(@RequestBody byte[] dataSet) {
		return deliverMail(dataSet);
    }

    @PostMapping(path = "/predict")
    public byte[] predict(@RequestBody byte[] algoAndData) {
		return deliverMail(algoAndData);
    }

    private byte[] deliverMail(byte[] in){
		enclave.deliverMail(1, in);
		byte[] out = requestToDeliver.get();
		return out;
	}
}