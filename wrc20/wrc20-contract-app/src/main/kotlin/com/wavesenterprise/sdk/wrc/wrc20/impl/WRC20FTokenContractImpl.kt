package com.wavesenterprise.sdk.wrc.wrc20.impl

import com.wavesenterprise.sdk.contract.api.annotation.ContractHandler
import com.wavesenterprise.sdk.contract.api.domain.ContractCall
import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping
import com.wavesenterprise.sdk.wrc.wrc10.WRC10RoleBasedAccessControl
import com.wavesenterprise.sdk.wrc.wrc10.impl.WRC10RoleBasedAccessControlImpl
import com.wavesenterprise.sdk.wrc.wrc10.impl.hasPermission
import com.wavesenterprise.sdk.wrc.wrc20.KEY_BURNABLE
import com.wavesenterprise.sdk.wrc.wrc20.KEY_CONTRACT_ID
import com.wavesenterprise.sdk.wrc.wrc20.KEY_DECIMALS
import com.wavesenterprise.sdk.wrc.wrc20.KEY_MINTABLE
import com.wavesenterprise.sdk.wrc.wrc20.KEY_NAME
import com.wavesenterprise.sdk.wrc.wrc20.KEY_TOTAL_SUPPLY
import com.wavesenterprise.sdk.wrc.wrc20.MAPPING_ALLOWANCES
import com.wavesenterprise.sdk.wrc.wrc20.MAPPING_BALANCES
import com.wavesenterprise.sdk.wrc.wrc20.PERM_MINTER
import com.wavesenterprise.sdk.wrc.wrc20.WRC20FTokenContract

@Suppress("TooManyFunctions")
@ContractHandler
class WRC20FTokenContractImpl private constructor(
    private val state: ContractState,
    private val call: ContractCall,
    private val rbac: WRC10RoleBasedAccessControlImpl,
    private val balance: Mapping<Long>,
    private val allowances: Mapping<Long>,
) : WRC20FTokenContract, WRC10RoleBasedAccessControl by rbac {
//    AsyncContract by AsyncContractImpl(state) ToDo: Extend AsyncContractImpl WTCH-186, WTCH-187

    // TODO: 02.02.2021 Java reflection not works well with kotlin default arguments
    // see https://stackoverflow.com/questions/46245206/how-can-i-instantiate-an-object-using-default-constructor-parameter-values-in-ko
    constructor(
        state: ContractState,
        call: ContractCall,
    ) : this(
        state,
        call,
        WRC10RoleBasedAccessControlImpl(state, call),
        state.getMapping(Long::class.java, MAPPING_BALANCES),
        state.getMapping(Long::class.java, MAPPING_ALLOWANCES),
    )

    override fun create(name: String) {
        createFull(
            name = name,
            burnable = true,
            mintable = true,
            supply = 0,
            supplyHolder = "",
            decimals = 2,
        )
    }

    override fun createFull(
        name: String,
        burnable: Boolean,
        mintable: Boolean,
        supply: Long,
        supplyHolder: String,
        decimals: Int,
    ) {
        state.put(KEY_NAME, name)
        state.put(KEY_TOTAL_SUPPLY, 0L)
        state.put(KEY_DECIMALS, decimals)
        state.put(KEY_MINTABLE, mintable)
        state.put(KEY_BURNABLE, burnable)
        state.put(KEY_CONTRACT_ID, call.id)

        rbac.init()
        if (mintable) {
            rbac.grant(call.caller, PERM_MINTER)
        }

        if (supply > 0) {
            mint(supplyHolder, supply)
        }
    }

    override fun transfer(to: String, amount: Long) {
        require(balanceOf(call.caller) >= amount) { "INSUFFICIENT_BALANCE" }
        modifyBalance(call.caller) { it - amount }
        modifyBalance(to) { it + amount }
    }

    override fun approve(to: String, amount: Long) {
        modifyAllowance(call.caller, to) { amount }
    }

    override fun transferFrom(from: String, to: String, amount: Long) {
        require(allowance(from, call.caller) >= amount) { "INSUFFICIENT_ALLOWANCE" }
        require(balanceOf(from) >= amount) { "INSUFFICIENT_BALANCE" }
        modifyBalance(from) { it - amount }
        modifyBalance(to) { it + amount }
        modifyAllowance(from, call.caller) { it - amount }
    }

    override fun burn(amount: Long) {
        require(isBurnable()) { "NOT_BURNABLE" }
        require(balanceOf(call.caller) >= amount) { "INSUFFICIENT_BALANCE" }
        modifyBalance(call.caller) { it - amount }
        modifyTotalSupply { it - amount }
    }

    override fun mint(to: String, amount: Long) {
        require(isMintable()) { "NON_MINTABLE" }
        require(rbac.hasPermission(call.caller, PERM_MINTER)) { "NOT_PERMITTED" }
        modifyBalance(to) { it + amount }
        modifyTotalSupply { it + amount }
    }

    fun balanceOf(address: String) = balance.tryGet(address).orElse(0)!!

    fun allowance(from: String, to: String) = allowances.tryGet(allowanceKey(from, to)).orElse(0)!!

    fun allowanceKey(from: String, to: String) = "${from}_$to"

    fun modifyBalance(address: String, fn: (Long) -> Long) {
        balance.put(address, fn(balanceOf(address)))
    }

    fun modifyAllowance(from: String, to: String, fn: (Long) -> Long) {
        allowances.put(allowanceKey(from, to), fn(allowance(from, to)))
    }

    fun modifyTotalSupply(fn: (Long) -> Long) {
        val totalSupply = state.tryGet(KEY_TOTAL_SUPPLY, Long::class.java).orElse(0)
        state.put(KEY_TOTAL_SUPPLY, fn(totalSupply))
    }

    fun isBurnable() = state.getBoolean(KEY_BURNABLE)

    fun isMintable() = state.getBoolean(KEY_MINTABLE)
}
