package com.wavesenterprise.sdk.wrc.wrc10

import com.wavesenterprise.sdk.contract.api.annotation.ContractAction
import com.wavesenterprise.sdk.contract.api.annotation.InvokeParam
import com.wavesenterprise.sdk.node.domain.Address

/**
 * Smart contract is used to control access.
 *
 * Roles of addresses are stored on the state.
 *
 * Extend this to enable access control for your custom smart contract.
 */
interface WRC10RoleBasedAccessControl {

    /**
     * Grant role to address
     * @param to address to which role is granted (Base58 string)
     * @see Address
     * @param role role to grant
     */
    @ContractAction
    fun grant(
        @InvokeParam(name = "to") to: String,
        @InvokeParam(name = "role") role: String,
    )

    /**
     * Revoke role from address
     * @param from address from which role is revoked (Base58 string)
     * @see Address
     * @param role role to revoke
     */
    @ContractAction
    fun revoke(
        @InvokeParam(name = "from") from: String,
        @InvokeParam(name = "role") role: String
    )

    /**
     * Grant admin privileges of role to address. Admin can grant and revoke the role for any address.
     * @param to address to which admin privileges is granted (Base58 string)
     * @see Address
     * @param role role for administration
     */
    @ContractAction
    fun grantRoleAdmin(
        @InvokeParam(name = "to") to: String,
        @InvokeParam(name = "role") role: String
    )

    /**
     * Revoke admin privileges of role to address. Admin can grant and revoke the role for any address.
     * @param from address from which admin privileges is revoked (Base58 string)
     * @see Address
     * @param role role for administration
     */
    @ContractAction
    fun revokeRoleAdmin(
        @InvokeParam(name = "from") from: String,
        @InvokeParam(name = "role") role: String
    )
}
