# PayToWin Minecrift Plugin

The plugin has two components: the client and the server. The server is the actual plugin, which runs a "sidecar" GRPC server on port 50051. This server handles all communication between the client and the Minecraft server. It's written in Kotlin.

The client is a very basic React site. It also has a docker-compose configuration, which will run [Envoy](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/other_protocols/grpc) to facilitate communication between the web client and the GRPC backend, as GRPC-web was not supported in the Kotlin framework at the time of writing.

## Requirements

- Java version 21
- Node version 18 (I think)

## Building the Plugin

You can build the plugin by running the ShadowJar Gradle job. It needs to be moved to the production server manually afterwards.

## Building the frontend

First, you should confirm the configuration of Envoy, mainly the IP addresses used. Afterwards, start the Envoy proxy by running `docker-compose up -d`. Afterwards, install the node dependencies by running `npm i` in the client directory. Confirm the hard-coded IP address in the typescript code to point to Envoy, build the GRPC glue code by running `npm run compile-proto-2`. `compile-proto` is supposed to be the Windows version, but I refused to make sure it actually works. To host it, run `npm run hosted`.
