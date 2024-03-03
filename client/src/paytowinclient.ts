import { GrpcWebFetchTransport } from "@protobuf-ts/grpcweb-transport";
//import { PlayersRequestType } from "./paytowin";

interface PayToWinClient {
    grpc: GrpcWebFetchTransport,
    //getPlayers: (req: PlayersRequestType) => PlayersResponse
}

export interface PlayersResponse {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    players: string[]
}

const CreatePayToWinClient = (grpc: GrpcWebFetchTransport): PayToWinClient => {
    //const getPlayersFn = (req: PlayersRequestType): PlayersResponse => {
    
    //}
    
    return {
        grpc: grpc,
        //getPlayers: getPlayersFn,
    }
}

export default CreatePayToWinClient;