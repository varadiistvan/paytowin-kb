// import { useEffect, useState } from 'react' //useState } from 'react'
// import './App.css'
// //import { GrpcWebFetchTransport } from '@protobuf-ts/grpcweb-transport'
// //import { PlayersRequest } from './paytowin_pb';
// //import { PaytowinClientImpl } from './paytowin';
// import { Client, UnaryCallback } from '@grpc/grpc-js/build/src/client';
// import * as grpc from '@grpc/grpc-js';
// import { PaytowinClientImpl, PaytowinServiceName, PlayersRequest, PlayersResponse } from './paytowin';
// import { Observable } from 'rxjs';

import { GrpcWebFetchTransport } from "@protobuf-ts/grpcweb-transport"
import { PayToWinClient } from "./generated/paytowin.client"
import { DatalessEffect, DiamondTool, EffectRequest, MinecraftEntity, MinecraftMaterial, PlayersRequest, PotionName, SpawnEntity } from "./generated/paytowin";
import { useEffect, useState } from "react";
import './index.css';
import ReactSelect from "react-select";

function App() {

  const transport: GrpcWebFetchTransport = new GrpcWebFetchTransport({
    baseUrl: "http://localhost:8080",
    meta: {
      "password": "assword",
    }
  });

  const client: PayToWinClient = new PayToWinClient(transport);

  const [playersOnline, setPlayersOnline] = useState<string[]>([] as string[]);

  const [selectedPlayer, selectPlayer] = useState<string>("");

  const [selectedEffect, selectEffect] = useState<string>("");

  const [selectedPotionEffect, selectPotionEffect] = useState<PotionName>();

  const [selectedMiscEffect, selectMiscEffect] = useState<DatalessEffect>();

  const [selectedToolEffect, selectToolEffect] = useState<DiamondTool>();

  const [selectedItemEffect, selectItemEffect] = useState<MinecraftMaterial>();

  const [itemAmount, selectItemAmount] = useState<number>();

  const [selectedEntityEffect, selectEntityEffect] = useState<MinecraftEntity>();

  const [entityAmount, selectEntityAmount] = useState<number>();

  const [requester, setRequester] = useState<string>();
  
  const [effectType, changeEffectType] = useState<EffectRequest["effect"]["oneofKind"]>(undefined);

  useEffect(() => {
    const playersResponse = client.getPlayers(PlayersRequest.create());

    (async () => {
      try {
          for await (const {players} of playersResponse.responses) {
            console.log(players);
            setPlayersOnline(players);
          }
      } catch (error) {
        console.log(error)
      }
    })()

    
  }, []);

  const applyMiscEffect = (player: string, effect: DatalessEffect): void => {
    client.applyEffect(
      EffectRequest.create({
        effect: {
          dataless: effect,
          oneofKind: "dataless"
        }, 
          player: player,
          requester: requester,
        })
    )
  }

  const applyToolEffect = (player: string, effect: DiamondTool): void => {
    client.applyEffect(
      EffectRequest.create({
        effect: {
          tool: effect,
          oneofKind: "tool"
        }, 
          player: player,
          requester: requester
        })
    )
  }

  const applyPotionEffect = (player: string, effect: PotionName): void => {
    client.applyEffect(
      EffectRequest.create({
        effect: {
          potion: {
            name: effect,
            duration: 60,
            amplifier: 3
          }, 
          oneofKind: "potion"
        }, 
          player: player,
          requester: requester
        })
    )
  }

  const applyItemEffect = (player: string, effect: MinecraftMaterial, amount: number): void => {
    client.applyEffect(
      EffectRequest.create({
        effect: {
          item: {
            itemName: effect,
            amount: amount,
          }, 
          oneofKind: "item"
        }, 
          player: player,
          requester: requester
        })
    )
  }

  const applyEntityEffect = (player: string, effect: MinecraftEntity, amount: number): void => {
    client.applyEffect(
      EffectRequest.create({
        effect: {
          spawnEntity: {
            entity: effect,
            amount: amount,
          }, 
          oneofKind: "spawnEntity"
        }, 
          player: player,
          requester: requester
        })
    )
  }

  const sendToHeaven = (player: string): void => {
    client.applyEffect(
      EffectRequest.create(
        {
          effect: {
            potion: {
              name: PotionName.LEVITATION,
              duration: 1,
              amplifier: 100,
            },
            oneofKind: "potion"
          },
          player: player,
          requester: requester
        }
      )
    )
  }

  const applyEffect = ():void => {
    if (selectedPlayer == "") return;
    if (selectedEffect == "Send to heaven") {
      sendToHeaven(selectedPlayer);
      return;
    }
    if (effectType == "potion" && selectedPotionEffect != undefined) {
      applyPotionEffect(selectedPlayer, selectedPotionEffect);
    }
    if (effectType == "dataless" && selectedMiscEffect != undefined) {
      applyMiscEffect(selectedPlayer, selectedMiscEffect);
    }
    if (effectType == "tool" && selectedToolEffect != undefined) {
      applyToolEffect(selectedPlayer, selectedToolEffect);
    }
    if (effectType == "item" && itemAmount && selectedItemEffect != undefined) {
      applyItemEffect(selectedPlayer, selectedItemEffect, itemAmount);
    }
    if (effectType == "spawnEntity" && entityAmount && selectedEntityEffect != undefined) {
      applyEntityEffect(selectedPlayer, selectedEntityEffect, entityAmount);
    }
  }

  const listOfPotionEffects = (Object.entries(PotionName))
  .filter(([, value]) => typeof value === 'number')
  .map(([key, value]) => ({ 
    value: value as PotionName, 
    label: key, 
  }));

  const listOfMiscEffects = (Object.entries(DatalessEffect))
  .filter(([, value]) => typeof value === 'number')
  .map(([key, value]) => ({ 
    value: value as DatalessEffect, 
    label: key, 
  }));

  const listOfToolEffects = (Object.entries(DiamondTool))
  .filter(([, value]) => typeof value === 'number')
  .map(([key, value]) => ({ 
    value: value as DiamondTool, 
    label: key, 
  }));

  const listOfItemEffects = (Object.entries(MinecraftMaterial))
  .filter(([, value]) => typeof value === 'number')
  .map(([key, value]) => ({ 
    value: value as MinecraftMaterial, 
    label: key, 
  }));

  const itemAmountList = [{ value: 1, label: 1 }, { value: 4, label: 4 }, { value: 16, label: 16 }, { value: 32, label: 32 }, { value: 64, label: 64 }];

  const listOfEntityEffects = (Object.entries(MinecraftEntity))
  .filter(([, value]) => typeof value === 'number')
  .map(([key, value]) => ({ 
    value: value as MinecraftEntity, 
    label: key, 
  }));

  const entityAmountList = [{ value: 1, label: 1 }, { value: 2, label: 2 }, { value: 4, label: 4 }, { value: 8, label: 8 }, { value: 16, label: 16 }];

  return (<>
    <div className="text-xl font-bold m-2">Koornbeurs Minecraft LAN</div>
    <div className="m-2">
      Requester: <input className="outline" type="text" onChange={(event) => setRequester(event.target.value)}></input>
    </div>
    <div className="m-2">
      Selected player: {selectedPlayer == "" ? "None" : <div className="font-bold">{selectedPlayer}</div>}
    </div>
    <div className="m-2">
      Players online: 
      <div className="flex flex-col sm:flex-row">
        {playersOnline.map((player: string) => <div className="bg-blue-400 hover:bg-blue-500 p-2 m-2 max-w-fit" onClick={() => selectPlayer(player)}>{player}</div>)}
      </div>
    </div>
    <div className="m-2">
      Selected effect: {selectedEffect == "" ? "None" : <div className="font-bold">{selectedEffect}</div>}
    </div>
    <div className="m-2">
      Effects: 
      <div className="flex flex-col">
        <div className="bg-blue-400 hover:bg-blue-500 p-2 m-2 max-w-fit" onClick={() => selectEffect("Send to heaven")}>
          Send to heaven!
        </div>
        <div>
        Some misc effects:
        <ReactSelect
            options={listOfMiscEffects}
            onChange={(newValue):void => {
              if (newValue) {
                console.log(newValue.value);
                selectMiscEffect(newValue.value)
                selectEffect(newValue.label)
                changeEffectType("dataless")
              }
            }}
            />
        </div>
        <div>
        Specific potion effects (mild, and 60 seconds):
        <ReactSelect
            options={listOfPotionEffects}
            onChange={(newValue):void => {
              if (newValue) {
                console.log(newValue.value);
                selectPotionEffect(newValue.value)
                selectEffect(newValue.label)
                changeEffectType("potion")
              }
            }}
            />
          </div>
          <div>
            Give a diamond item:
            <ReactSelect
            options={listOfToolEffects}
            onChange={(newValue):void => {
              if (newValue) {
                console.log(newValue.value);
                selectToolEffect(newValue.value)
                selectEffect(newValue.label)
                changeEffectType("tool")
              }
            }}
            />
          </div>
          <div className="flex flex-row w-full m-2">
            Give a certain amount of items:
            Item:
            <div className="w-1/4 m-2">
            <ReactSelect
            options={listOfItemEffects}
            onChange={(newValue):void => {
              if (newValue) {
                console.log(newValue.value);
                selectItemEffect(newValue.value)
                selectEffect(newValue.label)
                changeEffectType("item")
              }
            }}
            />
            </div>
            Amount:
            <div className="w-1/4 m-2">
            <ReactSelect
            options={itemAmountList}
            onChange={(newValue):void => {
              if (newValue) {
                console.log(newValue.value);
                selectItemAmount(newValue.value)
              }
            }}
            />
            </div>
          </div>
          <div className="flex flex-row">
            Spawn a certain amount of mobs:
            Mob:
            <div className="w-1/4 m-2">
            <ReactSelect
            options={listOfEntityEffects}
            onChange={(newValue):void => {
              if (newValue) {
                console.log(newValue.value);
                selectEntityEffect(newValue.value)
                selectEffect(newValue.label)
                changeEffectType("spawnEntity")
              }
            }}
            />
            </div>
            Amount:
            <div className="w-1/4 m-2">
            <ReactSelect
            options={entityAmountList}
            onChange={(newValue):void => {
              if (newValue) {
                console.log(newValue.value);
                selectEntityAmount(newValue.value)
              }
            }}
            />
            </div>
          </div>
        
      </div>
    </div>
    <div className="bg-red-400 hover:bg-red-500 p-2 m-2 max-w-fit" onClick={() => applyEffect()}>
      Apply the effect!
    </div>
  </>);


  // //const [count, setCount] = useState(0)

  // const client = new Client("https://localhost:50051", grpc.credentials.createInsecure());

  // //const grpcClient = grpc.makeClientConstructor(grpc.)

  // // const transport = new GrpcWebFetchTransport({
  // //   baseUrl: "http://localhost:50051",
  // //   meta: {
  // //     "password": "password"
  // //   }
  // // });

  // type RpcRequestImpl = (service: string, method: string, data: Uint8Array) => Promise<Uint8Array>;

  // const sendRequest: RpcRequestImpl = (service, method, data) => {
  //   const path = `/${service}/${method}`;

  //   return new Promise((resolve, reject) => {
  //     // makeUnaryRequest transmits the result (and error) with a callback
  //     // transform this into a promise!
  //     // eslint-disable-next-line @typescript-eslint/no-explicit-any
  //     const resultCallback: UnaryCallback<any> = (err, res) => {
  //       if (err) {
  //         return reject(err);
  //       }
  //       resolve(res);
  //     };
  
  //     // eslint-disable-next-line @typescript-eslint/no-explicit-any
  //     function passThrough(argument: any) {
  //       return argument;
  //     }
  
  //     // Using passThrough as the serialize and deserialize functions
  //     client.makeUnaryRequest(path, passThrough, passThrough, data, resultCallback);
  //   });
  // }

  // type RpcObservableImpl = (service: string, method: string, data: Uint8Array) => Observable<Uint8Array>;

  // const sendServerStreamingRequest: RpcObservableImpl = (service, method, data) => {
  //   return new Observable<Uint8Array>(() => {
  //     const path = `/${service}/${method}`;

  //     // const resultCallback = (err: Error | null, res: Uint8Array | null) => {
  //     //     if (err) {
  //     //         subscriber.error(err);
  //     //         return;
  //     //     }
  //     //     if (res) {
  //     //         subscriber.next(res);
  //     //     } else {
  //     //         // End the stream if no more data is expected.
  //     //         subscriber.complete();
  //     //     }
  //     // };

  //     // eslint-disable-next-line @typescript-eslint/no-explicit-any
  //     function passThrough(argument: any) {
  //       return argument;
  //     }

  //     const metadata = new grpc.Metadata();
  //     metadata.add("password", "password");

  //     // Assuming conn is your connection object and it has a method to handle server streaming
  //     // This method should be designed to keep the connection open and emit data as it arrives
  //     client.makeServerStreamRequest(path, passThrough, passThrough, data, metadata);

  //     // Return a teardown logic which will be invoked when the Observable is unsubscribed
  //     // return () => {
  //     //     // Here you should implement the logic to cancel the server streaming if possible
  //     //     // This could be sending a cancel message to the server or closing the connection
  //     //     client.close();
  //     // };
  // });
  // }

  // const rpc = { 
  //   request: sendRequest,
  //   //clientStreamingRequest: sendRequest,
  //   serverStreamingRequest: sendServerStreamingRequest,
  //   //bidirectionalStreamingRequest: sendRequest,
  // };

  // const PayToWinClient: PaytowinClientImpl = new PaytowinClientImpl(rpc, { service: PaytowinServiceName });

  // const [players, setPlayers] = useState([] as string[]);

  // useEffect(() => {
  //   const playerResponse = PayToWinClient.GetPlayers(PlayersRequest.create());
  //   (async () => {
  //     try {
  //         playerResponse.subscribe((response: PlayersResponse) => {
  //           setPlayers(response.players);
  //         })
  //     } catch (error) {
  //       console.log(error)
  //     }
  //   })()

    
  // }, [])


  // return (<>
  //   <div>
  //     Players online: {players.map(player => (<div>{player}</div>))}
  //   </div>
  // </>);
}

export default App
