package com.wavesenterprise.wrc.wrc20.impl

import com.wavesenterprise.sdk.contract.api.domain.DefaultContractCall
import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.test.state.ContractTestStateFactory
import com.wavesenterprise.sdk.node.domain.Address
import com.wavesenterprise.sdk.node.test.data.TestDataFactory.Companion.contractTransaction
import com.wavesenterprise.sdk.node.test.data.Util.Companion.randomStringBase58
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled // ToDo: Implement convertFromBoolean() in JacksonFromDataEntryConverter WTCH-186
class WRC20FTokenContractImplTest {
    lateinit var state: ContractState
    private val a = randomStringBase58()
    private val b = randomStringBase58()
    private val c = randomStringBase58()

    @BeforeEach
    fun init() {
        state = ContractTestStateFactory.state()
    }

    @Test
    fun `should issue tokens`() {
        asContractUser(a) {
            create("ERC20-Test")
            mint(b, 300L)
            assertEquals(balanceOf(b), 300L)
        }
    }

    @Test
    fun `should be able to burn tokens`() {
        asContractUser(a) {
            create("ERC20-Test")
            mint(b, 300L)
        }
        asContractUser(b) {
            burn(100L)
            assertEquals(balanceOf(b), 200L)
        }
    }

    @Test
    fun `should not be able to mint tokens as non minter`() {
        asContractUser(a) {
            create("ERC20-Test")
        }
        asContractUser(b) {
            Assertions.assertThrows(IllegalArgumentException::class.java) {
                mint(b, 300L)
            }
        }
    }

    @Test
    fun `should be able grant mint tokens as admin`() {
        asContractUser(a) {
            create("ERC20-Test")
            grant(b, "MINTER")
        }
        asContractUser(b) {
            mint(b, 300L)
            assertEquals(balanceOf(b), 300L)
        }
    }

    @Test
    fun `should be able to grant minter admin tokens as admin`() {
        asContractUser(a) {
            create("ERC20-Test")
            grantRoleAdmin(b, "MINTER")
        }
        asContractUser(b) {
            grant(c, "MINTER")
        }
        asContractUser(c) {
            mint(c, 300L)
            assertEquals(balanceOf(c), 300L)
        }
    }

    @Test
    fun `should be able transfer own tokens`() {
        asContractUser(a) {
            create("ERC20-Test")
            mint(a, 300L)
            transfer(b, 100L)
            assertEquals(balanceOf(a), 200L)
            assertEquals(balanceOf(b), 100L)
        }
    }

    @Test
    fun `should be able to delegate transfer of own tokens via approve`() {
        asContractUser(a) {
            create("ERC20-Test")
            mint(a, 300L)
            approve(b, 300L)
            assertEquals(allowance(a, b), 300L)
        }
        asContractUser(b) {
            transferFrom(a, c, 100L)
            assertEquals(balanceOf(a), 200L)
            assertEquals(balanceOf(c), 100L)
            assertEquals(allowance(a, b), 200L)
        }
    }

    private fun asContractUser(
        sender: String,
        block: WRC20FTokenContractImpl.() -> Unit,
    ) {
        WRC20FTokenContractImpl(
            state = state,
            call = DefaultContractCall(
                tx = contractTransaction(sender = Address.fromBase58(sender))
            )
        ).apply {
            block(this)
        }
    }
}
