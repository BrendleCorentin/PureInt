package izly;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PurseIntegration {

    private String validCode;
    private String invalidCode;
    private Purse purse;
    private CodeSecret codeSecret;

    @BeforeEach
    public void setUp() {
        codeSecret = (CodeSecret) CodeSecretFactory.createCodeSecret(true);
        validCode = "8888";
        invalidCode = "1234";
        purse = new Purse(150.0, (izly.CodeSecret) codeSecret, 10);
    }

    @Test
    public void testWithdrawAcceptedWithValidCode() throws Exception {
        purse.withdrawFunds(10, validCode);
        assertEquals(140, purse.getBalance());
    }

    @Test
    public void testAddFundsAcceptedWithValidCode() throws Exception {
        purse.addFunds(50);
        assertEquals(200, purse.getBalance());
    }

    @Test
    public void testRevealCodeWhenNotBlocked() {
        assertEquals("8888", codeSecret.revelerCode());
    }

    @Test
    public void testRevealCodeWhenBlocked() throws CodeBlockedException {
        codeSecret.verifierCode(invalidCode);
        codeSecret.verifierCode(invalidCode);
        codeSecret.verifierCode(invalidCode);
        assertEquals("xxxx", codeSecret.revelerCode());
    }

    @Test
    public void testIsBlockedAfterMultipleInvalidAttempts() throws CodeBlockedException {
        assertFalse(codeSecret.isBlocked());
        codeSecret.verifierCode(invalidCode);
        codeSecret.verifierCode(invalidCode);
        codeSecret.verifierCode(invalidCode);
        assertTrue(codeSecret.isBlocked());
    }



    @Test
    public void testWithdrawRejectedWithInvalidCode() {
        assertThrows(MauvaisCodeException.class, () -> purse.withdrawFunds(10, invalidCode));
    }

    @Test
    public void testAddFundsRejectedWhenCodeBlocked() throws Exception {
        codeSecret.verifierCode(invalidCode);
        codeSecret.verifierCode(invalidCode);
        codeSecret.verifierCode(invalidCode);
        assertThrows(CodeBloqueException.class, () -> purse.addFunds(10));
    }

    @Test
    public void testWithdrawRejectedWhenCodeBlocked() throws Exception {
        codeSecret.verifierCode(invalidCode);
        codeSecret.verifierCode(invalidCode);
        codeSecret.verifierCode(invalidCode);
        assertThrows(CodeBloqueException.class, () -> purse.withdrawFunds(10, validCode));
    }

    @Test
    public void testBalanceNeverGoesNegative() {
        assertThrows(MauvaisMontantException.class, () -> purse.withdrawFunds(200, validCode));
    }

    @Test
    public void testBalanceDoesNotExceedLimitWithAddFunds() throws Exception {
        purse.addFunds(100);
        assertThrows(MauvaisMontantException.class, () -> purse.addFunds(51));
    }

    @Test
    public void testBalanceReachesLimitWithAddFunds() throws Exception {
        purse.addFunds(60);
        assertEquals(210, purse.getBalance());
    }


    public static class CodeSecretFactory {
        public static CodeSecret createCodeSecret(boolean useStub) {
            return useStub ? new CodeSecretStub() : null;
        }
    }

    private interface CodeSecret {
        Boolean verifierCode(String code) throws CodeBlockedException;
        Boolean isBlocked();
        String revelerCode();
    }

    private static class CodeSecretStub implements CodeSecret {
        private final String code = "8888";
        private boolean isBlocked = false;

        @Override
        public Boolean verifierCode(String code) {
            isBlocked = !this.code.equals(code) || isBlocked;
            return this.code.equals(code);
        }

        @Override
        public Boolean isBlocked() {
            return isBlocked;
        }

        @Override
        public String revelerCode() {
            return isBlocked ? "xxxx" : code;
        }
    }
}
