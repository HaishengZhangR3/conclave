package com.r3.conclave.sample.enclave;

import com.r3.conclave.host.EnclaveLoadException;
import com.r3.conclave.testing.MockHost;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the enclave fully in-memory in a mock environment.
 */
public class ReverseEnclaveTest {
    @Test
    void reverseNumber() throws EnclaveLoadException {
        MockHost<DiabetesEnclave> mockHost = MockHost.loadMock(DiabetesEnclave.class);
        mockHost.start(null, null, null);
        DiabetesEnclave diabetesEnclave = mockHost.getEnclave();


        byte[] response = mockHost.callEnclave("1234".getBytes());
        assertNotNull(response);
        assertEquals("4321", new String(response));

    }
}
