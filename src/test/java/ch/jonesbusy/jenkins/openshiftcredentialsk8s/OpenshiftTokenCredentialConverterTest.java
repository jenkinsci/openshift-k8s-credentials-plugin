/*
 * The MIT License
 *
 * Copyright 2022 jonesbusy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ch.jonesbusy.jenkins.openshiftcredentialsk8s;

import com.cloudbees.jenkins.plugins.kubernetes_credentials_provider.CredentialsConvertionException;
import com.openshift.jenkins.plugins.OpenShiftTokenCredentials;
import hudson.util.HistoricalSecrets;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.utils.Serialization;
import jenkins.security.ConfidentialStore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link OpenshiftTokenCredentialConverter}.
 */
class OpenshiftTokenCredentialConverterTest {

    private static MockedStatic<ConfidentialStore> confidentialStore;
    private static MockedStatic<HistoricalSecrets> historicalSecrets;

    @BeforeAll
    static void beforeAll() {
        confidentialStore = Mockito.mockStatic(ConfidentialStore.class);
        historicalSecrets = Mockito.mockStatic(HistoricalSecrets.class);
    }

    @AfterAll
    static void afterAll() {
        confidentialStore.close();
        historicalSecrets.close();
    }

    @BeforeEach
    void setUp() {
        ConfidentialStore csMock = Mockito.mock(ConfidentialStore.class);
        Mockito.when(ConfidentialStore.get()).thenReturn(csMock);
        Mockito.when(csMock.randomBytes(ArgumentMatchers.anyInt())).thenAnswer(it -> new byte[(Integer) (it.getArguments()[0])]);
    }

    @Test
    void canConvert() {
        OpenshiftTokenCredentialConverter converter = new OpenshiftTokenCredentialConverter();
        assertThat("correct registration of valid type", converter.canConvert("openshiftToken"), is(true));
        assertThat("incorrect type is rejected", converter.canConvert("something"), is(false));
    }

    @Test
    void failsToConvertASecretMissingText() {
        OpenshiftTokenCredentialConverter converter = new OpenshiftTokenCredentialConverter();
        assertThrows(CredentialsConvertionException.class, () -> {
            try (InputStream is = get("missing-text.yaml")) {
                Secret secret = Serialization.unmarshal(is, Secret.class);
                assertThat("The Secret was loaded correctly from disk", notNullValue());
                converter.convert(secret);
            }
        });
    }

    @Test
    void failsToConvertWithNonBase64EncodedText() {
        OpenshiftTokenCredentialConverter converter = new OpenshiftTokenCredentialConverter();
        assertThrows(CredentialsConvertionException.class, () -> {
            try (InputStream is = get("text-isnt-base64.yaml")) {
                Secret secret = Serialization.unmarshal(is, Secret.class);
                assertThat("The Secret was loaded correctly from disk", notNullValue());
                converter.convert(secret);
            }
        });
    }

    @Test
    void canConvertAValidSecret() throws Exception {
        ConfidentialStore.get();
        OpenshiftTokenCredentialConverter converter = new OpenshiftTokenCredentialConverter();
        try (InputStream is = get("valid.yaml")) {
            Secret secret = Serialization.unmarshal(is, Secret.class);
            assertThat("The Secret was loaded correctly from disk", notNullValue());

            OpenShiftTokenCredentials credential = converter.convert(secret);
            assertThat(credential, notNullValue());

            assertThat("credential id is mapped correctly", credential.getId(), is("a-test-secret"));
            assertThat("credential description is mapped correctly", credential.getDescription(), is("secret openshift token credential from Kubernetes"));
            assertThat("credential text mapped to the secret", credential.getSecret().getPlainText(), is("someSuperDuperSecret"));
        }
    }

    private static InputStream get(String resource) {
        InputStream is = OpenshiftTokenCredentialConverterTest.class.getResourceAsStream(resource);
        assertNotNull(is, "failed to load resource " + resource);
        return is;
    }
}