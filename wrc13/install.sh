#!/usr/bin/env bash

if [[ "$#" -ne 3 ]]; then
    echo "Correct parameters: <registry owner address> <registry owner password> <url>"
    exit 1
fi

REGISTRY_OWNER=$1
REGISTRY_OWNER_PASSWORD=$2
NODE_URL=$3
METHOD=sign

if [[ -z "$REGISTRY_OWNER_PASSWORD" ]]
then
      REGISTRY_OWNER_PASSWORD=null
else
      REGISTRY_OWNER_PASSWORD='"'"$REGISTRY_OWNER_PASSWORD"'"'
fi

TX_CREATE_ALIAS='{
    "type": 10,
    "fee": 0,
    "sender": "'"$REGISTRY_OWNER"'",
    "password": '"$REGISTRY_OWNER_PASSWORD"',
    "alias": "_wrc13registry",
    "version": 2
}';

curl -s -X POST "$NODE_URL/transactions/$METHOD" -H 'Content-Type: application/json' --data "$TX_CREATE_ALIAS"

echo ""

if [[ $? -eq 0 ]]
then
  echo "Successfully created alias"
else
  echo "Error creating alias" >&2
  exit 1
fi

TX_DEPLOY_CONTRACT='{
      "type": 103,
      "contractName": "_wrc13registry_impl",
      "sender": "'"$REGISTRY_OWNER"'",
      "password": '"$REGISTRY_OWNER_PASSWORD"',
      "image": "registry.weintegrator.com/icore-sc/wrc13-contract-app:1.1.4-SNAPSHOT-20210707160837-9e16eb0.dirty",
      "imageHash": "57e4fe1af0429ffe4b23440a3a9c28c1647e12fbc3491c14fab9b7515a3f891e",
      "fee": 0,
      "version": 3,
      "params": [
          {
              "key": "action",
              "value": "create",
              "type": "string"
          }
      ]
    }
';

DEPLOY_RESULT=$(curl -s -X POST "$NODE_URL/transactions/$METHOD" -H 'Content-Type: application/json' --data "$TX_DEPLOY_CONTRACT")

echo ${DEPLOY_RESULT}
echo ""

if [[ $? -eq 0 ]]
then
  echo "Successfully created contract"
else
  echo "Error creating contract" >&2
  exit 1
fi

TX_ID=$(echo ${DEPLOY_RESULT} | jq -r .id)

TX_REGISTER_IMPL='{
  "type": 12,
  "sender": "'"$REGISTRY_OWNER"'",
  "password": '"$REGISTRY_OWNER_PASSWORD"',  
  "author": "'"$REGISTRY_OWNER"'",
  "fee": 0,
  "data": [
      {
          "key": "_wrc13registry_impl",
          "value": "'"$TX_ID"'",
          "type": "string"
      }
  ]
}'

curl -s -X POST "$NODE_URL/transactions/$METHOD" -H 'Content-Type: application/json' --data "$TX_REGISTER_IMPL"

echo ""

if [[ $? -eq 0 ]]
then
  echo "Successfully registered impl"
else
  echo "Error registering impl" >&2
  exit 1
fi
