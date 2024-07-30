package com.wavesenterprise.sdk.wrc.wrc13.impl

import com.wavesenterprise.sdk.contract.api.domain.DefaultContractCall
import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.test.state.ContractTestStateFactory
import com.wavesenterprise.sdk.node.domain.Address
import com.wavesenterprise.sdk.node.test.data.TestDataFactory.Companion.contractTransaction
import com.wavesenterprise.sdk.node.test.data.Util.Companion.randomStringBase58
import com.wavesenterprise.sdk.wrc.wrc10.DefaultPermissions
import com.wavesenterprise.sdk.wrc.wrc10.impl.hasPermission
import com.wavesenterprise.sdk.wrc.wrc10.impl.isOwner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class WRC13RegistryContractImplTest {
    private lateinit var state: ContractState
    private val owner = randomStringBase58()

    @BeforeEach
    fun init() {
        state = ContractTestStateFactory.state()
    }

    @Test
    fun `should store contract owner and role admin`() {
        asContractUser(owner) {
            create()

            assertTrue(rbac.isOwner(owner))
            assertTrue(rbac.hasPermission(owner, DefaultPermissions.ADMIN))
        }
    }

    @Test
    fun `should set value`() {
        asContractUser(owner) {
            create()
            setValue("key", "value")

            assertEquals("value", data["key"])
        }
    }

    @Test
    fun `should update value`() {
        asContractUser(owner) {
            create()
            setValue("key", "value1")
        }
        asContractUser(owner) {
            setValue("key", "value2")
            assertEquals("value2", data["key"])
        }
    }

    @Test
    fun `should throw exception when set value without role`() {
        asContractUser(owner) {
            create()
        }
        asContractUser(randomStringBase58()) {
            assertThrows<IllegalArgumentException> {
                setValue("key", "value")
            }
        }
    }

    private fun asContractUser(
        sender: String,
        block: WRC13RegistryContractImpl.() -> Unit,
    ) {
        WRC13RegistryContractImpl(
            state = state,
            call = DefaultContractCall(
                tx = contractTransaction(sender = Address.fromBase58(sender)),
            ),
        ).apply {
            block(this)
        }
    }
}
