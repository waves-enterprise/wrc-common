package com.wavesenterprise.sdk.wrc.wrc20

import com.wavesenterprise.sdk.contract.api.annotation.ContractAction
import com.wavesenterprise.sdk.contract.api.annotation.ContractInit
import com.wavesenterprise.sdk.contract.api.annotation.InvokeParam
import com.wavesenterprise.sdk.wrc.wrc10.WRC10RoleBasedAccessControl

const val KEY_NAME = "NAME"
const val KEY_BURNABLE = "BURNABLE"
const val KEY_MINTABLE = "MINTABLE"
const val KEY_TOTAL_SUPPLY = "TOTAL_SUPPLY"
const val KEY_DECIMALS = "DECIMALS"
const val KEY_CONTRACT_ID = "CONTRACT_ID"

const val MAPPING_BALANCES = "BALANCES"
const val MAPPING_ALLOWANCES = "ALLOWANCES"

const val PERM_MINTER = "MINTER"

// ToDo: Write javadoc, extend AsyncContract WTCH-187
interface WRC20FTokenContract : WRC10RoleBasedAccessControl {

    @ContractInit
    fun create(
        @InvokeParam(name = "name") name: String,
    )

    @ContractInit
    fun createFull(
        @InvokeParam(name = "name") name: String,
        @InvokeParam(name = "burnable") burnable: Boolean,
        @InvokeParam(name = "mintable") mintable: Boolean,
        @InvokeParam(name = "supply") supply: Long,
        @InvokeParam(name = "supplyHolder") supplyHolder: String,
        @InvokeParam(name = "decimals") decimals: Int,
    )

    @ContractAction
    fun transfer(
        @InvokeParam(name = "to") to: String,
        @InvokeParam(name = "amount") amount: Long,
    )

    @ContractAction
    fun burn(@InvokeParam(name = "amount") amount: Long)

    @ContractAction
    fun mint(
        @InvokeParam(name = "to") to: String,
        @InvokeParam(name = "amount") amount: Long,
    )

    @ContractAction
    fun approve(
        @InvokeParam(name = "to") to: String,
        @InvokeParam(name = "amount") amount: Long,
    )

    @ContractAction
    fun transferFrom(
        @InvokeParam(name = "from") from: String,
        @InvokeParam(name = "to") to: String,
        @InvokeParam(name = "amount") amount: Long,
    )
}
