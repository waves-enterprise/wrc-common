#!/usr/bin/env bash

if [[ "$#" -ne 5 ]]; then
    echo "Correct parameters: <registry owner address> <registry owner password> <url> <key> <value>"
    exit 1
fi

REGISTRY_OWNER=$1
REGISTRY_OWNER_PASSWORD=$2
NODE_URL=$3
KEY=$4
VALUE=$5
METHOD=sign

if [[ -z "$REGISTRY_OWNER_PASSWORD" ]]
then
      REGISTRY_OWNER_PASSWORD=null
else
      REGISTRY_OWNER_PASSWORD='"'"$REGISTRY_OWNER_PASSWORD"'"'
fi

REGISTRY_REFERENCE=$(curl -s -X GET "$NODE_URL/alias/by-alias/_wrc13registry" -H "accept: application/json" | jq -r .address)
echo "Got registry $REGISTRY_REFERENCE"

REGISTRY_CONTRACT_ADDRESS=$(curl -s -X GET "$NODE_URL/addresses/data/${REGISTRY_REFERENCE}/_wrc13registry_impl" -H "accept: application/json" | jq -r .value)
echo "Got registry contract $REGISTRY_CONTRACT_ADDRESS"

TX_CALL_CONTRACT='{
      "type": 104,
      "contractId": "'"$REGISTRY_CONTRACT_ADDRESS"'",
      "sender": "'"$REGISTRY_OWNER"'",
      "password": '"$REGISTRY_OWNER_PASSWORD"',
      "contractVersion": 1,
      "fee": 0,
      "version": 3,
      "params": [
          {
              "key": "action",
              "value": "setValue",
              "type": "string"
          },
          {
              "key": "key",
              "value": "'"$KEY"'",
              "type": "string"
          },
          {
              "key": "value",
              "value": "'"$VALUE"'",
              "type": "string"
          }
      ]
    }
';

DEPLOY_RESULT=$(curl -s -X POST "$NODE_URL/transactions/$METHOD" -H 'Content-Type: application/json' --data "$TX_CALL_CONTRACT")

echo ${DEPLOY_RESULT}
echo ""


