# WRC-Common

Interfaces and reference implementations of WRC Standards


## Building (all modules)
This command will build all modules in the project and publish corresponding
smart contracts into a docker registry

`./gradlew clean dockerTag`

Alternatively run the following command to push all docker images (both applications and smart contracts) to remote docker registry
`./gradlew clean dockerPush`

## Installing WRC13 Registry

Run script wrc13/install.sh