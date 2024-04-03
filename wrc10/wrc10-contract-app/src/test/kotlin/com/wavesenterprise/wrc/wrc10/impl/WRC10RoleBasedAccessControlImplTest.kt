package com.wavesenterprise.wrc.wrc10.impl

import com.wavesenterprise.sdk.contract.api.domain.DefaultContractCall
import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.api.state.TypeReference
import com.wavesenterprise.sdk.contract.test.state.ContractTestStateFactory
import com.wavesenterprise.sdk.node.domain.Address
import com.wavesenterprise.sdk.node.test.data.TestDataFactory.Companion.contractTransaction
import com.wavesenterprise.sdk.node.test.data.Util.Companion.randomStringBase58
import com.wavesenterprise.wrc.wrc10.DefaultPermissions
import com.wavesenterprise.wrc.wrc10.StateMappings
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class WRC10RoleBasedAccessControlImplTest {

    lateinit var state: ContractState

    private val owner = randomStringBase58()
    private val admin = randomStringBase58()
    private val testAddress = randomStringBase58()

    companion object {
        private const val TEST_ROLE = "TEST_ROLE"
        private const val ANOTHER_TEST_ROLE = "ANOTHER_TEST_ROLE"
    }

    @BeforeEach
    fun init() {
        state = ContractTestStateFactory.state()
    }

    @Test
    fun `should store contract owner`() {
        asContractUser(owner) {
            init()

            assertTrue(isOwner(owner))
            assertTrue(state["${StateMappings.PERMISSIONS}_$owner", object : TypeReference<Set<String>>() {}].isEmpty())
            assertTrue(state["${StateMappings.ROLE_ADMINS}_$owner", object : TypeReference<Set<String>>() {}].isEmpty())
        }
    }

    @Test
    fun `owner should be able to grant role`() {
        asContractUser(owner) {
            init()
            grant(testAddress, TEST_ROLE)

            assertTrue(hasPermission(testAddress, TEST_ROLE))
            assertFalse(isRoleAdmin(testAddress, TEST_ROLE))
        }
    }

    @Test
    fun `owner should be able to grant role admin`() {
        asContractUser(owner) {
            init()
            grantRoleAdmin(testAddress, TEST_ROLE)

            assertFalse(hasPermission(testAddress, TEST_ROLE))
            assertTrue(isRoleAdmin(testAddress, TEST_ROLE))
        }
    }

    @Test
    fun `admin should be able to grant role`() {
        asContractUser(owner) {
            init()
            grant(admin, DefaultPermissions.ADMIN)
        }

        asContractUser(admin) {
            grant(testAddress, TEST_ROLE)

            assertTrue(hasPermission(testAddress, TEST_ROLE))
            assertFalse(isRoleAdmin(testAddress, TEST_ROLE))
        }
    }

    @Test
    fun `admin should be able to grant role admin`() {
        asContractUser(owner) {
            init()
            grant(admin, DefaultPermissions.ADMIN)
        }

        asContractUser(admin) {
            grantRoleAdmin(testAddress, TEST_ROLE)

            assertFalse(hasPermission(testAddress, TEST_ROLE))
            assertTrue(isRoleAdmin(testAddress, TEST_ROLE))
        }
    }

    @Test
    fun `role admin should be able to grant role`() {
        asContractUser(owner) {
            init()
            grantRoleAdmin(admin, TEST_ROLE)
        }

        asContractUser(admin) {
            grant(testAddress, TEST_ROLE)

            assertTrue(hasPermission(testAddress, TEST_ROLE))
            assertFalse(isRoleAdmin(testAddress, TEST_ROLE))
        }
    }

    @Test
    fun `role admin should be able to grant role admin`() {
        asContractUser(owner) {
            init()
            grantRoleAdmin(admin, TEST_ROLE)
        }

        asContractUser(admin) {
            grantRoleAdmin(testAddress, TEST_ROLE)

            assertFalse(hasPermission(testAddress, TEST_ROLE))
            assertTrue(isRoleAdmin(testAddress, TEST_ROLE))
        }
    }

    @Test
    fun `should not granted role admin to all address roles`() {
        asContractUser(owner) {
            init()
            grant(testAddress, TEST_ROLE)
            grantRoleAdmin(testAddress, ANOTHER_TEST_ROLE)

            assertTrue(hasPermission(testAddress, TEST_ROLE))
            assertFalse(isRoleAdmin(testAddress, TEST_ROLE))

            assertFalse(hasPermission(testAddress, ANOTHER_TEST_ROLE))
            assertTrue(isRoleAdmin(testAddress, ANOTHER_TEST_ROLE))
        }
    }

    @Test
    fun `user without roles should not be able to grant role and role admin`() {
        asContractUser(owner) {
            init()
        }

        asContractUser(testAddress) {
            assertThrows<IllegalArgumentException> {
                grant(testAddress, TEST_ROLE)
            }
            assertThrows<IllegalArgumentException> {
                grantRoleAdmin(testAddress, TEST_ROLE)
            }
        }
    }

    @Test
    fun `user with role should not be able to grant role and role admin`() {
        asContractUser(owner) {
            init()

            grant(testAddress, TEST_ROLE)
        }

        asContractUser(testAddress) {
            assertThrows<IllegalArgumentException> {
                grant(testAddress, TEST_ROLE)
            }
            assertThrows<IllegalArgumentException> {
                grantRoleAdmin(testAddress, TEST_ROLE)
            }
        }
    }

    @Test
    fun `user with one role admin should not be able to grant another role admin`() {
        asContractUser(owner) {
            init()

            grantRoleAdmin(admin, TEST_ROLE)
        }

        asContractUser(admin) {
            assertThrows<IllegalArgumentException> {
                grant(testAddress, ANOTHER_TEST_ROLE)
            }
            assertThrows<IllegalArgumentException> {
                grantRoleAdmin(testAddress, ANOTHER_TEST_ROLE)
            }
        }
    }

    @Test
    fun `user with role and another role admin should not be able to grant role and role admin`() {
        asContractUser(owner) {
            init()

            grant(admin, TEST_ROLE)
            grantRoleAdmin(admin, ANOTHER_TEST_ROLE)
        }

        asContractUser(admin) {
            assertThrows<IllegalArgumentException> {
                grant(testAddress, TEST_ROLE)
            }
            assertThrows<IllegalArgumentException> {
                grantRoleAdmin(testAddress, TEST_ROLE)
            }
        }
    }

    private fun asContractUser(
        sender: String,
        block: WRC10RoleBasedAccessControlImpl.() -> Unit,
    ) {
        WRC10RoleBasedAccessControlImpl(
            state = state,
            call = DefaultContractCall(
                tx = contractTransaction(sender = Address.fromBase58(sender))
            ),
        ).apply {
            block(this)
        }
    }
}
