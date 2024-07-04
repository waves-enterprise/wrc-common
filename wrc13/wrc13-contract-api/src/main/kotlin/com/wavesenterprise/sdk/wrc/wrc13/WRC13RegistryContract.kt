package com.wavesenterprise.sdk.wrc.wrc13

import com.wavesenterprise.sdk.contract.api.annotation.ContractAction
import com.wavesenterprise.sdk.contract.api.annotation.ContractInit
import com.wavesenterprise.sdk.contract.api.annotation.InvokeParam
import com.wavesenterprise.sdk.wrc.wrc10.WRC10RoleBasedAccessControl

/**
 * Smart contract is used as contract registry.
 *
 * Key is contract name. Value is contract id.
 *
 * This contract has access control by WRC10.
 */
interface WRC13RegistryContract : WRC10RoleBasedAccessControl {

    /**
     * Create contract and initialise WRC10 for contract creator.
     */
    @ContractInit
    fun create()

    /**
     * Set value for key. If key already exists updates the value.
     * @param key key on the contract state
     * @param value value on the contract state
     */
    @ContractAction
    fun setValue(
        @InvokeParam(name = "key") key: String,
        @InvokeParam(name = "value") value: String,
    )
}
